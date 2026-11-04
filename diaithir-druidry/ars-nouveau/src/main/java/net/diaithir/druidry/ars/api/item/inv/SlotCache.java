package net.diaithir.druidry.ars.api.item.inv;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

/** Remembers which slots of an inventory hold which item, to speed up repeated insertion. */
public class SlotCache {
    public final Map<Item, IntList> slotsByItem = new HashMap<>();
    public final Map<Item, Integer> lastAccepting = new HashMap<>();
    private final Map<Integer, Item> itemBySlot = new HashMap<>();

    public SlotCache(int slots) {
        IntList empty = new IntArrayList();
        for (int i = 0; i < slots; i++) {
            empty.add(i);
            itemBySlot.put(i, Items.AIR);
        }
        slotsByItem.put(Items.AIR, empty);
    }

    public IntList getIfPresent(Item item) {
        return slotsByItem.get(item);
    }

    public IntList getOrCreateSlots(Item item) {
        return slotsByItem.computeIfAbsent(item, i -> new IntArrayList());
    }

    public void initEmpty(int slot) {
        replaceSlotWithItem(Items.AIR, slot);
    }

    public void replaceSlotWithItem(Item item, int slot) {
        Item old = itemBySlot.put(slot, item);
        if (old != null && old != item) {
            IntList oldList = slotsByItem.get(old);
            if (oldList != null) {
                oldList.rem(slot);
            }
        }
        IntList list = getOrCreateSlots(item);
        if (!list.contains(slot)) {
            list.add(slot);
        }
    }
}
