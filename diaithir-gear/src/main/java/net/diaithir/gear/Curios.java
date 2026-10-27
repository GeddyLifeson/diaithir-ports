package net.diaithir.gear;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Reflective Curios lookup so the mod needs no compile dependency; empty when Curios is absent or its API differs. */
final class Curios {
    private static boolean tried, ok;
    private static Method inv, find, stack;

    private Curios() {}

    private static synchronized boolean init() {
        if (tried) return ok;
        tried = true;
        try {
            Class<?> api = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            Class<?> handler = Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler");
            inv = api.getMethod("getCuriosInventoryOrNull", LivingEntity.class);
            find = handler.getMethod("findCurios", Predicate.class);
            stack = Class.forName("top.theillusivec4.curios.api.SlotResult").getMethod("stack");
            ok = true;
        } catch (ReflectiveOperationException | LinkageError ex) {
            DiaithirGear.LOG.info("Curios not available, accessory set families stay empty ({})", ex.toString());
        }
        return ok;
    }

    static boolean available() { return init(); }

    static List<ItemStack> worn(LivingEntity e, Predicate<ItemStack> p) {
        List<ItemStack> out = new ArrayList<>();
        if (!init()) return out;
        try {
            Object h = inv.invoke(null, e);
            if (h == null) return out;
            for (Object r : (List<?>) find.invoke(h, p)) out.add((ItemStack) stack.invoke(r));
        } catch (ReflectiveOperationException | RuntimeException ex) {
            DiaithirGear.LOG.warn("Curios lookup failed: {}", ex.toString());
        }
        return out;
    }
}
