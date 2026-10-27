package net.createmod.catnip.platform.services;

import java.util.function.Supplier;
import net.createmod.catnip.platform.Env;

public interface PlatformHelper {
    Env getEnv();
    default void executeOnClientOnly(Supplier<Runnable> toRun) { if (getEnv().isClient()) toRun.get().run(); }
    default void executeOnServerOnly(Supplier<Runnable> toRun) { if (getEnv().isServer()) toRun.get().run(); }
}
