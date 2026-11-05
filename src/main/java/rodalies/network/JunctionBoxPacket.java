package rodalies.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import rodalies.JunctionBoxBlock;

import java.util.function.Supplier;

/**
 * Paquete cliente -> servidor: un boton del menu de la caja de conexiones (centro, 1 px o hasta el tope
 * hacia una direccion, o guardar la posicion actual por defecto). El servidor calcula el nuevo
 * desplazamiento a partir del estado actual.
 */
public class JunctionBoxPacket {

    private final BlockPos pos;
    private final int action;
    private final Direction dir;

    public JunctionBoxPacket(BlockPos pos, int action, Direction dir) {
        this.pos = pos;
        this.action = action;
        this.dir = dir;
    }

    public JunctionBoxPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.action = buf.readByte();
        this.dir = buf.readEnum(Direction.class);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeByte(action);
        buf.writeEnum(dir);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender == null) {
                return;
            }
            Level level = sender.level();
            if (!level.isLoaded(pos) || sender.distanceToSqr(Vec3.atCenterOf(pos)) > 64.0) {
                return;
            }
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof JunctionBoxBlock)) {
                return;
            }
            if (action == JunctionBoxBlock.ACTION_SET_DEFAULT) {
                JunctionBoxBlock.saveDefault(sender, state);
            } else {
                level.setBlock(pos, JunctionBoxBlock.applyAction(state, action, dir), Block.UPDATE_ALL);
            }
        });
        ctx.setPacketHandled(true);
    }
}
