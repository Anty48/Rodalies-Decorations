package rodalies;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import rodalies.client.ClientHooks;

import javax.annotation.Nullable;

/**
 * Cartel grande de estacion (multiblock FLOOR/WALL/AIR). Ver documentacion completa en la version Forge.
 *
 * Port a 1.21: {@code use()} -> {@code useWithoutItem}; {@code playerWillDestroy} devuelve
 * {@code BlockState}; {@code DistExecutor} desaparece; varios metodos de bloque pasan a protected.
 */
public class StationSignalBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
    public static final EnumProperty<Mount> MOUNT = EnumProperty.create("mount", Mount.class);

    private static final VoxelShape POLE = Block.box(7, 0, 7, 9, 16, 9);
    private static final VoxelShape FLOOR_PANEL_N = Block.box(0, 5, 6, 16, 16, 10);
    private static final VoxelShape WALL_PANEL_N = Block.box(0, 4, 14, 16, 13, 16);
    private static final VoxelShape AIR_PANEL_N = Block.box(0, 4, 7, 16, 13, 9);

    private final boolean hasLogo;

    public static final MapCodec<StationSignalBlock> CODEC = simpleCodec(p -> new StationSignalBlock(p, true));

    public StationSignalBlock(Properties properties, boolean hasLogo) {
        super(properties);
        this.hasLogo = hasLogo;
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, Part.MID)
                .setValue(MOUNT, Mount.FLOOR));
    }

    @Override
    protected MapCodec<? extends StationSignalBlock> codec() {
        return CODEC;
    }

    public boolean hasLogo() {
        return hasLogo;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, MOUNT);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        Direction facing = state.getValue(FACING);
        return switch (state.getValue(MOUNT)) {
            case FLOOR -> switch (state.getValue(PART)) {
                case BASE, MID -> POLE;
                default -> RailSignBlock.rotateToFacing(FLOOR_PANEL_N, facing);
            };
            case WALL -> RailSignBlock.rotateToFacing(WALL_PANEL_N, facing);
            case AIR -> RailSignBlock.rotateToFacing(AIR_PANEL_N, facing);
        };
    }

    private static Direction widthDir(Direction facing) {
        return facing.getClockWise();
    }

    private static BlockPos midOf(BlockPos pos, BlockState state) {
        Direction w = widthDir(state.getValue(FACING));
        if (state.getValue(MOUNT) == Mount.FLOOR) {
            return switch (state.getValue(PART)) {
                case BASE -> pos.above();
                case TOP -> pos.below();
                case TOP_LEFT -> pos.below().relative(w);
                case TOP_RIGHT -> pos.below().relative(w.getOpposite());
                default -> pos;
            };
        }
        return switch (state.getValue(PART)) {
            case TOP_LEFT -> pos.relative(w);
            case TOP_RIGHT -> pos.relative(w.getOpposite());
            default -> pos;
        };
    }

    private static BlockPos[] cellsFromMid(BlockPos mid, BlockState state) {
        Direction w = widthDir(state.getValue(FACING));
        if (state.getValue(MOUNT) == Mount.FLOOR) {
            return new BlockPos[] {
                    mid.below(),
                    mid,
                    mid.above(),
                    mid.above().relative(w.getOpposite()),
                    mid.above().relative(w)
            };
        }
        return new BlockPos[] {
                mid,
                mid.relative(w.getOpposite()),
                mid.relative(w)
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        Direction clicked = context.getClickedFace();
        Direction look = context.getHorizontalDirection();

        Mount mount;
        Direction facing;
        if (clicked == Direction.UP) {
            mount = Mount.FLOOR;
            facing = look.getOpposite();
        } else if (clicked.getAxis().isHorizontal()) {
            if (clicked.getAxis() == look.getAxis()) {
                mount = Mount.WALL;
                facing = clicked;
            } else {
                mount = Mount.AIR;
                facing = look.getOpposite();
            }
        } else {
            mount = Mount.AIR;
            facing = look.getOpposite();
        }

        BlockState state = defaultBlockState().setValue(FACING, facing).setValue(MOUNT, mount);
        BlockPos clickedPos = context.getClickedPos();
        Direction w = widthDir(facing);

        if (mount == Mount.FLOOR) {
            state = state.setValue(PART, Part.BASE);
            BlockPos mid = clickedPos.above();
            return allReplaceable(level, context, cellsFromMid(mid, state), clickedPos) ? state : null;
        }

        if (mount == Mount.AIR && clicked.getAxis().isHorizontal()) {
            Part clickedPart = clicked == w ? Part.TOP_LEFT : Part.TOP_RIGHT;
            state = state.setValue(PART, clickedPart);
            BlockPos mid = midOf(clickedPos, state);
            return allReplaceable(level, context, cellsFromMid(mid, state), clickedPos) ? state : null;
        }

        state = state.setValue(PART, Part.MID);
        return allReplaceable(level, context, cellsFromMid(clickedPos, state), clickedPos) ? state : null;
    }

    private static boolean allReplaceable(Level level, BlockPlaceContext context, BlockPos[] cells, BlockPos clickedPos) {
        for (BlockPos p : cells) {
            if (p.equals(clickedPos)) continue;
            if (!level.getBlockState(p).canBeReplaced(context)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide) {
            return;
        }
        Direction w = widthDir(state.getValue(FACING));
        if (state.getValue(MOUNT) == Mount.FLOOR) {
            BlockPos mid = pos.above();
            BlockPos top = mid.above();
            level.setBlock(mid, state.setValue(PART, Part.MID), 3);
            level.setBlock(top, state.setValue(PART, Part.TOP), 3);
            level.setBlock(top.relative(w.getOpposite()), state.setValue(PART, Part.TOP_LEFT), 3);
            level.setBlock(top.relative(w), state.setValue(PART, Part.TOP_RIGHT), 3);
        } else {
            BlockPos mid = midOf(pos, state);
            Part part = state.getValue(PART);
            if (part != Part.MID) {
                level.setBlock(mid, state.setValue(PART, Part.MID), 3);
            }
            if (part != Part.TOP_LEFT) {
                level.setBlock(mid.relative(w.getOpposite()), state.setValue(PART, Part.TOP_LEFT), 3);
            }
            if (part != Part.TOP_RIGHT) {
                level.setBlock(mid.relative(w), state.setValue(PART, Part.TOP_RIGHT), 3);
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            BlockPos mid = midOf(pos, state);
            for (BlockPos p : cellsFromMid(mid, state)) {
                if (p.equals(pos)) continue;
                if (level.getBlockState(p).getBlock() == this) {
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), 35);
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        BlockPos mid = midOf(pos, state);
        BlockEntity be = level.getBlockEntity(mid);
        if (!(be instanceof StationSignalBlockEntity sign)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            ClientHooks.openStationSignalScreen(sign);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == Part.MID ? new StationSignalBlockEntity(pos, state) : null;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    public enum Mount implements StringRepresentable {
        FLOOR("floor"),
        WALL("wall"),
        AIR("air");

        private final String name;

        Mount(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum Part implements StringRepresentable {
        BASE("base"),
        MID("mid"),
        TOP("top"),
        TOP_LEFT("top_left"),
        TOP_RIGHT("top_right");

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
