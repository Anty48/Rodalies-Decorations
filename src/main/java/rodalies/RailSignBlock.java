package rodalies;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
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
import rodalies.client.ClientHooks;

import javax.annotation.Nullable;

/**
 * Senal ferroviaria: un poste con una senal encima. Multiblock VERTICAL de 3 celdas (bottom/middle/top);
 * la celda MIDDLE dibuja el modelo y lleva el BlockEntity con el texto. Ver documentacion completa en la
 * version Forge; el comportamiento es identico.
 *
 * Port a 1.21: {@code use()} se divide en {@code useItemOn} (logica con item: combinar con velocidad) y
 * {@code useWithoutItem} (abrir el editor). {@code playerWillDestroy} devuelve {@code BlockState}.
 * {@code DistExecutor} desaparece: el hook de cliente se llama directamente bajo {@code isClientSide}.
 */
public class RailSignBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    private static final VoxelShape POLE = Block.box(7, 0, 7, 9, 16, 9);

    private static final VoxelShape TOP_DIAMOND = Block.box(0, 1, 5, 16, 14, 8);
    private static final VoxelShape TOP_NORMAL = Block.box(1, 2, 5, 15, 16, 8);
    private static final VoxelShape TOP_LVT_END = Block.box(1, 2, 5, 15, 15, 8);
    private static final VoxelShape TOP_PLATFORM = Shapes.or(
            Block.box(7, 0, 7, 9, 16, 9),
            Block.box(7, 6, 9, 9, 16, 16));

    private final RailSignType type;
    private final boolean shortPole;

    // codec() es abstracto en HorizontalDirectionalBlock (1.21). Estos bloques se registran por codigo y
    // nunca se serializan por datapack, asi que un codec aproximado (tipo NORMAL) es suficiente y seguro.
    public static final MapCodec<RailSignBlock> CODEC = simpleCodec(p -> new RailSignBlock(p, RailSignType.NORMAL));

    public RailSignBlock(Properties properties, RailSignType type) {
        this(properties, type, false);
    }

    public RailSignBlock(Properties properties, RailSignType type, boolean shortPole) {
        super(properties);
        this.type = type;
        this.shortPole = shortPole;
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, Part.MIDDLE));
    }

    @Override
    protected MapCodec<? extends RailSignBlock> codec() {
        return CODEC;
    }

    public boolean isShort() {
        return shortPole;
    }

    public RailSignType getSignType() {
        return type;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        if (state.getValue(PART) == Part.TOP) {
            return rotateToFacing(topShapeNorth(), state.getValue(FACING));
        }
        return POLE;
    }

    private VoxelShape topShapeNorth() {
        return switch (type) {
            case SPEED_LIMIT, LTV -> TOP_DIAMOND;
            case NORMAL -> TOP_NORMAL;
            case LTV_END -> TOP_LVT_END;
            case PLATFORM_NUMBER -> TOP_PLATFORM;
        };
    }

    static VoxelShape rotateToFacing(VoxelShape north, Direction facing) {
        int times = (facing.get2DDataValue() - Direction.NORTH.get2DDataValue() + 4) % 4;
        VoxelShape shape = north;
        for (int i = 0; i < times; i++) {
            VoxelShape src = shape;
            VoxelShape[] acc = {Shapes.empty()};
            src.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                    acc[0] = Shapes.or(acc[0], Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            shape = acc[0];
        }
        return shape;
    }

    static BlockPos middleOf(BlockPos pos, BlockState state) {
        return switch (state.getValue(PART)) {
            case BOTTOM -> pos.above();
            case TOP -> pos.below();
            default -> pos;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        Direction facing = context.getHorizontalDirection().getOpposite();
        if (shortPole) {
            BlockPos middle = context.getClickedPos();
            if (level.getBlockState(middle.above()).canBeReplaced(context)) {
                return defaultBlockState().setValue(FACING, facing).setValue(PART, Part.MIDDLE);
            }
            return null;
        }
        BlockPos bottom = context.getClickedPos();
        BlockPos middle = bottom.above();
        BlockPos top = middle.above();
        if (level.getBlockState(middle).canBeReplaced(context)
                && level.getBlockState(top).canBeReplaced(context)) {
            return defaultBlockState().setValue(FACING, facing).setValue(PART, Part.BOTTOM);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            if (shortPole) {
                level.setBlock(pos.above(), state.setValue(PART, Part.TOP), 3);
            } else {
                BlockPos middle = pos.above();
                level.setBlock(middle, state.setValue(PART, Part.MIDDLE), 3);
                level.setBlock(middle.above(), state.setValue(PART, Part.TOP), 3);
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            BlockPos middle = middleOf(pos, state);
            BlockPos[] cells = shortPole
                    ? new BlockPos[]{middle, middle.above()}
                    : new BlockPos[]{middle.below(), middle, middle.above()};
            for (BlockPos p : cells) {
                if (p.equals(pos)) continue;
                if (level.getBlockState(p).getBlock() == this) {
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), 35);
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        // Combinar: un FIN DE LTV colocado al que se le anade una senal de velocidad en mano se convierte
        // en la senal doble (velocidad arriba, fin de LTV abajo). Solo la version normal.
        if (this == ModBlocks.LTV_END.get() && !shortPole
                && stack.is(ModBlocks.SPEED_LIMIT_ITEM.get())
                && !player.isSecondaryUseActive()) {
            if (!level.isClientSide) {
                SpeedLimitBlock.combineOntoLtvEnd(level, pos, state);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                level.playSound(null, pos, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1f, 1f);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (!type.editable()) {
            return InteractionResult.PASS;
        }
        BlockPos middle = middleOf(pos, state);
        BlockEntity be = level.getBlockEntity(middle);
        if (!(be instanceof RailSignBlockEntity sign)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            ClientHooks.openRailSignScreen(sign, 0);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return (type.editable() && state.getValue(PART) == Part.MIDDLE)
                ? new RailSignBlockEntity(pos, state) : null;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    public enum Part implements StringRepresentable {
        BOTTOM("bottom"),
        MIDDLE("middle"),
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
