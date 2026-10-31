package rodalies.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Matrix4f;

/**
 * Estado del cursor de la pantalla de edicion ({@link SignPreviewEditScreen}). Mientras la pantalla
 * dibuja su vista previa marca su BlockEntity de preview como "objetivo"; los renderers de texto lo
 * consultan para (1) dibujar tambien las lineas vacias de ese BE y (2) pintar el cursor en la linea
 * que se esta editando, con la misma escala y posicion que el texto. Fuera de la pantalla no hay
 * objetivo, asi que en el mundo nada cambia.
 */
public final class SignEditPreview {

    private static BlockEntity target;
    private static int slot = -1;
    private static int line = -1;
    private static int cursor;
    private static boolean visible;

    private SignEditPreview() {
    }

    static void begin(BlockEntity be, int editSlot, int editLine, int cursorPos, boolean cursorVisible) {
        target = be;
        slot = editSlot;
        line = editLine;
        cursor = cursorPos;
        visible = cursorVisible;
    }

    static void end() {
        target = null;
    }

    /** ¿Este BE es la vista previa de la pantalla de edicion? (entonces se dibujan tambien textos vacios) */
    public static boolean isTarget(BlockEntity be) {
        return be != null && be == target;
    }

    /** Linea que se esta editando dentro de ese slot del BE, o -1 si no se edita. */
    public static int focusedLine(BlockEntity be, int s) {
        return isTarget(be) && slot == s ? line : -1;
    }

    /**
     * Dibuja el cursor justo despues de dibujar una linea de texto, en el mismo espacio (ya escalado)
     * en el que se dibujo: {@code x, y} es la esquina de la linea. '_' al final, '|' en medio.
     */
    public static void drawCursor(Font font, BlockEntity be, int s, int l, String text, float x, float y,
                                  int color, Matrix4f matrix, MultiBufferSource buffer, int light) {
        if (!visible || focusedLine(be, s) != l) {
            return;
        }
        int p = Math.min(cursor, text.length());
        if (p >= text.length()) {
            font.drawInBatch("_", x + font.width(text), y, color, false, matrix, buffer,
                    Font.DisplayMode.POLYGON_OFFSET, 0, light);
        } else {
            font.drawInBatch("|", x + font.width(text.substring(0, p)) - 1f, y, color, false, matrix, buffer,
                    Font.DisplayMode.POLYGON_OFFSET, 0, light);
        }
    }
}
