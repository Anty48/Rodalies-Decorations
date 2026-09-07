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
 * Guarda el texto (hasta 4 caracteres) del bloque de velocidad maxima ({@link TrainMaxSpeedBlock}) y
 * lo sincroniza al cliente para que {@code TrainMaxSpeedRenderer} lo dibuje centrado en el rombo.
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

    /** El texto se dibuja delante de la cara: amplia un poco la caja para que no se recorte. */
    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos()).inflate(0.5);
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
