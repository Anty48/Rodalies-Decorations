package rodalies.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import rodalies.SlidingTrainDoorBlock;
import rodalies.SlidingTrainDoorBlockEntity;

/**
 * Dibuja las dos hojas de la puerta corredera y las anima deslizandose. La blockstate usa un modelo
 * vacio: aqui pintamos las hojas a mano y las desplazamos segun el progreso de la animacion, para que
 * el movimiento sea suave (no un salto de estado). Solo la celda LOWER tiene BlockEntity, y dibuja las
 * dos hojas completas (2 bloques de alto). Sin backface culling (entityCutoutNoCull) para no depender
 * del sentido de giro de las caras.
 */
public class SlidingTrainDoorRenderer implements BlockEntityRenderer<SlidingTrainDoorBlockEntity> {

    private static final ResourceLocation TEX_LEFT = new ResourceLocation("rodalies", "block/door_left");
    private static final ResourceLocation TEX_RIGHT = new ResourceLocation("rodalies", "block/door_right");

    // Cuanto se abren las hojas (en unidades de bloque): a los lados y un poco hacia adelante/atras.
    private static final float SLIDE = 8f / 16f;   // desplazamiento lateral maximo
    private static final float FORWARD = 3f / 16f; // salida en Z (adelante o atras) maxima

    // Reparto de la animacion: primero sale en Z (0..FWD_PHASE del progreso), luego corre a los lados
    // (FWD_PHASE..1). Al cerrar se recorre al reves (recoge los lados y luego mete la hoja en Z).
    private static final float FWD_PHASE = 0.35f;

    public SlidingTrainDoorRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(SlidingTrainDoorBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Direction facing = be.getBlockState().getValue(SlidingTrainDoorBlock.FACING);
        // Sentido del pequeño desplazamiento en Z: adelante (-Z, hacia el frente) o atras (+Z).
        boolean forward = !(be.getBlockState().getBlock() instanceof SlidingTrainDoorBlock d) || d.isOpenForward();

        float p = be.getRenderProgress(partialTick);
        // Fase 1: salida en Z hasta completar en FWD_PHASE. Fase 2: deslizamiento lateral despues.
        float fwd = Math.min(p / FWD_PHASE, 1f) * FORWARD;
        float slide = p <= FWD_PHASE ? 0f : (p - FWD_PHASE) / (1f - FWD_PHASE) * SLIDE;
        float zShift = forward ? -fwd : fwd; // -Z = adelante, +Z = atras

        pose.pushPose();
        // Orientar como la blockstate (north=0, east=90, south=180, west=270), igual que las señales.
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-blockstateY(facing)));
        pose.translate(-0.5, -0.5, -0.5);

        // Translucido y SIN culling (como el cutout original, para no depender del sentido de giro de
        // las caras): la textura lleva alfa en la ventanilla (cristal tintado) -> se ve a traves.
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        TextureAtlasSprite spriteLeft = sprite(TEX_LEFT);
        TextureAtlasSprite spriteRight = sprite(TEX_RIGHT);

        // Hoja derecha: cerrada en x[0..0.5]; se desliza hacia -X y un poco en Z.
        addLeaf(pose, vc, 0f - slide, 0f, 0f + zShift, 0.5f - slide, 2f, 0.125f + zShift,
                spriteRight, packedLight, packedOverlay);
        // Hoja izquierda: cerrada en x[0.5..1]; se desliza hacia +X y un poco en Z.
        addLeaf(pose, vc, 0.5f + slide, 0f, 0f + zShift, 1f + slide, 2f, 0.125f + zShift,
                spriteLeft, packedLight, packedOverlay);

        pose.popPose();
    }

    private static TextureAtlasSprite sprite(ResourceLocation rl) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(rl);
    }

    /**
     * Dibuja una hoja de puerta reproduciendo el mapeo de UV de Blockbench (textura 8x32 = door_left /
     * door_right):
     *  - caras grandes (norte/sur): textura completa a lo ancho, con u0 hacia el borde +X (convencion
     *    de Minecraft) para que el pixel del borde de la textura (verde) caiga en el centro, donde se
     *    juntan las dos hojas.
     *  - laterales (este/oeste) y arriba/abajo: una columna fina del borde (u14..16) -> naranja/gris/
     *    naranja, no la textura entera aplastada.
     * Los argumentos {@code s.getU(x)} / {@code s.getV(y)} toman coordenadas en el espacio 0..16 del
     * sprite (V dividido por 4 respecto al export original, que iba hasta 64).
     */
    private static void addLeaf(PoseStack pose, VertexConsumer vc, float x0, float y0, float z0,
                                float x1, float y1, float z1, TextureAtlasSprite s, int light, int overlay) {
        Matrix4f m = pose.last().pose();
        Matrix3f n = pose.last().normal();
        float uL = s.getU(16), uR = s.getU(0);         // min-x -> u16, max-x -> u0 (convencion norte MC)
        float vT = s.getV(0),  vB = s.getV(16);        // arriba -> v0, abajo -> v16 (alto completo)
        float uSa = s.getU(14), uSb = s.getU(16);      // franja de borde para los cantos (naranja/gris/naranja)
        // Norte (-Z, frontal)
        quad(vc, m, n, x0, y1, z0, x0, y0, z0, x1, y0, z0, x1, y1, z0, uL, vT, uR, vB, light, overlay, 0, 0, -1);
        // Sur (+Z, trasera): mismo x1->uR que el norte para que la textura sea consistente por ambas caras
        quad(vc, m, n, x1, y1, z1, x1, y0, z1, x0, y0, z1, x0, y1, z1, uR, vT, uL, vB, light, overlay, 0, 0, 1);
        // Oeste (-X): canto
        quad(vc, m, n, x0, y1, z1, x0, y0, z1, x0, y0, z0, x0, y1, z0, uSa, vT, uSb, vB, light, overlay, -1, 0, 0);
        // Este (+X): canto
        quad(vc, m, n, x1, y1, z0, x1, y0, z0, x1, y0, z1, x1, y1, z1, uSa, vT, uSb, vB, light, overlay, 1, 0, 0);
        // Arriba (+Y): franja naranja superior
        quad(vc, m, n, x0, y1, z1, x0, y1, z0, x1, y1, z0, x1, y1, z1, uSa, s.getV(0), uSb, s.getV(1), light, overlay, 0, 1, 0);
        // Abajo (-Y): franja naranja inferior
        quad(vc, m, n, x0, y0, z0, x0, y0, z1, x1, y0, z1, x1, y0, z0, uSa, s.getV(15), uSb, s.getV(16), light, overlay, 0, -1, 0);
    }

    /** Emite un quad de 4 vertices (a,b,c,d) con las UV (u0,v0)-(u1,v1) mapeadas a las esquinas. */
    private static void quad(VertexConsumer vc, Matrix4f m, Matrix3f n,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz,
                             float u0, float v0, float u1, float v1, int light, int overlay,
                             float nx, float ny, float nz) {
        vc.vertex(m, ax, ay, az).color(255, 255, 255, 255).uv(u0, v0).overlayCoords(overlay).uv2(light).normal(n, nx, ny, nz).endVertex();
        vc.vertex(m, bx, by, bz).color(255, 255, 255, 255).uv(u0, v1).overlayCoords(overlay).uv2(light).normal(n, nx, ny, nz).endVertex();
        vc.vertex(m, cx, cy, cz).color(255, 255, 255, 255).uv(u1, v1).overlayCoords(overlay).uv2(light).normal(n, nx, ny, nz).endVertex();
        vc.vertex(m, dx, dy, dz).color(255, 255, 255, 255).uv(u1, v0).overlayCoords(overlay).uv2(light).normal(n, nx, ny, nz).endVertex();
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
