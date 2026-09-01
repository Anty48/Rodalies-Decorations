package rodalies;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Puerta corredera de tren: dos hojas que se abren por el centro deslizandose hacia los lados (y un
 * poco hacia adelante), como las puertas de los trenes de Rodalies. Es un multiblock VERTICAL de 2
 * celdas (mitad baja + mitad alta), al estilo de una puerta vanilla pero SIN girar al abrir.
 *
 * <b>Extiende {@link DoorBlock} a proposito</b>: asi ES una puerta de verdad para todo el juego. En
 * concreto, Create (y cualquier mod de contraptions) hace interactuables sobre un tren en movimiento
 * TODOS los bloques del tag {@code #minecraft:wooden_doors} <b>que ademas sean {@code instanceof DoorBlock}</b>
 * (su {@code DoorMovingInteraction} sale sin hacer nada si el bloque no es un DoorBlock). Por eso, para
 * que la puerta se abra sobre los trenes SIN que el mod dependa de Create, tiene que ser un DoorBlock y
 * estar en el tag. No es un addon de Create.
 *
 * <b>Render 100% por BlockEntity</b>: {@code getRenderShape} es SIEMPRE {@code ENTITYBLOCK_ANIMATED}, asi
 * que la puerta NUNCA se dibuja como modelo baked en el "mesh" del chunk; siempre la dibuja el
 * {@link SlidingTrainDoorBlockEntity} (celda LOWER) via su BER, en cualquier estado (cerrada, abierta o
 * animando). Esto:
 * <ul>
 *   <li>Elimina el salto/hueco de 1 frame que aparecia al conmutar entre modelo baked y BER (el chunk
 *       tarda en re-mallar).</li>
 *   <li>Hace que ANIME en los trenes de Create SIN depender de Create: como es ENTITYBLOCK_ANIMATED,
 *       Create la saca del mesh estatico y renderiza nuestro BlockEntity viajero como cualquier otro
 *       (verificado en Create 6.0.8, {@code ClientContraption}); el BER anima el deslizamiento por tiempo
 *       real leyendo {@code OPEN} (que Create conmuta al interactuar). Funciona igual en Create 5.0, 6.0
 *       y sin Create.</li>
 * </ul>
 *
 * Se sobreescribe {@link #canSurvive} para que la puerta pueda "flotar" (los trenes la mueven por el
 * aire; una puerta vanilla exigiria un bloque solido debajo). El emparejado de las dos mitades y su
 * rotura conjunta los gestiona {@link DoorBlock#updateShape} de vanilla; el loot (createDoorTable) dropea
 * 1 sola puerta.
 */
public class SlidingTrainDoorBlock extends DoorBlock implements EntityBlock {

    public static final DirectionProperty FACING = DoorBlock.FACING;
    public static final BooleanProperty OPEN = DoorBlock.OPEN;
    public static final EnumProperty<DoubleBlockHalf> HALF = DoorBlock.HALF;

    /**
     * Tipo de puerta propio: suena a metal PERO es ABRIBLE A MANO ({@code canOpenByHand = true}).
     * Con {@link BlockSetType#IRON} ({@code canOpenByHand = false}) la puerta se comporta como una de
     * hierro: sobre un tren de Create solo se abriria con redstone, no con clic derecho. Por eso usamos
     * uno propio. (En un tren, el sonido de abrir/cerrar lo pone Create y es el de puerta de madera: su
     * {@code DoorMovingInteraction} usa ese sonido para cualquier DoorBlock que no sea su propia
     * SlidingDoorBlock; no es controlable sin depender de Create.)
     */
    private static final BlockSetType TRAIN_DOOR_SET = BlockSetType.register(new BlockSetType(
            "rodalies_train_door", true, SoundType.METAL,
            SoundEvents.IRON_DOOR_CLOSE, SoundEvents.IRON_DOOR_OPEN,
            SoundEvents.IRON_TRAPDOOR_CLOSE, SoundEvents.IRON_TRAPDOOR_OPEN,
            SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON,
            SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON));

    // Losa fina pegada al borde norte de la celda (se rota segun el facing). Se usa siempre, abierta
    // o cerrada, para el contorno de seleccion; y como COLISION solo cuando esta cerrada.
    private static final VoxelShape DOOR_NORTH = Block.box(0, 0, 0, 16, 16, 2);

    /**
     * Si true, la puerta al abrir se desplaza un poco hacia ADELANTE (-Z, hacia el jugador que la
     * coloco) antes de correrse a los lados; si false, hacia ATRAS (+Z). Solo afecta al render.
     */
    private final boolean openForward;

    public SlidingTrainDoorBlock(Properties properties, boolean openForward) {
        // TRAIN_DOOR_SET: metalica pero abrible a mano -> se abre con clic derecho tambien sobre un tren.
        super(properties, TRAIN_DOOR_SET);
        this.openForward = openForward;
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(HINGE, DoorHingeSide.LEFT)
                .setValue(POWERED, false));
    }

    /** true = se abre hacia adelante; false = hacia atras. Lo consulta el renderer. */
    public boolean isOpenForward() {
        return openForward;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // SIEMPRE la dibuja el BER (nunca modelo baked en el chunk) -> sin hueco al conmutar y anima en
        // los trenes de Create sin dependencia. Ver el javadoc de la clase.
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    // La puerta puede flotar (los trenes la mueven por el aire). Una puerta vanilla exige soporte debajo.
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        // Contorno de SELECCION: constante (la caja de la puerta cerrada), abierta o no, para que
        // siempre se pueda apuntar al hueco y volver a clicar para cerrarla.
        return RailSignBlock.rotateToFacing(DOOR_NORTH, state.getValue(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        // COLISION: al abrir, las hojas se corren a los lados y dejan el hueco libre -> sin colision, se
        // puede pasar. Cerrada, bloquea con la losa de siempre.
        return state.getValue(OPEN)
                ? Shapes.empty()
                : RailSignBlock.rotateToFacing(DOOR_NORTH, state.getValue(FACING));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        // Hace falta hueco para la mitad de arriba. Colocamos la mitad UPPER en setPlacedBy.
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
        // Coloca explicitamente la mitad UPPER (no dependemos de DoorBlock.setPlacedBy). Sin super para
        // no colocarla dos veces.
        if (!level.isClientSide) {
            level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        // Se sobreescribe la de DoorBlock (que con BlockSetType.IRON no abre a mano): conmuta OPEN en
        // las dos mitades y suena como puerta de hierro. En un tren la abre Create (DoorMovingInteraction).
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
            level.setBlock(pos, s.setValue(OPEN, open), 3);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Solo la celda LOWER lleva BlockEntity (su BER dibuja/anima las 2 hojas completas, 2 de alto).
        return state.getValue(HALF) == DoubleBlockHalf.LOWER
                ? new SlidingTrainDoorBlockEntity(pos, state) : null;
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
