package rodalies.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import rodalies.ModBlocks;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * Recetas de crafteo de los bloques nuevos (port a NeoForge 1.21.1).
 *
 * Port: en 1.21.1 {@link RecipeProvider} recibe {@code (PackOutput, CompletableFuture<HolderLookup.Provider>)}
 * y {@code buildRecipes(RecipeOutput)} recibe el output por parametro. El guardado con id explicito usa la
 * sobrecarga de String ({@code "rodalies:..."}).
 */
public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        // --- Bloques antiguos (antes con recetas escritas a mano; ahora por datagen, formato 1.21). ---
        // R logo: cruz de terracota naranja (O) con cuarzo (gema, Q) al centro.
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.R_LOGO.get(), 4)
                .pattern(" O ").pattern("OQO").pattern(" O ")
                .define('O', Blocks.ORANGE_TERRACOTTA)
                .define('Q', Items.QUARTZ)
                .unlockedBy("has_quartz", has(Items.QUARTZ))
                .save(output);
        // Renfe logo: cruz de bloque de cuarzo (O) con tinte lila (Q) al centro.
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RENFE_LOGO.get(), 4)
                .pattern(" O ").pattern("OQO").pattern(" O ")
                .define('O', Blocks.QUARTZ_BLOCK)
                .define('Q', Items.PURPLE_DYE)
                .unlockedBy("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
                .save(output);
        // Purple side: bloque de cuarzo + tinte lila (shapeless).
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.PURPLE_SIDE.get(), 1)
                .requires(Blocks.QUARTZ_BLOCK)
                .requires(Items.PURPLE_DYE)
                .unlockedBy("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
                .save(output);
        // Conversiones 1:1 caras iguales <-> una cara de los logos (sin gastar materiales).
        convertPair(output, "r_logo", ModBlocks.R_LOGO.get(), "r_logo_directional", ModBlocks.R_LOGO_DIRECTIONAL.get());
        convertPair(output, "renfe_logo", ModBlocks.RENFE_LOGO.get(), "renfe_logo_directional", ModBlocks.RENFE_LOGO_DIRECTIONAL.get());
        convertPair(output, "purple_side", ModBlocks.PURPLE_SIDE.get(), "purple_side_directional", ModBlocks.PURPLE_SIDE_DIRECTIONAL.get());
        convertPair(output, "cercanias_logo", ModBlocks.CERCANIAS_LOGO.get(), "cercanias_logo_directional", ModBlocks.CERCANIAS_LOGO_DIRECTIONAL.get());

        // Cercanias logo: tinte rojo (T) en el centro rodeado en cruz por smooth quartz (X).
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CERCANIAS_LOGO.get(), 4)
                .pattern(" X ")
                .pattern("XTX")
                .pattern(" X ")
                .define('X', Blocks.SMOOTH_QUARTZ)
                .define('T', Items.RED_DYE)
                .unlockedBy("has_smooth_quartz", has(Blocks.SMOOTH_QUARTZ))
                .save(output);

        // Aluminio rojo: fila de piedra arriba y abajo, y en medio tinte rojo - hierro - tinte rojo -> 12.
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RED_CERCANIAS_ALUMINIUM.get(), 12)
                .pattern("SSS")
                .pattern("RIR")
                .pattern("SSS")
                .define('S', Blocks.STONE)
                .define('R', Items.RED_DYE)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_red_dye", has(Items.RED_DYE))
                .save(output);

        // Cercanias logo INVERTIDO (logo blanco sobre rojo): como la R de Rodalies (terracota naranja en cruz
        // con cuarzo al centro), pero con aluminio rojo -> 4.
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CERCANIAS_LOGO_INVERTED.get(), 4)
                .pattern(" R ")
                .pattern("RQR")
                .pattern(" R ")
                .define('R', ModBlocks.RED_CERCANIAS_ALUMINIUM.get())
                .define('Q', Items.QUARTZ)
                .unlockedBy("has_red_cercanias_aluminium", has(ModBlocks.RED_CERCANIAS_ALUMINIUM.get()))
                .save(output);

        // Cuarzo con linea roja: tinte rojo sobre bloque de cuarzo (como la franja naranja+morada, con un tinte).
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RED_STRIPE.get(), 1)
                .pattern("R")
                .pattern("Q")
                .define('R', Items.RED_DYE)
                .define('Q', Blocks.QUARTZ_BLOCK)
                .unlockedBy("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
                .save(output);

        // Cuarzo con lineas roja y lila: columna roja-morada-cuarzo (la misma que la naranja+morada).
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RED_PURPLE_STRIPES.get(), 1)
                .pattern("R")
                .pattern("P")
                .pattern("Q")
                .define('R', Items.RED_DYE)
                .define('P', Items.PURPLE_DYE)
                .define('Q', Blocks.QUARTZ_BLOCK)
                .unlockedBy("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
                .save(output);

        // Conversion 1:1 de los bloques nuevos de caras iguales <-> su variante de una cara.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CERCANIAS_LOGO_INVERTED_DIRECTIONAL.get(), 1)
                .requires(ModBlocks.CERCANIAS_LOGO_INVERTED.get())
                .unlockedBy("has_cercanias_logo_inverted", has(ModBlocks.CERCANIAS_LOGO_INVERTED.get()))
                .save(output, ResourceLocation.fromNamespaceAndPath("rodalies", "cercanias_logo_inverted_directional_from_cercanias_logo_inverted"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CERCANIAS_LOGO_INVERTED.get(), 1)
                .requires(ModBlocks.CERCANIAS_LOGO_INVERTED_DIRECTIONAL.get())
                .unlockedBy("has_cercanias_logo_inverted_directional", has(ModBlocks.CERCANIAS_LOGO_INVERTED_DIRECTIONAL.get()))
                .save(output, ResourceLocation.fromNamespaceAndPath("rodalies", "cercanias_logo_inverted_from_cercanias_logo_inverted_directional"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RED_STRIPE_DIRECTIONAL.get(), 1)
                .requires(ModBlocks.RED_STRIPE.get())
                .unlockedBy("has_red_stripe", has(ModBlocks.RED_STRIPE.get()))
                .save(output, ResourceLocation.fromNamespaceAndPath("rodalies", "red_stripe_directional_from_red_stripe"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RED_STRIPE.get(), 1)
                .requires(ModBlocks.RED_STRIPE_DIRECTIONAL.get())
                .unlockedBy("has_red_stripe_directional", has(ModBlocks.RED_STRIPE_DIRECTIONAL.get()))
                .save(output, ResourceLocation.fromNamespaceAndPath("rodalies", "red_stripe_from_red_stripe_directional"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RED_PURPLE_STRIPES_DIRECTIONAL.get(), 1)
                .requires(ModBlocks.RED_PURPLE_STRIPES.get())
                .unlockedBy("has_red_purple_stripes", has(ModBlocks.RED_PURPLE_STRIPES.get()))
                .save(output, ResourceLocation.fromNamespaceAndPath("rodalies", "red_purple_stripes_directional_from_red_purple_stripes"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RED_PURPLE_STRIPES.get(), 1)
                .requires(ModBlocks.RED_PURPLE_STRIPES_DIRECTIONAL.get())
                .unlockedBy("has_red_purple_stripes_directional", has(ModBlocks.RED_PURPLE_STRIPES_DIRECTIONAL.get()))
                .save(output, ResourceLocation.fromNamespaceAndPath("rodalies", "red_purple_stripes_from_red_purple_stripes_directional"));

        // Caja de conexiones: tapa de hormigon amarillo (Y), redstone dentro (R) y cuerpo de hormigon gris
        // claro (C) sobre una reja de hierro (I, el poste) -> 8 cajas.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.JUNCTION_BOX.get(), 8)
                .pattern(" Y ")
                .pattern("CRC")
                .pattern(" I ")
                .define('Y', Blocks.YELLOW_CONCRETE)
                .define('R', Items.REDSTONE)
                .define('C', Blocks.LIGHT_GRAY_CONCRETE)
                .define('I', Items.IRON_BARS)
                .unlockedBy("has_light_gray_concrete", has(Blocks.LIGHT_GRAY_CONCRETE))
                .save(output);

        // Caja de conexiones con cables: caja + lingote de cobre (los cables), en cualquier disposicion.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, ModBlocks.JUNCTION_BOX_WITH_CABLES.get(), 1)
                .requires(ModBlocks.JUNCTION_BOX.get())
                .requires(Items.COPPER_INGOT)
                .unlockedBy("has_junction_box", has(ModBlocks.JUNCTION_BOX.get()))
                .save(output);

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
                .save(output);

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
                .save(output);

        // Conversion 1:1 franja de 4 caras <-> franja de 1 cara (sin gastar materiales).
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ORANGE_PURPLE_LINES_DIRECTIONAL.get(), 1)
                .requires(ModBlocks.ORANGE_PURPLE_LINES.get())
                .unlockedBy("has_orange_purple_lines", has(ModBlocks.ORANGE_PURPLE_LINES.get()))
                .save(output, "rodalies:orange_purple_lines_directional_from_orange_purple_lines");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ORANGE_PURPLE_LINES.get(), 1)
                .requires(ModBlocks.ORANGE_PURPLE_LINES_DIRECTIONAL.get())
                .unlockedBy("has_orange_purple_lines_directional", has(ModBlocks.ORANGE_PURPLE_LINES_DIRECTIONAL.get()))
                .save(output, "rodalies:orange_purple_lines_from_orange_purple_lines_directional");

        // Conversion 1:1 de la variante "bottom" (4 caras <-> 1 cara).
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ORANGE_PURPLE_LINES_BOTTOM_DIRECTIONAL.get(), 1)
                .requires(ModBlocks.ORANGE_PURPLE_LINES_BOTTOM.get())
                .unlockedBy("has_orange_purple_lines_bottom", has(ModBlocks.ORANGE_PURPLE_LINES_BOTTOM.get()))
                .save(output, "rodalies:orange_purple_lines_bottom_directional_from_orange_purple_lines_bottom");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ORANGE_PURPLE_LINES_BOTTOM.get(), 1)
                .requires(ModBlocks.ORANGE_PURPLE_LINES_BOTTOM_DIRECTIONAL.get())
                .unlockedBy("has_orange_purple_lines_bottom_directional", has(ModBlocks.ORANGE_PURPLE_LINES_BOTTOM_DIRECTIONAL.get()))
                .save(output, "rodalies:orange_purple_lines_bottom_from_orange_purple_lines_bottom_directional");

        // Franja doble sobre gray concrete: hormigon gris + franja "bottom" (en cualquier disposicion).
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.DOUBLE_LINES_GRAY_CONCRETE.get(), 1)
                .requires(Blocks.GRAY_CONCRETE)
                .requires(ModBlocks.ORANGE_PURPLE_LINES_BOTTOM.get())
                .unlockedBy("has_orange_purple_lines_bottom", has(ModBlocks.ORANGE_PURPLE_LINES_BOTTOM.get()))
                .save(output);

        // Luz de tren: 4 hierros en cruz con una antorcha al centro -> 4 luces.
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.TRAIN_LIGHT.get(), 4)
                .pattern(" I ")
                .pattern("ITI")
                .pattern(" I ")
                .define('I', Items.IRON_INGOT)
                .define('T', Items.TORCH)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        // Luz frontal de tren: hormigon gris + luz de tren (en cualquier disposicion).
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.FRONT_LIGHT.get(), 1)
                .requires(Blocks.GRAY_CONCRETE)
                .requires(ModBlocks.TRAIN_LIGHT.get())
                .unlockedBy("has_train_light", has(ModBlocks.TRAIN_LIGHT.get()))
                .save(output);

        // Ventana de tren: 1 bloque de cuarzo + 1 cristal, en cualquier disposicion (shapeless).
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.TRAIN_WINDOW.get(), 1)
                .requires(Blocks.QUARTZ_BLOCK)
                .requires(Blocks.GLASS)
                .unlockedBy("has_glass", has(Blocks.GLASS))
                .save(output);

        // Bloque de velocidad maxima: cuarzo normal (Q) con un tinte negro (D) encima (el rombo).
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.TRAIN_MAX_SPEED.get(), 1)
                .pattern("D")
                .pattern("Q")
                .define('D', Items.BLACK_DYE)
                .define('Q', Blocks.QUARTZ_BLOCK)
                .unlockedBy("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
                .save(output);

        // Senal de hectometro: 3 dioritas en columna.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.HECTOMETER_SIGNAL.get(), 4)
                .pattern("D")
                .pattern("D")
                .pattern("D")
                .define('D', Blocks.DIORITE)
                .unlockedBy("has_diorite", has(Blocks.DIORITE))
                .save(output);

        // Parabrisas 447: lingote de hierro (L) arriba, fila de 3 cristales tintados negros (C).
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.PARABRISAS_447.get(), 1)
                .pattern(" L ")
                .pattern("CCC")
                .define('L', Items.IRON_INGOT)
                .define('C', Blocks.BLACK_STAINED_GLASS)
                .unlockedBy("has_black_stained_glass", has(Blocks.BLACK_STAINED_GLASS))
                .save(output);

        // Cartel de estacion de Rodalies (con tinte naranja de marca) -> 5.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.STATION_SIGNAL.get(), 5)
                .pattern("CCC")
                .pattern("OL ")
                .pattern(" L ")
                .define('C', Blocks.GRAY_CONCRETE)
                .define('L', Items.IRON_INGOT)
                .define('O', Items.ORANGE_DYE)
                .unlockedBy("has_gray_concrete", has(Blocks.GRAY_CONCRETE))
                .save(output);

        // Cartel de estacion generico (sin logo) -> 5.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.STATION_SIGNAL_PLAIN.get(), 5)
                .pattern("CCC")
                .pattern(" L ")
                .pattern(" L ")
                .define('C', Blocks.GRAY_CONCRETE)
                .define('L', Items.IRON_INGOT)
                .unlockedBy("has_gray_concrete", has(Blocks.GRAY_CONCRETE))
                .save(output);

        // Cartel de numero de via: hormigon gris (C) sobre 2 hierros (L) -> 2 carteles.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.PLATFORM_NUMBER_SIGNAL.get(), 2)
                .pattern("C")
                .pattern("L")
                .pattern("L")
                .define('C', Blocks.GRAY_CONCRETE)
                .define('L', Items.IRON_INGOT)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        // Puerta corredera de tren: terracota naranja (T) arriba/abajo, cristal negro (G) en medio -> 3.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.SLIDING_TRAIN_DOOR.get(), 3)
                .pattern("TT")
                .pattern("GG")
                .pattern("TT")
                .define('T', Blocks.ORANGE_TERRACOTTA)
                .define('G', Blocks.BLACK_STAINED_GLASS)
                .unlockedBy("has_orange_terracotta", has(Blocks.ORANGE_TERRACOTTA))
                .save(output);

        // Intercambio de sentido de apertura (adelante <-> atras) sin gastar materiales.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, ModBlocks.SLIDING_TRAIN_DOOR_BACK.get(), 1)
                .requires(ModBlocks.SLIDING_TRAIN_DOOR.get())
                .unlockedBy("has_sliding_train_door", has(ModBlocks.SLIDING_TRAIN_DOOR.get()))
                .save(output, "rodalies:sliding_train_door_back_from_front");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, ModBlocks.SLIDING_TRAIN_DOOR.get(), 1)
                .requires(ModBlocks.SLIDING_TRAIN_DOOR_BACK.get())
                .unlockedBy("has_sliding_train_door_back", has(ModBlocks.SLIDING_TRAIN_DOOR_BACK.get()))
                .save(output, "rodalies:sliding_train_door_front_from_back");

        // Puerta corredera BLANCA: 2 columnas iguales de cuarzo (Q) arriba y abajo con cristal (G) en
        // medio -> 3 puertas. Misma forma que una puerta vanilla pero de cuarzo+cristal.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.WHITE_SLIDING_TRAIN_DOOR.get(), 3)
                .pattern("QQ")
                .pattern("GG")
                .pattern("QQ")
                .define('Q', Blocks.QUARTZ_BLOCK)
                .define('G', Blocks.GLASS)
                .unlockedBy("has_quartz_block", has(Blocks.QUARTZ_BLOCK))
                .save(output);

        // Intercambio de sentido de apertura de la puerta blanca (adelante <-> atras), sin gastar
        // materiales, igual que la naranja. IDs explicitos para no chocar con la receta principal.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, ModBlocks.WHITE_SLIDING_TRAIN_DOOR_BACK.get(), 1)
                .requires(ModBlocks.WHITE_SLIDING_TRAIN_DOOR.get())
                .unlockedBy("has_white_sliding_train_door", has(ModBlocks.WHITE_SLIDING_TRAIN_DOOR.get()))
                .save(output, "rodalies:white_sliding_train_door_back_from_front");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, ModBlocks.WHITE_SLIDING_TRAIN_DOOR.get(), 1)
                .requires(ModBlocks.WHITE_SLIDING_TRAIN_DOOR_BACK.get())
                .unlockedBy("has_white_sliding_train_door_back", has(ModBlocks.WHITE_SLIDING_TRAIN_DOOR_BACK.get()))
                .save(output, "rodalies:white_sliding_train_door_front_from_back");

        // Base: una reja de hierro (I) encima de hormigon gris o su polvo (C), una sobre otra.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.BASE.get(), 1)
                .pattern("I")
                .pattern("C")
                .define('I', Items.IRON_BARS)
                .define('C', Ingredient.of(Blocks.GRAY_CONCRETE, Blocks.GRAY_CONCRETE_POWDER))
                .unlockedBy("has_iron_bars", has(Items.IRON_BARS))
                .save(output);

        // --- Senales ferroviarias: base(s) en la columna central + tintes en la fila de arriba. ---
        signRecipes(output, ModBlocks.SPEED_LIMIT.get(), ModBlocks.SPEED_LIMIT_SHORT.get(),
                Items.WHITE_DYE, Items.WHITE_DYE, Items.WHITE_DYE);
        signRecipes(output, ModBlocks.LTV.get(), ModBlocks.LTV_SHORT.get(),
                Items.YELLOW_DYE, Items.YELLOW_DYE, Items.YELLOW_DYE);
        signRecipes(output, ModBlocks.NORMAL_SIGNAL.get(), ModBlocks.NORMAL_SIGNAL_SHORT.get(),
                null, Items.WHITE_DYE, null);
        signRecipes(output, ModBlocks.LTV_END.get(), ModBlocks.LTV_END_SHORT.get(),
                null, Items.YELLOW_DYE, null);
        signRecipes(output, ModBlocks.APEADERO_SIGNAL.get(), ModBlocks.APEADERO_SIGNAL_SHORT.get(),
                Items.WHITE_DYE, Items.GRAY_DYE, Items.WHITE_DYE);
        signRecipes(output, ModBlocks.SILBATO_SIGNAL.get(), ModBlocks.SILBATO_SIGNAL_SHORT.get(),
                Items.GRAY_DYE, Items.WHITE_DYE, Items.GRAY_DYE);
        signRecipes(output, ModBlocks.STOP_SIGNAL.get(), ModBlocks.STOP_SIGNAL_SHORT.get(),
                Items.WHITE_DYE, Items.RED_DYE, Items.WHITE_DYE);

    }

    /** Conversion 1:1 (ambos sentidos, sin gastar materiales) entre un bloque y su variante de una cara. */
    private void convertPair(RecipeOutput output, String baseName, ItemLike base, String dirName, ItemLike dir) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, dir, 1)
                .requires(base)
                .unlockedBy("has_" + baseName, has(base))
                .save(output, "rodalies:" + dirName + "_from_" + baseName);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, base, 1)
                .requires(dir)
                .unlockedBy("has_" + dirName, has(dir))
                .save(output, "rodalies:" + baseName + "_from_" + dirName);
    }

    private void signRecipes(RecipeOutput output, ItemLike full, ItemLike shortSign,
                             @Nullable Item left, Item center, @Nullable Item right) {
        String top = "" + (left != null ? 'L' : ' ') + 'C' + (right != null ? 'R' : ' ');
        buildSign(output, full, top, true, left, center, right);
        buildSign(output, shortSign, top, false, left, center, right);
    }

    private void buildSign(RecipeOutput output, ItemLike out, String top, boolean normal,
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
        b.unlockedBy("has_base", has(base)).save(output);
    }
}
