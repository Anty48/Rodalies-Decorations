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
import rodalies.RailSignBlock;
import rodalies.RailSignBlockEntity;
import rodalies.RailSignType;
import rodalies.SpeedLimitBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Dibuja el texto de las señales ferroviarias sobre su panel. Cada tipo de señal tiene su propia
 * geometria (posicion del panel, cara(s) donde va el texto, color). Todas las medidas de abajo van
 * en pixeles del modelo (1 bloque = 16 px), relativas a la celda MIDDLE (la que dibuja el modelo).
 * Si al probar en runClient el texto queda descolocado o de mal tamaño, ajusta las constantes del
 * bloque correspondiente. El poste esta centrado en x=8, z=8: sirve de referencia para centrar.
 */
public class RailSignRenderer implements BlockEntityRenderer<RailSignBlockEntity> {

    private static final int FULL_BRIGHT = LightTexture.FULL_BRIGHT; // texto siempre legible
    private static final float Z_OFFSET = 0.02f;                     // separa el texto del panel (z-fighting)

    private static final int BLACK = 0xFF000000;
    // Blanco algo apagado para el numero de via: el blanco puro a plena luz "brillaba" demasiado.
    private static final int VIA_WHITE = 0xFFD8D8D8;

    // --- Cartel de numero de via (panel colgado que mira a ±X, texto por ambos lados) ---
    private static final float VIA_CY = 26.5f;   // centro vertical del panel (panel en y22..31)
    private static final float VIA_CZ = 13.5f;   // centro (eje Z) del panel colgante
    private static final float VIA_FACE_E = 9f;  // cara este del panel
    private static final float VIA_FACE_W = 7f;  // cara oeste del panel
    private static final float VIA_MAX_W = 8f;   // ancho maximo del texto (~90% del panel de 9px)
    private static final float VIA_MAX_H = 6.5f; // alto maximo del texto (~70% del panel de 9px)
    // Version DOBLE: dos paneles a y22..31 (centro 26.5, misma altura que el sencillo); el delantero a
    // +Z (cz 13.5) y el trasero a -Z (cz 2.5). El delantero usa el texto principal (slot 0) y el
    // trasero el texto 2 (slot 1).
    private static final float VIA_DBL_CY = 26.5f;
    private static final float VIA_DBL_CZ_FRONT = 13.5f;
    private static final float VIA_DBL_CZ_BACK = 2.5f;

    // --- Rombos de velocidad (Speed_limit y LTV): cara frontal norte, texto horizontal centrado ---
    // Base en el poste (x=8) con medio pixel a la derecha del observador (-X) y 1px abajo (-Y).
    private static final float DIAMOND_CX = 7.5f;
    private static final float DIAMOND_CY = 25.614f;  // centro vertical de la LTV (subido 0.25px)
    private static final float SPEED_CY = 25.614f;    // velocidad normal (bajada 0.25px respecto a antes)
    private static final float DIAMOND_FACE_Z = 6f;  // cara frontal (norte)
    // En la triple la señal central se movio 0.6px hacia atras (evita z-fighting): su texto tambien.
    private static final float DIAMOND_FACE_Z_BACK = 6.6f;
    // Reducidos un 15% respecto al panel: con 2 digitos el texto se veia demasiado grande.
    private static final float DIAMOND_MAX_W = 9.35f;
    private static final float DIAMOND_MAX_H = 6.8f;
    private static final float DIAMOND_TEXT_ROT = 0f; // 45/-45 si se quisiera el texto inclinado

    // --- Cartel normal (cuadrado blanco, hasta 2 lineas): base en el poste + 0.5px derecha / 1px abajo ---
    private static final float NORMAL_CX = 7.5f;
    private static final float NORMAL_CY = 24.5f;
    private static final float NORMAL_FACE_Z = 6f;
    private static final float NORMAL_MAX_W = 10.5f;
    private static final float NORMAL_MAX_H = 9f;

    private final Font font;

    public RailSignRenderer(BlockEntityRendererProvider.Context ctx) {
        this.font = ctx.getFont();
    }

    @Override
    public void render(RailSignBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        String text = be.getText();
        RailSignType type = be.getSignType();
        // Estado de apilado de la velocidad (double/triple llevan mas textos; ltv_end abajo no tiene).
        SpeedLimitBlock.Stack stack = type == RailSignType.SPEED_LIMIT
                && be.getBlockState().hasProperty(SpeedLimitBlock.STACK)
                ? be.getBlockState().getValue(SpeedLimitBlock.STACK) : SpeedLimitBlock.Stack.NONE;
        // Cartel de via doble: tambien lleva un segundo texto (panel trasero) en text2.
        boolean viaDoubled = type == RailSignType.PLATFORM_NUMBER
                && be.getBlockState().hasProperty(rodalies.PlatformNumberSignBlock.DOUBLED)
                && be.getBlockState().getValue(rodalies.PlatformNumberSignBlock.DOUBLED);
        boolean hasExtra = stack == SpeedLimitBlock.Stack.DOUBLE || stack == SpeedLimitBlock.Stack.TRIPLE
                || (viaDoubled && be.getText2() != null && !be.getText2().isEmpty());
        boolean hasMain = text != null && !text.isEmpty();
        if (!hasMain && !hasExtra) {
            return; // nada que dibujar
        }
        Direction facing = be.getBlockState().getValue(RailSignBlock.FACING);

        pose.pushPose();
        // Orientar como la blockstate (north=0, east=90, south=180, west=270).
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-blockstateY(facing)));
        pose.translate(-0.5, -0.5, -0.5);

        switch (type) {
            case PLATFORM_NUMBER -> {
                // Numero de via: blanco apagado y con luz natural (no emisivo) para que no "brille".
                if (viaDoubled) {
                    // Panel delantero (+Z): texto principal. Panel trasero (-Z): texto 2.
                    if (hasMain) {
                        drawViaPanel(pose, buffer, firstLine(text), VIA_DBL_CY, VIA_DBL_CZ_FRONT, packedLight);
                    }
                    String back = be.getText2();
                    if (back != null && !back.isEmpty()) {
                        drawViaPanel(pose, buffer, firstLine(back), VIA_DBL_CY, VIA_DBL_CZ_BACK, packedLight);
                    }
                } else if (hasMain) {
                    drawViaPanel(pose, buffer, firstLine(text), VIA_CY, VIA_CZ, packedLight);
                }
            }
            case SPEED_LIMIT -> {
                if (stack == SpeedLimitBlock.Stack.TRIPLE) {
                    // Triple: la de arriba sube 1px respecto a la normal; desde esa referencia la
                    // central va -17 (movida 0.6 atras por z-fighting) y la inferior -33.
                    float triTop = SPEED_CY + 1f;
                    drawExtraSpeed(pose, buffer, text, triTop, DIAMOND_FACE_Z);
                    drawExtraSpeed(pose, buffer, be.getText2(), triTop - 17f, DIAMOND_FACE_Z_BACK);
                    drawExtraSpeed(pose, buffer, be.getText3(), triTop - 33f, DIAMOND_FACE_Z);
                } else {
                    if (hasMain) {
                        drawOnNorthFace(pose, buffer, List.of(firstLine(text)), DIAMOND_CX, SPEED_CY,
                                DIAMOND_FACE_Z, BLACK, FULL_BRIGHT, DIAMOND_MAX_W, DIAMOND_MAX_H, DIAMOND_TEXT_ROT);
                    }
                    if (stack == SpeedLimitBlock.Stack.DOUBLE) {
                        // Segunda velocidad: 22px por debajo (editable aparte).
                        drawExtraSpeed(pose, buffer, be.getText2(), SPEED_CY - 22f, DIAMOND_FACE_Z);
                    }
                }
            }
            case LTV -> {
                List<String> lines = List.of(firstLine(text));
                drawOnNorthFace(pose, buffer, lines, DIAMOND_CX, DIAMOND_CY, DIAMOND_FACE_Z, BLACK, FULL_BRIGHT,
                        DIAMOND_MAX_W, DIAMOND_MAX_H, DIAMOND_TEXT_ROT);
            }
            case NORMAL -> {
                List<String> lines = splitLines(text, 2);
                drawOnNorthFace(pose, buffer, lines, NORMAL_CX, NORMAL_CY, NORMAL_FACE_Z, BLACK, FULL_BRIGHT,
                        NORMAL_MAX_W, NORMAL_MAX_H, 0f);
            }
            default -> { /* LTV_END: sin texto */ }
        }

        pose.popPose();
    }

    /** Dibuja una señal de velocidad apilada (una linea, estilo rombo) si el texto no esta vacio. */
    private void drawExtraSpeed(PoseStack pose, MultiBufferSource buffer, String t, float cy, float faceZ) {
        if (t == null || t.isEmpty()) {
            return;
        }
        drawOnNorthFace(pose, buffer, List.of(firstLine(t)), DIAMOND_CX, cy, faceZ,
                BLACK, FULL_BRIGHT, DIAMOND_MAX_W, DIAMOND_MAX_H, DIAMOND_TEXT_ROT);
    }

    /** Texto en la cara frontal (norte, -Z). El texto horizontal se ve recto de frente. */
    private void drawOnNorthFace(PoseStack pose, MultiBufferSource buffer, List<String> lines,
                                 float cx, float cy, float faceZ, int color, int light,
                                 float maxWpx, float maxHpx, float textRot) {
        pose.pushPose();
        pose.translate(cx / 16f, cy / 16f, faceZ / 16f);
        pose.mulPose(Axis.YP.rotationDegrees(180f)); // la fuente mira a +Z; la giramos para mirar al norte
        pose.translate(0, 0, Z_OFFSET);
        if (textRot != 0f) {
            pose.mulPose(Axis.ZP.rotationDegrees(textRot));
        }
        drawLinesCentered(pose, buffer, lines, color, light, maxWpx, maxHpx);
        pose.popPose();
    }

    /** Dibuja un numero de via en ambas caras (±X) de un panel colgante centrado en cy/cz. */
    private void drawViaPanel(PoseStack pose, MultiBufferSource buffer, String number,
                             float cy, float cz, int packedLight) {
        List<String> lines = List.of(number);
        drawOnXFace(pose, buffer, lines, cy, cz, VIA_FACE_E, true, VIA_WHITE, packedLight, VIA_MAX_W, VIA_MAX_H);
        drawOnXFace(pose, buffer, lines, cy, cz, VIA_FACE_W, false, VIA_WHITE, packedLight, VIA_MAX_W, VIA_MAX_H);
    }

    /** Texto en una cara lateral (±X): panel del cartel de via, visible por ambos lados. */
    private void drawOnXFace(PoseStack pose, MultiBufferSource buffer, List<String> lines,
                             float cy, float cz, float faceX, boolean east, int color, int light,
                             float maxWpx, float maxHpx) {
        pose.pushPose();
        pose.translate(faceX / 16f, cy / 16f, cz / 16f);
        // La fuente mira a +Z; girar para que mire a +X (este) o -X (oeste).
        pose.mulPose(Axis.YP.rotationDegrees(east ? 90f : -90f));
        pose.translate(0, 0, Z_OFFSET);
        drawLinesCentered(pose, buffer, lines, color, light, maxWpx, maxHpx);
        pose.popPose();
    }

    /**
     * Dibuja las lineas centradas (horizontal y vertical) escalando para que el bloque de texto
     * quepa en maxWpx × maxHpx. Con pocas letras el texto sale grande (limitado por el alto).
     */
    private void drawLinesCentered(PoseStack pose, MultiBufferSource buffer, List<String> lines,
                                   int color, int light, float maxWpx, float maxHpx) {
        int n = lines.size();
        if (n == 0) {
            return;
        }
        float lh = font.lineHeight;
        float widest = 1f;
        for (String l : lines) {
            widest = Math.max(widest, font.width(l));
        }
        float totalH = n * lh;
        float maxW = maxWpx / 16f;
        float maxH = maxHpx / 16f;
        float scale = Math.min(maxW / widest, maxH / totalH);

        pose.pushPose();
        // -scale en Y: la fuente crece hacia abajo; en el mundo +Y es arriba.
        pose.scale(scale, -scale, scale);
        Matrix4f matrix = pose.last().pose();
        float yStart = -totalH / 2f;
        for (int i = 0; i < n; i++) {
            String l = lines.get(i);
            float x = -font.width(l) / 2f;
            float y = yStart + i * lh;
            font.drawInBatch(l, x, y, color, false, matrix, buffer,
                    Font.DisplayMode.POLYGON_OFFSET, 0, light);
        }
        pose.popPose();
    }

    private static String firstLine(String text) {
        int nl = text.indexOf('\n');
        return nl >= 0 ? text.substring(0, nl) : text;
    }

    /** Parte el texto en como mucho 'max' lineas por '\n', descartando lineas vacias al final. */
    private static List<String> splitLines(String text, int max) {
        String[] raw = text.split("\n", -1);
        List<String> out = new ArrayList<>();
        for (int i = 0; i < raw.length && out.size() < max; i++) {
            out.add(raw[i]);
        }
        while (out.size() > 1 && out.get(out.size() - 1).isEmpty()) {
            out.remove(out.size() - 1);
        }
        return out;
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
