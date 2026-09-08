package rodalies.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import rodalies.ModBlocks;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Recetas de crafteo de los bloques nuevos.
 *
 * Las señales ferroviarias usan un sistema comun: 1 bloque {@code base} (short) o 2 (normal) en la
 * columna central y los tintes de su fila superior (segun la señal). Cada receta rinde 2 señales.
 * Las señales de estacion (cartel, numero de via) NO entran en este sistema.
 */
public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> writer) {
        // Cercanias logo: tinte rojo (T) en el centro rodeado en cruz por smooth quartz (X).
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CERCANIAS_LOGO.get(), 4)
                .pattern(" X ")
                .pattern("XTX")
                .pattern(" X ")
                .define('X', Blocks.SMOOTH_QUARTZ)
                .define('T', Items.RED_DYE)
                .unlockedBy("has_smooth_quartz", has(Blocks.SMOOTH_QUARTZ))
                .save(writer);

        // Franja naranja+morada CENTRADA: columna vertical de cuarzo (abajo) + 2 tintes encima. Leida de
        // abajo arriba es cuarzo-naranja-morada; asi se distingue de la variante "bottom" (columna al reves).
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ORANGE_PURPLE_LINES.get(), 1)
                .pattern("O")
                .pattern("P")
                .pattern("Q")
                .define('O', Items.ORANGE_DYE)
                .define('P', Items.PURPLE_DYE)
                .define('Q', Blocks.QUARTZ_BLOCK)
                .unlockedBy("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
                .save(writer);

        // Franja naranja+morada BOTTOM: la misma columna al reves (tinte-tinte-cuarzo leido de abajo arriba),
        // es decir cuarzo arriba y los tintes debajo. Rinde la version con la franja pegada abajo.
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ORANGE_PURPLE_LINES_BOTTOM.get(), 1)
                .pattern("Q")
                .pattern("O")
                .pattern("P")
                .define('O', Items.ORANGE_DYE)
                .define('P', Items.PURPLE_DYE)
                .define('Q', Blocks.QUARTZ_BLOCK)
                .unlockedBy("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
                .save(writer);

        // Conversion 1:1 franja de 4 caras <-> franja de 1 cara (sin gastar materiales), como los logos.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ORANGE_PURPLE_LINES_DIRECTIONAL.get(), 1)
                .requires(ModBlocks.ORANGE_PURPLE_LINES.get())
                .unlockedBy("has_orange_purple_lines", has(ModBlocks.ORANGE_PURPLE_LINES.get()))
                .save(writer, new ResourceLocation("rodalies", "orange_purple_lines_directional_from_orange_purple_lines"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ORANGE_PURPLE_LINES.get(), 1)
                .requires(ModBlocks.ORANGE_PURPLE_LINES_DIRECTIONAL.get())
                .unlockedBy("has_orange_purple_lines_directional", has(ModBlocks.ORANGE_PURPLE_LINES_DIRECTIONAL.get()))
                .save(writer, new ResourceLocation("rodalies", "orange_purple_lines_from_orange_purple_lines_directional"));

        // Conversion 1:1 de la variante "bottom" (4 caras <-> 1 cara).
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ORANGE_PURPLE_LINES_BOTTOM_DIRECTIONAL.get(), 1)
                .requires(ModBlocks.ORANGE_PURPLE_LINES_BOTTOM.get())
                .unlockedBy("has_orange_purple_lines_bottom", has(ModBlocks.ORANGE_PURPLE_LINES_BOTTOM.get()))
                .save(writer, new ResourceLocation("rodalies", "orange_purple_lines_bottom_directional_from_orange_purple_lines_bottom"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ORANGE_PURPLE_LINES_BOTTOM.get(), 1)
                .requires(ModBlocks.ORANGE_PURPLE_LINES_BOTTOM_DIRECTIONAL.get())
                .unlockedBy("has_orange_purple_lines_bottom_directional", has(ModBlocks.ORANGE_PURPLE_LINES_BOTTOM_DIRECTIONAL.get()))
                .save(writer, new ResourceLocation("rodalies", "orange_purple_lines_bottom_from_orange_purple_lines_bottom_directional"));

        // Franja doble sobre gray concrete: hormigon gris + franja "bottom" (en cualquier disposicion).
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.DOUBLE_LINES_GRAY_CONCRETE.get(), 1)
                .requires(Blocks.GRAY_CONCRETE)
                .requires(ModBlocks.ORANGE_PURPLE_LINES_BOTTOM.get())
                .unlockedBy("has_orange_purple_lines_bottom", has(ModBlocks.ORANGE_PURPLE_LINES_BOTTOM.get()))
                .save(writer);

        // Luz de tren: 4 hierros en cruz con una antorcha al centro -> 4 luces.
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.TRAIN_LIGHT.get(), 4)
                .pattern(" I ")
                .pattern("ITI")
                .pattern(" I ")
                .define('I', Items.IRON_INGOT)
                .define('T', Items.TORCH)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(writer);

        // Luz frontal de tren: hormigon gris + luz de tren (en cualquier disposicion).
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.FRONT_LIGHT.get(), 1)
                .requires(Blocks.GRAY_CONCRETE)
                .requires(ModBlocks.TRAIN_LIGHT.get())
                .unlockedBy("has_train_light", has(ModBlocks.TRAIN_LIGHT.get()))
                .save(writer);

        // Ventana de tren: 1 bloque de cuarzo + 1 cristal, en cualquier disposicion (shapeless).
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.TRAIN_WINDOW.get(), 1)
                .requires(Blocks.QUARTZ_BLOCK)
                .requires(Blocks.GLASS)
                .unlockedBy("has_glass", has(Blocks.GLASS))
                .save(writer);

        // Bloque de velocidad maxima: cuarzo normal (Q) con un tinte negro (D) encima (el rombo).
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.TRAIN_MAX_SPEED.get(), 1)
                .pattern("D")
                .pattern("Q")
                .define('D', Items.BLACK_DYE)
                .define('Q', Blocks.QUARTZ_BLOCK)
                .unlockedBy("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
                .save(writer);

        // Señal de hectometro: 3 dioritas en columna.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.HECTOMETER_SIGNAL.get(), 1)
                .pattern("D")
                .pattern("D")
                .pattern("D")
                .define('D', Blocks.DIORITE)
                .unlockedBy("has_diorite", has(Blocks.DIORITE))
                .save(writer);

        // Parabrisas 447: lingote de hierro (L) arriba, fila de 3 cristales tintados negros (C).
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.PARABRISAS_447.get(), 1)
                .pattern(" L ")
                .pattern("CCC")
                .define('L', Items.IRON_INGOT)
                .define('C', Blocks.BLACK_STAINED_GLASS)
                .unlockedBy("has_black_stained_glass", has(Blocks.BLACK_STAINED_GLASS))
                .save(writer);

        // Cartel de estacion de Rodalies (con tinte naranja de marca).
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.STATION_SIGNAL.get(), 1)
                .pattern("CCC")
                .pattern("OL ")
                .pattern(" L ")
                .define('C', Blocks.GRAY_CONCRETE)
                .define('L', Items.IRON_INGOT)
                .define('O', Items.ORANGE_DYE)
                .unlockedBy("has_gray_concrete", has(Blocks.GRAY_CONCRETE))
                .save(writer);

        // Cartel de estacion generico (sin logo).
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.STATION_SIGNAL_PLAIN.get(), 1)
                .pattern("CCC")
                .pattern(" L ")
                .pattern(" L ")
                .define('C', Blocks.GRAY_CONCRETE)
                .define('L', Items.IRON_INGOT)
                .unlockedBy("has_gray_concrete", has(Blocks.GRAY_CONCRETE))
                .save(writer);

        // Cartel de numero de via: hormigon gris (C) sobre 2 hierros (L) -> 2 carteles.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.PLATFORM_NUMBER_SIGNAL.get(), 2)
                .pattern("C")
                .pattern("L")
                .pattern("L")
                .define('C', Blocks.GRAY_CONCRETE)
                .define('L', Items.IRON_INGOT)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(writer);

        // Puerta corredera de tren: como una puerta vanilla (2 columnas, 3 de alto -> 3 puertas), pero
        // terracota naranja (T) arriba y abajo con black stained glass (G) en medio (la ventanilla).
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.SLIDING_TRAIN_DOOR.get(), 3)
                .pattern("TT")
                .pattern("GG")
                .pattern("TT")
                .define('T', Blocks.ORANGE_TERRACOTTA)
                .define('G', Blocks.BLACK_STAINED_GLASS)
                .unlockedBy("has_orange_terracotta", has(Blocks.ORANGE_TERRACOTTA))
                .save(writer);

        // Intercambio de sentido de apertura (adelante <-> atras) sin gastar materiales: una puerta
        // se convierte en la otra en la mesa de crafteo, y viceversa, infinitamente. IDs explicitos
        // para no chocar con la receta principal de la puerta de arriba.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, ModBlocks.SLIDING_TRAIN_DOOR_BACK.get(), 1)
                .requires(ModBlocks.SLIDING_TRAIN_DOOR.get())
                .unlockedBy("has_sliding_train_door", has(ModBlocks.SLIDING_TRAIN_DOOR.get()))
                .save(writer, new ResourceLocation("rodalies", "sliding_train_door_back_from_front"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, ModBlocks.SLIDING_TRAIN_DOOR.get(), 1)
                .requires(ModBlocks.SLIDING_TRAIN_DOOR_BACK.get())
                .unlockedBy("has_sliding_train_door_back", has(ModBlocks.SLIDING_TRAIN_DOOR_BACK.get()))
                .save(writer, new ResourceLocation("rodalies", "sliding_train_door_front_from_back"));

        // Base: una reja de hierro (I) encima de hormigon gris o su polvo (C), una sobre otra.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.BASE.get(), 1)
                .pattern("I")
                .pattern("C")
                .define('I', Items.IRON_BARS)
                .define('C', Ingredient.of(Blocks.GRAY_CONCRETE, Blocks.GRAY_CONCRETE_POWDER))
                .unlockedBy("has_iron_bars", has(Items.IRON_BARS))
                .save(writer);

        // --- Señales ferroviarias: base(s) en la columna central + tintes en la fila de arriba. ---
        // Rombos (3 tintes en horizontal):
        signRecipes(writer, ModBlocks.SPEED_LIMIT.get(), ModBlocks.SPEED_LIMIT_SHORT.get(),
                Items.WHITE_DYE, Items.WHITE_DYE, Items.WHITE_DYE);
        signRecipes(writer, ModBlocks.LTV.get(), ModBlocks.LTV_SHORT.get(),
                Items.YELLOW_DYE, Items.YELLOW_DYE, Items.YELLOW_DYE);
        // Cuadrados (1 tinte al centro):
        signRecipes(writer, ModBlocks.NORMAL_SIGNAL.get(), ModBlocks.NORMAL_SIGNAL_SHORT.get(),
                null, Items.WHITE_DYE, null);
        signRecipes(writer, ModBlocks.LTV_END.get(), ModBlocks.LTV_END_SHORT.get(),
                null, Items.YELLOW_DYE, null);
        // Apeadero: blanco-gris-blanco. Silbato: gris-blanco-gris. Detencion: blanco-rojo-blanco.
        signRecipes(writer, ModBlocks.APEADERO_SIGNAL.get(), ModBlocks.APEADERO_SIGNAL_SHORT.get(),
                Items.WHITE_DYE, Items.GRAY_DYE, Items.WHITE_DYE);
        signRecipes(writer, ModBlocks.SILBATO_SIGNAL.get(), ModBlocks.SILBATO_SIGNAL_SHORT.get(),
                Items.GRAY_DYE, Items.WHITE_DYE, Items.GRAY_DYE);
        signRecipes(writer, ModBlocks.STOP_SIGNAL.get(), ModBlocks.STOP_SIGNAL_SHORT.get(),
                Items.WHITE_DYE, Items.RED_DYE, Items.WHITE_DYE);
    }

    /**
     * Genera las dos recetas (normal y short) de una señal. La fila superior lleva los tintes
     * left/center/right (null = celda vacia) y la columna central 2 bases (normal) o 1 (short).
     * Cada receta rinde 2 señales.
     */
    private void signRecipes(Consumer<FinishedRecipe> writer, ItemLike full, ItemLike shortSign,
                             @Nullable Item left, Item center, @Nullable Item right) {
        String top = "" + (left != null ? 'L' : ' ') + 'C' + (right != null ? 'R' : ' ');
        buildSign(writer, full, top, true, left, center, right);
        buildSign(writer, shortSign, top, false, left, center, right);
    }

    private void buildSign(Consumer<FinishedRecipe> writer, ItemLike out, String top, boolean normal,
                           @Nullable Item left, Item center, @Nullable Item right) {
        ItemLike base = ModBlocks.BASE.get();
        ShapedRecipeBuilder b = ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, out, 2)
                .pattern(top)
                .pattern(" B ");
        if (normal) {
            b.pattern(" B ");
        }
        b.define('C', center).define('B', base);
        if (left != null) {
            b.define('L', left);
        }
        if (right != null) {
            b.define('R', right);
        }
        b.unlockedBy("has_base", has(base)).save(writer);
    }
}
