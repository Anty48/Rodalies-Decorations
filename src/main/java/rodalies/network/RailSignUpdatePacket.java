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
import rodalies.RailSignBlockEntity;

/**
 * Paquete cliente -> servidor: guarda el texto que ha escrito el jugador en una senal ferroviaria.
 * slot 0 = senal principal; 1 = senal inferior (velocidad doble); 2 = tercera.
 */
public record RailSignUpdatePacket(BlockPos pos, String text, int slot) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RailSignUpdatePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("rodalies", "rail_sign_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RailSignUpdatePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, RailSignUpdatePacket::pos,
            ByteBufCodecs.stringUtf8(256), RailSignUpdatePacket::text,
            ByteBufCodecs.VAR_INT, RailSignUpdatePacket::slot,
            RailSignUpdatePacket::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RailSignUpdatePacket packet, IPayloadContext ctx) {
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
            if (be instanceof RailSignBlockEntity sign) {
                switch (packet.slot()) {
                    case 1 -> sign.setText2(packet.text());
                    case 2 -> sign.setText3(packet.text());
                    default -> sign.setText(packet.text());
                }
            }
        });
    }
}
