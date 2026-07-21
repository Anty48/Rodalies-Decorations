package rodalies.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import rodalies.ModBlocks;

/**
 * Genera blockstate + modelo de bloque + modelo de item para los bloques nuevos.
 */
public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, "rodalies", exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // Cercanias logo: cube_bottom_top (top/bottom = una textura, laterales = otra).
        // La textura del item es la del bloque (simpleBlockWithItem lo hereda).
        ResourceLocation topBottom = modLoc("block/cercanias_top_bottom");
        ResourceLocation side = modLoc("block/cercanias_side");

        ModelFile cercaniasModel = models().cubeBottomTop(
                "cercanias_logo", side, topBottom, topBottom);

        simpleBlockWithItem(ModBlocks.CERCANIAS_LOGO.get(), cercaniasModel);
    }
}
