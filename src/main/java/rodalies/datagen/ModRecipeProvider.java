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

        // Cartel de estacion de Rodalies: como el generico pero con un tinte naranja (O) que marca
        // la version de marca Rodalies.
        // CCC
        // OL
        //  L    -> 1 cartel de Rodalies
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.STATION_SIGNAL.get(), 1)
                .pattern("CCC")
                .pattern("OL ")
                .pattern(" L ")
                .define('C', Blocks.GRAY_CONCRETE)
                .define('L', Items.IRON_INGOT)
                .define('O', Items.ORANGE_DYE)
                .unlockedBy("has_gray_concrete", has(Blocks.GRAY_CONCRETE))
                .save(writer);

        // Cartel de estacion generico (sin logo): banda de hormigon gris (C) arriba sobre un poste
        // de hierro (L). Sin tinte -> version neutra.
        // CCC
        //  L
        //  L    -> 1 cartel generico
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.STATION_SIGNAL_PLAIN.get(), 1)
                .pattern("CCC")
                .pattern(" L ")
                .pattern(" L ")
                .define('C', Blocks.GRAY_CONCRETE)
                .define('L', Items.IRON_INGOT)
                .unlockedBy("has_gray_concrete", has(Blocks.GRAY_CONCRETE))
                .save(writer);

        // Cartel de numero de via: hormigon gris (C) sobre 2 hierros (L) en columna -> 2 carteles.
        // C
        // L
        // L
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.PLATFORM_NUMBER_SIGNAL.get(), 2)
                .pattern("C")
                .pattern("L")
                .pattern("L")
                .define('C', Blocks.GRAY_CONCRETE)
                .define('L', Items.IRON_INGOT)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(writer);

        // Señales de tren cuadradas (T de hierro + tinte). El tinte marca el color de la señal.
        //  L D L
        //  _ L _   (T)
        //  _ L _
        // Cartel de señales (cuadrado blanco): tinte blanco.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.NORMAL_SIGNAL.get(), 1)
                .pattern("LDL")
                .pattern(" L ")
                .pattern(" L ")
                .define('L', Items.IRON_INGOT)
                .define('D', Items.WHITE_DYE)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(writer);

        // Fin de LVT (cuadrado amarillo): misma T, tinte amarillo.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.LVT_END.get(), 1)
                .pattern("LDL")
                .pattern(" L ")
                .pattern(" L ")
                .define('L', Items.IRON_INGOT)
                .define('D', Items.YELLOW_DYE)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(writer);

        // Señales de tren en rombo (+ de hierro + tinte).
        //  _ D _
        //  L L L   (+)
        //  _ L _
        // Señal de velocidad permanente (rombo blanco): tinte blanco.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.SPEED_LIMIT.get(), 1)
                .pattern(" D ")
                .pattern("LLL")
                .pattern(" L ")
                .define('L', Items.IRON_INGOT)
                .define('D', Items.WHITE_DYE)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(writer);

        // LVT (rombo amarillo): mismo +, tinte amarillo.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.LVT.get(), 1)
                .pattern(" D ")
                .pattern("LLL")
                .pattern(" L ")
                .define('L', Items.IRON_INGOT)
                .define('D', Items.YELLOW_DYE)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(writer);
    }
}
