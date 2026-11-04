package net.diaithir.druidry.ars.api.item.inv;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

/** A handler plus slot index pointing at a stack. */
public class SlotReference {
    private static final SlotReference EMPTY = new SlotReference(null, -1);

    private final IItemHandler handler;
    private final int slot;

    public SlotReference(IItemHandler handler, int slot) {
        this.handler = handler;
        this.slot = slot;
    }

    public static SlotReference empty() {
        return EMPTY;
    }

    public boolean isEmpty() {
        return handler == null || slot < 0 || slot >= handler.getSlots();
    }

    public IItemHandler getHandler() {
        return handler;
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getStack() {
        return isEmpty() ? ItemStack.EMPTY : handler.getStackInSlot(slot);
    }
}
