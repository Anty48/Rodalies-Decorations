package rodalies.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import rodalies.StationSignalBlock;
import rodalies.StationSignalBlockEntity;

/**
 * Dibuja el nombre de la estacion, en grande y centrado, sobre el panel del cartel.
 * Se pinta en las dos caras (frontal y trasera) con el mismo texto.
 *
 * Las constantes de abajo controlan donde y como de grande sale el texto: si al probar en
 * runClient el texto queda desplazado o pequenno, ajusta estos valores.
 */
public class StationSignalRenderer implements BlockEntityRenderer<StationSignalBlockEntity> {

    // --- Ajustes del texto (en unidades de bloque; 1 bloque = 16 px del modelo) ---
    // El renderer cuelga del BlockEntity de la celda MID. El panel (relativo a MID) va de
    // x=-16..32 (48px de ancho) y=23..32 px. El logo de Rodalies ocupa la IZQUIERDA de CADA cara:
    // en la textura son 14px de 128 -> ~5.25px proyectados en el panel. Como el logo esta a la
    // izquierda en AMBAS caras (no es espejo), la zona libre queda en lados opuestos del eje X del
    // mundo, asi que la cara frontal y la trasera usan centros de texto DISTINTOS (espejados).
    private static final float PANEL_MIN_X = -16f;          // borde izquierdo del panel en px (modelo)
    private static final float PANEL_MAX_X = 32f;           // borde derecho del panel en px
    // El panel mapea la textura x[0..101] sobre sus 48px -> el logo (textura x0..14) ocupa 14/101.
    private static final float LOGO_W = 14f / 101f * 48f;   // ancho del logo proyectado en el panel (~6.65px)
    private static final float MARGIN_LOGO = 2f;            // hueco entre el logo y el texto, en px
    private static final float MARGIN_EDGE = 2f;            // margen en el lado sin logo, en px

    // El logo esta a la izquierda de CADA cara vista, pero como no es espejo eso cae en lados
    // opuestos del eje X del mundo. Empiricamente (ver captura): en la cara FRONTAL el logo esta
    // en el lado +X, en la TRASERA en el lado -X. Cada cara desplaza el texto al lado contrario.
    // Cara FRONTAL: logo en +X -> banda de texto hacia -X.
    private static final float FRONT_LEFT = PANEL_MIN_X + MARGIN_EDGE;
    private static final float FRONT_RIGHT = PANEL_MAX_X - LOGO_W - MARGIN_LOGO;
    private static final float CENTER_FRONT_X = ((FRONT_LEFT + FRONT_RIGHT) / 2f) / 16f;
    // Cara TRASERA: logo en -X -> banda de texto espejada, hacia +X.
    private static final float BACK_LEFT = PANEL_MIN_X + LOGO_W + MARGIN_LOGO;
    private static final float BACK_RIGHT = PANEL_MAX_X - MARGIN_EDGE;
    private static final float CENTER_BACK_X = ((BACK_LEFT + BACK_RIGHT) / 2f) / 16f;

    private static final float PANEL_CENTER_Y = 27.5f / 16f;
    private static final float FRONT_Z = 7f / 16f;          // cara norte (frontal)
    private static final float BACK_Z = 9f / 16f;           // cara sur (trasera)
    private static final float Z_OFFSET = 0.01f;            // separa el texto del panel (evita z-fighting)

    private static final float BASE_SCALE = 0.055f;         // tamanno base de la letra (mas alto = mas grande)
    private static final float MAX_WIDTH = (FRONT_RIGHT - FRONT_LEFT) / 16f; // ancho util (igual en ambas caras)
    private static final int COLOR = 0xFF000000;           // color del texto (ARGB): negro sobre fondo crema
    private static final int LIGHT = LightTexture.FULL_BRIGHT; // texto siempre legible (como cartel retroiluminado)

    private final Font font;

    public StationSignalRenderer(BlockEntityRendererProvider.Context ctx) {
        this.font = ctx.getFont();
    }

    @Override
    public void render(StationSignalBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        String text = be.getText();
        if (text == null || text.isEmpty()) {
            return;
        }

        Direction facing = be.getBlockState().getValue(StationSignalBlock.FACING);

        pose.pushPose();
        // Orientar como en la blockstate (north=0, east=90, south=180, west=270 en sentido horario).
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-blockstateY(facing)));
        pose.translate(-0.5, -0.5, -0.5);

        drawFace(text, pose, buffer, true);   // cara frontal (norte)
        drawFace(text, pose, buffer, false);  // cara trasera (sur)

        pose.popPose();
    }

    private void drawFace(String text, PoseStack pose, MultiBufferSource buffer, boolean front) {
        pose.pushPose();

        float z = front ? FRONT_Z : BACK_Z;
        float centerX = front ? CENTER_FRONT_X : CENTER_BACK_X;
        pose.translate(centerX, PANEL_CENTER_Y, z);
        if (front) {
            // La cara norte mira a -Z; la fuente por defecto mira a +Z, asi que la giramos 180.
            pose.mulPose(Axis.YP.rotationDegrees(180f));
        }
        pose.translate(0, 0, Z_OFFSET); // hacia afuera de la cara

        float width = font.width(text);
        float scale = BASE_SCALE;
        if (width > 0) {
            scale = Math.min(BASE_SCALE, MAX_WIDTH / width); // encoger para caber en el panel
        }
        // -scale en Y: la fuente crece hacia abajo; en el mundo +Y es arriba.
        pose.scale(scale, -scale, scale);

        Matrix4f matrix = pose.last().pose();
        float x = -width / 2f;                 // centrado horizontal
        float y = -font.lineHeight / 2f;       // centrado vertical
        font.drawInBatch(text, x, y, COLOR, false, matrix, buffer,
                Font.DisplayMode.POLYGON_OFFSET, 0, LIGHT);

        pose.popPose();
    }

    private static float blockstateY(Direction facing) {
        return switch (facing) {
            case EAST -> 90f;
            case SOUTH -> 180f;
            case WEST -> 270f;
            default -> 0f; // NORTH
        };
    }
}
