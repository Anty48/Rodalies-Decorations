package rodalies;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Puerta corredera de tren: dos hojas que se abren por el centro deslizandose hacia los lados (y un
 * poco hacia adelante), como las puertas de los trenes de Rodalies. Es un multiblock VERTICAL de 2
 * celdas (mitad baja + mitad alta), al estilo de una puerta vanilla pero SIN girar: al abrir/cerrar no
 * cambia de orientacion. Se abre/cierra con clic derecho.
 *
 * El deslizamiento se ANIMA: el modelo de la blockstate esta vacio y las dos hojas las dibuja el
 * {@code SlidingTrainDoorRenderer} desde la celda LOWER (que lleva el BlockEntity), interpolando su
 * posicion segun el estado OPEN. La celda UPPER es solo colision.
 *
 * Colision e hitbox de seleccion: SIEMPRE la losa de la puerta cerrada (no cambia al abrir). Asi,
 * aunque abierta selecciones "aire" donde estaba la puerta, es comodo volver a clicar para cerrarla.
 */
public class SlidingTrainDoorBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    public static final EnumProperty<DoubleBlockHalf> HALF = EnumProperty.create("half", DoubleBlockHalf.class);

    // Losa fina pegada al borde norte de la celda (se rota segun el facing). Se usa siempre, abierta
    // o cerrada, tanto para colision como para el contorno de seleccion.
    private static final VoxelShape DOOR_NORTH = Block.box(0, 0, 0, 16, 16, 2);

    public SlidingTrainDoorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, HALF);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL; // el modelo (vacio) no dibuja nada; las hojas las pinta el BER
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        // Constante: la caja de la puerta cerrada, abierta o no (asi siempre se puede clicar y cerrar).
        return RailSignBlock.rotateToFacing(DOOR_NORTH, state.getValue(FACING));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        // Hace falta hueco para la mitad de arriba.
        if (pos.getY() < level.getMaxBuildHeight() - 1
                && level.getBlockState(pos.above()).canBeReplaced(context)) {
            Direction facing = context.getHorizontalDirection().getOpposite(); // mira hacia el jugador
            return defaultBlockState()
                    .setValue(FACING, facing)
                    .setValue(HALF, DoubleBlockHalf.LOWER)
                    .setValue(OPEN, false);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // Rompe la otra mitad sin drop (flag 35); la que rompe el jugador dropea 1 via loot.
        if (!level.isClientSide) {
            BlockPos other = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
            if (level.getBlockState(other).getBlock() == this) {
                level.setBlock(other, Blocks.AIR.defaultBlockState(), 35);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        boolean newOpen = !state.getValue(OPEN);
        if (!level.isClientSide) {
            BlockPos lower = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
            setOpen(level, lower, newOpen);
            setOpen(level, lower.above(), newOpen);
            level.playSound(null, pos,
                    newOpen ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE,
                    SoundSource.BLOCKS, 1f, 1f);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void setOpen(Level level, BlockPos pos, boolean open) {
        BlockState s = level.getBlockState(pos);
        if (s.getBlock() == this && s.hasProperty(OPEN) && s.getValue(OPEN) != open) {
            level.setBlock(pos, s.setValue(OPEN, open), 3); // flag 3: cambia bloque + avisa a clientes
        }
    }

    // --- BlockEntity (solo LOWER, para animar) ---

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER
                ? new SlidingTrainDoorBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        // La animacion es puramente visual -> solo tick de cliente.
        if (!level.isClientSide || type != ModBlockEntities.SLIDING_TRAIN_DOOR_BE.get()) {
            return null;
        }
        return (lvl, p, st, be) -> {
            if (be instanceof SlidingTrainDoorBlockEntity door) {
                door.clientTick(st);
            }
        };
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
