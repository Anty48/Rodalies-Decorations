package rodalies.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import rodalies.SlidingTrainDoorBlock;
import rodalies.SlidingTrainDoorBlockEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Dibuja las dos hojas de la puerta corredera. Ver documentacion completa en la version Forge.
 *
 * Port a 1.21:
 *  - {@code new ResourceLocation} -> {@link ResourceLocation#fromNamespaceAndPath}.
 *  - La API de {@link VertexConsumer} cambia: {@code addVertex/setColor/setUv/setLight/setNormal} y ya
 *    no existe {@code endVertex()}; el normal se transforma con la {@link PoseStack.Pose}.
 */
public class SlidingTrainDoorRenderer implements BlockEntityRenderer<SlidingTrainDoorBlockEntity> {

    private static final float SLIDE = 8f / 16f;     // desplazamiento lateral maximo de cada hoja (8px)
    private static final float FORWARD = 3.5f / 16f; // salida en Z maxima (3,5px)
    private static final float FWD_PHASE = 0.35f;    // reparto: 0..FWD_PHASE sale en Z; FWD_PHASE..1 a los lados
    private static final float RATE = 2.5f;          // velocidad del progreso (1/s) -> ~0,4 s abrir/cerrar

    private static final Map<Level, Map<BlockPos, Anim>> ANIMS = new WeakHashMap<>();
    private static final Map<BlockPos, Anim> FALLBACK = new HashMap<>();

    private static final class Anim {
        float progress;
        long lastMs = -1L;
    }

    private static final float SHADE_NS = 0.8f, SHADE_EW = 0.6f;

    public SlidingTrainDoorRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    /** Caja de render ampliada: las hojas ocupan 2 celdas de alto y salen a los lados al abrir. */
    @Override
    public AABB getRenderBoundingBox(SlidingTrainDoorBlockEntity be) {
        return new AABB(be.getBlockPos()).inflate(1.0, 2.0, 1.0);
    }

    @Override
    public void render(SlidingTrainDoorBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof SlidingTrainDoorBlock door)) {
            return;
        }
        Direction facing = state.getValue(SlidingTrainDoorBlock.FACING);
        boolean open = state.getValue(SlidingTrainDoorBlock.OPEN);
        drawLeaves(pose, buffer, facing, door.isOpenForward(),
                progress(be.getLevel(), be.getBlockPos(), open), packedLight,
                door.getTexLeft(), door.getTexRight(), door.getTexTop());
    }

    private static float progress(Level level, BlockPos pos, boolean open) {
        Map<BlockPos, Anim> byPos = level != null
                ? ANIMS.computeIfAbsent(level, l -> new HashMap<>())
                : FALLBACK;
        Anim a = byPos.get(pos);
        if (a == null) {
            a = new Anim();
            a.progress = open ? 1f : 0f;
            byPos.put(pos.immutable(), a);
        }
        float target = open ? 1f : 0f;
        long now = System.currentTimeMillis();
        if (a.lastMs < 0L) {
            a.lastMs = now;
        }
        float dt = Math.min((now - a.lastMs) / 1000f, 0.1f);
        a.lastMs = now;
        if (a.progress < target) {
            a.progress = Math.min(target, a.progress + RATE * dt);
        } else if (a.progress > target) {
            a.progress = Math.max(target, a.progress - RATE * dt);
        }
        return a.progress;
    }

    public static void drawLeaves(PoseStack pose, MultiBufferSource buffer, Direction facing,
                                  boolean forward, float p, int light,
                                  ResourceLocation texLeft, ResourceLocation texRight, ResourceLocation texTop) {
        // Fase 1 (0..FWD_PHASE): sale en Z. Fase 2 (FWD_PHASE..1): se corre a los lados.
        float fwd = Math.min(p / FWD_PHASE, 1f) * FORWARD;
        float slide = p <= FWD_PHASE ? 0f : (p - FWD_PHASE) / (1f - FWD_PHASE) * SLIDE;
        float zShift = forward ? -fwd : fwd;

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-blockstateY(facing)));
        pose.translate(-0.5, -0.5, -0.5);

        VertexConsumer vc = buffer.getBuffer(RenderType.translucent());
        TextureAtlasSprite spriteLeft = sprite(texLeft);
        TextureAtlasSprite spriteRight = sprite(texRight);
        TextureAtlasSprite spriteTop = sprite(texTop);

        // Hoja derecha: cerrada en x[0..0.5], se desliza a -X. Izquierda: x[0.5..1] -> +X. Ambas + zShift.
        addLeaf(pose, vc, 0f - slide, 0f, 0f + zShift, 0.5f - slide, 2f, 0.125f + zShift, spriteRight, spriteTop, light);
        addLeaf(pose, vc, 0.5f + slide, 0f, 0f + zShift, 1f + slide, 2f, 0.125f + zShift, spriteLeft, spriteTop, light);

        pose.popPose();
    }

    private static TextureAtlasSprite sprite(ResourceLocation rl) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(rl);
    }

    private static void addLeaf(PoseStack pose, VertexConsumer vc, float x0, float y0, float z0,
                                float x1, float y1, float z1, TextureAtlasSprite s, TextureAtlasSprite sTop, int light) {
        PoseStack.Pose entry = pose.last();
        // 1.21: TextureAtlasSprite.getU/getV toman coordenadas NORMALIZADAS 0..1 (antes eran 0..16 en pixeles).
        // Pasar 16/14 muestreaba fuera del sprite -> "bloquecitos random" de otras texturas del atlas.
        float u0 = s.getU(0f), u16 = s.getU(1f), u14 = s.getU(14f / 16f);
        float v0 = s.getV(0f), v16 = s.getV(1f);
        // Norte (-Z, frontal)
        face(vc, entry, SHADE_NS, 0, 0, -1,
                x0, y0, z0, u16, v16,  x0, y1, z0, u16, v0,  x1, y1, z0, u0, v0,  x1, y0, z0, u0, v16, light);
        // Sur (+Z, trasera)
        face(vc, entry, SHADE_NS, 0, 0, 1,
                x1, y0, z1, u0, v16,  x1, y1, z1, u0, v0,  x0, y1, z1, u16, v0,  x0, y0, z1, u16, v16, light);
        // Oeste (-X): canto
        face(vc, entry, SHADE_EW, -1, 0, 0,
                x0, y0, z1, u14, v16,  x0, y1, z1, u14, v0,  x0, y1, z0, u16, v0,  x0, y0, z0, u16, v16, light);
        // Este (+X): canto
        face(vc, entry, SHADE_EW, 1, 0, 0,
                x1, y0, z0, u14, v16,  x1, y1, z0, u14, v0,  x1, y1, z1, u16, v0,  x1, y0, z1, u16, v16, light);
        // Techo (up, +Y): la cara estrecha que mira al cielo. Usa su PROPIA textura (sTop: gray concrete en
        // la naranja, cuarzo en la blanca) muestreando su zona central, para que no quede sin textura.
        // Winding CCW hacia +Y.
        float uT0 = sTop.getU(6f / 16f), uT1 = sTop.getU(10f / 16f), vT0 = sTop.getV(6f / 16f), vT1 = sTop.getV(10f / 16f);
        face(vc, entry, 1.0f, 0, 1, 0,
                x0, y1, z0, uT0, vT0,  x0, y1, z1, uT0, vT1,  x1, y1, z1, uT1, vT1,  x1, y1, z0, uT1, vT0, light);
    }

    /** Una cara (quad) en formato BLOCK. En 1.21: addVertex/setColor/setUv/setLight/setNormal (sin endVertex). */
    private static void face(VertexConsumer vc, PoseStack.Pose pose, float shade,
                             float nx, float ny, float nz,
                             float ax, float ay, float az, float au, float av,
                             float bx, float by, float bz, float bu, float bv,
                             float cx, float cy, float cz, float cu, float cv,
                             float dx, float dy, float dz, float du, float dv, int light) {
        int c = (int) (255f * shade);
        vc.addVertex(pose, ax, ay, az).setColor(c, c, c, 255).setUv(au, av).setLight(light).setNormal(pose, nx, ny, nz);
        vc.addVertex(pose, bx, by, bz).setColor(c, c, c, 255).setUv(bu, bv).setLight(light).setNormal(pose, nx, ny, nz);
        vc.addVertex(pose, cx, cy, cz).setColor(c, c, c, 255).setUv(cu, cv).setLight(light).setNormal(pose, nx, ny, nz);
        vc.addVertex(pose, dx, dy, dz).setColor(c, c, c, 255).setUv(du, dv).setLight(light).setNormal(pose, nx, ny, nz);
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
