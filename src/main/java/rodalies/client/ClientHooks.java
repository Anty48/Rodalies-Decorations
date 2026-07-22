package rodalies.client;

import net.minecraft.client.Minecraft;
import rodalies.StationSignalBlockEntity;

/**
 * Helpers solo-cliente invocados desde codigo comun via DistExecutor (nunca se cargan en servidor).
 */
public class ClientHooks {

    public static void openStationSignalScreen(StationSignalBlockEntity sign) {
        Minecraft.getInstance().setScreen(new StationSignalEditScreen(sign));
    }
}
