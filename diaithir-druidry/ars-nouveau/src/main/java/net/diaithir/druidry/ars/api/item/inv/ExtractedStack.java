package net.diaithir.druidry.ars.api.item.inv;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

/** A stack that was extracted from a handler slot, remembering where it came from. */
public class ExtractedStack {
    private static final ExtractedStack EMPTY = new ExtractedStack(ItemStack.EMPTY, SlotReference.empty());

    public ItemStack stack;
    public final SlotReference slot;

    public ExtractedStack(ItemStack stack, SlotReference slot) {
        this.stack = stack;
        this.slot = slot;
    }

    public static ExtractedStack empty() {
        return EMPTY;
    }

    /** Actually extracts up to count items from the referenced slot. */
    public static ExtractedStack from(SlotReference ref, int count) {
        if (ref.isEmpty()) {
            return EMPTY;
        }
        ItemStack extracted = ref.getHandler().extractItem(ref.getSlot(), count, false);
        return extracted.isEmpty() ? EMPTY : new ExtractedStack(extracted, ref);
    }

    public static ExtractedStack from(IItemHandler handler, int slot, int count) {
        return from(new SlotReference(handler, slot), count);
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public ItemStack getStack() {
        return stack;
    }

    /** Puts the extracted items back where they came from; returns whatever did not fit. */
    public ItemStack restore() {
        return slot.isEmpty() ? stack : slot.getHandler().insertItem(slot.getSlot(), stack, false);
    }

    /** Returns the stack to its source slot, dropping any remainder at pos. */
    public void returnOrDrop(Level level, BlockPos pos) {
        if (isEmpty()) {
            return;
        }
        ItemStack remainder = restore();
        if (!remainder.isEmpty()) {
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, remainder.copy()));
        }
        this.stack = ItemStack.EMPTY;
    }

    public void replaceAndReturnOrDrop(ItemStack newStack, Level level, BlockPos pos) {
        this.stack = newStack;
        returnOrDrop(level, pos);
    }
}
