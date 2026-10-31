package rodalies;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import rodalies.network.ModNetwork;

/**
 * Clase principal del mod (port a NeoForge 1.21.1). En NeoForge el bus de eventos del mod se inyecta
 * en el constructor (ya no se usa FMLJavaModLoadingContext).
 */
@Mod("rodalies")
public class RodaliesMod {

    public RodaliesMod(IEventBus bus) {
        ModBlocks.BLOCKS.register(bus);
        ModBlocks.ITEMS.register(bus);
        ModBlocks.CREATIVE_TABS.register(bus);
        ModBlockEntities.BLOCK_ENTITIES.register(bus);

        // Registro de red: en NeoForge los paquetes son "payloads" y se registran en
        // RegisterPayloadHandlersEvent (bus del mod). Ver ModNetwork.
        bus.addListener(ModNetwork::register);

        bus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Integracion OPCIONAL con Create (apertura automatica de la puerta al parar en estacion), SOLO
        // con Create 6.0+. DESACTIVADA en este port hasta tener los jars de Create/Flywheel/Registrate
        // para 1.21.1 en ./libs (los de 1.20.1 NO valen). Cuando esten:
        //   1) descomentar la dependencia compileOnly en build.gradle,
        //   2) quitar la exclusion de 'rodalies/compat/**' del sourceSet en build.gradle,
        //   3) descomentar el bloque de abajo.
        // if (isCreate6OrNewer()) {
        //     event.enqueueWork(rodalies.compat.create.CreateCompat::register);
        // }
    }

    /** True solo si Create esta cargado y su version mayor es >= 6 (la API de MovementBehaviour de 6.0). */
    @SuppressWarnings("unused")
    private static boolean isCreate6OrNewer() {
        return ModList.get().getModContainerById("create")
                .map(c -> c.getModInfo().getVersion().getMajorVersion() >= 6)
                .orElse(false);
    }
}
