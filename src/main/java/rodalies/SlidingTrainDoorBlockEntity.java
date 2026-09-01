package rodalies;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

/**
 * BlockEntity de la puerta corredera (solo en la celda LOWER). Existe para que su BER
 * ({@code SlidingTrainDoorRenderer}) dibuje y anime las dos hojas — tanto en el mundo como sobre un tren
 * de Create, que renderiza los BlockEntity viajeros de sus contraptions.
 *
 * El progreso de la animacion NO se guarda aqui: Create RECREA los BlockEntity de una contraption en cada
 * cambio de bloque (su {@code AbstractContraptionEntity.handleBlockChange} llama a
 * {@code resetClientContraption()} salvo para su propia SlidingDoorBlock), asi que un progreso guardado en
 * el BE se perderia en cada toggle -> la puerta no animaria en el tren. Por eso el BER lo guarda fuera,
 * keyed por (nivel de render, posicion), que sobrevive a la recreacion. Ver {@code SlidingTrainDoorRenderer}.
 */
public class SlidingTrainDoorBlockEntity extends BlockEntity {

    public SlidingTrainDoorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SLIDING_TRAIN_DOOR_BE.get(), pos, state);
    }

    /** Caja de render ampliada: las hojas ocupan 2 celdas de alto y salen a los lados al abrir. */
    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos()).inflate(1.0, 2.0, 1.0);
    }
}
