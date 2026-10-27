package net.diaithir.perf;

import java.lang.reflect.Method;

/** Asks Distant Horizons whether its ignored-dimension list covers a level wrapper, by reflection so the mod is not a build dependency. */
public final class DhIgnore {
    private static Method dimensionName, shouldBeIgnored;
    private static Object handler;
    private static boolean failed;

    private DhIgnore() {}

    public static synchronized boolean ignored(Object levelWrapper) {
        if (failed) return false;
        try {
            ClassLoader cl = levelWrapper.getClass().getClassLoader();
            if (dimensionName == null) {
                dimensionName = Class.forName("com.seibel.distanthorizons.core.wrapperInterfaces.world.ILevelWrapper", false, cl).getMethod("getDimensionName");
                Class<?> h = Class.forName("com.seibel.distanthorizons.core.config.eventHandlers.IgnoredDimensionCsvHandler", false, cl);
                handler = h.getField("INSTANCE").get(null);
                shouldBeIgnored = h.getMethod("dimensionNameShouldBeIgnored", String.class);
            }
            if (handler == null) return false;
            return (Boolean) shouldBeIgnored.invoke(handler, (String) dimensionName.invoke(levelWrapper));
        } catch (Throwable t) {
            failed = true;
            return false;
        }
    }
}
