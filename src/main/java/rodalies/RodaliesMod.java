package rodalies;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import rodalies.network.ModNetwork;

@Mod("rodalies")
public class RodaliesMod {

    public RodaliesMod() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(bus);
        ModBlocks.ITEMS.register(bus);
        ModBlocks.CREATIVE_TABS.register(bus);
        ModBlockEntities.BLOCK_ENTITIES.register(bus);

        ModNetwork.register();

        bus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Integracion OPCIONAL con Create, SOLO con Create 6.0+ (apertura automatica de la puerta al parar
        // en estacion). La API que usa CreateCompat (MovementBehaviour.REGISTRY, SlidingDoorMovementBehaviour)
        // es de Create 6.0; en Create 0.5.x/5.x esas clases estan en otro paquete o no existen, asi que
        // referenciar CreateCompat con 5.x daria NoClassDefFoundError.
        //
        // Por eso comprobamos la VERSION MAYOR, no solo la presencia (isLoaded seria true tambien con 5.x).
        // Si Create no es 6.0+, NO tocamos CreateCompat: como su unica mencion esta dentro de este if (una
        // method reference que solo se enlaza al ejecutarse esa linea), la clase CreateCompat ni siquiera se
        // carga -> imposible que sus imports de Create 6.0 rompan nada. El mod carga sin errores y la puerta
        // funciona a mano (sin apertura automatica en estacion).
        if (isCreate6OrNewer()) {
            event.enqueueWork(rodalies.compat.create.CreateCompat::register);
        }
    }

    /** True solo si Create esta cargado y su version mayor es >= 6 (la API de MovementBehaviour de 6.0). */
    private static boolean isCreate6OrNewer() {
        return ModList.get().getModContainerById("create")
                .map(c -> c.getModInfo().getVersion().getMajorVersion() >= 6)
                .orElse(false);
    }
}
