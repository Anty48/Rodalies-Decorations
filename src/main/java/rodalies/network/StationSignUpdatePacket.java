package rodalies.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import rodalies.StationSignalBlockEntity;

/**
 * Paquete cliente -> servidor: guarda el texto que ha escrito el jugador en el cartel de estacion.
 */
public record StationSignUpdatePacket(BlockPos pos, String text) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<StationSignUpdatePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("rodalies", "station_sign_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StationSignUpdatePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, StationSignUpdatePacket::pos,
            ByteBufCodecs.stringUtf8(256), StationSignUpdatePacket::text,
            StationSignUpdatePacket::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(StationSignUpdatePacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sender)) {
                return;
            }
            Level level = sender.level();
            if (!level.isLoaded(packet.pos())) {
                return;
            }
            if (sender.distanceToSqr(Vec3.atCenterOf(packet.pos())) > 64.0) {
                return;
            }
            BlockEntity be = level.getBlockEntity(packet.pos());
            if (be instanceof StationSignalBlockEntity sign) {
                sign.setText(packet.text());
            }
        });
    }
}
