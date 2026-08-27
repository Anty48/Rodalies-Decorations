package rodalies.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import rodalies.RailSignBlockEntity;

import java.util.function.Supplier;

/**
 * Paquete cliente -> servidor: guarda el texto que ha escrito el jugador en una señal ferroviaria.
 */
public class RailSignUpdatePacket {

    private final BlockPos pos;
    private final String text;

    public RailSignUpdatePacket(BlockPos pos, String text) {
        this.pos = pos;
        this.text = text;
    }

    public RailSignUpdatePacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.text = buf.readUtf(256);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(text, 256);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender == null) {
                return;
            }
            Level level = sender.level();
            if (!level.isLoaded(pos)) {
                return;
            }
            // Seguridad basica: el jugador tiene que estar cerca de la señal que edita.
            if (sender.distanceToSqr(Vec3.atCenterOf(pos)) > 64.0) {
                return;
            }
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RailSignBlockEntity sign) {
                sign.setText(text);
            }
        });
        ctx.setPacketHandled(true);
    }
}
