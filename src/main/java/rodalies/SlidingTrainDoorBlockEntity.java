package rodalies;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

/**
 * Estado de animacion de una {@link SlidingTrainDoorBlock}. Vive solo en la celda LOWER, que es la que
 * dibuja las dos hojas completas (2 bloques de alto) via {@code SlidingTrainDoorRenderer}.
 *
 * No guarda si esta abierta: eso lo dice la propiedad {@code OPEN} de la blockstate (que ya se
 * sincroniza sola). Aqui solo llevamos el progreso visual [0=cerrada, 1=abierta], que avanza en el
 * cliente hacia el objetivo (OPEN) para animar el deslizamiento. Es puramente visual: no se guarda en
 * NBT ni se sincroniza.
 */
public class SlidingTrainDoorBlockEntity extends BlockEntity {

    /** Cuanto avanza la animacion por tick (1/velocidad). 0.12 -> se abre/cierra en ~8-9 ticks. */
    private static final float STEP = 0.12f;

    private float progress;      // [0..1] estado actual de la animacion
    private float prevProgress;  // valor del tick anterior (para interpolar con partialTick)

    public SlidingTrainDoorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SLIDING_TRAIN_DOOR_BE.get(), pos, state);
        float target = state.getValue(SlidingTrainDoorBlock.OPEN) ? 1f : 0f;
        this.progress = target;
        this.prevProgress = target;
    }

    /** Tick de cliente: acerca el progreso al objetivo (OPEN de la blockstate). */
    public void clientTick(BlockState state) {
        prevProgress = progress;
        float target = state.getValue(SlidingTrainDoorBlock.OPEN) ? 1f : 0f;
        if (progress < target) {
            progress = Math.min(target, progress + STEP);
        } else if (progress > target) {
            progress = Math.max(target, progress - STEP);
        }
    }

    /** Progreso interpolado [0..1] para el frame actual. */
    public float getRenderProgress(float partialTick) {
        return prevProgress + (progress - prevProgress) * partialTick;
    }

    /**
     * Caja de render ampliada: las hojas salen hacia arriba (2 celdas) y, al abrir, hacia los lados y
     * un poco adelante. Sin ampliar, el render se corta al salir la celda LOWER del frustum.
     */
    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos()).inflate(1.0, 2.0, 1.0);
    }
}
