package rodalies.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import rodalies.StationSignalBlockEntity;
import rodalies.network.ModNetwork;
import rodalies.network.StationSignUpdatePacket;

/**
 * Pantalla para escribir el nombre de la estacion (una linea). Al confirmar envia el texto
 * al servidor, que lo guarda en el BlockEntity y lo sincroniza a todos los clientes.
 */
public class StationSignalEditScreen extends Screen {

    private static final int MAX_LENGTH = 48;

    private final StationSignalBlockEntity sign;
    private EditBox input;

    public StationSignalEditScreen(StationSignalBlockEntity sign) {
        super(Component.translatable("screen.rodalies.station_signal"));
        this.sign = sign;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;

        input = new EditBox(this.font, cx - 120, cy - 10, 240, 20,
                Component.translatable("screen.rodalies.station_signal"));
        input.setMaxLength(MAX_LENGTH);
        input.setValue(sign.getText());
        addRenderableWidget(input);
        setInitialFocus(input);

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> confirmAndClose())
                .bounds(cx - 100, cy + 20, 200, 20)
                .build());
    }

    private void confirmAndClose() {
        ModNetwork.CHANNEL.sendToServer(new StationSignUpdatePacket(sign.getBlockPos(), input.getValue()));
        onClose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Enter confirma.
        if (keyCode == 257 || keyCode == 335) {
            confirmAndClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 35, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
