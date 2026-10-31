package rodalies;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Guarda el texto (hasta 4 caracteres) del bloque de velocidad maxima ({@link TrainMaxSpeedBlock}) y
 * lo sincroniza al cliente para que {@code TrainMaxSpeedRenderer} lo dibuje centrado en el rombo.
 *
 * Port a 1.21: los metodos de guardado/carga llevan {@link HolderLookup.Provider} y {@code load} pasa a
 * ser {@code loadAdditional}.
 */
public class TrainMaxSpeedBlockEntity extends BlockEntity implements EditableSign {

    private String text = "";

    public TrainMaxSpeedBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRAIN_MAX_SPEED_BE.get(), pos, state);
    }

    public String getText() {
        return text;
    }

    @Override
    public void setLine(int slot, String newText) {
        // Solo hay una linea: se ignora el slot.
        this.text = newText == null ? "" : newText;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Text", text);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.text = tag.getString("Text");
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
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt,
                             HolderLookup.Provider registries) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            loadAdditional(tag, registries);
        }
    }
}
