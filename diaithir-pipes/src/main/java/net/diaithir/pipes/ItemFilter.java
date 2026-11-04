package net.diaithir.pipes;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.List;

/** Nine ghost slots with whitelist or blacklist mode. Empty list lets everything through. */
public final class ItemFilter {
    public static final int SLOTS = 9;

    private final List<ItemResource> items = new ArrayList<>();
    private boolean blacklist;

    public boolean allows(ItemResource r) {
        if (items.isEmpty()) return true;
        return items.contains(r) != blacklist;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public List<ItemResource> items() {
        return items;
    }

    public boolean blacklist() {
        return blacklist;
    }

    public void setBlacklist(boolean b) {
        blacklist = b;
    }

    public void toggleMode() {
        blacklist = !blacklist;
    }

    /** Sets one ghost slot from a stack; empty stack clears it. Slots are kept compact. */
    public void setSlot(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SLOTS) return;
        while (items.size() <= slot) items.add(ItemResource.EMPTY);
        items.set(slot, stack.isEmpty() ? ItemResource.EMPTY : ItemResource.of(stack));
        items.removeIf(ItemResource::isEmpty);
    }

    public ItemStack ghost(int slot) {
        return slot >= 0 && slot < items.size() ? items.get(slot).toStack(1) : ItemStack.EMPTY;
    }
}
