package rodalies;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Guarda el texto (una linea) del cartel de estacion y lo sincroniza al cliente
 * para que el renderer lo dibuje grande sobre el panel.
 */
public class StationSignalBlockEntity extends BlockEntity {

    private String text = "";

    public StationSignalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STATION_SIGNAL_BE.get(), pos, state);
    }

    public String getText() {
        return text;
    }

    /**
     * Caja de render ampliada: el panel del cartel sobresale de la celda del BlockEntity, asi que
     * sin ampliar la caja el texto "desaparece" cuando la celda base sale del frustum al moverte.
     */
    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos()).inflate(2.0);
    }

    /** Cambia el texto y fuerza el guardado + sincronizacion a los clientes (llamar en el servidor). */
    public void setText(String newText) {
        this.text = newText == null ? "" : newText;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Text", text);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.text = tag.getString("Text");
    }

    // --- Sincronizacion servidor -> cliente ---

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            load(tag);
        }
    }
}
