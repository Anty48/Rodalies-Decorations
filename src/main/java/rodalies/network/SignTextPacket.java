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
import rodalies.EditableSign;

/**
 * Paquete cliente -> servidor generico para los carteles con texto editable que implementan
 * {@link EditableSign} (bloque de velocidad maxima, hectometro...). Guarda una linea (slot) de texto.
 */
public record SignTextPacket(BlockPos pos, String text, int slot) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SignTextPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("rodalies", "sign_text"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SignTextPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SignTextPacket::pos,
            ByteBufCodecs.stringUtf8(256), SignTextPacket::text,
            ByteBufCodecs.VAR_INT, SignTextPacket::slot,
            SignTextPacket::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SignTextPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sender)) {
                return;
            }
            Level level = sender.level();
            if (!level.isLoaded(packet.pos())) {
                return;
            }
            // Seguridad basica: el jugador tiene que estar cerca del cartel que edita.
            if (sender.distanceToSqr(Vec3.atCenterOf(packet.pos())) > 64.0) {
                return;
            }
            BlockEntity be = level.getBlockEntity(packet.pos());
            if (be instanceof EditableSign sign) {
                sign.setLine(packet.slot(), packet.text());
            }
        });
    }
}
