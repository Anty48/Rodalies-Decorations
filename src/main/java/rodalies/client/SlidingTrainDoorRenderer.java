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
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import rodalies.SlidingTrainDoorBlock;
import rodalies.SlidingTrainDoorBlockEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Dibuja las dos hojas de la puerta corredera en TODO momento (cerrada, abierta o deslizandose): la celda
 * es siempre {@code ENTITYBLOCK_ANIMATED} (ver {@link SlidingTrainDoorBlock#getRenderShape}), asi que la
 * puerta nunca se dibuja como modelo baked y no hay hueco al conmutar. El mismo BER vale en el mundo y
 * sobre un tren de Create (Create renderiza nuestro BlockEntity viajero con este BER).
 *
 * <b>Transparencia consistente</b>: se pinta en la capa translucida de BLOQUE ({@link RenderType#translucent()}),
 * con culling de caras traseras -> una sola capa de alfa para el cristal tintado (antes, con la capa de
 * entidad SIN culling, se veian las 2 caras y el cristal quedaba mas opaco). Ver {@link #addLeaf}.
 *
 * <b>Progreso de la animacion — keyed por (nivel, posicion)</b>: NO se guarda en el BlockEntity porque
 * Create RECREA los BE de una contraption en cada cambio de bloque (su
 * {@code AbstractContraptionEntity.handleBlockChange} llama a {@code resetClientContraption()} salvo para su
 * propia SlidingDoorBlock) -> un progreso en el BE se perderia en cada toggle (la puerta no animaria en el
 * tren, solo saltaria). Se guarda aqui, en {@link #ANIMS}, keyed por el {@link Level} de render y la
 * posicion. Clave doble a proposito:
 * <ul>
 *   <li>Cada vagon (contraption) tiene su PROPIO {@code VirtualRenderWorld} (estable entre recreaciones de
 *       BE), asi que dos puertas en la misma posicion LOCAL de vagones distintos NO colisionan. Con una
 *       clave solo-posicion se abririan/cerrarian entre si (coords locales de vagones distintos coinciden).</li>
 *   <li>El nivel es la clave debil ({@link WeakHashMap}) -> cuando el vagon se descarga, su entrada se
 *       limpia sola.</li>
 * </ul>
 * El progreso avanza por tiempo real hacia {@code OPEN} y sobrevive a que Create recree el BE.
 *
 * Animacion en 2 fases: primero las hojas salen un poco en Z (adelante si {@code openForward}, atras si
 * no), luego se corren a los lados.
 */
public class SlidingTrainDoorRenderer implements BlockEntityRenderer<SlidingTrainDoorBlockEntity> {

    private static final ResourceLocation TEX_LEFT = new ResourceLocation("rodalies", "block/door_left");
    private static final ResourceLocation TEX_RIGHT = new ResourceLocation("rodalies", "block/door_right");

    private static final float SLIDE = 8f / 16f;     // desplazamiento lateral maximo de cada hoja (8px)
    private static final float FORWARD = 3.5f / 16f; // salida en Z maxima (3,5px)
    private static final float FWD_PHASE = 0.35f;    // reparto: 0..FWD_PHASE sale en Z; FWD_PHASE..1 a los lados
    private static final float RATE = 2.5f;          // velocidad del progreso (1/s) -> ~0,4 s abrir/cerrar

    /** Progreso por (nivel de render, posicion). WeakHashMap por nivel -> se limpia al descargar el vagon. */
    private static final Map<Level, Map<BlockPos, Anim>> ANIMS = new WeakHashMap<>();
    /** Respaldo si el BE aun no tiene nivel (raro): sin persistencia entre vagones, pero no crashea. */
    private static final Map<BlockPos, Anim> FALLBACK = new HashMap<>();

    private static final class Anim {
        float progress;
        long lastMs = -1L;
    }

    // Sombreado direccional por cara (igual que aplica el chunk a un modelo baked, con luz ambiente no
    // constante): norte/sur 0.8, este/oeste 0.6. Asi el brillo casa con el modelo baked cerrado en el
    // instante en que una releva a la otra. (Arriba/abajo no se dibujan.)
    private static final float SHADE_NS = 0.8f, SHADE_EW = 0.6f;

    public SlidingTrainDoorRenderer(BlockEntityRendererProvider.Context ctx) {
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
                progress(be.getLevel(), be.getBlockPos(), open), packedLight);
    }

    /**
     * Progreso [0..1] hacia el objetivo OPEN de ESTA puerta, avanzado por tiempo real. Persiste keyed por
     * (nivel, posicion) -> sobrevive a que Create recree el BE, y no colisiona entre vagones (cada uno su
     * nivel de render). Ver el javadoc de la clase.
     */
    private static float progress(Level level, BlockPos pos, boolean open) {
        Map<BlockPos, Anim> byPos = level != null
                ? ANIMS.computeIfAbsent(level, l -> new HashMap<>())
                : FALLBACK;
        Anim a = byPos.get(pos);
        if (a == null) {
            a = new Anim();
            a.progress = open ? 1f : 0f; // primer render: arranca en el estado actual (sin salto)
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

    /**
     * Dibuja las dos hojas deslizandose (animacion en 2 fases). El {@code pose} debe estar en el origen
     * del bloque. Es {@code static} y publico por si otra parte quiere reutilizarlo.
     */
    public static void drawLeaves(PoseStack pose, MultiBufferSource buffer, Direction facing,
                                  boolean forward, float p, int light) {
        // Fase 1 (0..FWD_PHASE): sale en Z. Fase 2 (FWD_PHASE..1): se corre a los lados.
        float fwd = Math.min(p / FWD_PHASE, 1f) * FORWARD;
        float slide = p <= FWD_PHASE ? 0f : (p - FWD_PHASE) / (1f - FWD_PHASE) * SLIDE;
        float zShift = forward ? -fwd : fwd;

        pose.pushPose();
        // Orientar como la blockstate (north=0, east=90, south=180, west=270), igual que las señales.
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-blockstateY(facing)));
        pose.translate(-0.5, -0.5, -0.5);

        VertexConsumer vc = buffer.getBuffer(RenderType.translucent());
        TextureAtlasSprite spriteLeft = sprite(TEX_LEFT);
        TextureAtlasSprite spriteRight = sprite(TEX_RIGHT);

        // Hoja derecha: cerrada en x[0..0.5], se desliza a -X. Izquierda: x[0.5..1] -> +X. Ambas + zShift.
        addLeaf(pose, vc, 0f - slide, 0f, 0f + zShift, 0.5f - slide, 2f, 0.125f + zShift, spriteRight, light);
        addLeaf(pose, vc, 0.5f + slide, 0f, 0f + zShift, 1f + slide, 2f, 0.125f + zShift, spriteLeft, light);

        pose.popPose();
    }

    private static TextureAtlasSprite sprite(ResourceLocation rl) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(rl);
    }

    /**
     * Dibuja una hoja reproduciendo el mapeo UV de Blockbench (texturas door_left/door_right, 8x32):
     * caras norte/sur = textura completa a lo ancho (x-min -> getU(16), x-max -> getU(0), para que el
     * pixel-borde quede al centro donde se juntan las hojas); cantos este/oeste = franja de borde.
     *
     * IMPORTANTE: la capa translucida de bloque tiene <b>culling de caras traseras</b>, asi que cada cara
     * se emite con el bobinado (winding) CCW hacia AFUERA (la normal). Antes se usaba una capa de entidad
     * SIN culling y el bobinado daba igual, pero eso mostraba las 2 caras del cristal a la vez (doble capa
     * de alfa -> se veia mas opaco en movimiento). Con culling correcto se ve UNA capa, igual que el
     * modelo baked. Las caras arriba/abajo se OMITEN: nunca son visibles (van pegadas al techo/suelo) y
     * dibujarlas provoca z-fighting con el bloque de debajo (el suelo).
     */
    private static void addLeaf(PoseStack pose, VertexConsumer vc, float x0, float y0, float z0,
                                float x1, float y1, float z1, TextureAtlasSprite s, int light) {
        Matrix4f m = pose.last().pose();
        Matrix3f nm = pose.last().normal();
        float u0 = s.getU(0), u16 = s.getU(16), u14 = s.getU(14);
        float v0 = s.getV(0), v16 = s.getV(16);
        // Norte (-Z, frontal): x0->u16, x1->u0 ; y1(arriba)->v0, y0(abajo)->v16.
        face(vc, m, nm, SHADE_NS, 0, 0, -1,
                x0, y0, z0, u16, v16,  x0, y1, z0, u16, v0,  x1, y1, z0, u0, v0,  x1, y0, z0, u0, v16, light);
        // Sur (+Z, trasera)
        face(vc, m, nm, SHADE_NS, 0, 0, 1,
                x1, y0, z1, u0, v16,  x1, y1, z1, u0, v0,  x0, y1, z1, u16, v0,  x0, y0, z1, u16, v16, light);
        // Oeste (-X): canto (franja de borde u14..u16)
        face(vc, m, nm, SHADE_EW, -1, 0, 0,
                x0, y0, z1, u14, v16,  x0, y1, z1, u14, v0,  x0, y1, z0, u16, v0,  x0, y0, z0, u16, v16, light);
        // Este (+X): canto
        face(vc, m, nm, SHADE_EW, 1, 0, 0,
                x1, y0, z0, u14, v16,  x1, y1, z0, u14, v0,  x1, y1, z1, u16, v0,  x1, y0, z1, u16, v16, light);
    }

    /** Una cara (quad) en formato BLOCK, con winding CCW hacia afuera (respetando el culling). */
    private static void face(VertexConsumer vc, Matrix4f m, Matrix3f nm, float shade,
                             float nx, float ny, float nz,
                             float ax, float ay, float az, float au, float av,
                             float bx, float by, float bz, float bu, float bv,
                             float cx, float cy, float cz, float cu, float cv,
                             float dx, float dy, float dz, float du, float dv, int light) {
        int c = (int) (255f * shade);
        vc.vertex(m, ax, ay, az).color(c, c, c, 255).uv(au, av).uv2(light).normal(nm, nx, ny, nz).endVertex();
        vc.vertex(m, bx, by, bz).color(c, c, c, 255).uv(bu, bv).uv2(light).normal(nm, nx, ny, nz).endVertex();
        vc.vertex(m, cx, cy, cz).color(c, c, c, 255).uv(cu, cv).uv2(light).normal(nm, nx, ny, nz).endVertex();
        vc.vertex(m, dx, dy, dz).color(c, c, c, 255).uv(du, dv).uv2(light).normal(nm, nx, ny, nz).endVertex();
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
