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

        // Franja naranja+morada: laterales con la franja, top/bottom smooth quartz (cube_bottom_top).
        // Nota: el cuarzo liso reutiliza la textura quartz_block_bottom (no existe smooth_quartz.png).
        ResourceLocation smoothQuartz = mcLoc("block/quartz_block_bottom");
        ModelFile linesModel = models().cubeBottomTop(
                "orange_purple_lines", modLoc("block/orange_purple_lines"), smoothQuartz, smoothQuartz);
        simpleBlockWithItem(ModBlocks.ORANGE_PURPLE_LINES.get(), linesModel);

        // Franja "bottom": igual que la anterior pero con su propia textura lateral (franja pegada abajo).
        ModelFile linesBottomModel = models().cubeBottomTop(
                "orange_purple_lines_bottom", modLoc("block/orange_purple_lines_bottom"), smoothQuartz, smoothQuartz);
        simpleBlockWithItem(ModBlocks.ORANGE_PURPLE_LINES_BOTTOM.get(), linesBottomModel);

        // Franja doble sobre gray concrete: laterales con la franja, top gray concrete, bottom smooth quartz.
        ResourceLocation grayConcrete = mcLoc("block/gray_concrete");
        ModelFile doubleLinesGrayModel = models().cubeBottomTop(
                "double_lines_gray_concrete", modLoc("block/orange_purple_lines_under_gray"), smoothQuartz, grayConcrete);
        simpleBlockWithItem(ModBlocks.DOUBLE_LINES_GRAY_CONCRETE.get(), doubleLinesGrayModel);

        // Luz de tren: todas las caras iguales (cubeAll).
        ModelFile trainLightModel = models().cubeAll("train_light", modLoc("block/train_light"));
        simpleBlockWithItem(ModBlocks.TRAIN_LIGHT.get(), trainLightModel);

        // Luz frontal de tren: laterales con los faros, top/bottom gray concrete.
        ModelFile frontLightModel = models().cubeBottomTop(
                "front_light", modLoc("block/frontal_train_lights"), grayConcrete, grayConcrete);
        simpleBlockWithItem(ModBlocks.FRONT_LIGHT.get(), frontLightModel);

        // Ventana de tren: laterales con el cristal train_window, top/bottom smooth quartz. El render
        // translucido se asigna en ClientSetup; aqui solo el modelo (renderType translucent para el mesh).
        ModelFile trainWindowModel = models().cubeBottomTop(
                "train_window", modLoc("block/train_window"), smoothQuartz, smoothQuartz)
                .renderType("translucent");
        simpleBlockWithItem(ModBlocks.TRAIN_WINDOW.get(), trainWindowModel);
    }
}
