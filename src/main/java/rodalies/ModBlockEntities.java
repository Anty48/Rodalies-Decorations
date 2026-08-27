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

    // BlockEntity compartido por las señales ferroviarias con texto editable (LVT_END no lleva).
    public static final RegistryObject<BlockEntityType<RailSignBlockEntity>> RAIL_SIGN_BE =
        BLOCK_ENTITIES.register("rail_sign",
            () -> BlockEntityType.Builder.of(
                    RailSignBlockEntity::new,
                    ModBlocks.PLATFORM_NUMBER_SIGNAL.get(),
                    ModBlocks.SPEED_LIMIT.get(),
                    ModBlocks.LVT.get(),
                    ModBlocks.NORMAL_SIGNAL.get())
                .build(null));
}
