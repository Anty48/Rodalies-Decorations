package rodalies.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import rodalies.HectometerSignBlockEntity;
import rodalies.PlatformNumberSignBlock;
import rodalies.RailSignBlockEntity;
import rodalies.RailSignType;
import rodalies.RailSignWallBlock;
import rodalies.SpeedLimitBlock;
import rodalies.StationSignalBlock;
import rodalies.StationSignalBlockEntity;
import rodalies.TrainMaxSpeedBlockEntity;
import rodalies.client.SignPreviewEditScreen.Field;
import rodalies.client.SignPreviewEditScreen.Framing;
import rodalies.network.ModNetwork;
import rodalies.network.RailSignUpdatePacket;
import rodalies.network.SignTextPacket;
import rodalies.network.StationSignUpdatePacket;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Helpers solo-cliente invocados desde codigo comun via DistExecutor (nunca se cargan en servidor).
 * Abren la {@link SignPreviewEditScreen} de cada señal con sus campos y su encuadre. Los encuadres
 * (centro y tamaño en px del modelo, celda del BlockEntity, mirando al norte) salen de los limites
 * de cada modelo: si una señal sale mal centrada o muy pequeña/grande, ajusta su {@link Framing}.
 */
public class ClientHooks {

    private static final int STATION_MAX_LENGTH = 48;
    private static final int TRAIN_MAX_SPEED_MAX_LENGTH = 4;
    private static final int HECTOMETER_MAX_LENGTH = 4;

    public static void openStationSignalScreen(StationSignalBlockEntity sign) {
        BlockState state = sign.getBlockState();
        StationSignalBlock.Mount mount = state.hasProperty(StationSignalBlock.MOUNT)
                ? state.getValue(StationSignalBlock.MOUNT) : StationSignalBlock.Mount.FLOOR;
        // Panel de 48 px (x -16..32): arriba (y23..32) en suelo, abajo (y4..13) en pared/aire.
        float cy = mount == StationSignalBlock.Mount.FLOOR ? 27.5f : 8.5f;
        BlockPos pos = sign.getBlockPos();
        open(Component.translatable("screen.rodalies.station_signal"), sign,
                new Field[]{new Field(0, 0, STATION_MAX_LENGTH, sign.getText())}, 0,
                (be, v) -> ((StationSignalBlockEntity) be).setText(v[0]),
                v -> ModNetwork.CHANNEL.sendToServer(new StationSignUpdatePacket(pos, v[0])),
                Framing.front(8f, cy, 52f, 15f));
    }

    public static void openRailSignScreen(RailSignBlockEntity sign, int slot) {
        RailSignType type = sign.getSignType();
        BlockState state = sign.getBlockState();
        BlockPos pos = sign.getBlockPos();
        Component title = Component.translatable("screen.rodalies.rail_sign");
        // Las de pared dibujan el modelo una celda mas abajo (mismo panel, 16 px menos).
        float dy = state.getBlock() instanceof RailSignWallBlock ? -16f : 0f;

        if (type.multiline) {
            // Cartel normal: dos lineas que se guardan juntas (separadas por '\n') en el slot 0.
            String[] parts = sign.getText().split("\n", -1);
            int per = Math.max(1, type.maxChars / 2);
            open(title, sign, new Field[]{
                            new Field(0, 0, per, parts.length > 0 ? parts[0] : ""),
                            new Field(0, 1, per, parts.length > 1 ? parts[1] : "")}, 0,
                    (be, v) -> ((RailSignBlockEntity) be).setText(v[0] + "\n" + v[1]),
                    v -> ModNetwork.CHANNEL.sendToServer(new RailSignUpdatePacket(pos, v[0] + "\n" + v[1], 0)),
                    Framing.front(8f, 25.5f + dy, 17f, 17f));
            return;
        }

        // Señales de una linea: un campo por slot (las velocidades apiladas y la via doble tienen varios).
        int slots = 1;
        Framing framing;
        switch (type) {
            case SPEED_LIMIT -> {
                SpeedLimitBlock.Stack stack = state.hasProperty(SpeedLimitBlock.STACK)
                        ? state.getValue(SpeedLimitBlock.STACK) : SpeedLimitBlock.Stack.NONE;
                switch (stack) {
                    case DOUBLE -> {
                        slots = 2;
                        framing = Framing.front(8f, 15.9f, 24f, 45f);
                    }
                    case TRIPLE -> {
                        slots = 3;
                        framing = Framing.front(8f, 11.4f, 24f, 58f);
                    }
                    case LTV_END -> framing = Framing.front(8f, 18.6f, 24f, 40f);
                    default -> framing = Framing.front(8f, 26.4f + dy, 24f, 24f);
                }
            }
            case PLATFORM_NUMBER -> {
                boolean doubled = state.hasProperty(PlatformNumberSignBlock.DOUBLED)
                        && state.getValue(PlatformNumberSignBlock.DOUBLED);
                // Se mira de lado (cara este): el panel cuelga en z9..18 (doble: tambien z-2..7).
                if (doubled) {
                    slots = 2;
                    framing = Framing.side(8f, 27f, 24f, 14f);
                } else {
                    framing = Framing.side(13.5f, 27f, 14f, 13f);
                }
            }
            default -> framing = Framing.front(8f, 26.4f + dy, 26f, 26f); // LTV
        }

        Field[] fields = new Field[slots];
        for (int i = 0; i < slots; i++) {
            fields[i] = new Field(i, 0, type.maxChars, firstLine(railText(sign, i)));
        }
        int n = slots;
        open(title, sign, fields, slot,
                (be, v) -> {
                    RailSignBlockEntity copy = (RailSignBlockEntity) be;
                    for (int i = 0; i < n; i++) {
                        switch (i) {
                            case 1 -> copy.setText2(v[i]);
                            case 2 -> copy.setText3(v[i]);
                            default -> copy.setText(v[i]);
                        }
                    }
                },
                v -> {
                    for (int i = 0; i < n; i++) {
                        ModNetwork.CHANNEL.sendToServer(new RailSignUpdatePacket(pos, v[i], i));
                    }
                },
                framing);
    }

    public static void openTrainMaxSpeedScreen(TrainMaxSpeedBlockEntity sign) {
        BlockPos pos = sign.getBlockPos();
        open(Component.translatable("screen.rodalies.train_max_speed"), sign,
                new Field[]{new Field(0, 0, TRAIN_MAX_SPEED_MAX_LENGTH, sign.getText())}, 0,
                (be, v) -> ((TrainMaxSpeedBlockEntity) be).setLine(0, v[0]),
                v -> ModNetwork.CHANNEL.sendToServer(new SignTextPacket(pos, v[0], 0)),
                Framing.front(8f, 8f, 20f, 20f));
    }

    public static void openHectometerScreen(HectometerSignBlockEntity sign) {
        BlockPos pos = sign.getBlockPos();
        open(Component.translatable("screen.rodalies.hectometer_signal"), sign,
                new Field[]{
                        new Field(0, 0, HECTOMETER_MAX_LENGTH, sign.getTop()),
                        new Field(1, 0, HECTOMETER_MAX_LENGTH, sign.getBottom())}, 0,
                (be, v) -> {
                    ((HectometerSignBlockEntity) be).setLine(0, v[0]);
                    ((HectometerSignBlockEntity) be).setLine(1, v[1]);
                },
                v -> {
                    ModNetwork.CHANNEL.sendToServer(new SignTextPacket(pos, v[0], 0));
                    ModNetwork.CHANNEL.sendToServer(new SignTextPacket(pos, v[1], 1));
                },
                Framing.front(8f, 26.5f, 15f, 16f));
    }

    private static void open(Component title, BlockEntity sign, Field[] fields, int focused,
                             BiConsumer<BlockEntity, String[]> apply, Consumer<String[]> send, Framing framing) {
        Minecraft.getInstance().setScreen(new SignPreviewEditScreen(title, sign, fields, focused, apply, send, framing));
    }

    private static String railText(RailSignBlockEntity sign, int slot) {
        return switch (slot) {
            case 1 -> sign.getText2();
            case 2 -> sign.getText3();
            default -> sign.getText();
        };
    }

    private static String firstLine(String text) {
        int nl = text.indexOf('\n');
        return nl >= 0 ? text.substring(0, nl) : text;
    }
}
