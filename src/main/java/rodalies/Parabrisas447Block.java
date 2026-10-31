package rodalies;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import javax.annotation.Nullable;

/**
 * Parabrisas del tren 447: multiblock horizontal de 3 celdas (izquierda/centro/derecha). La celda
 * CENTRAL dibuja el modelo 3D completo; las laterales son solo colision. Ver documentacion en Forge.
 *
 * Port a 1.21: {@code playerWillDestroy} devuelve {@code BlockState}; varios metodos de bloque pasan a
 * protected. El render translucido del cristal se declara en el JSON del modelo ({@code render_type}).
 */
public class Parabrisas447Block extends HorizontalDirectionalBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    public static final MapCodec<Parabrisas447Block> CODEC = simpleCodec(Parabrisas447Block::new);

    public Parabrisas447Block(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, Part.CENTER));
    }

    @Override
    protected MapCodec<? extends Parabrisas447Block> codec() {
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
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    private static Direction widthDir(Direction facing) {
        return facing.getClockWise();
    }

    private static BlockPos centerOf(BlockPos pos, BlockState state) {
        Direction w = widthDir(state.getValue(FACING));
        return switch (state.getValue(PART)) {
            case LEFT -> pos.relative(w);
            case RIGHT -> pos.relative(w.getOpposite());
            default -> pos;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos center = context.getClickedPos();
        Direction w = widthDir(facing);
        BlockPos left = center.relative(w.getOpposite());
        BlockPos right = center.relative(w);

        if (level.getBlockState(left).canBeReplaced(context)
                && level.getBlockState(right).canBeReplaced(context)) {
            return defaultBlockState().setValue(FACING, facing).setValue(PART, Part.CENTER);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            Direction w = widthDir(state.getValue(FACING));
            level.setBlock(pos.relative(w.getOpposite()), state.setValue(PART, Part.LEFT), 3);
            level.setBlock(pos.relative(w), state.setValue(PART, Part.RIGHT), 3);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            BlockPos center = centerOf(pos, state);
            Direction w = widthDir(state.getValue(FACING));
            BlockPos[] all = { center, center.relative(w.getOpposite()), center.relative(w) };
            for (BlockPos p : all) {
                if (p.equals(pos)) continue;
                if (level.getBlockState(p).getBlock() == this) {
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), 35);
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
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
        LEFT("left"),
        CENTER("center"),
        RIGHT("right");

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
