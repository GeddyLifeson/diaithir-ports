package net.diaithir.freight;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** A load's item list (as kept inside a carried bay) seen as a Container. */
final class ListContainer implements Container {
    private final NonNullList<ItemStack> items;
    private final Runnable changed;

    ListContainer(NonNullList<ItemStack> items, Runnable changed) {
        this.items = items;
        this.changed = changed;
    }

    @Override public int getContainerSize() { return items.size(); }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int n) { ItemStack s = ContainerHelper.removeItem(items, slot, n); if (!s.isEmpty()) setChanged(); return s; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) { items.set(slot, stack); setChanged(); }
    @Override public void setChanged() { changed.run(); }
    @Override public boolean stillValid(Player p) { return true; }
    @Override public void clearContent() { items.clear(); setChanged(); }
}
