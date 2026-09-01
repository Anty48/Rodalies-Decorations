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
        // Integracion OPCIONAL con Create: solo si esta instalado. Da a la puerta corredera la apertura
        // automatica al parar en estacion (MovementBehaviour de Create). Sin Create el mod funciona igual.
        // El registro va en enqueueWork porque el registro de comportamientos de Create es un mapa comun.
        if (ModList.get().isLoaded("create")) {
            event.enqueueWork(rodalies.compat.create.CreateCompat::register);
        }
    }
}
