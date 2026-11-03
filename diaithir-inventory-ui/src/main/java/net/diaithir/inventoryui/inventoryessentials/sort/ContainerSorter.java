package net.diaithir.inventoryui.inventoryessentials.sort;

import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class ContainerSorter {
    public static List<? extends String> categoryOrder = List.of("tools", "weapons", "armor", "blocks", "food", "other");

    private ContainerSorter() {}

    /** Sorts the container group of the first sortable slot; prefers the non-player container if present. */
    public static boolean sort(AbstractContainerMenu menu, Player player, IntSet lockedPlayerSlots) {
        List<Slot> target = pickGroup(menu, player);
        if (target.isEmpty()) return false;
        Container backing = target.get(0).container;
        boolean isPlayer = backing instanceof Inventory;
        List<Slot> slots = new ArrayList<>();
        for (Slot s : target) {
            if (isPlayer && lockedPlayerSlots.contains(s.getContainerSlot())) continue;
            slots.add(s);
        }
        if (slots.isEmpty()) return false;

        List<ItemStack> stacks = new ArrayList<>();
        for (Slot s : slots) {
            ItemStack st = s.getItem();
            if (!st.isEmpty()) stacks.add(st.copy());
        }
        List<ItemStack> merged = new ArrayList<>();
        for (ItemStack st : stacks) {
            for (ItemStack m : merged) {
                if (st.isEmpty()) break;
                if (ItemStack.isSameItemSameComponents(m, st)) {
                    int room = m.getMaxStackSize() - m.getCount();
                    int mv = Math.min(room, st.getCount());
                    if (mv > 0) {
                        m.grow(mv);
                        st.shrink(mv);
                    }
                }
            }
            if (!st.isEmpty()) merged.add(st);
        }
        merged.sort(comparator());

        List<ItemStack> result = new ArrayList<>(slots.size());
        int idx = 0;
        for (Slot s : slots) {
            ItemStack want = ItemStack.EMPTY;
            while (idx < merged.size()) {
                ItemStack cand = merged.get(idx);
                if (s.mayPlace(cand)) {
                    want = cand;
                    idx++;
                }
                break;
            }
            result.add(want);
        }
        if (idx < merged.size()) return false; // could not place everything; abort without changes

        for (int i = 0; i < slots.size(); i++) {
            Slot s = slots.get(i);
            ItemStack want = result.get(i);
            if (!ItemStack.matches(s.getItem(), want)) {
                s.set(want.copy());
            }
        }
        menu.broadcastChanges();
        return true;
    }

    private static List<Slot> pickGroup(AbstractContainerMenu menu, Player player) {
        List<Slot> external = new ArrayList<>();
        List<Slot> own = new ArrayList<>();
        for (Slot s : menu.slots) {
            if (!sortable(s)) continue;
            if (s.container instanceof Inventory) {
                if (s.getContainerSlot() < 36) own.add(s);
            } else {
                external.add(s);
            }
        }
        return external.isEmpty() ? own : external;
    }

    public static boolean sortable(Slot s) {
        if (s instanceof ResultSlot) return false;
        Class<?> c = s.getClass();
        if (c != Slot.class) return false;
        Container ct = s.container;
        if (ct instanceof Inventory) return s.getContainerSlot() < 36;
        if (ct instanceof SimpleContainer) return true;
        String n = ct.getClass().getName();
        if (n.startsWith("net.minecraft.world.level.block.entity.") && !n.contains("Furnace") && !n.contains("BrewingStand")
                && !n.contains("Crafter") && !n.contains("Lectern") && !n.contains("Jukebox")) {
            return true;
        }
        return false;
    }

    public static Comparator<ItemStack> comparator() {
        return Comparator
                .comparingInt((ItemStack s) -> SortCategories.rank(s, categoryOrder))
                .thenComparing(s -> BuiltInRegistries.ITEM.getKey(s.getItem()).toString())
                .thenComparingInt(s -> s.getComponents().hashCode())
                .thenComparing(Comparator.comparingInt(ItemStack::getCount).reversed());
    }
}
