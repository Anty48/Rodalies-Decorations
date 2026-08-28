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
 * Guarda el texto editable de una señal ferroviaria ({@link RailSignBlock}) y lo sincroniza al
 * cliente para que {@code RailSignRenderer} lo dibuje sobre la señal. En señales de 2 lineas el
 * texto lleva un '\n' como separador.
 */
public class RailSignBlockEntity extends BlockEntity {

    private String text = "";
    // Textos extra de las velocidades apiladas: text2 = señal central (doble/triple), text3 = inferior
    // (solo en la triple). Vacios en el resto de señales.
    private String text2 = "";
    private String text3 = "";

    public RailSignBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RAIL_SIGN_BE.get(), pos, state);
    }

    public String getText() {
        return text;
    }

    /** Texto de la señal central de una velocidad doble/triple (vacio en señales normales). */
    public String getText2() {
        return text2;
    }

    /** Texto de la señal inferior de una velocidad triple (vacio en el resto). */
    public String getText3() {
        return text3;
    }

    /**
     * Caja de render ampliada. El BlockEntity vive en la celda MIDDLE, pero el texto se dibuja
     * arriba (celda TOP) y la señal sobresale de su celda. Sin ampliar la caja, Minecraft deja de
     * llamar al renderer en cuanto la celda MIDDLE sale del frustum, y el texto "desaparece" segun
     * como mires/te muevas. Ampliamos 2 bloques en todas direcciones para cubrir toda la señal.
     */
    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos()).inflate(2.0);
    }

    /** El tipo de señal (de que bloque cuelga este BE), para saber limites de texto y estilo. */
    public RailSignType getSignType() {
        return getBlockState().getBlock() instanceof RailSignBlock b ? b.getSignType() : RailSignType.NORMAL;
    }

    /** Cambia el texto y fuerza guardado + sincronizacion a los clientes (llamar en el servidor). */
    public void setText(String newText) {
        this.text = newText == null ? "" : newText;
        sync();
    }

    /** Cambia el texto de la señal central (velocidad doble/triple) y sincroniza. */
    public void setText2(String newText) {
        this.text2 = newText == null ? "" : newText;
        sync();
    }

    /** Cambia el texto de la señal inferior (velocidad triple) y sincroniza. */
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
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Text", text);
        tag.putString("Text2", text2);
        tag.putString("Text3", text3);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.text = tag.getString("Text");
        this.text2 = tag.getString("Text2");
        this.text3 = tag.getString("Text3");
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
