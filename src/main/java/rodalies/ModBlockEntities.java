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
                    ModBlocks.NORMAL_SIGNAL_SHORT.get())
                .build(null));

    // Puerta corredera: guarda el progreso de la animacion (solo la celda LOWER lo lleva).
    public static final RegistryObject<BlockEntityType<SlidingTrainDoorBlockEntity>> SLIDING_TRAIN_DOOR_BE =
        BLOCK_ENTITIES.register("sliding_train_door",
            () -> BlockEntityType.Builder.of(
                    SlidingTrainDoorBlockEntity::new,
                    ModBlocks.SLIDING_TRAIN_DOOR.get())
                .build(null));
}
