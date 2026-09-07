package rodalies.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import rodalies.network.ModNetwork;
import rodalies.network.SignTextPacket;

/**
 * Pantalla generica para escribir el texto de un cartel que implementa {@code EditableSign}. Muestra
 * una fila de {@link EditBox} por linea (cada una con su limite de caracteres) y al confirmar manda
 * un {@link SignTextPacket} por linea (slot = indice de la linea). La usan el bloque de velocidad
 * maxima (1 linea) y el hectometro (2 lineas).
 */
public class SignTextEditScreen extends Screen {

    private final BlockPos pos;
    private final String[] initial;
    private final int[] maxLen;
    private final EditBox[] boxes;

    /**
     * @param title    titulo mostrado arriba (Component ya traducido)
     * @param pos      posicion del BlockEntity a editar
     * @param initial  valor inicial de cada linea
     * @param maxLen   limite de caracteres de cada linea (misma longitud que {@code initial})
     */
    public SignTextEditScreen(Component title, BlockPos pos, String[] initial, int[] maxLen) {
        super(title);
        this.pos = pos;
        this.initial = initial;
        this.maxLen = maxLen;
        this.boxes = new EditBox[initial.length];
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;
        int n = boxes.length;
        // Campo estrecho (pocas letras); apila las lineas centradas verticalmente.
        int startY = cy - 10 - (n - 1) * 13;
        for (int i = 0; i < n; i++) {
            EditBox box = new EditBox(this.font, cx - 60, startY + i * 26, 120, 20, Component.literal("" + (i + 1)));
            box.setMaxLength(maxLen[i]);
            box.setValue(initial[i]);
            boxes[i] = box;
            addRenderableWidget(box);
        }
        setInitialFocus(boxes[0]);

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> confirmAndClose())
                .bounds(cx - 100, cy + 30 + (n - 1) * 26, 200, 20)
                .build());
    }

    private void confirmAndClose() {
        for (int i = 0; i < boxes.length; i++) {
            ModNetwork.CHANNEL.sendToServer(new SignTextPacket(pos, boxes[i].getValue(), i));
        }
        onClose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Enter: salta al siguiente campo, o confirma si esta en el ultimo.
        if (keyCode == 257 || keyCode == 335) {
            for (int i = 0; i < boxes.length; i++) {
                if (boxes[i].isFocused() && i < boxes.length - 1) {
                    boxes[i].setFocused(false);
                    boxes[i + 1].setFocused(true);
                    return true;
                }
            }
            confirmAndClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2,
                boxes.length > 0 ? boxes[0].getY() - 15 : this.height / 2 - 45, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
