package rodalies;

import net.minecraftforge.fml.common.Mod;
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
    }
}