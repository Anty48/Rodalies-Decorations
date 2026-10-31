package rodalies.client;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Pantalla de edicion de texto con vista previa: dibuja la señal real de frente (su modelo de bloque
 * + su BlockEntityRenderer) y el texto se escribe directamente encima, como el cartel de madera de
 * vanilla. Se ve exactamente como quedara porque el texto lo pinta el mismo renderer que en el mundo,
 * sobre una COPIA del BlockEntity (el del mundo no se toca hasta confirmar).
 *
 * Cada señal define sus campos (slot + linea del renderer), como se aplican a la copia, como se envian
 * al servidor y un encuadre ({@link Framing}). Las factorias estan en {@link ClientHooks}.
 *
 * Teclas: escribir edita el campo activo; ↑/↓ o Tab cambian de campo; Enter pasa al siguiente o
 * confirma en el ultimo; Esc cancela sin guardar.
 */
public class SignPreviewEditScreen extends Screen {

    /** Una linea editable: a que slot/linea del renderer corresponde, su limite y su valor. */
    public static final class Field {
        final int slot;
        final int line;
        final int maxLen;
        String value;

        public Field(int slot, int line, int maxLen, String initial) {
            this.slot = slot;
            this.line = line;
            this.maxLen = maxLen;
            this.value = initial == null ? "" : initial;
        }
    }

    /**
     * Encuadre de la vista previa, en pixeles del modelo (1 bloque = 16 px) relativos a la celda del
     * BlockEntity y SIN la rotacion de la blockstate (como si mirase al norte). {@code x,y,z} es el
     * punto que queda en el centro; {@code w,h} el rectangulo que tiene que caber en pantalla.
     * {@code side=false}: se mira la cara norte (frontal); {@code side=true}: la cara este (+X).
     */
    public record Framing(float x, float y, float z, float w, float h, boolean side) {
        public static Framing front(float x, float y, float w, float h) {
            return new Framing(x, y, 8f, w, h, false);
        }

        public static Framing side(float z, float y, float w, float h) {
            return new Framing(8f, y, z, w, h, true);
        }
    }

    private static final int FULL_BRIGHT = LightTexture.FULL_BRIGHT;
    private static final float MAX_PX_SCALE = 8f;  // tope de pixeles de pantalla por pixel del modelo
    private static final int TOP_MARGIN = 30;      // hueco para el titulo
    private static final int BOTTOM_MARGIN = 36;   // hueco para el boton "Hecho"

    private final BlockEntity source;
    private final BlockEntity preview;
    private final Field[] fields;
    private final BiConsumer<BlockEntity, String[]> apply;
    private final Consumer<String[]> send;
    private final Framing framing;
    private final TextFieldHelper[] helpers;
    private int focused;

    /**
     * @param source  BlockEntity del mundo que se edita
     * @param fields  lineas editables (en orden de navegacion)
     * @param focused indice del campo que empieza activo
     * @param apply   escribe los valores en la copia de preview (llamado en cada cambio)
     * @param send    envia los valores al servidor al confirmar
     */
    public SignPreviewEditScreen(Component title, BlockEntity source, Field[] fields, int focused,
                                 BiConsumer<BlockEntity, String[]> apply, Consumer<String[]> send,
                                 Framing framing) {
        super(title);
        this.source = source;
        this.fields = fields;
        this.focused = Math.max(0, Math.min(focused, fields.length - 1));
        this.apply = apply;
        this.send = send;
        this.framing = framing;
        this.preview = copyOf(source);
        this.helpers = new TextFieldHelper[fields.length];
        Minecraft mc = Minecraft.getInstance();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            helpers[i] = new TextFieldHelper(() -> f.value, v -> {
                f.value = v;
                applyToPreview();
            }, TextFieldHelper.createClipboardGetter(mc), TextFieldHelper.createClipboardSetter(mc),
                    v -> v.length() <= f.maxLen);
            helpers[i].setCursorToEnd();
        }
        applyToPreview();
    }

    /** Copia desacoplada del BE (sin level): los cambios de la preview no afectan al mundo. */
    private static BlockEntity copyOf(BlockEntity be) {
        BlockEntity copy = be.getType().create(be.getBlockPos(), be.getBlockState());
        if (copy == null) {
            return be;
        }
        copy.load(be.saveWithoutMetadata());
        return copy;
    }

    private String[] values() {
        String[] v = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            v[i] = fields[i].value;
        }
        return v;
    }

    private void applyToPreview() {
        if (preview != source) {
            apply.accept(preview, values());
        }
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> confirmAndClose())
                .bounds(this.width / 2 - 100, this.height - BOTTOM_MARGIN + 8, 200, 20)
                .build());
    }

    private void confirmAndClose() {
        send.accept(values());
        onClose();
    }

    private void moveFocus(int delta) {
        focused = Math.floorMod(focused + delta, fields.length);
        helpers[focused].setCursorToEnd();
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        helpers[focused].charTyped(c);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_UP -> {
                moveFocus(-1);
                return true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                moveFocus(1);
                return true;
            }
            case GLFW.GLFW_KEY_TAB -> {
                moveFocus(hasShiftDown() ? -1 : 1);
                return true;
            }
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                if (focused < fields.length - 1) {
                    moveFocus(1);
                } else {
                    confirmAndClose();
                }
                return true;
            }
            default -> {
            }
        }
        if (keyCode != GLFW.GLFW_KEY_ESCAPE && helpers[focused].keyPressed(keyCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        renderPreview(graphics, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    /** Dibuja el modelo de la señal de frente + su texto (renderer real) centrado y escalado. */
    private void renderPreview(GuiGraphics graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        BlockState state = preview.getBlockState();

        int areaH = this.height - TOP_MARGIN - BOTTOM_MARGIN;
        float pxScale = Math.min(MAX_PX_SCALE,
                Math.min((this.width - 32f) / framing.w(), areaH / framing.h()));
        float blockScale = pxScale * 16f; // pixeles de pantalla por bloque
        float centerY = TOP_MARGIN + areaH / 2f;

        Field f = fields[focused];
        boolean blink = (Util.getMillis() / 300L) % 2L == 0L;
        SignEditPreview.begin(preview, f.slot, f.line, helpers[focused].getCursorPos(), blink);
        PoseStack pose = graphics.pose();
        pose.pushPose();
        try {
            pose.translate(this.width / 2f, centerY, 150f);
            pose.scale(blockScale, -blockScale, blockScale); // -Y: en pantalla Y crece hacia abajo
            // Girar la cara a mostrar hacia el jugador (+Z): la norte con 180, la este con -90.
            pose.mulPose(Axis.YP.rotationDegrees(framing.side() ? -90f : 180f));
            pose.translate(-framing.x() / 16f, -framing.y() / 16f, -framing.z() / 16f);
            // Deshacer la rotacion de la blockstate: asi el encuadre es el mismo mire donde mire.
            if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                float y = blockstateY(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
                pose.translate(0.5, 0.5, 0.5);
                pose.mulPose(Axis.YP.rotationDegrees(y));
                pose.translate(-0.5, -0.5, -0.5);
            }

            Lighting.setupForFlatItems(); // cara frontal a plena luz, como un item plano
            MultiBufferSource.BufferSource buffer = graphics.bufferSource();
            mc.getBlockRenderer().renderSingleBlock(state, pose, buffer, FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            BlockEntityRenderer<BlockEntity> renderer = mc.getBlockEntityRenderDispatcher().getRenderer(preview);
            if (renderer != null) {
                renderer.render(preview, partialTick, pose, buffer, FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            }
            graphics.flush();
        } finally {
            Lighting.setupFor3DItems();
            pose.popPose();
            SignEditPreview.end();
        }
        // La señal se dibuja "delante" (z alta): limpiar profundidad para que el titulo/boton queden encima.
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
    }

    private static float blockstateY(net.minecraft.core.Direction facing) {
        return switch (facing) {
            case EAST -> 90f;
            case SOUTH -> 180f;
            case WEST -> 270f;
            default -> 0f; // NORTH
        };
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
