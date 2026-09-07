package rodalies.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import rodalies.HectometerSignBlockEntity;
import rodalies.RailSignBlockEntity;
import rodalies.StationSignalBlockEntity;
import rodalies.TrainMaxSpeedBlockEntity;

/**
 * Helpers solo-cliente invocados desde codigo comun via DistExecutor (nunca se cargan en servidor).
 */
public class ClientHooks {

    public static void openStationSignalScreen(StationSignalBlockEntity sign) {
        Minecraft.getInstance().setScreen(new StationSignalEditScreen(sign));
    }

    public static void openRailSignScreen(RailSignBlockEntity sign, int slot) {
        Minecraft.getInstance().setScreen(new RailSignEditScreen(sign, slot));
    }

    public static void openTrainMaxSpeedScreen(TrainMaxSpeedBlockEntity sign) {
        Minecraft.getInstance().setScreen(new SignTextEditScreen(
                Component.translatable("screen.rodalies.train_max_speed"),
                sign.getBlockPos(),
                new String[]{sign.getText()},
                new int[]{4}));
    }

    public static void openHectometerScreen(HectometerSignBlockEntity sign) {
        Minecraft.getInstance().setScreen(new SignTextEditScreen(
                Component.translatable("screen.rodalies.hectometer_signal"),
                sign.getBlockPos(),
                new String[]{sign.getTop(), sign.getBottom()},
                new int[]{4, 4}));
    }
}
