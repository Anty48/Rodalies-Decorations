package rodalies.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import rodalies.ModBlocks;

import java.util.function.Consumer;

/**
 * Recetas de crafteo de los bloques nuevos.
 */
public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> writer) {
        // Cercanias logo: tinte rojo (T) en el centro rodeado en cruz por smooth quartz (X).
        //  X
        // XTX   -> 4 bloques
        //  X
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CERCANIAS_LOGO.get(), 4)
                .pattern(" X ")
                .pattern("XTX")
                .pattern(" X ")
                .define('X', Blocks.SMOOTH_QUARTZ)
                .define('T', Items.RED_DYE)
                .unlockedBy("has_smooth_quartz", has(Blocks.SMOOTH_QUARTZ))
                .save(writer);

        // Parabrisas 447: lingote de hierro (L) arriba, fila de 3 cristales tintados negros (C).
        //  L
        // CCC   -> 1 parabrisas (coloca las 3 celdas)
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.PARABRISAS_447.get(), 1)
                .pattern(" L ")
                .pattern("CCC")
                .define('L', Items.IRON_INGOT)
                .define('C', Blocks.BLACK_STAINED_GLASS)
                .unlockedBy("has_black_stained_glass", has(Blocks.BLACK_STAINED_GLASS))
                .save(writer);

        // Cartel de estacion: banda de hormigon gris (C) arriba sobre un poste de hierro (L).
        // CCC
        //  L
        //  L    -> 1 cartel
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.STATION_SIGNAL.get(), 1)
                .pattern("CCC")
                .pattern(" L ")
                .pattern(" L ")
                .define('C', Blocks.GRAY_CONCRETE)
                .define('L', Items.IRON_INGOT)
                .unlockedBy("has_gray_concrete", has(Blocks.GRAY_CONCRETE))
                .save(writer);
    }
}
