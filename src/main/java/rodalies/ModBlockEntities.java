package rodalies;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registro de los BlockEntity del mod (port a NeoForge 1.21.1).
 */
public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "rodalies");

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StationSignalBlockEntity>> STATION_SIGNAL_BE =
            BLOCK_ENTITIES.register("station_signal",
                    () -> BlockEntityType.Builder.of(
                            StationSignalBlockEntity::new,
                            ModBlocks.STATION_SIGNAL.get(),
                            ModBlocks.STATION_SIGNAL_PLAIN.get())
                        .build(null));

    // BlockEntity compartido por las senales ferroviarias con texto editable (LTV_END no lleva).
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RailSignBlockEntity>> RAIL_SIGN_BE =
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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TrainMaxSpeedBlockEntity>> TRAIN_MAX_SPEED_BE =
            BLOCK_ENTITIES.register("train_max_speed",
                    () -> BlockEntityType.Builder.of(
                            TrainMaxSpeedBlockEntity::new,
                            ModBlocks.TRAIN_MAX_SPEED.get())
                        .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HectometerSignBlockEntity>> HECTOMETER_BE =
            BLOCK_ENTITIES.register("hectometer_signal",
                    () -> BlockEntityType.Builder.of(
                            HectometerSignBlockEntity::new,
                            ModBlocks.HECTOMETER_SIGNAL.get())
                        .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SlidingTrainDoorBlockEntity>> SLIDING_TRAIN_DOOR_BE =
            BLOCK_ENTITIES.register("sliding_train_door",
                    () -> BlockEntityType.Builder.of(
                            SlidingTrainDoorBlockEntity::new,
                            ModBlocks.SLIDING_TRAIN_DOOR.get(),
                            ModBlocks.SLIDING_TRAIN_DOOR_BACK.get(),
                            ModBlocks.WHITE_SLIDING_TRAIN_DOOR.get(),
                            ModBlocks.WHITE_SLIDING_TRAIN_DOOR_BACK.get())
                        .build(null));
}
