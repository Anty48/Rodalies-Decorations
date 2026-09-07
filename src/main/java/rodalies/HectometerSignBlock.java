package rodalies;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import rodalies.client.ClientHooks;

import javax.annotation.Nullable;

/**
 * Señal de hectometro: un poste de diorita con una placa arriba. Modelo 3D que ocupa 2 celdas de
 * alto (0..32 px). Es un multiblock VERTICAL de 2 celdas que comparten orientacion:
 *
 *   [TOP]     colision de la placa (la placa sobresale hacia arriba); modelo vacio
 *   [BOTTOM]  celda que clica el jugador: dibuja el modelo COMPLETO + guarda el texto (BlockEntity);
 *             colision = poste
 *
 * El texto editable va en 2 filas en la parte superior de la placa (fila de arriba grande, la de
 * abajo pequeña); lo dibuja {@code HectometerSignRenderer}. noOcclusion porque casi toda cada celda
 * es aire y el modelo sobresale.
 */
public class HectometerSignBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    // Poste centrado (simetrico -> invariante a la rotacion).
    private static final VoxelShape POLE = Block.box(6, 0, 6, 10, 16, 10);
    // Colision de la celda TOP mirando al norte (poste 16..21 -> 0..5, y placa 21..32 -> 5..16).
    private static final VoxelShape TOP_NORTH = Shapes.or(
            Block.box(6, 0, 6, 10, 5, 10),     // tramo de poste dentro de la celda superior
            Block.box(3, 5, 6, 13, 16, 10));   // placa (incluye la punta)

    public HectometerSignBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, Part.BOTTOM));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        if (state.getValue(PART) == Part.TOP) {
            return RailSignBlock.rotateToFacing(TOP_NORTH, state.getValue(FACING));
        }
        return POLE;
    }

    /** Posicion de la celda BOTTOM (la que renderiza + guarda el texto) desde cualquier parte. */
    private static BlockPos bottomOf(BlockPos pos, BlockState state) {
        return state.getValue(PART) == Part.TOP ? pos.below() : pos;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        Direction facing = context.getHorizontalDirection().getOpposite(); // la placa mira al jugador
        BlockPos bottom = context.getClickedPos();
        if (level.getBlockState(bottom.above()).canBeReplaced(context)) {
            return defaultBlockState().setValue(FACING, facing).setValue(PART, Part.BOTTOM);
        }
        return null; // no hay hueco para la celda de arriba
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            level.setBlock(pos.above(), state.setValue(PART, Part.TOP), 3);
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // Rompe la otra celda sin drop (flag 35); la que rompe el jugador dropea 1 item via loot.
        if (!level.isClientSide) {
            BlockPos bottom = bottomOf(pos, state);
            for (BlockPos p : new BlockPos[]{bottom, bottom.above()}) {
                if (p.equals(pos)) continue;
                if (level.getBlockState(p).getBlock() == this) {
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), 35);
                }
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        // El texto lo guarda el BlockEntity de BOTTOM; reenvia la interaccion alli desde cualquier celda.
        BlockPos bottom = bottomOf(pos, state);
        BlockEntity be = level.getBlockEntity(bottom);
        if (!(be instanceof HectometerSignBlockEntity sign)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.openHectometerScreen(sign));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Solo la celda BOTTOM tiene BlockEntity (dibuja el modelo y guarda el texto).
        return state.getValue(PART) == Part.BOTTOM ? new HectometerSignBlockEntity(pos, state) : null;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    public enum Part implements StringRepresentable {
        BOTTOM("bottom"),
        TOP("top");

        private final String name;

        Part(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
