package rodalies;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Cartel de numero de via con estado {@code doubled} (dos paneles). Ver documentacion en la version Forge.
 *
 * Port a 1.21: duplicar va en {@code useItemOn}, editar en {@code useWithoutItem};
 * {@code playerWillDestroy} devuelve {@code BlockState}.
 */
public class PlatformNumberSignBlock extends RailSignBlock {

    public static final BooleanProperty DOUBLED = BooleanProperty.create("doubled");

    private static final VoxelShape DOUBLE_TOP = Shapes.or(
            Block.box(7, 0, 7, 9, 16, 9),
            Block.box(7, 6, 9, 9, 15, 16),
            Block.box(7, 6, 0, 9, 15, 7));

    public PlatformNumberSignBlock(Properties properties) {
        super(properties, RailSignType.PLATFORM_NUMBER);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, Part.MIDDLE)
                .setValue(DOUBLED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder); // FACING, PART
        builder.add(DOUBLED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        if (state.getValue(DOUBLED) && state.getValue(PART) == Part.TOP) {
            return rotateToFacing(DOUBLE_TOP, state.getValue(FACING));
        }
        return super.getShape(state, level, pos, ctx);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        // Duplicar: clic derecho con otro cartel de numero de via en mano, sin agacharse, sobre uno simple.
        if (!state.getValue(DOUBLED) && !player.isSecondaryUseActive()
                && held.is(ModBlocks.PLATFORM_NUMBER_SIGNAL_ITEM.get())) {
            if (!level.isClientSide) {
                setDoubledOnAllCells(level, pos, state);
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
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
        BlockPos middle = middleOf(pos, state);
        BlockEntity be = level.getBlockEntity(middle);
        if (!(be instanceof RailSignBlockEntity sign)) {
            return InteractionResult.PASS;
        }
        int slot = state.getValue(DOUBLED) ? slotForHit(state.getValue(FACING), middle, hit) : 0;
        if (level.isClientSide) {
            rodalies.client.ClientHooks.openRailSignScreen(sign, slot);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    private static int slotForHit(Direction facing, BlockPos middle, BlockHitResult hit) {
        double dx = hit.getLocation().x - (middle.getX() + 0.5);
        double dz = hit.getLocation().z - (middle.getZ() + 0.5);
        double modelZ = switch (facing) {
            case EAST -> -dx;
            case SOUTH -> -dz;
            case WEST -> dx;
            default -> dz; // NORTH
        };
        return modelZ >= 0 ? 0 : 1;
    }

    private void setDoubledOnAllCells(Level level, BlockPos pos, BlockState state) {
        BlockPos middle = middleOf(pos, state);
        for (BlockPos p : new BlockPos[]{middle.below(), middle, middle.above()}) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() == this && s.hasProperty(DOUBLED)) {
                level.setBlock(p, s.setValue(DOUBLED, true), 3);
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && !player.getAbilities().instabuild && state.getValue(DOUBLED)) {
            popResource(level, pos, new ItemStack(ModBlocks.PLATFORM_NUMBER_SIGNAL_ITEM.get()));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
