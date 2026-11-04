package net.diaithir.goety.magic;

import net.diaithir.goety.GoetyComponents;
import net.diaithir.goety.GoetyItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import java.util.List;

/** Soul energy: stored in totems of souls carried in the inventory. */
public final class Souls {
    public static final int MAX = 10000;

    private Souls() {}

    public static boolean isTotem(ItemStack s) {
        return s.has(GoetyComponents.SOULS.get());
    }

    public static int get(ItemStack totem) {
        return totem.getOrDefault(GoetyComponents.SOULS.get(), 0);
    }

    public static void set(ItemStack totem, int souls) {
        int v = Math.max(0, Math.min(MAX, souls));
        totem.set(GoetyComponents.SOULS.get(), v);
        // drives the item model: 0 empty, then lesser, mid and full totem sprites
        totem.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(v / (float) MAX), List.of(), List.of(), List.of()));
    }

    public static int total(Player p) {
        int t = 0;
        Inventory inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) if (isTotem(inv.getItem(i))) t += get(inv.getItem(i));
        return t;
    }

    public static boolean consume(Player p, int amount) {
        if (p.isCreative()) return true;
        if (total(p) < amount) return false;
        Inventory inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize() && amount > 0; i++) {
            ItemStack s = inv.getItem(i);
            if (!isTotem(s)) continue;
            int take = Math.min(get(s), amount);
            set(s, get(s) - take);
            amount -= take;
        }
        return true;
    }

    /** Adds souls to the carried totems; returns how many were stored. */
    public static int add(Player p, int amount) {
        int stored = 0;
        Inventory inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize() && amount > 0; i++) {
            ItemStack s = inv.getItem(i);
            if (!isTotem(s)) continue;
            int put = Math.min(MAX - get(s), amount);
            if (put > 0) { set(s, get(s) + put); amount -= put; stored += put; }
        }
        return stored;
    }

    static void touch() { GoetyItems.ALL.size(); }
}
