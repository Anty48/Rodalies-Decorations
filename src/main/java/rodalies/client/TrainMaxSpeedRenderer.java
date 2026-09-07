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
import rodalies.TrainMaxSpeedBlock;
import rodalies.TrainMaxSpeedBlockEntity;

/**
 * Dibuja el texto (hasta 4 caracteres) del bloque de velocidad maxima, centrado en el rombo de la
 * cara frontal (norte). Medidas en pixeles del modelo (1 bloque = 16 px). Si al probar el texto
 * queda descolocado o de mal tamaño, ajusta las constantes CX/CY/MAX_W/MAX_H.
 */
public class TrainMaxSpeedRenderer implements BlockEntityRenderer<TrainMaxSpeedBlockEntity> {

    private static final float Z_OFFSET = 0.02f;
    // Gris del contorno del rombo. Aclarado un poco a peticion del usuario (antes se veia muy negro).
    private static final int TEXT_COLOR = 0xFF8E8D8C;

    // Centro del rombo en la cara frontal. Analizando la textura, el rombo esta centrado en x=8 y
    // su centro vertical cae en y=7.5 px (vertices en filas 4..12).
    private static final float CX = 8f;
    private static final float CY = 7.25f + 1f / 3f - 0.2f; // 1/3 px arriba y luego 0.2 px abajo (peticion del usuario)
    private static final float FACE_Z = 0f;      // cara norte del cubo (z=0)
    // Hueco util dentro del rombo: es pequeño, asi que el texto va bastante reducido.
    private static final float MAX_W = 6.2f;
    private static final float MAX_H = 2.6f;

    private final Font font;

    public TrainMaxSpeedRenderer(BlockEntityRendererProvider.Context ctx) {
        this.font = ctx.getFont();
    }

    @Override
    public void render(TrainMaxSpeedBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        String text = be.getText();
        if (text == null || text.isEmpty()) {
            return;
        }
        Direction facing = be.getBlockState().getValue(TrainMaxSpeedBlock.FACING);

        pose.pushPose();
        // Orientar como la blockstate (north=0, east=90, south=180, west=270).
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-blockstateY(facing)));
        pose.translate(-0.5, -0.5, -0.5);

        // Dibujar a brillo pleno para que el hex TEXT_COLOR se aplique EXACTAMENTE, sin que la luz del
        // entorno (packedLight) lo oscurezca hasta verse casi negro. Texto en la cara norte, centrado.
        pose.translate(CX / 16f, CY / 16f, FACE_Z / 16f);
        pose.mulPose(Axis.YP.rotationDegrees(180f)); // la fuente mira a +Z; girar para mirar al norte
        pose.translate(0, 0, Z_OFFSET);
        drawCentered(pose, buffer, text, TEXT_COLOR, LightTexture.FULL_BRIGHT);

        pose.popPose();
    }

    private void drawCentered(PoseStack pose, MultiBufferSource buffer, String line, int color, int light) {
        float lh = font.lineHeight;
        float w = Math.max(1f, font.width(line));
        float scale = Math.min((MAX_W / 16f) / w, (MAX_H / 16f) / lh);

        pose.pushPose();
        pose.scale(scale, -scale, scale); // -Y: la fuente crece hacia abajo
        Matrix4f matrix = pose.last().pose();
        font.drawInBatch(line, -font.width(line) / 2f, -lh / 2f, color, false, matrix, buffer,
                Font.DisplayMode.POLYGON_OFFSET, 0, light);
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
