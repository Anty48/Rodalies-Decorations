package rodalies;

import net.minecraft.core.BlockPos;

/**
 * Contrato comun de los BlockEntity con texto editable que usan la pantalla generica
 * ({@code SignPreviewEditScreen}) y el paquete de red {@code SignTextPacket}. Cada slot es una linea:
 * los carteles de una linea solo usan el slot 0; el hectometro usa 0 (fila de arriba) y 1 (abajo).
 */
public interface EditableSign {

    /** Guarda el texto de una linea (llamar en el servidor; el BlockEntity sincroniza al cliente). */
    void setLine(int slot, String text);

    /** Posicion del BlockEntity (lo aporta {@link net.minecraft.world.level.block.entity.BlockEntity}). */
    BlockPos getBlockPos();
}
