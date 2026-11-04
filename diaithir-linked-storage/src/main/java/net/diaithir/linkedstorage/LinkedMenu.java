package net.diaithir.linkedstorage;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/** 27 shared slots over the player inventory. */
public final class LinkedMenu extends AbstractContainerMenu {
    public LinkedMenu(int id, Inventory inv, IItemHandler handler) {
        super(Menus.LINKED.get(), id);
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) addSlot(new SlotItemHandler(handler, c + r * 9, 8 + c * 18, 18 + r * 18));
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c + r * 9 + 9, 8 + c * 18, 85 + r * 18));
        for (int c = 0; c < 9; c++) addSlot(new Slot(inv, c, 8 + c * 18, 143));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack moved = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack in = slot.getItem();
            moved = in.copy();
            boolean fromChest = index < LinkedData.CHEST_SLOTS;
            boolean ok = fromChest ? moveItemStackTo(in, LinkedData.CHEST_SLOTS, slots.size(), true) : moveItemStackTo(in, 0, LinkedData.CHEST_SLOTS, false);
            if (!ok) return ItemStack.EMPTY;
            if (in.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return moved;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
