package rodalies;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Senal de velocidad permanente apilable (double/triple/ltv_end). Ver documentacion en la version Forge.
 *
 * Port a 1.21: apilar va en {@code useItemOn} (devuelve {@code ItemInteractionResult}), editar en
 * {@code useWithoutItem}. {@code playerWillDestroy} devuelve {@code BlockState}.
 */
public class SpeedLimitBlock extends RailSignBlock {

    public enum Stack implements StringRepresentable {
        NONE("none"),
        DOUBLE("double"),
        TRIPLE("triple"),
        LTV_END("ltv_end");

        private final String name;

        Stack(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final EnumProperty<Stack> STACK = EnumProperty.create("stack", Stack.class);

    private static final VoxelShape SPEED_MID = Block.box(0, 0, 5, 16, 9, 8);
    private static final VoxelShape SPEED_BOT = Block.box(0, 10, 5, 16, 16, 8);
    private static final VoxelShape TRIPLE_SIGN = Block.box(0, 0, 5, 16, 15, 8);
    private static final VoxelShape LTVEND_MID = Block.box(1, 2, 5, 15, 13, 8);

    public SpeedLimitBlock(Properties properties) {
        super(properties, RailSignType.SPEED_LIMIT, false);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, Part.MIDDLE)
                .setValue(STACK, Stack.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder); // FACING, PART
        builder.add(STACK);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        VoxelShape base = super.getShape(state, level, pos, ctx);
        Stack stack = state.getValue(STACK);
        if (stack == Stack.NONE) {
            return base;
        }
        Direction facing = state.getValue(FACING);
        Part part = state.getValue(PART);
        return switch (stack) {
            case DOUBLE -> switch (part) {
                case MIDDLE -> Shapes.or(base, rotateToFacing(SPEED_MID, facing));
                case BOTTOM -> Shapes.or(base, rotateToFacing(SPEED_BOT, facing));
                default -> base;
            };
            case TRIPLE -> switch (part) {
                case MIDDLE, BOTTOM -> Shapes.or(base, rotateToFacing(TRIPLE_SIGN, facing));
                default -> base;
            };
            case LTV_END -> part == Part.MIDDLE ? Shapes.or(base, rotateToFacing(LTVEND_MID, facing)) : base;
            default -> base;
        };
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        Stack stack = state.getValue(STACK);
        if (!player.isSecondaryUseActive()) {
            if (held.is(ModBlocks.SPEED_LIMIT_ITEM.get())) {
                if (stack == Stack.NONE) {
                    return apply(level, pos, state, Stack.DOUBLE, player, held);
                }
                if (stack == Stack.DOUBLE) {
                    return apply(level, pos, state, Stack.TRIPLE, player, held);
                }
            } else if (held.is(ModBlocks.LTV_END_ITEM.get()) && stack == Stack.NONE) {
                return apply(level, pos, state, Stack.LTV_END, player, held);
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        Stack stack = state.getValue(STACK);
        BlockPos middle = middleOf(pos, state);
        BlockEntity be = level.getBlockEntity(middle);
        if (!(be instanceof RailSignBlockEntity sign)) {
            return InteractionResult.PASS;
        }
        int slot = slotForHit(stack, hit.getLocation().y - middle.getY());
        if (level.isClientSide) {
            rodalies.client.ClientHooks.openRailSignScreen(sign, slot);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    private static int slotForHit(Stack stack, double localY) {
        return switch (stack) {
            case DOUBLE -> localY > 0.9 ? 0 : 1;
            case TRIPLE -> localY > 1.0 ? 0 : (localY > 0.0 ? 1 : 2);
            default -> 0;
        };
    }

    private ItemInteractionResult apply(Level level, BlockPos pos, BlockState state, Stack stack,
                                        Player player, ItemStack held) {
        if (!level.isClientSide) {
            setStackOnAllCells(level, pos, state, stack);
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            level.playSound(null, pos, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1f, 1f);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    private void setStackOnAllCells(Level level, BlockPos pos, BlockState state, Stack stack) {
        BlockPos middle = middleOf(pos, state);
        for (BlockPos p : new BlockPos[]{middle.below(), middle, middle.above()}) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() == this && s.hasProperty(STACK)) {
                level.setBlock(p, s.setValue(STACK, stack), 3);
            }
        }
    }

    static void combineOntoLtvEnd(Level level, BlockPos pos, BlockState ltvEndState) {
        SpeedLimitBlock block = (SpeedLimitBlock) ModBlocks.SPEED_LIMIT.get();
        Direction facing = ltvEndState.getValue(FACING);
        BlockPos middle = middleOf(pos, ltvEndState);
        BlockState base = block.defaultBlockState().setValue(FACING, facing).setValue(STACK, Stack.LTV_END);
        level.setBlock(middle.below(), base.setValue(PART, Part.BOTTOM), 3);
        level.setBlock(middle.above(), base.setValue(PART, Part.TOP), 3);
        level.setBlock(middle, base.setValue(PART, Part.MIDDLE), 3);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && !player.getAbilities().instabuild) {
            switch (state.getValue(STACK)) {
                case DOUBLE -> popResource(level, pos, new ItemStack(ModBlocks.SPEED_LIMIT_ITEM.get()));
                case TRIPLE -> {
                    popResource(level, pos, new ItemStack(ModBlocks.SPEED_LIMIT_ITEM.get()));
                    popResource(level, pos, new ItemStack(ModBlocks.SPEED_LIMIT_ITEM.get()));
                }
                case LTV_END -> popResource(level, pos, new ItemStack(ModBlocks.LTV_END_ITEM.get()));
                default -> { /* NONE */ }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
