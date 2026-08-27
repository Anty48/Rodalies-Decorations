package rodalies;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Guarda el texto editable de una señal ferroviaria ({@link RailSignBlock}) y lo sincroniza al
 * cliente para que {@code RailSignRenderer} lo dibuje sobre la señal. En señales de 2 lineas el
 * texto lleva un '\n' como separador.
 */
public class RailSignBlockEntity extends BlockEntity {

    private String text = "";

    public RailSignBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RAIL_SIGN_BE.get(), pos, state);
    }

    public String getText() {
        return text;
    }

    /** El tipo de señal (de que bloque cuelga este BE), para saber limites de texto y estilo. */
    public RailSignType getSignType() {
        return getBlockState().getBlock() instanceof RailSignBlock b ? b.getSignType() : RailSignType.NORMAL;
    }

    /** Cambia el texto y fuerza guardado + sincronizacion a los clientes (llamar en el servidor). */
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
