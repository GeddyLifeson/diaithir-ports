package net.diaithir.relics;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

/** Which relics a player has in force: trinkets in Curios slots (found by reflection, so Curios stays optional) or the off hand. */
public final class Equipped {
    private Equipped() {}

    public record Slot(DiaithirRelics.Def def, ItemStack stack) {}

    private static boolean tried;
    private static Method getInv, findCurios, stackOf;

    private static void init() {
        if (tried) return;
        tried = true;
        if (!ModList.get().isLoaded("curios")) return;
        try {
            Class<?> api = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            Class<?> handler = Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler");
            Class<?> result = Class.forName("top.theillusivec4.curios.api.SlotResult");
            getInv = api.getMethod("getCuriosInventory", LivingEntity.class);
            findCurios = handler.getMethod("findCurios", Predicate.class);
            stackOf = result.getMethod("stack");
        } catch (Throwable t) {
            getInv = null;
        }
    }

    public static boolean curiosPresent() {
        init();
        return getInv != null;
    }

    /** Curios inventory handler of an entity, or null. */
    public static Object curios(LivingEntity e) {
        init();
        if (getInv == null) return null;
        try {
            Optional<?> o = (Optional<?>) getInv.invoke(null, e);
            return o.orElse(null);
        } catch (Throwable t) {
            return null;
        }
    }

    public static List<Slot> trinkets(LivingEntity p) {
        List<Slot> out = new ArrayList<>();
        Object h = curios(p);
        if (h != null) {
            try {
                Predicate<ItemStack> isRelic = s -> {
                    DiaithirRelics.Def d = DiaithirRelics.defOf(s);
                    return d != null && d.trinket();
                };
                for (Object r : (List<?>) findCurios.invoke(h, isRelic)) {
                    ItemStack s = (ItemStack) stackOf.invoke(r);
                    out.add(new Slot(DiaithirRelics.defOf(s), s));
                }
            } catch (Throwable t) {
                // Curios API changed: fall back to the off hand below
            }
        }
        ItemStack off = p.getItemInHand(InteractionHand.OFF_HAND);
        DiaithirRelics.Def d = DiaithirRelics.defOf(off);
        if (d != null && d.trinket()) out.add(new Slot(d, off));
        return out;
    }

    /** The relic in the main hand (weapons) or null. */
    public static Slot held(ServerPlayer p) {
        ItemStack s = p.getMainHandItem();
        DiaithirRelics.Def d = DiaithirRelics.defOf(s);
        return d != null && !d.trinket() && !d.kind().equals("shield") ? new Slot(d, s) : null;
    }

    /** The shield relic being used to block, or null. */
    public static Slot blocking(ServerPlayer p) {
        if (!p.isBlocking()) return null;
        ItemStack s = p.getUseItem();
        DiaithirRelics.Def d = DiaithirRelics.defOf(s);
        return d != null && d.kind().equals("shield") ? new Slot(d, s) : null;
    }
}
