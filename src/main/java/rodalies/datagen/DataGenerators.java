package rodalies.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Punto de entrada de los data generators (port a NeoForge 1.21.1). Se dispara con
 * {@code ./gradlew runData} y escribe los JSON en src/generated/resources.
 */
@EventBusSubscriber(modid = "rodalies", bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        // Cliente: blockstates + modelos de bloque e item.
        generator.addProvider(event.includeClient(),
                new ModBlockStateProvider(packOutput, existingFileHelper));

        // Servidor: loot tables (en 1.21 el provider y el subprovider reciben el HolderLookup.Provider).
        generator.addProvider(event.includeServer(), new LootTableProvider(
                packOutput, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(
                        ModBlockLootTables::new, LootContextParamSets.BLOCK)),
                lookup));

        // Servidor: recetas.
        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput, lookup));
    }
}
