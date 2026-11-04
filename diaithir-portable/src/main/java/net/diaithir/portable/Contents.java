package net.diaithir.portable;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

/** A container backed by a data component on an ItemStack, or an in-memory list for a block. Stack limit follows installed stack upgrades. */
public final class Contents extends SimpleContainer {
    private final Runnable onChange;
    private int stackLimit = 99;

    public Contents(int size, Runnable onChange) {
        super(size);
        this.onChange = onChange;
    }

    public void setStackLimit(int limit) {
        this.stackLimit = limit;
    }

    @Override
    public int getMaxStackSize() {
        return stackLimit;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Math.min(stackLimit, Math.max(stack.getMaxStackSize(), 1) * Math.max(1, stackLimit / 64));
    }

    @Override
    public void setChanged() {
        super.setChanged();
        onChange.run();
    }

    public static Contents load(ItemStack host, DataComponentType<ItemContainerContents> type, int size, Runnable onChange) {
        Contents c = new Contents(size, () -> {
        });
        ItemContainerContents data = host.get(type);
        if (data != null) data.copyInto(c.getItems());
        return new Contents(size, onChange).fillFrom(c);
    }

    private Contents fillFrom(Contents other) {
        for (int i = 0; i < Math.min(getContainerSize(), other.getContainerSize()); i++) super.setItem(i, other.getItem(i));
        return this;
    }

    public void saveInto(ItemStack host, DataComponentType<ItemContainerContents> type) {
        host.set(type, ItemContainerContents.fromItems(getItems()));
    }

    public NonNullList<ItemStack> list() {
        return getItems();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public static boolean same(Container a, Container b) {
        return a == b;
    }
}
