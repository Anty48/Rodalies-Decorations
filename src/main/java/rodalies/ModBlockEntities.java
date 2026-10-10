package rodalies;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registro de los BlockEntity del mod.
 */
public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "rodalies");

    public static final RegistryObject<BlockEntityType<StationSignalBlockEntity>> STATION_SIGNAL_BE =
        BLOCK_ENTITIES.register("station_signal",
            () -> BlockEntityType.Builder.of(
                    StationSignalBlockEntity::new,
                    ModBlocks.STATION_SIGNAL.get(),
                    ModBlocks.STATION_SIGNAL_PLAIN.get())
                .build(null));

    // BlockEntity compartido por las señales ferroviarias con texto editable (LTV_END no lleva).
    public static final RegistryObject<BlockEntityType<RailSignBlockEntity>> RAIL_SIGN_BE =
        BLOCK_ENTITIES.register("rail_sign",
            () -> BlockEntityType.Builder.of(
                    RailSignBlockEntity::new,
                    ModBlocks.PLATFORM_NUMBER_SIGNAL.get(),
                    ModBlocks.SPEED_LIMIT.get(),
                    ModBlocks.LTV.get(),
                    ModBlocks.NORMAL_SIGNAL.get(),
                    ModBlocks.SPEED_LIMIT_SHORT.get(),
                    ModBlocks.LTV_SHORT.get(),
                    ModBlocks.NORMAL_SIGNAL_SHORT.get(),
                    ModBlocks.SPEED_LIMIT_WALL.get(),
                    ModBlocks.SPEED_LIMIT_SHORT_WALL.get(),
                    ModBlocks.LTV_WALL.get(),
                    ModBlocks.LTV_SHORT_WALL.get(),
                    ModBlocks.NORMAL_SIGNAL_WALL.get(),
                    ModBlocks.NORMAL_SIGNAL_SHORT_WALL.get())
                .build(null));

    // Bloque de velocidad maxima (cubo de cuarzo): guarda su texto de hasta 4 caracteres.
    public static final RegistryObject<BlockEntityType<TrainMaxSpeedBlockEntity>> TRAIN_MAX_SPEED_BE =
        BLOCK_ENTITIES.register("train_max_speed",
            () -> BlockEntityType.Builder.of(
                    TrainMaxSpeedBlockEntity::new,
                    ModBlocks.TRAIN_MAX_SPEED.get())
                .build(null));

    // Señal de hectometro (celda BOTTOM): guarda las 2 filas de texto.
    public static final RegistryObject<BlockEntityType<HectometerSignBlockEntity>> HECTOMETER_BE =
        BLOCK_ENTITIES.register("hectometer_signal",
            () -> BlockEntityType.Builder.of(
                    HectometerSignBlockEntity::new,
                    ModBlocks.HECTOMETER_SIGNAL.get())
                .build(null));

    // Puerta corredera: BlockEntity (solo LOWER) para la animacion de deslizamiento EN EL MUNDO
    // (temporizador de VISIBLE en el servidor + progreso del BER en el cliente).
    public static final RegistryObject<BlockEntityType<SlidingTrainDoorBlockEntity>> SLIDING_TRAIN_DOOR_BE =
        BLOCK_ENTITIES.register("sliding_train_door",
            () -> BlockEntityType.Builder.of(
                    SlidingTrainDoorBlockEntity::new,
                    ModBlocks.SLIDING_TRAIN_DOOR.get(),
                    ModBlocks.SLIDING_TRAIN_DOOR_BACK.get(),
                    ModBlocks.WHITE_SLIDING_TRAIN_DOOR.get(),
                    ModBlocks.WHITE_SLIDING_TRAIN_DOOR_BACK.get())
                .build(null));
}
