package rodalies;

import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registro de bloques, items y pestanas creativas (port a NeoForge 1.21.1).
 *
 * En 1.21 las Properties de bloque/item necesitan un id de registro; los helpers de NeoForge
 * {@link DeferredRegister.Blocks#registerBlock} y {@link DeferredRegister.Items#registerSimpleBlockItem}
 * lo asignan automaticamente, asi que se usan en vez del patron antiguo de ForgeRegistries.
 *
 * Nota: las subclases vacias identicas de los bloques antiguos (Rgrande/RenfeLogo/PurpleSide) se han
 * colapsado en {@code Block::new} (los modelos se referencian por el id de registro, no por la clase).
 */
public class ModBlocks {

    // --- Registros ---

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("rodalies");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("rodalies");
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "rodalies");

    // Dureza/herramienta = las de su bloque "padre" (con el que se craftea): ver los ofFullCopy de abajo.
    // Se pican con pico (tag minecraft mineable/pickaxe).

    // --- Bloques antiguos (logos / franja morada) ---

    public static final DeferredBlock<Block> R_LOGO =
            BLOCKS.registerBlock("r_logo", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.ORANGE_TERRACOTTA));

    public static final DeferredBlock<Block> RENFE_LOGO =
            BLOCKS.registerBlock("renfe_logo", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK));

    public static final DeferredBlock<Block> PURPLE_SIDE =
            BLOCKS.registerBlock("purple_side", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK));

    // Cercanias logo: mismas propiedades que el smooth quartz (con el que se craftea).
    public static final DeferredBlock<Block> CERCANIAS_LOGO =
            BLOCKS.registerBlock("cercanias_logo", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_QUARTZ));

    // Aluminio rojo de Cercanias: cubo rojo liso. Se craftea con piedra (+ hierro y tinte rojo) -> props de la
    // piedra.
    public static final DeferredBlock<Block> RED_CERCANIAS_ALUMINIUM =
            BLOCKS.registerBlock("red_cercanias_aluminium", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE));

    // Cercanias logo de colores invertidos: logo blanco sobre fondo rojo; top/bottom aluminio rojo.
    // Se craftea con aluminio rojo -> mismas props (las de la piedra).
    public static final DeferredBlock<Block> CERCANIAS_LOGO_INVERTED =
            BLOCKS.registerBlock("cercanias_logo_inverted", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE));

    // Logo invertido "de una cara": el logo solo en la cara frontal, el resto aluminio rojo.
    public static final DeferredBlock<Block> CERCANIAS_LOGO_INVERTED_DIRECTIONAL =
            BLOCKS.registerBlock("cercanias_logo_inverted_directional", DirectionalLogoBlock::new,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.STONE));

    // --- Variantes "de una cara" (logo/franja solo en la cara frontal). Ver DirectionalLogoBlock. ---
    public static final DeferredBlock<Block> R_LOGO_DIRECTIONAL =
            BLOCKS.registerBlock("r_logo_directional", DirectionalLogoBlock::new,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.ORANGE_TERRACOTTA));

    public static final DeferredBlock<Block> RENFE_LOGO_DIRECTIONAL =
            BLOCKS.registerBlock("renfe_logo_directional", DirectionalLogoBlock::new,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK));

    public static final DeferredBlock<Block> PURPLE_SIDE_DIRECTIONAL =
            BLOCKS.registerBlock("purple_side_directional", DirectionalLogoBlock::new,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK));

    public static final DeferredBlock<Block> CERCANIAS_LOGO_DIRECTIONAL =
            BLOCKS.registerBlock("cercanias_logo_directional", DirectionalLogoBlock::new,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_QUARTZ));

    // Franja naranja+morada sobre cuarzo (cubeBottomTop via datagen).
    public static final DeferredBlock<Block> ORANGE_PURPLE_LINES =
            BLOCKS.registerBlock("orange_purple_lines", Block::new,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK));

    public static final DeferredBlock<Block> ORANGE_PURPLE_LINES_DIRECTIONAL =
            BLOCKS.registerBlock("orange_purple_lines_directional", DirectionalLogoBlock::new,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK));

    // Parabrisas del 447: multiblock 3x1x1 con cristal tintado (render translucido en ClientSetup).
    // Variante "bottom": misma idea que orange_purple_lines pero con la franja pegada abajo (naranja
    // encima de morada, en el tercio inferior). Caras laterales con textura propia; top/bottom smooth quartz.
    public static final DeferredBlock<Block> ORANGE_PURPLE_LINES_BOTTOM =
            BLOCKS.registerBlock("orange_purple_lines_bottom", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK));

    // "Bottom" de una cara: la franja inferior SOLO en la cara frontal (DirectionalLogoBlock).
    public static final DeferredBlock<Block> ORANGE_PURPLE_LINES_BOTTOM_DIRECTIONAL =
            BLOCKS.registerBlock("orange_purple_lines_bottom_directional", DirectionalLogoBlock::new,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK));

    // Franja doble sobre hormigon gris: caras laterales con la franja, cara superior gray concrete y
    // cara inferior smooth quartz. Solo existe en version de caras iguales (sin variante de una cara).
    public static final DeferredBlock<Block> DOUBLE_LINES_GRAY_CONCRETE =
            BLOCKS.registerBlock("double_lines_gray_concrete", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.GRAY_CONCRETE));

    // Cuarzo con linea roja: como orange_purple_lines pero con una sola franja roja (laterales); top/bottom
    // smooth quartz.
    public static final DeferredBlock<Block> RED_STRIPE =
            BLOCKS.registerBlock("red_stripe", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK));

    // Cuarzo con lineas roja y lila (laterales); top/bottom smooth quartz.
    public static final DeferredBlock<Block> RED_PURPLE_STRIPES =
            BLOCKS.registerBlock("red_purple_stripes", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK));

    // Variantes "de una cara" de las franjas roja y roja+lila (franja solo en la cara frontal).
    public static final DeferredBlock<Block> RED_STRIPE_DIRECTIONAL =
            BLOCKS.registerBlock("red_stripe_directional", DirectionalLogoBlock::new,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK));

    public static final DeferredBlock<Block> RED_PURPLE_STRIPES_DIRECTIONAL =
            BLOCKS.registerBlock("red_purple_stripes_directional", DirectionalLogoBlock::new,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK));

    // Luz de tren: bloque luminoso (luz 15) con las mismas caracteristicas que la sea lantern (blando,
    // sonido de cristal). Todas las caras iguales. Se craftea 4 con hierro en cruz + antorcha al centro.
    public static final DeferredBlock<Block> TRAIN_LIGHT =
            BLOCKS.registerBlock("train_light", Block::new,
                    BlockBehaviour.Properties.of().strength(0.3f).sound(SoundType.GLASS).lightLevel(s -> 15));

    // Luz frontal de tren: bloque tipo hormigon gris que tambien ilumina (luz 15). Caras laterales con
    // la textura de faros frontales; top/bottom gray concrete. NO confundir con la luz de tren.
    public static final DeferredBlock<Block> FRONT_LIGHT =
            BLOCKS.registerBlock("front_light", Block::new,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.GRAY_CONCRETE).lightLevel(s -> 15));

    // Ventana de tren: bloque de cristal (todas las propiedades del cristal vanilla) con las caras
    // laterales de train_window (cristal tintado semitransparente) y top/bottom smooth quartz. Render
    // translucido (render_type en el modelo, ver datagen) + noOcclusion para que no recorte lo de detras.
    public static final DeferredBlock<Block> TRAIN_WINDOW =
            BLOCKS.registerBlock("train_window", Block::new,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).noOcclusion());

    public static final DeferredBlock<Block> PARABRISAS_447 =
            BLOCKS.registerBlock("parabrisas_447", Parabrisas447Block::new,
                    BlockBehaviour.Properties.of().strength(0.3f).sound(SoundType.GLASS).noOcclusion());

    // Cartel de estacion con logo de Rodalies -> texto desplazado.
    public static final DeferredBlock<Block> STATION_SIGNAL =
            BLOCKS.registerBlock("station_signal", p -> new StationSignalBlock(p, true),
                    BlockBehaviour.Properties.of().strength(1.5f, 10.0f).sound(SoundType.METAL).noOcclusion());

    // Cartel de estacion generico (sin logo) -> texto centrado.
    public static final DeferredBlock<Block> STATION_SIGNAL_PLAIN =
            BLOCKS.registerBlock("station_signal_plain", p -> new StationSignalBlock(p, false),
                    BlockBehaviour.Properties.of().strength(1.5f, 10.0f).sound(SoundType.METAL).noOcclusion());

    // Propiedades comunes de las senales ferroviarias (dureza baja, resistencia alta, metal, noOcclusion).
    private static BlockBehaviour.Properties signProps() {
        return BlockBehaviour.Properties.of().strength(1.5f, 10.0f).sound(SoundType.METAL).noOcclusion();
    }

    public static final DeferredBlock<Block> BASE =
            BLOCKS.registerBlock("base", BaseBlock::new, signProps());

    public static final DeferredBlock<Block> PLATFORM_NUMBER_SIGNAL =
            BLOCKS.registerBlock("platform_number_signal", PlatformNumberSignBlock::new, signProps());

    public static final DeferredBlock<Block> SPEED_LIMIT =
            BLOCKS.registerBlock("speed_limit", SpeedLimitBlock::new, signProps());

    public static final DeferredBlock<Block> LTV =
            BLOCKS.registerBlock("ltv", p -> new RailSignBlock(p, RailSignType.LTV), signProps());

    public static final DeferredBlock<Block> LTV_END =
            BLOCKS.registerBlock("ltv_end", p -> new RailSignBlock(p, RailSignType.LTV_END), signProps());

    public static final DeferredBlock<Block> NORMAL_SIGNAL =
            BLOCKS.registerBlock("normal_signal", p -> new RailSignBlock(p, RailSignType.NORMAL), signProps());

    // --- Versiones SHORT (poste una celda mas corto) ---

    public static final DeferredBlock<Block> SPEED_LIMIT_SHORT =
            BLOCKS.registerBlock("speed_limit_short", p -> new RailSignBlock(p, RailSignType.SPEED_LIMIT, true), signProps());

    public static final DeferredBlock<Block> LTV_SHORT =
            BLOCKS.registerBlock("ltv_short", p -> new RailSignBlock(p, RailSignType.LTV, true), signProps());

    public static final DeferredBlock<Block> LTV_END_SHORT =
            BLOCKS.registerBlock("ltv_end_short", p -> new RailSignBlock(p, RailSignType.LTV_END, true), signProps());

    public static final DeferredBlock<Block> NORMAL_SIGNAL_SHORT =
            BLOCKS.registerBlock("normal_signal_short", p -> new RailSignBlock(p, RailSignType.NORMAL, true), signProps());

    public static final DeferredBlock<Block> APEADERO_SIGNAL =
            BLOCKS.registerBlock("apeadero_signal", p -> new RailSignBlock(p, RailSignType.LTV_END), signProps());

    public static final DeferredBlock<Block> APEADERO_SIGNAL_SHORT =
            BLOCKS.registerBlock("apeadero_signal_short", p -> new RailSignBlock(p, RailSignType.LTV_END, true), signProps());

    public static final DeferredBlock<Block> SILBATO_SIGNAL =
            BLOCKS.registerBlock("silbato_signal", p -> new RailSignBlock(p, RailSignType.LTV_END), signProps());

    public static final DeferredBlock<Block> SILBATO_SIGNAL_SHORT =
            BLOCKS.registerBlock("silbato_signal_short", p -> new RailSignBlock(p, RailSignType.LTV_END, true), signProps());

    public static final DeferredBlock<Block> STOP_SIGNAL =
            BLOCKS.registerBlock("stop_signal", p -> new RailSignBlock(p, RailSignType.LTV_END), signProps());

    public static final DeferredBlock<Block> STOP_SIGNAL_SHORT =
            BLOCKS.registerBlock("stop_signal_short", p -> new RailSignBlock(p, RailSignType.LTV_END, true), signProps());

    // --- Modo PARED de las senales simples (sin poste, 1 celda). No tienen item propio: los coloca el
    // item normal/short (RailSignItem) al hacer clic en la cara de un bloque mirandola de frente, como el
    // cartel de estacion. Hay uno por item (normal y short) para que al romperlo devuelva el mismo item.
    // Se pegan al bloque de detras y, si es un poste fino (p.ej. de Pantographs & Wires), se meten hasta
    // tocarlo (RailSignWallBlock). La forma es la de la senal mirando al norte con la trasera en z=16. ---

    private static DeferredBlock<Block> wallSign(String name, RailSignType type, double x1, double y1, double z1,
                                                 double x2, double y2, double z2) {
        return BLOCKS.registerBlock(name, p -> new RailSignWallBlock(p, type, Block.box(x1, y1, z1, x2, y2, z2)),
                signProps());
    }

    public static final DeferredBlock<Block> SPEED_LIMIT_WALL =
            wallSign("speed_limit_wall", RailSignType.SPEED_LIMIT, 0, 0.5, 13.9, 16, 16, 16);
    public static final DeferredBlock<Block> SPEED_LIMIT_SHORT_WALL =
            wallSign("speed_limit_short_wall", RailSignType.SPEED_LIMIT, 0, 0.5, 13.9, 16, 16, 16);
    public static final DeferredBlock<Block> LTV_WALL =
            wallSign("ltv_wall", RailSignType.LTV, 0, 0, 13.9, 16, 16, 16);
    public static final DeferredBlock<Block> LTV_SHORT_WALL =
            wallSign("ltv_short_wall", RailSignType.LTV, 0, 0, 13.9, 16, 16, 16);
    public static final DeferredBlock<Block> LTV_END_WALL =
            wallSign("ltv_end_wall", RailSignType.LTV_END, 1.5, 2, 14, 14.5, 15, 16);
    public static final DeferredBlock<Block> LTV_END_SHORT_WALL =
            wallSign("ltv_end_short_wall", RailSignType.LTV_END, 1.5, 2, 14, 14.5, 15, 16);
    public static final DeferredBlock<Block> NORMAL_SIGNAL_WALL =
            wallSign("normal_signal_wall", RailSignType.NORMAL, 1.5, 3, 14, 14.5, 16, 16);
    public static final DeferredBlock<Block> NORMAL_SIGNAL_SHORT_WALL =
            wallSign("normal_signal_short_wall", RailSignType.NORMAL, 1.5, 3, 14, 14.5, 16, 16);
    public static final DeferredBlock<Block> APEADERO_SIGNAL_WALL =
            wallSign("apeadero_signal_wall", RailSignType.LTV_END, 2, 0, 15, 14, 16, 16);
    public static final DeferredBlock<Block> APEADERO_SIGNAL_SHORT_WALL =
            wallSign("apeadero_signal_short_wall", RailSignType.LTV_END, 2, 0, 15, 14, 16, 16);
    public static final DeferredBlock<Block> SILBATO_SIGNAL_WALL =
            wallSign("silbato_signal_wall", RailSignType.LTV_END, 2, 0, 15, 14, 16, 16);
    public static final DeferredBlock<Block> SILBATO_SIGNAL_SHORT_WALL =
            wallSign("silbato_signal_short_wall", RailSignType.LTV_END, 2, 0, 15, 14, 16, 16);
    public static final DeferredBlock<Block> STOP_SIGNAL_WALL =
            wallSign("stop_signal_wall", RailSignType.LTV_END, 0, 0, 15, 16, 16, 16);
    public static final DeferredBlock<Block> STOP_SIGNAL_SHORT_WALL =
            wallSign("stop_signal_short_wall", RailSignType.LTV_END, 0, 0, 15, 16, 16, 16);

    // Cubo de cuarzo con la senal de velocidad maxima (rombo) y texto editable (<=4) en la cara frontal.
    public static final DeferredBlock<Block> TRAIN_MAX_SPEED =
            BLOCKS.registerBlock("train_max_speed", TrainMaxSpeedBlock::new,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK));

    // Senal de hectometro: multiblock vertical de 2 celdas; texto editable en 2 filas.
    public static final DeferredBlock<Block> HECTOMETER_SIGNAL =
            BLOCKS.registerBlock("hectometer_signal", HectometerSignBlock::new, signProps());

    // Puerta corredera de tren (multiblock vertical de 2 celdas). FRONT abre adelante, BACK atras.
    // Dureza de la terracota naranja (su material); sonido de metal.
    private static BlockBehaviour.Properties doorProps() {
        return BlockBehaviour.Properties.of().strength(1.25f, 4.2f).sound(SoundType.METAL).noOcclusion();
    }

    // Puerta blanca: dureza del bloque de cuarzo.
    private static BlockBehaviour.Properties whiteDoorProps() {
        return BlockBehaviour.Properties.of().strength(0.8f).sound(SoundType.METAL).noOcclusion();
    }

    public static final DeferredBlock<Block> SLIDING_TRAIN_DOOR =
            BLOCKS.registerBlock("sliding_train_door", p -> new SlidingTrainDoorBlock(p, true), doorProps());

    public static final DeferredBlock<Block> SLIDING_TRAIN_DOOR_BACK =
            BLOCKS.registerBlock("sliding_train_door_back", p -> new SlidingTrainDoorBlock(p, false), doorProps());

    // Puerta corredera de tren BLANCA: retexturizacion de la anterior (mismo tipo de puerta, animacion,
    // colision y compat con Create). Unica diferencia: ambas hojas usan la textura white_train_door
    // (izquierda = derecha). Igual que la naranja, tiene variante FRONT y BACK intercambiables.
    private static final ResourceLocation WHITE_DOOR_TEX =
            ResourceLocation.fromNamespaceAndPath("rodalies", "block/white_train_door");
    // Techo (cara up) de la puerta blanca: cuarzo (la naranja usa gray concrete por defecto).
    private static final ResourceLocation WHITE_DOOR_TOP =
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/quartz_block_top");

    public static final DeferredBlock<Block> WHITE_SLIDING_TRAIN_DOOR =
            BLOCKS.registerBlock("white_sliding_train_door",
                    p -> new SlidingTrainDoorBlock(p, true, WHITE_DOOR_TEX, WHITE_DOOR_TEX, WHITE_DOOR_TOP), whiteDoorProps());

    public static final DeferredBlock<Block> WHITE_SLIDING_TRAIN_DOOR_BACK =
            BLOCKS.registerBlock("white_sliding_train_door_back",
                    p -> new SlidingTrainDoorBlock(p, false, WHITE_DOOR_TEX, WHITE_DOOR_TEX, WHITE_DOOR_TOP), whiteDoorProps());

    // Cajas de conexiones (modelo 3D: poste + caja). Propiedades del hormigon gris claro (su material
    // principal). La normal se desplaza dentro de la celda con un menu (JunctionBoxBlock); la de cables
    // solo se orienta al colocarla (JunctionBoxCablesBlock).
    public static final DeferredBlock<Block> JUNCTION_BOX =
            BLOCKS.registerBlock("junction_box", JunctionBoxBlock::new,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHT_GRAY_CONCRETE).noOcclusion());

    public static final DeferredBlock<Block> JUNCTION_BOX_WITH_CABLES =
            BLOCKS.registerBlock("junction_box_with_cables", JunctionBoxCablesBlock::new,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHT_GRAY_CONCRETE).noOcclusion());

    // --- Items (BlockItem con el mismo id que su bloque) ---

    // Item de senal simple: coloca la senal con poste, o su version de pared (sin item propio) si se hace
    // clic en la cara de un bloque mirandola de frente (ver RailSignItem).
    private static DeferredItem<BlockItem> signItem(String name, DeferredBlock<Block> sign, DeferredBlock<Block> wall) {
        return ITEMS.register(name, () -> new RailSignItem(sign.get(), wall.get(), new Item.Properties()));
    }


    public static final DeferredItem<BlockItem> R_LOGO_ITEM = ITEMS.registerSimpleBlockItem(R_LOGO);
    public static final DeferredItem<BlockItem> RENFE_LOGO_ITEM = ITEMS.registerSimpleBlockItem(RENFE_LOGO);
    public static final DeferredItem<BlockItem> PURPLE_SIDE_ITEM = ITEMS.registerSimpleBlockItem(PURPLE_SIDE);
    public static final DeferredItem<BlockItem> CERCANIAS_LOGO_ITEM = ITEMS.registerSimpleBlockItem(CERCANIAS_LOGO);
    public static final DeferredItem<BlockItem> RED_CERCANIAS_ALUMINIUM_ITEM = ITEMS.registerSimpleBlockItem(RED_CERCANIAS_ALUMINIUM);
    public static final DeferredItem<BlockItem> CERCANIAS_LOGO_INVERTED_ITEM = ITEMS.registerSimpleBlockItem(CERCANIAS_LOGO_INVERTED);
    public static final DeferredItem<BlockItem> CERCANIAS_LOGO_INVERTED_DIRECTIONAL_ITEM = ITEMS.registerSimpleBlockItem(CERCANIAS_LOGO_INVERTED_DIRECTIONAL);
    public static final DeferredItem<BlockItem> R_LOGO_DIRECTIONAL_ITEM = ITEMS.registerSimpleBlockItem(R_LOGO_DIRECTIONAL);
    public static final DeferredItem<BlockItem> RENFE_LOGO_DIRECTIONAL_ITEM = ITEMS.registerSimpleBlockItem(RENFE_LOGO_DIRECTIONAL);
    public static final DeferredItem<BlockItem> PURPLE_SIDE_DIRECTIONAL_ITEM = ITEMS.registerSimpleBlockItem(PURPLE_SIDE_DIRECTIONAL);
    public static final DeferredItem<BlockItem> CERCANIAS_LOGO_DIRECTIONAL_ITEM = ITEMS.registerSimpleBlockItem(CERCANIAS_LOGO_DIRECTIONAL);
    public static final DeferredItem<BlockItem> ORANGE_PURPLE_LINES_ITEM = ITEMS.registerSimpleBlockItem(ORANGE_PURPLE_LINES);
    public static final DeferredItem<BlockItem> ORANGE_PURPLE_LINES_DIRECTIONAL_ITEM = ITEMS.registerSimpleBlockItem(ORANGE_PURPLE_LINES_DIRECTIONAL);
    public static final DeferredItem<BlockItem> ORANGE_PURPLE_LINES_BOTTOM_ITEM = ITEMS.registerSimpleBlockItem(ORANGE_PURPLE_LINES_BOTTOM);
    public static final DeferredItem<BlockItem> ORANGE_PURPLE_LINES_BOTTOM_DIRECTIONAL_ITEM = ITEMS.registerSimpleBlockItem(ORANGE_PURPLE_LINES_BOTTOM_DIRECTIONAL);
    public static final DeferredItem<BlockItem> DOUBLE_LINES_GRAY_CONCRETE_ITEM = ITEMS.registerSimpleBlockItem(DOUBLE_LINES_GRAY_CONCRETE);
    public static final DeferredItem<BlockItem> RED_STRIPE_ITEM = ITEMS.registerSimpleBlockItem(RED_STRIPE);
    public static final DeferredItem<BlockItem> RED_PURPLE_STRIPES_ITEM = ITEMS.registerSimpleBlockItem(RED_PURPLE_STRIPES);
    public static final DeferredItem<BlockItem> RED_STRIPE_DIRECTIONAL_ITEM = ITEMS.registerSimpleBlockItem(RED_STRIPE_DIRECTIONAL);
    public static final DeferredItem<BlockItem> RED_PURPLE_STRIPES_DIRECTIONAL_ITEM = ITEMS.registerSimpleBlockItem(RED_PURPLE_STRIPES_DIRECTIONAL);
    public static final DeferredItem<BlockItem> TRAIN_LIGHT_ITEM = ITEMS.registerSimpleBlockItem(TRAIN_LIGHT);
    public static final DeferredItem<BlockItem> FRONT_LIGHT_ITEM = ITEMS.registerSimpleBlockItem(FRONT_LIGHT);
    public static final DeferredItem<BlockItem> TRAIN_WINDOW_ITEM = ITEMS.registerSimpleBlockItem(TRAIN_WINDOW);
    public static final DeferredItem<BlockItem> TRAIN_MAX_SPEED_ITEM = ITEMS.registerSimpleBlockItem(TRAIN_MAX_SPEED);
    public static final DeferredItem<BlockItem> HECTOMETER_SIGNAL_ITEM = ITEMS.registerSimpleBlockItem(HECTOMETER_SIGNAL);
    public static final DeferredItem<BlockItem> PARABRISAS_447_ITEM = ITEMS.registerSimpleBlockItem(PARABRISAS_447);
    public static final DeferredItem<BlockItem> STATION_SIGNAL_ITEM = ITEMS.registerSimpleBlockItem(STATION_SIGNAL);
    public static final DeferredItem<BlockItem> STATION_SIGNAL_PLAIN_ITEM = ITEMS.registerSimpleBlockItem(STATION_SIGNAL_PLAIN);
    public static final DeferredItem<BlockItem> PLATFORM_NUMBER_SIGNAL_ITEM = ITEMS.registerSimpleBlockItem(PLATFORM_NUMBER_SIGNAL);
    public static final DeferredItem<BlockItem> SPEED_LIMIT_ITEM = signItem("speed_limit", SPEED_LIMIT, SPEED_LIMIT_WALL);
    public static final DeferredItem<BlockItem> LTV_ITEM = signItem("ltv", LTV, LTV_WALL);
    public static final DeferredItem<BlockItem> LTV_END_ITEM = signItem("ltv_end", LTV_END, LTV_END_WALL);
    public static final DeferredItem<BlockItem> NORMAL_SIGNAL_ITEM = signItem("normal_signal", NORMAL_SIGNAL, NORMAL_SIGNAL_WALL);
    public static final DeferredItem<BlockItem> SPEED_LIMIT_SHORT_ITEM = signItem("speed_limit_short", SPEED_LIMIT_SHORT, SPEED_LIMIT_SHORT_WALL);
    public static final DeferredItem<BlockItem> LTV_SHORT_ITEM = signItem("ltv_short", LTV_SHORT, LTV_SHORT_WALL);
    public static final DeferredItem<BlockItem> LTV_END_SHORT_ITEM = signItem("ltv_end_short", LTV_END_SHORT, LTV_END_SHORT_WALL);
    public static final DeferredItem<BlockItem> NORMAL_SIGNAL_SHORT_ITEM = signItem("normal_signal_short", NORMAL_SIGNAL_SHORT, NORMAL_SIGNAL_SHORT_WALL);
    public static final DeferredItem<BlockItem> APEADERO_SIGNAL_ITEM = signItem("apeadero_signal", APEADERO_SIGNAL, APEADERO_SIGNAL_WALL);
    public static final DeferredItem<BlockItem> APEADERO_SIGNAL_SHORT_ITEM = signItem("apeadero_signal_short", APEADERO_SIGNAL_SHORT, APEADERO_SIGNAL_SHORT_WALL);
    public static final DeferredItem<BlockItem> BASE_ITEM = ITEMS.registerSimpleBlockItem(BASE);
    public static final DeferredItem<BlockItem> SILBATO_SIGNAL_ITEM = signItem("silbato_signal", SILBATO_SIGNAL, SILBATO_SIGNAL_WALL);
    public static final DeferredItem<BlockItem> SILBATO_SIGNAL_SHORT_ITEM = signItem("silbato_signal_short", SILBATO_SIGNAL_SHORT, SILBATO_SIGNAL_SHORT_WALL);
    public static final DeferredItem<BlockItem> STOP_SIGNAL_ITEM = signItem("stop_signal", STOP_SIGNAL, STOP_SIGNAL_WALL);
    public static final DeferredItem<BlockItem> STOP_SIGNAL_SHORT_ITEM = signItem("stop_signal_short", STOP_SIGNAL_SHORT, STOP_SIGNAL_SHORT_WALL);
    public static final DeferredItem<BlockItem> SLIDING_TRAIN_DOOR_ITEM = ITEMS.registerSimpleBlockItem(SLIDING_TRAIN_DOOR);
    public static final DeferredItem<BlockItem> SLIDING_TRAIN_DOOR_BACK_ITEM = ITEMS.registerSimpleBlockItem(SLIDING_TRAIN_DOOR_BACK);
    public static final DeferredItem<BlockItem> WHITE_SLIDING_TRAIN_DOOR_ITEM = ITEMS.registerSimpleBlockItem(WHITE_SLIDING_TRAIN_DOOR);
    public static final DeferredItem<BlockItem> WHITE_SLIDING_TRAIN_DOOR_BACK_ITEM = ITEMS.registerSimpleBlockItem(WHITE_SLIDING_TRAIN_DOOR_BACK);
    public static final DeferredItem<BlockItem> JUNCTION_BOX_ITEM = ITEMS.registerSimpleBlockItem(JUNCTION_BOX);
    public static final DeferredItem<BlockItem> JUNCTION_BOX_WITH_CABLES_ITEM = ITEMS.registerSimpleBlockItem(JUNCTION_BOX_WITH_CABLES);

    // --- Pestanas creativas ---

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DECOR_TAB =
            CREATIVE_TABS.register("rodalies_decor", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.rodalies_decor"))
                    .icon(() -> R_LOGO_ITEM.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(R_LOGO_ITEM.get());
                        output.accept(R_LOGO_DIRECTIONAL_ITEM.get());
                        output.accept(RENFE_LOGO_ITEM.get());
                        output.accept(RENFE_LOGO_DIRECTIONAL_ITEM.get());
                        output.accept(PURPLE_SIDE_ITEM.get());
                        output.accept(PURPLE_SIDE_DIRECTIONAL_ITEM.get());
                        output.accept(CERCANIAS_LOGO_ITEM.get());
                        output.accept(CERCANIAS_LOGO_DIRECTIONAL_ITEM.get());
                        output.accept(CERCANIAS_LOGO_INVERTED_ITEM.get());
                        output.accept(CERCANIAS_LOGO_INVERTED_DIRECTIONAL_ITEM.get());
                        output.accept(RED_CERCANIAS_ALUMINIUM_ITEM.get());
                        output.accept(ORANGE_PURPLE_LINES_ITEM.get());
                        output.accept(ORANGE_PURPLE_LINES_DIRECTIONAL_ITEM.get());
                        output.accept(ORANGE_PURPLE_LINES_BOTTOM_ITEM.get());
                        output.accept(ORANGE_PURPLE_LINES_BOTTOM_DIRECTIONAL_ITEM.get());
                        output.accept(DOUBLE_LINES_GRAY_CONCRETE_ITEM.get());
                        output.accept(RED_STRIPE_ITEM.get());
                        output.accept(RED_STRIPE_DIRECTIONAL_ITEM.get());
                        output.accept(RED_PURPLE_STRIPES_ITEM.get());
                        output.accept(RED_PURPLE_STRIPES_DIRECTIONAL_ITEM.get());
                        output.accept(TRAIN_LIGHT_ITEM.get());
                        output.accept(FRONT_LIGHT_ITEM.get());
                        output.accept(TRAIN_MAX_SPEED_ITEM.get());
                        output.accept(TRAIN_WINDOW_ITEM.get());
                        output.accept(PARABRISAS_447_ITEM.get());
                        output.accept(STATION_SIGNAL_ITEM.get());
                        output.accept(STATION_SIGNAL_PLAIN_ITEM.get());
                        output.accept(SLIDING_TRAIN_DOOR_ITEM.get());
                        output.accept(SLIDING_TRAIN_DOOR_BACK_ITEM.get());
                        output.accept(WHITE_SLIDING_TRAIN_DOOR_ITEM.get());
                        output.accept(WHITE_SLIDING_TRAIN_DOOR_BACK_ITEM.get());
                        output.accept(JUNCTION_BOX_ITEM.get());
                        output.accept(JUNCTION_BOX_WITH_CABLES_ITEM.get());
                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SIGNALS_TAB =
            CREATIVE_TABS.register("rodalies_signals", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.rodalies_signals"))
                    .icon(() -> SPEED_LIMIT_ITEM.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(PLATFORM_NUMBER_SIGNAL_ITEM.get());
                        output.accept(HECTOMETER_SIGNAL_ITEM.get());
                        output.accept(SPEED_LIMIT_ITEM.get());
                        output.accept(LTV_ITEM.get());
                        output.accept(LTV_END_ITEM.get());
                        output.accept(NORMAL_SIGNAL_ITEM.get());
                        output.accept(APEADERO_SIGNAL_ITEM.get());
                        output.accept(SILBATO_SIGNAL_ITEM.get());
                        output.accept(STOP_SIGNAL_ITEM.get());
                        output.accept(SPEED_LIMIT_SHORT_ITEM.get());
                        output.accept(LTV_SHORT_ITEM.get());
                        output.accept(LTV_END_SHORT_ITEM.get());
                        output.accept(NORMAL_SIGNAL_SHORT_ITEM.get());
                        output.accept(APEADERO_SIGNAL_SHORT_ITEM.get());
                        output.accept(SILBATO_SIGNAL_SHORT_ITEM.get());
                        output.accept(STOP_SIGNAL_SHORT_ITEM.get());
                        output.accept(BASE_ITEM.get());
                    })
                    .build());
}
