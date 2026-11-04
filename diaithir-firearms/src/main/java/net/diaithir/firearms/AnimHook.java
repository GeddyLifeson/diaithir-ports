package net.diaithir.firearms;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.minecraft.server.level.ServerPlayer;

/** Soft link to diaithir-animation (Anim.gun): plays the fire, dry, reload and inspect clips. A no-op when that mod is absent. */
final class AnimHook {
    private static final MethodHandle GUN = find();

    private static MethodHandle find() {
        try {
            return MethodHandles.publicLookup().findStatic(Class.forName("net.diaithir.animation.Anim"), "gun", MethodType.methodType(void.class, ServerPlayer.class, String.class));
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    static void gun(ServerPlayer p, String action) {
        if (GUN == null) return;
        try { GUN.invoke(p, action); } catch (Throwable ignored) { }
    }

    private AnimHook() {}
}
