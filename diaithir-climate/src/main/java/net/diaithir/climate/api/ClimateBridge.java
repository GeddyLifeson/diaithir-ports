package net.diaithir.climate.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** KubeJS plugs the room-temperature model in here; Homeostatic reads it through the Homeostatic mixin. */
public final class ClimateBridge {
    @FunctionalInterface
    public interface Provider {
        /** Degrees C the room at {@code pos} sits above (or below) the open-air temperature; 0 outdoors. */
        double roomDelta(ServerLevel level, BlockPos pos);
    }

    private static volatile Provider provider;

    private ClimateBridge() {}

    public static void setProvider(Provider p) {
        provider = p;
    }

    public static double delta(ServerLevel level, BlockPos pos) {
        Provider p = provider;
        if (p == null) {
            return 0.0;
        }
        try {
            double d = p.roomDelta(level, pos);
            return Double.isFinite(d) ? d : 0.0;
        } catch (Throwable t) {
            return 0.0;
        }
    }
}
