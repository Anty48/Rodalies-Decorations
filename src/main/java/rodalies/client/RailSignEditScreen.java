package rodalies.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import rodalies.RailSignBlockEntity;
import rodalies.RailSignType;
import rodalies.network.ModNetwork;
import rodalies.network.RailSignUpdatePacket;

/**
 * Pantalla para escribir el texto de una señal ferroviaria. Segun el tipo:
 *  - señales de una linea (via, velocidad, LVT): un unico campo limitado a pocos caracteres.
 *  - cartel normal: dos campos (linea 1 y linea 2), unidos con '\n' al guardar.
 */
public class RailSignEditScreen extends Screen {

    private final RailSignBlockEntity sign;
    private final RailSignType type;
    private final int slot; // 0 = señal principal (texto); 1 = señal inferior de una velocidad doble
    private EditBox line1;
    private EditBox line2; // solo en señales de 2 lineas

    public RailSignEditScreen(RailSignBlockEntity sign, int slot) {
        super(Component.translatable("screen.rodalies.rail_sign"));
        this.sign = sign;
        this.type = sign.getSignType();
        this.slot = slot;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;

        String current = switch (slot) {
            case 1 -> sign.getText2();
            case 2 -> sign.getText3();
            default -> sign.getText();
        };
        String[] parts = current.split("\n", -1);

        if (type.multiline) {
            int per = Math.max(1, type.maxChars / 2);
            line1 = new EditBox(this.font, cx - 120, cy - 24, 240, 20, Component.literal("1"));
            line1.setMaxLength(per);
            line1.setValue(parts.length > 0 ? parts[0] : "");
            addRenderableWidget(line1);
            setInitialFocus(line1);

            line2 = new EditBox(this.font, cx - 120, cy, 240, 20, Component.literal("2"));
            line2.setMaxLength(per);
            line2.setValue(parts.length > 1 ? parts[1] : "");
            addRenderableWidget(line2);
        } else {
            // Campo unico y estrecho: pocas letras.
            line1 = new EditBox(this.font, cx - 60, cy - 10, 120, 20,
                    Component.translatable("screen.rodalies.rail_sign"));
            line1.setMaxLength(type.maxChars);
            line1.setValue(parts.length > 0 ? parts[0] : "");
            addRenderableWidget(line1);
            setInitialFocus(line1);
        }

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> confirmAndClose())
                .bounds(cx - 100, cy + 30, 200, 20)
                .build());
    }

    private void confirmAndClose() {
        String text = line2 != null ? (line1.getValue() + "\n" + line2.getValue()) : line1.getValue();
        ModNetwork.CHANNEL.sendToServer(new RailSignUpdatePacket(sign.getBlockPos(), text, slot));
        onClose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Enter confirma (salvo que este en la linea 1 de un cartel de 2 lineas: salta a la linea 2).
        if (keyCode == 257 || keyCode == 335) {
            if (line2 != null && line1.isFocused()) {
                line1.setFocused(false);
                line2.setFocused(true);
                return true;
            }
            confirmAndClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 45, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
