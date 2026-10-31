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
import rodalies.HectometerSignBlock;
import rodalies.HectometerSignBlockEntity;

/**
 * Dibuja las dos filas de texto de la señal de hectometro sobre la placa (cara norte). La placa vive
 * en la parte superior del modelo (y21..31 px, medidos desde la celda BOTTOM que lleva el BlockEntity).
 * Fila de arriba grande, la de abajo pequeña. Medidas en pixeles del modelo (1 bloque = 16 px); si al
 * probar el texto queda descolocado o de mal tamaño, ajusta las constantes TOP_ y BOT_.
 */
public class HectometerSignRenderer implements BlockEntityRenderer<HectometerSignBlockEntity> {

    private static final int FULL_BRIGHT = LightTexture.FULL_BRIGHT;
    private static final float Z_OFFSET = 0.02f;
    private static final int BLACK = 0xFF000000;

    private static final float CX = 8f;          // centrado en el poste (x=8)
    // La placa tiene 4 px de fondo: cara delantera (norte) en z=6 y trasera (sur) en z=10.
    private static final float FRONT_Z = 6f;
    private static final float BACK_Z = 10f;

    // Fila de arriba (grande) y fila de abajo (pequeña): centro vertical y hueco util.
    private static final float TOP_CY = 27.5f;
    private static final float TOP_MAX_W = 7.5f;
    private static final float TOP_MAX_H = 3.6f;

    private static final float BOT_CY = 23f;
    private static final float BOT_MAX_W = 8f;
    private static final float BOT_MAX_H = 3f;

    private final Font font;

    public HectometerSignRenderer(BlockEntityRendererProvider.Context ctx) {
        this.font = ctx.getFont();
    }

    @Override
    public void render(HectometerSignBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        String top = be.getTop();
        String bottom = be.getBottom();
        boolean hasTop = top != null && !top.isEmpty();
        boolean hasBottom = bottom != null && !bottom.isEmpty();
        boolean preview = SignEditPreview.isTarget(be);
        if (!hasTop && !hasBottom && !preview) {
            return;
        }
        Direction facing = be.getBlockState().getValue(HectometerSignBlock.FACING);

        pose.pushPose();
        // Orientar como la blockstate (north=0, east=90, south=180, west=270).
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-blockstateY(facing)));
        pose.translate(-0.5, -0.5, -0.5);

        // Se dibuja en las dos caras de la placa (delante y detras) para que se lea por ambos lados.
        if (hasTop || preview) {
            drawRow(pose, buffer, be, 0, top, TOP_CY, TOP_MAX_W, TOP_MAX_H, false);
            drawRow(pose, buffer, be, 0, top, TOP_CY, TOP_MAX_W, TOP_MAX_H, true);
        }
        if (hasBottom || preview) {
            drawRow(pose, buffer, be, 1, bottom, BOT_CY, BOT_MAX_W, BOT_MAX_H, false);
            drawRow(pose, buffer, be, 1, bottom, BOT_CY, BOT_MAX_W, BOT_MAX_H, true);
        }

        pose.popPose();
    }

    /**
     * Dibuja una fila centrada en (CX, cy). {@code back=false} -> cara delantera (norte, z=6);
     * {@code back=true} -> cara trasera (sur, z=10), para que se lea tambien por el otro lado.
     */
    private void drawRow(PoseStack pose, MultiBufferSource buffer, HectometerSignBlockEntity be, int slot, String line,
                         float cy, float maxWpx, float maxHpx, boolean back) {
        pose.pushPose();
        pose.translate(CX / 16f, cy / 16f, (back ? BACK_Z : FRONT_Z) / 16f);
        if (!back) {
            pose.mulPose(Axis.YP.rotationDegrees(180f)); // la fuente mira a +Z; girar para mirar al norte
        }
        pose.translate(0, 0, Z_OFFSET);

        float lh = font.lineHeight;
        float w = Math.max(1f, font.width(line));
        float scale = Math.min((maxWpx / 16f) / w, (maxHpx / 16f) / lh);

        pose.scale(scale, -scale, scale); // -Y: la fuente crece hacia abajo
        Matrix4f matrix = pose.last().pose();
        font.drawInBatch(line, -font.width(line) / 2f, -lh / 2f, BLACK, false, matrix, buffer,
                Font.DisplayMode.POLYGON_OFFSET, 0, FULL_BRIGHT);
        SignEditPreview.drawCursor(font, be, slot, 0, line, -font.width(line) / 2f, -lh / 2f, BLACK, matrix, buffer,
                FULL_BRIGHT);
        pose.popPose();
    }

    private static float blockstateY(Direction facing) {
        return switch (facing) {
            case EAST -> 90f;
            case SOUTH -> 180f;
            case WEST -> 270f;
            default -> 0f;
        };
    }
}
