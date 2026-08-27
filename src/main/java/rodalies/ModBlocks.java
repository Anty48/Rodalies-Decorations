package rodalies;

import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
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
                .strength(1.5f)
                .sound(SoundType.METAL)
                .noOcclusion(), true)); // con logo de Rodalies -> texto desplazado

    // Version generica del cartel (sin logo de Rodalies): mismo modelo/comportamiento, pero el
    // texto va centrado. Usable por quien no quiera la marca Rodalies.
    public static final RegistryObject<Block> STATION_SIGNAL_PLAIN =
        BLOCKS.register("station_signal_plain",
            () -> new StationSignalBlock(BlockBehaviour.Properties.of()
                .strength(1.5f)
                .sound(SoundType.METAL)
                .noOcclusion(), false)); // sin logo -> texto centrado

    // Señales ferroviarias (poste + señal que sobresale de la celda). Comparten RailSignBlock y solo
    // cambian por su RailSignType (cuanto texto llevan y como se dibuja). noOcclusion porque casi
    // toda la celda es aire y el modelo sobresale hacia arriba.
    private static BlockBehaviour.Properties signProps() {
        return BlockBehaviour.Properties.of().strength(1.5f).sound(SoundType.METAL).noOcclusion();
    }

    // Cartel de numero de via: hasta 3 caracteres blancos, visible por ambos lados.
    public static final RegistryObject<Block> PLATFORM_NUMBER_SIGNAL =
        BLOCKS.register("platform_number_signal",
            () -> new RailSignBlock(signProps(), RailSignType.PLATFORM_NUMBER));

    // Señal de velocidad permanente (rombo blanco): hasta 3 digitos negros, un lado.
    public static final RegistryObject<Block> SPEED_LIMIT =
        BLOCKS.register("speed_limit",
            () -> new RailSignBlock(signProps(), RailSignType.SPEED_LIMIT));

    // LVT (rombo amarillo temporal): hasta 3 digitos negros, un lado.
    public static final RegistryObject<Block> LVT =
        BLOCKS.register("lvt",
            () -> new RailSignBlock(signProps(), RailSignType.LVT));

    // Fin de LVT (cuadrado amarillo): sin texto.
    public static final RegistryObject<Block> LVT_END =
        BLOCKS.register("lvt_end",
            () -> new RailSignBlock(signProps(), RailSignType.LVT_END));

    // Cartel de señales (cuadrado blanco): hasta 2 lineas de texto negro que se adaptan, un lado.
    public static final RegistryObject<Block> NORMAL_SIGNAL =
        BLOCKS.register("normal_signal",
            () -> new RailSignBlock(signProps(), RailSignType.NORMAL));

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
            () -> new BlockItem(SPEED_LIMIT.get(), new Item.Properties()));

    public static final RegistryObject<Item> LVT_ITEM =
        ITEMS.register("lvt",
            () -> new BlockItem(LVT.get(), new Item.Properties()));

    public static final RegistryObject<Item> LVT_END_ITEM =
        ITEMS.register("lvt_end",
            () -> new BlockItem(LVT_END.get(), new Item.Properties()));

    public static final RegistryObject<Item> NORMAL_SIGNAL_ITEM =
        ITEMS.register("normal_signal",
            () -> new BlockItem(NORMAL_SIGNAL.get(), new Item.Properties()));

    // --- Creative Tab ---

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "rodalies");

    public static final RegistryObject<CreativeModeTab> RODALIES_TAB =
        CREATIVE_TABS.register("rodalies_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.rodalies"))
            .icon(() -> R_LOGO_ITEM.get().getDefaultInstance())
            .displayItems((params, output) -> {
                output.accept(R_LOGO_ITEM.get());
                output.accept(RENFE_LOGO_ITEM.get());
                output.accept(PURPLE_SIDE_ITEM.get());
                output.accept(CERCANIAS_LOGO_ITEM.get());
                output.accept(PARABRISAS_447_ITEM.get());
                output.accept(STATION_SIGNAL_ITEM.get());
                output.accept(STATION_SIGNAL_PLAIN_ITEM.get());
                output.accept(PLATFORM_NUMBER_SIGNAL_ITEM.get());
                output.accept(SPEED_LIMIT_ITEM.get());
                output.accept(LVT_ITEM.get());
                output.accept(LVT_END_ITEM.get());
                output.accept(NORMAL_SIGNAL_ITEM.get());
            })
            .build());
}