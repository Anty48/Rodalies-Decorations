package rodalies;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import javax.annotation.Nullable;

/**
 * Variante "de una cara" de los bloques de logo/franja (r_logo, renfe_logo, cercanias_logo, purple_side):
 * un cubo con la textura distintiva (logo o franja) SOLO en la cara frontal (la cara donde lo colocas) y
 * el fondo liso en las otras cinco. Bloque direccional de 6 caras ({@code FACING}); al colocarlo, la cara
 * con el logo apunta hacia la cara que has clicado ({@link BlockPlaceContext#getClickedFace}).
 *
 * El modelo lleva el logo en su cara NORTE; la blockstate lo rota a cada facing con el mismo mapeo que un
 * bloque direccional vanilla (observer). Se craftea 1:1 (shapeless) con su bloque hermano de caras iguales
 * y viceversa, sin gastar materiales (recetas hand-written, igual que la puerta front/back).
 */
public class DirectionalLogoBlock extends DirectionalBlock {

    public DirectionalLogoBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // La cara del logo apunta hacia la cara que has clicado (hacia fuera de la superficie donde lo pones).
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }
}
