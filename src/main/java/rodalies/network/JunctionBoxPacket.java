package rodalies.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import rodalies.JunctionBoxBlock;

/**
 * Paquete cliente -> servidor: un boton del menu de la caja de conexiones (centro, 1 px o hasta el tope
 * hacia una direccion, o guardar la posicion actual por defecto). El servidor calcula el nuevo
 * desplazamiento a partir del estado actual.
 */
public record JunctionBoxPacket(BlockPos pos, int action, Direction dir) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<JunctionBoxPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("rodalies", "junction_box"));

    public static final StreamCodec<RegistryFriendlyByteBuf, JunctionBoxPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, JunctionBoxPacket::pos,
            ByteBufCodecs.VAR_INT, JunctionBoxPacket::action,
            Direction.STREAM_CODEC, JunctionBoxPacket::dir,
            JunctionBoxPacket::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(JunctionBoxPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sender)) {
                return;
            }
            Level level = sender.level();
            BlockPos pos = packet.pos();
            if (!level.isLoaded(pos) || sender.distanceToSqr(Vec3.atCenterOf(pos)) > 64.0) {
                return;
            }
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof JunctionBoxBlock)) {
                return;
            }
            if (packet.action() == JunctionBoxBlock.ACTION_SET_DEFAULT) {
                JunctionBoxBlock.saveDefault(sender, state);
            } else {
                level.setBlock(pos, JunctionBoxBlock.applyAction(state, packet.action(), packet.dir()), Block.UPDATE_ALL);
            }
        });
    }
}
