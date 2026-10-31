package rodalies;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
 * Puerta corredera de tren (multiblock vertical de 2 celdas, extiende {@link DoorBlock}). Ver
 * documentacion completa en la version Forge; el comportamiento es identico.
 *
 * Port a 1.21:
 *  - El constructor de {@link DoorBlock} pasa a {@code (BlockSetType, Properties)} (antes al reves).
 *  - {@link BlockSetType} anade dos booleanos ({@code canOpenByWindCharge}, {@code canButtonBeActivatedByArrows}).
 *  - {@code use()} -> {@code useWithoutItem}; varios metodos de bloque pasan a protected.
 */
public class SlidingTrainDoorBlock extends DoorBlock implements EntityBlock {

    public static final DirectionProperty FACING = DoorBlock.FACING;
    public static final BooleanProperty OPEN = DoorBlock.OPEN;
    public static final EnumProperty<DoubleBlockHalf> HALF = DoorBlock.HALF;

    /** Tipo de puerta propio: suena a metal pero es ABRIBLE A MANO (canOpenByHand = true). */
    private static final BlockSetType TRAIN_DOOR_SET = BlockSetType.register(new BlockSetType(
            "rodalies_train_door", true, true, true,
            BlockSetType.PressurePlateSensitivity.EVERYTHING, SoundType.METAL,
            SoundEvents.IRON_DOOR_CLOSE, SoundEvents.IRON_DOOR_OPEN,
            SoundEvents.IRON_TRAPDOOR_CLOSE, SoundEvents.IRON_TRAPDOOR_OPEN,
            SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF, SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON,
            SoundEvents.STONE_BUTTON_CLICK_OFF, SoundEvents.STONE_BUTTON_CLICK_ON));

    private static final VoxelShape DOOR_NORTH = Block.box(0, 0, 0, 16, 16, 2);

    /** Texturas por defecto de las hojas (puerta naranja original): izquierda y derecha distintas. */
    private static final ResourceLocation DEFAULT_TEX_LEFT = ResourceLocation.fromNamespaceAndPath("rodalies", "block/door_left");
    private static final ResourceLocation DEFAULT_TEX_RIGHT = ResourceLocation.fromNamespaceAndPath("rodalies", "block/door_right");
    /** Textura por defecto del techo (cara up): hormigon gris para la puerta naranja original. */
    private static final ResourceLocation DEFAULT_TEX_TOP = ResourceLocation.fromNamespaceAndPath("minecraft", "block/gray_concrete");

    /**
     * Si true, la puerta al abrir se desplaza un poco hacia ADELANTE (-Z, hacia el jugador que la
     * coloco) antes de correrse a los lados; si false, hacia ATRAS (+Z). Solo afecta al render.
     */
    private final boolean openForward;

    /** Texturas de las hojas izquierda/derecha; las lee el BER. Permiten reskinear la misma puerta. */
    private final ResourceLocation texLeft;
    private final ResourceLocation texRight;
    /** Textura del techo (cara up), independiente de las hojas; la lee el BER. */
    private final ResourceLocation texTop;

    public SlidingTrainDoorBlock(Properties properties, boolean openForward) {
        this(properties, openForward, DEFAULT_TEX_LEFT, DEFAULT_TEX_RIGHT, DEFAULT_TEX_TOP);
    }

    public SlidingTrainDoorBlock(Properties properties, boolean openForward,
                                 ResourceLocation texLeft, ResourceLocation texRight, ResourceLocation texTop) {
        // TRAIN_DOOR_SET: metalica pero abrible a mano -> se abre con clic derecho tambien sobre un tren.
        super(TRAIN_DOOR_SET, properties);
        this.openForward = openForward;
        this.texLeft = texLeft;
        this.texRight = texRight;
        this.texTop = texTop;
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(HINGE, DoorHingeSide.LEFT)
                .setValue(POWERED, false));
    }

    public boolean isOpenForward() {
        return openForward;
    }

    /** Textura de la hoja izquierda. La lee el BER (misma puerta, distinto skin). */
    public ResourceLocation getTexLeft() {
        return texLeft;
    }

    /** Textura de la hoja derecha. La lee el BER. */
    public ResourceLocation getTexRight() {
        return texRight;
    }

    /** Textura del techo (cara up). La lee el BER. */
    public ResourceLocation getTexTop() {
        return texTop;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return true;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return RailSignBlock.rotateToFacing(DOOR_NORTH, state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return state.getValue(OPEN)
                ? Shapes.empty()
                : RailSignBlock.rotateToFacing(DOOR_NORTH, state.getValue(FACING));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (pos.getY() < level.getMaxBuildHeight() - 1
                && level.getBlockState(pos.above()).canBeReplaced(context)) {
            Direction facing = context.getHorizontalDirection().getOpposite();
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
        if (!level.isClientSide) {
            level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
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
        return state.getValue(HALF) == DoubleBlockHalf.LOWER
                ? new SlidingTrainDoorBlockEntity(pos, state) : null;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
