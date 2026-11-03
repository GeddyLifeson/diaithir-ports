package net.diaithir.druidry.ars.common.util;

import net.diaithir.druidry.ars.common.entity.ScryerCamera;
import net.minecraft.client.Minecraft;

public class ClientCameraUtil {
    public static boolean isPlayerMountedOnCamera() {
        return Minecraft.getInstance().getCameraEntity() instanceof ScryerCamera;
    }
}
