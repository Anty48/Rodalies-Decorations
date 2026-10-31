package rodalies;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * BlockEntity de la puerta corredera (solo en la celda LOWER). Existe para que su BER
 * ({@code SlidingTrainDoorRenderer}) dibuje y anime las dos hojas. Ver documentacion en la version Forge.
 *
 * Port a 1.21: la caja de render ampliada se declara ahora en el renderer
 * ({@code getRenderBoundingBox(be)} de IBlockEntityRendererExtension), no aqui.
 */
public class SlidingTrainDoorBlockEntity extends BlockEntity {

    public SlidingTrainDoorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SLIDING_TRAIN_DOOR_BE.get(), pos, state);
    }
}
