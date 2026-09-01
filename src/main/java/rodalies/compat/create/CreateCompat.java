package rodalies.compat.create;

import com.mojang.logging.LogUtils;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.decoration.slidingDoor.SlidingDoorMovementBehaviour;
import org.slf4j.Logger;
import rodalies.ModBlocks;

/**
 * Integracion OPCIONAL con Create. TODAS las referencias a clases de Create viven en este paquete
 * ({@code rodalies.compat.create}); nunca se cargan si Create no esta instalado.
 *
 * {@link #register()} solo debe llamarse tras comprobar {@code ModList.get().isLoaded("create")}
 * (ver {@code RodaliesMod}). Da a la puerta corredera la <b>apertura automatica al parar en estacion</b>
 * registrando el propio {@link SlidingDoorMovementBehaviour} de Create para nuestra puerta.
 *
 * <b>Por que funciona y es seguro</b>: ese MovementBehaviour de Create es generico para cualquier
 * {@code DoorBlock} — su {@code tickOpen} consulta el {@code DoorControlBehaviour} de la estacion y
 * conmuta la propiedad {@code OPEN} (que nuestra puerta tiene). La unica parte que referencia el
 * BlockEntity propio de Create ({@code SlidingDoorBlockEntity}) es la animacion de cliente, que se salta
 * sola para nuestra puerta (nuestro BE no es un {@code SlidingDoorBlockEntity}); nuestra animacion la
 * sigue dibujando nuestro BER. No desactiva el render del BE ({@code disableBlockEntityRendering=false}),
 * asi que la animacion en el tren no se rompe. Va en try/catch: con otra version de Create simplemente no
 * se activa la apertura en estacion y la puerta funciona igual (abrir a mano + animacion).
 */
public final class CreateCompat {

    private static final Logger LOGGER = LogUtils.getLogger();

    private CreateCompat() {}

    /** Registra el MovementBehaviour de estacion de Create para nuestras dos puertas. */
    public static void register() {
        try {
            SlidingDoorMovementBehaviour behaviour = new SlidingDoorMovementBehaviour();
            MovementBehaviour.REGISTRY.register(ModBlocks.SLIDING_TRAIN_DOOR.get(), behaviour);
            MovementBehaviour.REGISTRY.register(ModBlocks.SLIDING_TRAIN_DOOR_BACK.get(), behaviour);
            LOGGER.info("[rodalies] Create detectado: la puerta corredera se abrira al parar en estacion.");
        } catch (Throwable t) {
            LOGGER.warn("[rodalies] No se pudo activar la apertura en estacion de la puerta (version de "
                    + "Create incompatible?): {}. La puerta funciona igual (abrir a mano + animacion).",
                    t.toString());
        }
    }
}
