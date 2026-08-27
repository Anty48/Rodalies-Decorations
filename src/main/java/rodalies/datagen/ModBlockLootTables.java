package rodalies.datagen;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import rodalies.ModBlocks;

import java.util.List;
import java.util.Set;

/**
 * Loot tables de los bloques nuevos: cada bloque se dropea a sí mismo.
 * Solo debe listar en getKnownBlocks() los bloques para los que genera loot.
 */
public class ModBlockLootTables extends BlockLootSubProvider {

    public ModBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.CERCANIAS_LOGO.get());
        dropSelf(ModBlocks.PARABRISAS_447.get());
        dropSelf(ModBlocks.STATION_SIGNAL.get());
        dropSelf(ModBlocks.STATION_SIGNAL_PLAIN.get());
        dropSelf(ModBlocks.PLATFORM_NUMBER_SIGNAL.get());
        dropSelf(ModBlocks.SPEED_LIMIT.get());
        dropSelf(ModBlocks.LVT.get());
        dropSelf(ModBlocks.LVT_END.get());
        dropSelf(ModBlocks.NORMAL_SIGNAL.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return List.of(
                ModBlocks.CERCANIAS_LOGO.get(),
                ModBlocks.PARABRISAS_447.get(),
                ModBlocks.STATION_SIGNAL.get(),
                ModBlocks.STATION_SIGNAL_PLAIN.get(),
                ModBlocks.PLATFORM_NUMBER_SIGNAL.get(),
                ModBlocks.SPEED_LIMIT.get(),
                ModBlocks.LVT.get(),
                ModBlocks.LVT_END.get(),
                ModBlocks.NORMAL_SIGNAL.get());
    }
}
