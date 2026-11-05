package rodalies;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

/**
 * Caja de conexiones con cables: la misma caja que {@link JunctionBoxBlock} pero con dos cables que salen
 * por un lado y bajan al suelo pegados al borde de la celda. No tiene menu ni desplazamiento.
 *
 * {@code FACING} = lado por el que salen los cables. Al colocarla, los cables quedan en el lado opuesto al
 * jugador (hacia donde mira). El modelo tiene los cables al sur; la blockstate lo rota.
 */
public class JunctionBoxCablesBlock extends HorizontalDirectionalBlock {

    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            SHAPES.put(dir, Shapes.or(
                    Block.box(6.5, 0, 6.5, 9.5, 7, 9.5),
                    Block.box(3, 7, 3, 13, 13, 13),
                    Block.box(2, 9, 2, 14, 11, 14),
                    rotated(dir, 5, 0, 13, 8, 11.5, 16)));
        }
    }

    public JunctionBoxCablesBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    /** Caja definida con los cables al sur, girada para que los cables queden hacia {@code dir}. */
    private static VoxelShape rotated(Direction dir, double x1, double y1, double z1, double x2, double y2, double z2) {
        return switch (dir) {
            case WEST -> Block.box(16 - z2, y1, x1, 16 - z1, y2, x2);
            case NORTH -> Block.box(16 - x2, y1, 16 - z2, 16 - x1, y2, 16 - z1);
            case EAST -> Block.box(z1, y1, 16 - x2, z2, y2, 16 - x1);
            default -> Block.box(x1, y1, z1, x2, y2, z2);
        };
    }
}
