package rodalies;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Bloque "base" de las señales: un poste corto con un pie ensanchado. Ocupa una sola celda y es la
 * pieza con la que se craftean todas las señales ferroviarias. Solo necesita una colision ajustada
 * al modelo (el poste central + el pie).
 */
public class BaseBlock extends Block {

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(7, 0, 7, 9, 16, 9),    // poste
            Block.box(6, 0, 6, 10, 4, 10));  // pie

    public BaseBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }
}
