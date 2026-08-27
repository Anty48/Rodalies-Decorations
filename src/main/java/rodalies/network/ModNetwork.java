package rodalies.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Canal de red del mod. De momento solo lleva el paquete de actualizacion del texto del cartel.
 */
public class ModNetwork {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("rodalies", "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals);

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(StationSignUpdatePacket.class, id++)
                .encoder(StationSignUpdatePacket::encode)
                .decoder(StationSignUpdatePacket::new)
                .consumerMainThread(StationSignUpdatePacket::handle)
                .add();
        CHANNEL.messageBuilder(RailSignUpdatePacket.class, id++)
                .encoder(RailSignUpdatePacket::encode)
                .decoder(RailSignUpdatePacket::new)
                .consumerMainThread(RailSignUpdatePacket::handle)
                .add();
    }
}
