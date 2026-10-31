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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import rodalies.client.ClientHooks;

import javax.annotation.Nullable;

/**
 * Senal de hectometro: poste de diorita con placa (multiblock vertical de 2 celdas). Ver documentacion
 * completa en la version Forge.
 *
 * Port a 1.21: {@code use()} -> {@code useWithoutItem}; {@code playerWillDestroy} devuelve
 * {@code BlockState}; {@code DistExecutor} desaparece; varios metodos de bloque pasan a protected.
 */
public class HectometerSignBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    private static final VoxelShape POLE = Block.box(6, 0, 6, 10, 16, 10);
    private static final VoxelShape TOP_NORTH = Shapes.or(
            Block.box(6, 0, 6, 10, 5, 10),
            Block.box(3, 5, 6, 13, 16, 10));

    public static final MapCodec<HectometerSignBlock> CODEC = simpleCodec(HectometerSignBlock::new);

    public HectometerSignBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, Part.BOTTOM));
    }

    @Override
    protected MapCodec<? extends HectometerSignBlock> codec() {
        return CODEC;
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
            return RailSignBlock.rotateToFacing(TOP_NORTH, state.getValue(FACING));
        }
        return POLE;
    }

    private static BlockPos bottomOf(BlockPos pos, BlockState state) {
        return state.getValue(PART) == Part.TOP ? pos.below() : pos;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos bottom = context.getClickedPos();
        if (level.getBlockState(bottom.above()).canBeReplaced(context)) {
            return defaultBlockState().setValue(FACING, facing).setValue(PART, Part.BOTTOM);
        }
        return null;
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
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            BlockPos bottom = bottomOf(pos, state);
            for (BlockPos p : new BlockPos[]{bottom, bottom.above()}) {
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
        BlockPos bottom = bottomOf(pos, state);
        BlockEntity be = level.getBlockEntity(bottom);
        if (!(be instanceof HectometerSignBlockEntity sign)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            ClientHooks.openHectometerScreen(sign);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == Part.BOTTOM ? new HectometerSignBlockEntity(pos, state) : null;
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
