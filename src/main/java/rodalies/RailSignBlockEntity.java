package rodalies;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Guarda el texto editable de una senal ferroviaria ({@link RailSignBlock}). Ver documentacion en Forge.
 *
 * Port a 1.21: guardado/carga con {@link HolderLookup.Provider}; {@code load} -> {@code loadAdditional}.
 */
public class RailSignBlockEntity extends BlockEntity {

    private String text = "";
    private String text2 = "";
    private String text3 = "";

    public RailSignBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RAIL_SIGN_BE.get(), pos, state);
    }

    public String getText() {
        return text;
    }

    public String getText2() {
        return text2;
    }

    public String getText3() {
        return text3;
    }

    public RailSignType getSignType() {
        Block block = getBlockState().getBlock();
        if (block instanceof RailSignBlock b) {
            return b.getSignType();
        }
        return block instanceof RailSignWallBlock w ? w.getSignType() : RailSignType.NORMAL;
    }

    public void setText(String newText) {
        this.text = newText == null ? "" : newText;
        sync();
    }

    public void setText2(String newText) {
        this.text2 = newText == null ? "" : newText;
        sync();
    }

    public void setText3(String newText) {
        this.text3 = newText == null ? "" : newText;
        sync();
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Text", text);
        tag.putString("Text2", text2);
        tag.putString("Text3", text3);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.text = tag.getString("Text");
        this.text2 = tag.getString("Text2");
        this.text3 = tag.getString("Text3");
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
