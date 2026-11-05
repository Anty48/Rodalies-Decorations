package rodalies.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registro de red del mod (port a NeoForge 1.21.1).
 *
 * En NeoForge los paquetes son "payloads" ({@link net.minecraft.network.protocol.common.custom.CustomPacketPayload})
 * y se registran en {@link RegisterPayloadHandlersEvent}. Cada payload lleva su {@code Type} y su
 * {@code StreamCodec}. Todos van cliente -> servidor (playToServer).
 */
public class ModNetwork {

    private static final String PROTOCOL = "1";

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL);
        registrar.playToServer(StationSignUpdatePacket.TYPE, StationSignUpdatePacket.STREAM_CODEC,
                StationSignUpdatePacket::handle);
        registrar.playToServer(RailSignUpdatePacket.TYPE, RailSignUpdatePacket.STREAM_CODEC,
                RailSignUpdatePacket::handle);
        registrar.playToServer(SignTextPacket.TYPE, SignTextPacket.STREAM_CODEC,
                SignTextPacket::handle);
        registrar.playToServer(JunctionBoxPacket.TYPE, JunctionBoxPacket.STREAM_CODEC,
                JunctionBoxPacket::handle);
    }
}
