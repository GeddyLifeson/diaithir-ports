package net.diaithir.bridges;

import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

import java.lang.reflect.Method;

/**
 * Soft link to the trains lane: {@code net.diaithir.carts.MagicFuel.fitted(cart)} and {@code charge(cart, joules)} (1 FE = 1 J). Reflection, so this
 * mod loads and the charger simply idles when diaithir-carts is absent. UNTESTED against a real diaithir-carts build until that PR (#228) merges.
 */
final class CartMagic {
    private CartMagic() {}

    private static boolean resolved;
    private static Method fitted, charge;

    private static synchronized void resolve() {
        if (resolved) return;
        resolved = true;
        try {
            Class<?> c = Class.forName("net.diaithir.carts.MagicFuel");
            fitted = c.getMethod("fitted", AbstractMinecart.class);
            charge = c.getMethod("charge", AbstractMinecart.class, long.class);
        } catch (ReflectiveOperationException | LinkageError e) {
            fitted = null; charge = null;
        }
    }

    static boolean available() { resolve(); return charge != null; }

    /** True for a locomotive fitted with a mana firebox or cell. */
    static boolean fitted(AbstractMinecart cart) {
        resolve();
        try { return fitted != null && (Boolean) fitted.invoke(null, cart); } catch (ReflectiveOperationException e) { return false; }
    }

    /** Adds joules to the locomotive's magic bank; false if the trains lane is absent or the call failed. */
    static boolean charge(AbstractMinecart cart, long joules) {
        resolve();
        if (charge == null) return false;
        try { charge.invoke(null, cart, joules); return true; } catch (ReflectiveOperationException e) { return false; }
    }
}
