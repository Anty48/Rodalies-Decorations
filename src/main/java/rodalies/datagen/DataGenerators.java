package rodalies.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Set;

/**
 * Punto de entrada de los data generators. Se dispara con `./gradlew runData`
 * y escribe los JSON en src/generated/resources.
 */
@Mod.EventBusSubscriber(modid = "rodalies", bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        // Cliente: blockstates + modelos de bloque e item.
        generator.addProvider(event.includeClient(),
                new ModBlockStateProvider(packOutput, existingFileHelper));

        // Servidor: loot tables.
        generator.addProvider(event.includeServer(), new LootTableProvider(
                packOutput, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(
                        ModBlockLootTables::new, LootContextParamSets.BLOCK))));

        // Servidor: recetas.
        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput));
    }
}
