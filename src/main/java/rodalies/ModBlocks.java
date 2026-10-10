package rodalies;

import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    // --- Clases de bloques ---

    public static class Rgrande extends Block {
        public Rgrande() {
            super(BlockBehaviour.Properties.of().strength(2.0f));
        }
    }

    public static class RenfeLogo extends Block {
        public RenfeLogo() {
            super(BlockBehaviour.Properties.of().strength(2.0f));
        }
    }

    public static class PurpleSide extends Block {
    public PurpleSide() {
        super(BlockBehaviour.Properties.of()
            .strength(2.0f)); // cambia la dureza si quieres
    }
}
    // --- Registro de bloques ---

    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(ForgeRegistries.BLOCKS, "rodalies");

    public static final RegistryObject<Block> R_LOGO =
        BLOCKS.register("r_logo", () -> new Rgrande());

    public static final RegistryObject<Block> RENFE_LOGO =
        BLOCKS.register("renfe_logo", () -> new RenfeLogo());
    
    public static final RegistryObject<Block> PURPLE_SIDE =
    BLOCKS.register("purple_side", () -> new PurpleSide());

    // Cercanias logo: dureza del cuarzo (0.8). Sin subclase: Block directo.
    public static final RegistryObject<Block> CERCANIAS_LOGO =
        BLOCKS.register("cercanias_logo",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.8f)));

    // --- Variantes "de una cara" (logo/franja solo en la cara donde lo colocas). Se craftean 1:1 con su
    //     hermano de caras iguales y viceversa (recetas shapeless hand-written). Ver DirectionalLogoBlock. ---
    public static final RegistryObject<Block> R_LOGO_DIRECTIONAL =
        BLOCKS.register("r_logo_directional",
            () -> new DirectionalLogoBlock(BlockBehaviour.Properties.of().strength(2.0f)));

    public static final RegistryObject<Block> RENFE_LOGO_DIRECTIONAL =
        BLOCKS.register("renfe_logo_directional",
            () -> new DirectionalLogoBlock(BlockBehaviour.Properties.of().strength(2.0f)));

    public static final RegistryObject<Block> PURPLE_SIDE_DIRECTIONAL =
        BLOCKS.register("purple_side_directional",
            () -> new DirectionalLogoBlock(BlockBehaviour.Properties.of().strength(2.0f)));

    public static final RegistryObject<Block> CERCANIAS_LOGO_DIRECTIONAL =
        BLOCKS.register("cercanias_logo_directional",
            () -> new DirectionalLogoBlock(BlockBehaviour.Properties.of().strength(0.8f)));

    // Franja naranja+morada sobre cuarzo: las 4 caras laterales llevan la franja (naranja arriba,
    // morada abajo); top/bottom son smooth quartz. Decorativo simple (cubeBottomTop via datagen).
    public static final RegistryObject<Block> ORANGE_PURPLE_LINES =
        BLOCKS.register("orange_purple_lines",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.8f)));

    // Variante "de una cara": la franja SOLO en la cara frontal (DirectionalLogoBlock), resto smooth quartz.
    public static final RegistryObject<Block> ORANGE_PURPLE_LINES_DIRECTIONAL =
        BLOCKS.register("orange_purple_lines_directional",
            () -> new DirectionalLogoBlock(BlockBehaviour.Properties.of().strength(0.8f)));

    // Variante "bottom": misma idea que orange_purple_lines pero con la franja pegada abajo (naranja
    // encima de morada, en el tercio inferior). Caras laterales con textura propia; top/bottom smooth quartz.
    public static final RegistryObject<Block> ORANGE_PURPLE_LINES_BOTTOM =
        BLOCKS.register("orange_purple_lines_bottom",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.8f)));

    // "Bottom" de una cara: la franja inferior SOLO en la cara frontal (DirectionalLogoBlock).
    public static final RegistryObject<Block> ORANGE_PURPLE_LINES_BOTTOM_DIRECTIONAL =
        BLOCKS.register("orange_purple_lines_bottom_directional",
            () -> new DirectionalLogoBlock(BlockBehaviour.Properties.of().strength(0.8f)));

    // Franja doble sobre hormigon gris: caras laterales con la franja, cara superior gray concrete y
    // cara inferior smooth quartz. Solo existe en version de caras iguales (sin variante de una cara).
    public static final RegistryObject<Block> DOUBLE_LINES_GRAY_CONCRETE =
        BLOCKS.register("double_lines_gray_concrete",
            () -> new Block(BlockBehaviour.Properties.of().strength(0.8f)));

    // Luz de tren: bloque luminoso (luz 15) con las mismas caracteristicas que la sea lantern (blando,
    // sonido de cristal). Todas las caras iguales. Se craftea 4 con hierro en cruz + antorcha al centro.
    public static final RegistryObject<Block> TRAIN_LIGHT =
        BLOCKS.register("train_light",
            () -> new Block(BlockBehaviour.Properties.of()
                .strength(0.3f)
                .sound(SoundType.GLASS)
                .lightLevel(s -> 15)));

    // Luz frontal de tren: bloque tipo hormigon gris que tambien ilumina (luz 15). Caras laterales con
    // la textura de faros frontales; top/bottom gray concrete. NO confundir con la luz de tren.
    public static final RegistryObject<Block> FRONT_LIGHT =
        BLOCKS.register("front_light",
            () -> new Block(BlockBehaviour.Properties.of()
                .strength(1.8f)
                .lightLevel(s -> 15)));

    // Ventana de tren: bloque de cristal (todas las propiedades del cristal vanilla) con las caras
    // laterales de train_window (cristal tintado semitransparente) y top/bottom smooth quartz. Render
    // translucido en ClientSetup + noOcclusion para que no recorte lo de detras.
    public static final RegistryObject<Block> TRAIN_WINDOW =
        BLOCKS.register("train_window",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.GLASS).noOcclusion()));

    // Parabrisas del 447: multiblock 3x1x1 con cristal tintado (render translucido en ClientSetup).
    public static final RegistryObject<Block> PARABRISAS_447 =
        BLOCKS.register("parabrisas_447",
            () -> new Parabrisas447Block(BlockBehaviour.Properties.of()
                .strength(1.5f)
                .sound(SoundType.GLASS)
                .noOcclusion()));

    // Cartel de estacion: bloque con texto editable (BlockEntity + renderer). Modelo 3x3 que
    // sobresale de la celda; colision solo el poste. noOcclusion porque casi toda su celda es aire.
    public static final RegistryObject<Block> STATION_SIGNAL =
        BLOCKS.register("station_signal",
            () -> new StationSignalBlock(BlockBehaviour.Properties.of()
                .strength(1.5f, 10.0f)
                .sound(SoundType.METAL)
                .noOcclusion(), true)); // con logo de Rodalies -> texto desplazado

    // Version generica del cartel (sin logo de Rodalies): mismo modelo/comportamiento, pero el
    // texto va centrado. Usable por quien no quiera la marca Rodalies.
    public static final RegistryObject<Block> STATION_SIGNAL_PLAIN =
        BLOCKS.register("station_signal_plain",
            () -> new StationSignalBlock(BlockBehaviour.Properties.of()
                .strength(1.5f, 10.0f)
                .sound(SoundType.METAL)
                .noOcclusion(), false)); // sin logo -> texto centrado

    // Señales ferroviarias (poste + señal que sobresale de la celda). Comparten RailSignBlock y solo
    // cambian por su RailSignType (cuanto texto llevan y como se dibuja). noOcclusion porque casi
    // toda la celda es aire y el modelo sobresale hacia arriba.
    private static BlockBehaviour.Properties signProps() {
        // Dureza baja (1.5, rapida de picar) pero resistencia a explosiones alta (10.0); sonido metal.
        return BlockBehaviour.Properties.of().strength(1.5f, 10.0f).sound(SoundType.METAL).noOcclusion();
    }

    // Bloque base (1 celda): la pieza con la que se craftean todas las señales.
    public static final RegistryObject<Block> BASE =
        BLOCKS.register("base", () -> new BaseBlock(signProps()));

    // Cartel de numero de via: hasta 3 caracteres blancos, visible por ambos lados. Clic derecho con
    // otro cartel en mano lo convierte en la version DOBLE (dos paneles, un numero por lado).
    public static final RegistryObject<Block> PLATFORM_NUMBER_SIGNAL =
        BLOCKS.register("platform_number_signal",
            () -> new PlatformNumberSignBlock(signProps()));

    // Señal de velocidad permanente (rombo blanco): hasta 3 digitos negros, un lado. Admite el estado
    // "doble" (SpeedLimitBlock): clic derecho con otra señal de velocidad en mano apila una 2ª señal.
    public static final RegistryObject<Block> SPEED_LIMIT =
        BLOCKS.register("speed_limit",
            () -> new SpeedLimitBlock(signProps()));

    // LTV (rombo amarillo temporal): hasta 3 digitos negros, un lado.
    public static final RegistryObject<Block> LTV =
        BLOCKS.register("ltv",
            () -> new RailSignBlock(signProps(), RailSignType.LTV));

    // Fin de LTV (cuadrado amarillo): sin texto.
    public static final RegistryObject<Block> LTV_END =
        BLOCKS.register("ltv_end",
            () -> new RailSignBlock(signProps(), RailSignType.LTV_END));

    // Cartel de señales (cuadrado blanco): hasta 2 lineas de texto negro que se adaptan, un lado.
    public static final RegistryObject<Block> NORMAL_SIGNAL =
        BLOCKS.register("normal_signal",
            () -> new RailSignBlock(signProps(), RailSignType.NORMAL));

    // --- Versiones SHORT (poste una celda mas corto; se colocan en la celda del medio) ---

    public static final RegistryObject<Block> SPEED_LIMIT_SHORT =
        BLOCKS.register("speed_limit_short",
            () -> new RailSignBlock(signProps(), RailSignType.SPEED_LIMIT, true));

    public static final RegistryObject<Block> LTV_SHORT =
        BLOCKS.register("ltv_short",
            () -> new RailSignBlock(signProps(), RailSignType.LTV, true));

    public static final RegistryObject<Block> LTV_END_SHORT =
        BLOCKS.register("ltv_end_short",
            () -> new RailSignBlock(signProps(), RailSignType.LTV_END, true));

    public static final RegistryObject<Block> NORMAL_SIGNAL_SHORT =
        BLOCKS.register("normal_signal_short",
            () -> new RailSignBlock(signProps(), RailSignType.NORMAL, true));

    // Señal de apeadero (sin texto): mismas propiedades de bloque que el fin de LTV. Full + short.
    public static final RegistryObject<Block> APEADERO_SIGNAL =
        BLOCKS.register("apeadero_signal",
            () -> new RailSignBlock(signProps(), RailSignType.LTV_END));

    public static final RegistryObject<Block> APEADERO_SIGNAL_SHORT =
        BLOCKS.register("apeadero_signal_short",
            () -> new RailSignBlock(signProps(), RailSignType.LTV_END, true));

    // Señal de silbato (sin texto): mismas props que el fin de LTV. Full + short.
    public static final RegistryObject<Block> SILBATO_SIGNAL =
        BLOCKS.register("silbato_signal",
            () -> new RailSignBlock(signProps(), RailSignType.LTV_END));

    public static final RegistryObject<Block> SILBATO_SIGNAL_SHORT =
        BLOCKS.register("silbato_signal_short",
            () -> new RailSignBlock(signProps(), RailSignType.LTV_END, true));

    // Señal de detencion inmediata (sin texto): mismas props que el fin de LTV. Full + short.
    public static final RegistryObject<Block> STOP_SIGNAL =
        BLOCKS.register("stop_signal",
            () -> new RailSignBlock(signProps(), RailSignType.LTV_END));

    public static final RegistryObject<Block> STOP_SIGNAL_SHORT =
        BLOCKS.register("stop_signal_short",
            () -> new RailSignBlock(signProps(), RailSignType.LTV_END, true));

    // --- Modo PARED de las señales simples (sin poste, 1 celda). No tienen item propio: los coloca el
    // item normal/short (RailSignItem) al hacer clic en la cara de un bloque mirandola de frente, como el
    // cartel de estacion. Hay uno por item (normal y short) para que al romperlo devuelva el mismo item.
    // Se pegan al bloque de detras y, si es un poste fino (p.ej. de Pantographs & Wires), se meten hasta
    // tocarlo (RailSignWallBlock). La forma es la de la señal mirando al norte con la trasera en z=16. ---

    public static final RegistryObject<Block> SPEED_LIMIT_WALL =
        BLOCKS.register("speed_limit_wall",
            () -> new RailSignWallBlock(signProps(), RailSignType.SPEED_LIMIT, Block.box(0, 0.5, 13.9, 16, 16, 16)));

    public static final RegistryObject<Block> SPEED_LIMIT_SHORT_WALL =
        BLOCKS.register("speed_limit_short_wall",
            () -> new RailSignWallBlock(signProps(), RailSignType.SPEED_LIMIT, Block.box(0, 0.5, 13.9, 16, 16, 16)));

    public static final RegistryObject<Block> LTV_WALL =
        BLOCKS.register("ltv_wall",
            () -> new RailSignWallBlock(signProps(), RailSignType.LTV, Block.box(0, 0, 13.9, 16, 16, 16)));

    public static final RegistryObject<Block> LTV_SHORT_WALL =
        BLOCKS.register("ltv_short_wall",
            () -> new RailSignWallBlock(signProps(), RailSignType.LTV, Block.box(0, 0, 13.9, 16, 16, 16)));

    public static final RegistryObject<Block> LTV_END_WALL =
        BLOCKS.register("ltv_end_wall",
            () -> new RailSignWallBlock(signProps(), RailSignType.LTV_END, Block.box(1.5, 2, 14, 14.5, 15, 16)));

    public static final RegistryObject<Block> LTV_END_SHORT_WALL =
        BLOCKS.register("ltv_end_short_wall",
            () -> new RailSignWallBlock(signProps(), RailSignType.LTV_END, Block.box(1.5, 2, 14, 14.5, 15, 16)));

    public static final RegistryObject<Block> NORMAL_SIGNAL_WALL =
        BLOCKS.register("normal_signal_wall",
            () -> new RailSignWallBlock(signProps(), RailSignType.NORMAL, Block.box(1.5, 3, 14, 14.5, 16, 16)));

    public static final RegistryObject<Block> NORMAL_SIGNAL_SHORT_WALL =
        BLOCKS.register("normal_signal_short_wall",
            () -> new RailSignWallBlock(signProps(), RailSignType.NORMAL, Block.box(1.5, 3, 14, 14.5, 16, 16)));

    public static final RegistryObject<Block> APEADERO_SIGNAL_WALL =
        BLOCKS.register("apeadero_signal_wall",
            () -> new RailSignWallBlock(signProps(), RailSignType.LTV_END, Block.box(2, 0, 15, 14, 16, 16)));

    public static final RegistryObject<Block> APEADERO_SIGNAL_SHORT_WALL =
        BLOCKS.register("apeadero_signal_short_wall",
            () -> new RailSignWallBlock(signProps(), RailSignType.LTV_END, Block.box(2, 0, 15, 14, 16, 16)));

    public static final RegistryObject<Block> SILBATO_SIGNAL_WALL =
        BLOCKS.register("silbato_signal_wall",
            () -> new RailSignWallBlock(signProps(), RailSignType.LTV_END, Block.box(2, 0, 15, 14, 16, 16)));

    public static final RegistryObject<Block> SILBATO_SIGNAL_SHORT_WALL =
        BLOCKS.register("silbato_signal_short_wall",
            () -> new RailSignWallBlock(signProps(), RailSignType.LTV_END, Block.box(2, 0, 15, 14, 16, 16)));

    public static final RegistryObject<Block> STOP_SIGNAL_WALL =
        BLOCKS.register("stop_signal_wall",
            () -> new RailSignWallBlock(signProps(), RailSignType.LTV_END, Block.box(0, 0, 15, 16, 16, 16)));

    public static final RegistryObject<Block> STOP_SIGNAL_SHORT_WALL =
        BLOCKS.register("stop_signal_short_wall",
            () -> new RailSignWallBlock(signProps(), RailSignType.LTV_END, Block.box(0, 0, 15, 16, 16, 16)));

    // Bloque de cuarzo con la señal de velocidad maxima (rombo) en la cara frontal; el resto smooth
    // quartz. Lleva texto editable de hasta 4 caracteres centrado en el rombo (BlockEntity + renderer).
    public static final RegistryObject<Block> TRAIN_MAX_SPEED =
        BLOCKS.register("train_max_speed",
            () -> new TrainMaxSpeedBlock(BlockBehaviour.Properties.of().strength(0.8f)));

    // Señal de hectometro: poste de diorita con placa (modelo 3D que sobresale 1 celda hacia arriba).
    // Multiblock vertical de 2 celdas; texto editable en 2 filas (arriba grande, abajo pequeña).
    public static final RegistryObject<Block> HECTOMETER_SIGNAL =
        BLOCKS.register("hectometer_signal",
            () -> new HectometerSignBlock(signProps()));

    // Puerta corredera de tren: 2 hojas que se abren por el centro (multiblock vertical de 2 celdas).
    // noOcclusion porque las hojas son finas y al abrir dejan la celda casi vacia.
    // Dos variantes identicas salvo el sentido del pequeño desplazamiento en Z al abrir:
    // FRONT (adelante, por defecto) y BACK (atras). Se intercambian en la mesa de crafteo (receta
    // shapeless 1:1, sin gastar materiales).
    private static BlockBehaviour.Properties doorProps() {
        return BlockBehaviour.Properties.of().strength(1.5f, 10.0f).sound(SoundType.METAL).noOcclusion();
    }

    public static final RegistryObject<Block> SLIDING_TRAIN_DOOR =
        BLOCKS.register("sliding_train_door",
            () -> new SlidingTrainDoorBlock(doorProps(), true));  // abre hacia adelante

    public static final RegistryObject<Block> SLIDING_TRAIN_DOOR_BACK =
        BLOCKS.register("sliding_train_door_back",
            () -> new SlidingTrainDoorBlock(doorProps(), false)); // abre hacia atras

    // Puerta corredera de tren BLANCA: retexturizacion de la anterior (mismo tipo de puerta, animacion,
    // colision y compat con Create). Unica diferencia: ambas hojas usan la textura white_train_door
    // (izquierda = derecha). Igual que la naranja, tiene variante FRONT y BACK intercambiables.
    private static final net.minecraft.resources.ResourceLocation WHITE_DOOR_TEX =
        new net.minecraft.resources.ResourceLocation("rodalies", "block/white_train_door");
    // Techo (cara up) de la puerta blanca: cuarzo (la naranja usa gray concrete por defecto).
    private static final net.minecraft.resources.ResourceLocation WHITE_DOOR_TOP =
        new net.minecraft.resources.ResourceLocation("minecraft", "block/quartz_block_top");

    public static final RegistryObject<Block> WHITE_SLIDING_TRAIN_DOOR =
        BLOCKS.register("white_sliding_train_door",
            () -> new SlidingTrainDoorBlock(doorProps(), true, WHITE_DOOR_TEX, WHITE_DOOR_TEX, WHITE_DOOR_TOP));

    public static final RegistryObject<Block> WHITE_SLIDING_TRAIN_DOOR_BACK =
        BLOCKS.register("white_sliding_train_door_back",
            () -> new SlidingTrainDoorBlock(doorProps(), false, WHITE_DOOR_TEX, WHITE_DOOR_TEX, WHITE_DOOR_TOP));

    // --- Registro de items ---

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, "rodalies");

    public static final RegistryObject<Item> R_LOGO_ITEM =
        ITEMS.register("r_logo",
            () -> new BlockItem(R_LOGO.get(), new Item.Properties()));

    public static final RegistryObject<Item> RENFE_LOGO_ITEM =
        ITEMS.register("renfe_logo",
            () -> new BlockItem(RENFE_LOGO.get(), new Item.Properties()));

    public static final RegistryObject<Item> PURPLE_SIDE_ITEM =
    ITEMS.register("purple_side",
        () -> new BlockItem(PURPLE_SIDE.get(), new Item.Properties()));

    public static final RegistryObject<Item> CERCANIAS_LOGO_ITEM =
        ITEMS.register("cercanias_logo",
            () -> new BlockItem(CERCANIAS_LOGO.get(), new Item.Properties()));

    public static final RegistryObject<Item> R_LOGO_DIRECTIONAL_ITEM =
        ITEMS.register("r_logo_directional",
            () -> new BlockItem(R_LOGO_DIRECTIONAL.get(), new Item.Properties()));

    public static final RegistryObject<Item> RENFE_LOGO_DIRECTIONAL_ITEM =
        ITEMS.register("renfe_logo_directional",
            () -> new BlockItem(RENFE_LOGO_DIRECTIONAL.get(), new Item.Properties()));

    public static final RegistryObject<Item> PURPLE_SIDE_DIRECTIONAL_ITEM =
        ITEMS.register("purple_side_directional",
            () -> new BlockItem(PURPLE_SIDE_DIRECTIONAL.get(), new Item.Properties()));

    public static final RegistryObject<Item> CERCANIAS_LOGO_DIRECTIONAL_ITEM =
        ITEMS.register("cercanias_logo_directional",
            () -> new BlockItem(CERCANIAS_LOGO_DIRECTIONAL.get(), new Item.Properties()));

    public static final RegistryObject<Item> ORANGE_PURPLE_LINES_ITEM =
        ITEMS.register("orange_purple_lines",
            () -> new BlockItem(ORANGE_PURPLE_LINES.get(), new Item.Properties()));

    public static final RegistryObject<Item> ORANGE_PURPLE_LINES_DIRECTIONAL_ITEM =
        ITEMS.register("orange_purple_lines_directional",
            () -> new BlockItem(ORANGE_PURPLE_LINES_DIRECTIONAL.get(), new Item.Properties()));

    public static final RegistryObject<Item> ORANGE_PURPLE_LINES_BOTTOM_ITEM =
        ITEMS.register("orange_purple_lines_bottom",
            () -> new BlockItem(ORANGE_PURPLE_LINES_BOTTOM.get(), new Item.Properties()));

    public static final RegistryObject<Item> ORANGE_PURPLE_LINES_BOTTOM_DIRECTIONAL_ITEM =
        ITEMS.register("orange_purple_lines_bottom_directional",
            () -> new BlockItem(ORANGE_PURPLE_LINES_BOTTOM_DIRECTIONAL.get(), new Item.Properties()));

    public static final RegistryObject<Item> DOUBLE_LINES_GRAY_CONCRETE_ITEM =
        ITEMS.register("double_lines_gray_concrete",
            () -> new BlockItem(DOUBLE_LINES_GRAY_CONCRETE.get(), new Item.Properties()));

    public static final RegistryObject<Item> TRAIN_LIGHT_ITEM =
        ITEMS.register("train_light",
            () -> new BlockItem(TRAIN_LIGHT.get(), new Item.Properties()));

    public static final RegistryObject<Item> FRONT_LIGHT_ITEM =
        ITEMS.register("front_light",
            () -> new BlockItem(FRONT_LIGHT.get(), new Item.Properties()));

    public static final RegistryObject<Item> TRAIN_MAX_SPEED_ITEM =
        ITEMS.register("train_max_speed",
            () -> new BlockItem(TRAIN_MAX_SPEED.get(), new Item.Properties()));

    public static final RegistryObject<Item> HECTOMETER_SIGNAL_ITEM =
        ITEMS.register("hectometer_signal",
            () -> new BlockItem(HECTOMETER_SIGNAL.get(), new Item.Properties()));

    public static final RegistryObject<Item> TRAIN_WINDOW_ITEM =
        ITEMS.register("train_window",
            () -> new BlockItem(TRAIN_WINDOW.get(), new Item.Properties()));

    public static final RegistryObject<Item> PARABRISAS_447_ITEM =
        ITEMS.register("parabrisas_447",
            () -> new BlockItem(PARABRISAS_447.get(), new Item.Properties()));

    public static final RegistryObject<Item> STATION_SIGNAL_ITEM =
        ITEMS.register("station_signal",
            () -> new BlockItem(STATION_SIGNAL.get(), new Item.Properties()));

    public static final RegistryObject<Item> STATION_SIGNAL_PLAIN_ITEM =
        ITEMS.register("station_signal_plain",
            () -> new BlockItem(STATION_SIGNAL_PLAIN.get(), new Item.Properties()));

    public static final RegistryObject<Item> PLATFORM_NUMBER_SIGNAL_ITEM =
        ITEMS.register("platform_number_signal",
            () -> new BlockItem(PLATFORM_NUMBER_SIGNAL.get(), new Item.Properties()));

    public static final RegistryObject<Item> SPEED_LIMIT_ITEM =
        ITEMS.register("speed_limit",
            () -> new RailSignItem(SPEED_LIMIT.get(), SPEED_LIMIT_WALL.get(), new Item.Properties()));

    public static final RegistryObject<Item> LTV_ITEM =
        ITEMS.register("ltv",
            () -> new RailSignItem(LTV.get(), LTV_WALL.get(), new Item.Properties()));

    public static final RegistryObject<Item> LTV_END_ITEM =
        ITEMS.register("ltv_end",
            () -> new RailSignItem(LTV_END.get(), LTV_END_WALL.get(), new Item.Properties()));

    public static final RegistryObject<Item> NORMAL_SIGNAL_ITEM =
        ITEMS.register("normal_signal",
            () -> new RailSignItem(NORMAL_SIGNAL.get(), NORMAL_SIGNAL_WALL.get(), new Item.Properties()));

    public static final RegistryObject<Item> SPEED_LIMIT_SHORT_ITEM =
        ITEMS.register("speed_limit_short",
            () -> new RailSignItem(SPEED_LIMIT_SHORT.get(), SPEED_LIMIT_SHORT_WALL.get(), new Item.Properties()));

    public static final RegistryObject<Item> LTV_SHORT_ITEM =
        ITEMS.register("ltv_short",
            () -> new RailSignItem(LTV_SHORT.get(), LTV_SHORT_WALL.get(), new Item.Properties()));

    public static final RegistryObject<Item> LTV_END_SHORT_ITEM =
        ITEMS.register("ltv_end_short",
            () -> new RailSignItem(LTV_END_SHORT.get(), LTV_END_SHORT_WALL.get(), new Item.Properties()));

    public static final RegistryObject<Item> NORMAL_SIGNAL_SHORT_ITEM =
        ITEMS.register("normal_signal_short",
            () -> new RailSignItem(NORMAL_SIGNAL_SHORT.get(), NORMAL_SIGNAL_SHORT_WALL.get(), new Item.Properties()));

    public static final RegistryObject<Item> APEADERO_SIGNAL_ITEM =
        ITEMS.register("apeadero_signal",
            () -> new RailSignItem(APEADERO_SIGNAL.get(), APEADERO_SIGNAL_WALL.get(), new Item.Properties()));

    public static final RegistryObject<Item> APEADERO_SIGNAL_SHORT_ITEM =
        ITEMS.register("apeadero_signal_short",
            () -> new RailSignItem(APEADERO_SIGNAL_SHORT.get(), APEADERO_SIGNAL_SHORT_WALL.get(), new Item.Properties()));

    public static final RegistryObject<Item> BASE_ITEM =
        ITEMS.register("base",
            () -> new BlockItem(BASE.get(), new Item.Properties()));

    public static final RegistryObject<Item> SILBATO_SIGNAL_ITEM =
        ITEMS.register("silbato_signal",
            () -> new RailSignItem(SILBATO_SIGNAL.get(), SILBATO_SIGNAL_WALL.get(), new Item.Properties()));

    public static final RegistryObject<Item> SILBATO_SIGNAL_SHORT_ITEM =
        ITEMS.register("silbato_signal_short",
            () -> new RailSignItem(SILBATO_SIGNAL_SHORT.get(), SILBATO_SIGNAL_SHORT_WALL.get(), new Item.Properties()));

    public static final RegistryObject<Item> STOP_SIGNAL_ITEM =
        ITEMS.register("stop_signal",
            () -> new RailSignItem(STOP_SIGNAL.get(), STOP_SIGNAL_WALL.get(), new Item.Properties()));

    public static final RegistryObject<Item> STOP_SIGNAL_SHORT_ITEM =
        ITEMS.register("stop_signal_short",
            () -> new RailSignItem(STOP_SIGNAL_SHORT.get(), STOP_SIGNAL_SHORT_WALL.get(), new Item.Properties()));

    public static final RegistryObject<Item> SLIDING_TRAIN_DOOR_ITEM =
        ITEMS.register("sliding_train_door",
            () -> new BlockItem(SLIDING_TRAIN_DOOR.get(), new Item.Properties()));

    public static final RegistryObject<Item> SLIDING_TRAIN_DOOR_BACK_ITEM =
        ITEMS.register("sliding_train_door_back",
            () -> new BlockItem(SLIDING_TRAIN_DOOR_BACK.get(), new Item.Properties()));

    public static final RegistryObject<Item> WHITE_SLIDING_TRAIN_DOOR_ITEM =
        ITEMS.register("white_sliding_train_door",
            () -> new BlockItem(WHITE_SLIDING_TRAIN_DOOR.get(), new Item.Properties()));

    public static final RegistryObject<Item> WHITE_SLIDING_TRAIN_DOOR_BACK_ITEM =
        ITEMS.register("white_sliding_train_door_back",
            () -> new BlockItem(WHITE_SLIDING_TRAIN_DOOR_BACK.get(), new Item.Properties()));

    // --- Creative Tab ---

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "rodalies");

    // Pestaña 1: bloques decorativos (logos, parabrisas, carteles de estacion, puerta).
    public static final RegistryObject<CreativeModeTab> DECOR_TAB =
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
                output.accept(ORANGE_PURPLE_LINES_ITEM.get());
                output.accept(ORANGE_PURPLE_LINES_DIRECTIONAL_ITEM.get());
                output.accept(ORANGE_PURPLE_LINES_BOTTOM_ITEM.get());
                output.accept(ORANGE_PURPLE_LINES_BOTTOM_DIRECTIONAL_ITEM.get());
                output.accept(DOUBLE_LINES_GRAY_CONCRETE_ITEM.get());
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
            })
            .build());

    // Pestaña 2: señales ferroviarias (via, velocidad, LTV, carteles, apeadero/silbato/stop) + base.
    public static final RegistryObject<CreativeModeTab> SIGNALS_TAB =
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