package net.diaithir.bridges;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Soft dependency on PneumaticCraft's air handler capability, by reflection, so this mod loads without it. Every failure turns the air side
 * off (available() false) and never throws. Class names follow the PneumaticCraft API (PNCCapabilities.AIR_HANDLER_MACHINE, IAirHandlerMachine).
 */
final class PneumaticAir {
    private PneumaticAir() {}

    private static boolean tried;
    private static @Nullable BlockCapability<Object, Direction> cap;
    private static @Nullable Method getAir, addAir, getPressure;

    @SuppressWarnings("unchecked")
    static synchronized boolean available() {
        if (!tried) {
            tried = true;
            try {
                Class<?> caps = Class.forName("me.desht.pneumaticcraft.api.PNCCapabilities");
                cap = (BlockCapability<Object, Direction>) caps.getField("AIR_HANDLER_MACHINE").get(null);
                Class<?> h = Class.forName("me.desht.pneumaticcraft.api.pressure.IAirHandlerMachine");
                getAir = h.getMethod("getAir");
                addAir = h.getMethod("addAir", int.class);
                getPressure = h.getMethod("getPressure");
            } catch (ReflectiveOperationException | LinkageError | ClassCastException e) {
                cap = null;
            }
        }
        return cap != null;
    }

    /** The neighbour's air handler on the face pointing at us, or null. */
    static @Nullable Object find(Level level, BlockPos pos, Direction face) {
        if (!available()) return null;
        try {
            return level.getCapability(cap, pos, face);
        } catch (RuntimeException e) {
            return null;
        }
    }

    static int air(Object h) {
        try { return (Integer) getAir.invoke(h); } catch (ReflectiveOperationException | RuntimeException e) { return 0; }
    }

    static float pressure(Object h) {
        try { return (Float) getPressure.invoke(h); } catch (ReflectiveOperationException | RuntimeException e) { return Float.MAX_VALUE; }
    }

    /** Adds (or removes, when negative) mL of air. */
    static void add(Object h, int ml) {
        try { addAir.invoke(h, ml); } catch (ReflectiveOperationException | RuntimeException ignored) { }
    }
}
