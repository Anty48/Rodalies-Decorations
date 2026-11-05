package rodalies.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import rodalies.JunctionBoxBlock;
import rodalies.network.JunctionBoxPacket;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Menu de la caja de conexiones: 4 flechas con su punto cardinal (como una brujula: North arriba) y un
 * boton de centro. Cada flecha mueve la caja 1 px en esa direccion; con Shift pulsado la lleva hasta el
 * tope (3 px). El centro la devuelve al medio. Los cambios se aplican al momento (el servidor actualiza
 * el estado del bloque).
 *
 * Los botones van en la parte de arriba y "Done"/"Set default" abajo, para dejar libre el centro de la
 * pantalla (donde esta la caja que se mira) y ver como se mueve. "Set default" guarda la posicion actual
 * para las siguientes cajas que coloque el jugador.
 */
public class JunctionBoxScreen extends Screen {

    private static final int BW = 70;
    private static final int BH = 20;
    private static final int GAP = 2;
    private static final int TOP = 20;              // primera fila de botones (North)
    private static final int SAVED_TICKS = 60;      // cuanto se muestra el aviso de "guardado"

    private final BlockPos pos;
    private int savedTicks;

    public JunctionBoxScreen(BlockPos pos) {
        super(Component.translatable("screen.rodalies.junction_box"));
        this.pos = pos;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int row = TOP + BH + GAP;
        arrow(Direction.NORTH, "↑ ", "", cx - BW / 2, TOP);
        arrow(Direction.WEST, "← ", "", cx - BW / 2 - BW - GAP, row);
        arrow(Direction.EAST, "", " →", cx + BW / 2 + GAP, row);
        arrow(Direction.SOUTH, "↓ ", "", cx - BW / 2, row + BH + GAP);
        addRenderableWidget(Button.builder(Component.translatable("screen.rodalies.junction_box.center"),
                        b -> send(JunctionBoxBlock.ACTION_CENTER, Direction.NORTH))
                .bounds(cx - BW / 2, row, BW, BH).build());

        int bottom = height - BH - 8;
        addRenderableWidget(Button.builder(Component.translatable("screen.rodalies.junction_box.set_default"),
                        b -> {
                            send(JunctionBoxBlock.ACTION_SET_DEFAULT, Direction.NORTH);
                            savedTicks = SAVED_TICKS;
                        })
                .bounds(cx - 100 - GAP, bottom, 100, BH).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(cx + GAP, bottom, 100, BH).build());
    }

    private void arrow(Direction dir, String before, String after, int x, int y) {
        Component label = Component.literal(before)
                .append(Component.translatable("screen.rodalies.junction_box." + dir.getName()))
                .append(after);
        addRenderableWidget(Button.builder(label,
                        b -> send(hasShiftDown() ? JunctionBoxBlock.ACTION_EDGE : JunctionBoxBlock.ACTION_STEP, dir))
                .bounds(x, y, BW, BH).build());
    }

    private void send(int action, Direction dir) {
        PacketDistributor.sendToServer(new JunctionBoxPacket(pos, action, dir));
    }

    @Override
    public void tick() {
        if (savedTicks > 0) {
            savedTicks--;
        }
        // Si rompen la caja con el menu abierto, se cierra.
        if (minecraft != null && minecraft.level != null
                && !(minecraft.level.getBlockState(pos).getBlock() instanceof JunctionBoxBlock)) {
            onClose();
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        int cx = width / 2;
        g.drawCenteredString(font, title, cx, TOP - 12, 0xFFFFFF);

        int textY = TOP + 3 * (BH + GAP) + 4;
        if (minecraft != null && minecraft.level != null) {
            BlockState state = minecraft.level.getBlockState(pos);
            if (state.getBlock() instanceof JunctionBoxBlock) {
                int x = state.getValue(JunctionBoxBlock.OFFSET_X) - JunctionBoxBlock.CENTER;
                int z = state.getValue(JunctionBoxBlock.OFFSET_Z) - JunctionBoxBlock.CENTER;
                Component position = Component.translatable("screen.rodalies.junction_box.position",
                        axisText(z, Direction.NORTH, Direction.SOUTH), axisText(x, Direction.WEST, Direction.EAST));
                g.drawCenteredString(font, position, cx, textY, 0xFFFFFF);
            }
        }
        // Pista (o aviso de "guardado") justo encima de Done, lejos del centro de la pantalla.
        Component hint = savedTicks > 0
                ? Component.translatable("screen.rodalies.junction_box.default_saved")
                : Component.translatable("screen.rodalies.junction_box.hint");
        g.drawCenteredString(font, hint, cx, height - BH - 20, savedTicks > 0 ? 0x80FF80 : 0xA0A0A0);
    }

    /** "3 px North", "0 px", ... segun el signo del desplazamiento en ese eje. */
    private static Component axisText(int value, Direction negative, Direction positive) {
        if (value == 0) {
            return Component.literal("0 px");
        }
        Direction dir = value < 0 ? negative : positive;
        return Component.literal(Math.abs(value) + " px ")
                .append(Component.translatable("screen.rodalies.junction_box." + dir.getName()));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
