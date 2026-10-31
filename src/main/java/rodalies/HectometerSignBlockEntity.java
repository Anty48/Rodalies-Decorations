package rodalies;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Guarda las dos filas de texto de la senal de hectometro ({@link HectometerSignBlock}).
 *
 * Port a 1.21: guardado/carga con {@link HolderLookup.Provider}; {@code load} -> {@code loadAdditional}.
 */
public class HectometerSignBlockEntity extends BlockEntity implements EditableSign {

    private String top = "";
    private String bottom = "";

    public HectometerSignBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HECTOMETER_BE.get(), pos, state);
    }

    public String getTop() {
        return top;
    }

    public String getBottom() {
        return bottom;
    }

    @Override
    public void setLine(int slot, String text) {
        String v = text == null ? "" : text;
        if (slot == 1) {
            this.bottom = v;
        } else {
            this.top = v;
        }
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Top", top);
        tag.putString("Bottom", bottom);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.top = tag.getString("Top");
        this.bottom = tag.getString("Bottom");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            loadAdditional(tag, registries);
        }
    }
}
