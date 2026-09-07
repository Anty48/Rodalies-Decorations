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
 * Guarda las dos filas de texto de la señal de hectometro ({@link HectometerSignBlock}) y las
 * sincroniza al cliente para que {@code HectometerSignRenderer} las dibuje: fila 0 arriba (grande),
 * fila 1 abajo (pequeña). Vive en la celda BOTTOM (la que dibuja el modelo completo).
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

    /** El modelo (y el texto) sobresale una celda hacia arriba: amplia la caja para que no se recorte. */
    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos()).inflate(1.0, 2.0, 1.0);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Top", top);
        tag.putString("Bottom", bottom);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.top = tag.getString("Top");
        this.bottom = tag.getString("Bottom");
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
