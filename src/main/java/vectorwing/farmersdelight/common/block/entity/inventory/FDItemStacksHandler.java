package vectorwing.farmersdelight.common.block.entity.inventory;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

/**
 * An item inventory on NeoForge's transfer API that keeps the slot accessors Farmer's Delight's block entities use.
 * Mutating a returned stack directly (grow, shrink) does not fire {@link #onContentsChanged}, as with the old ItemStackHandler.
 */
public class FDItemStacksHandler extends ItemStacksResourceHandler
{
	public FDItemStacksHandler(int size) {
		super(size);
	}

	public ItemStack getStackInSlot(int slot) {
		return stacks.get(slot);
	}

	public void setStackInSlot(int slot, ItemStack stack) {
		ItemStack previous = stacks.get(slot);
		stacks.set(slot, stack);
		onContentsChanged(slot, previous);
	}

	/** Inserts as much of {@code stack} as fits into {@code slot}; returns what did not fit. */
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (stack.isEmpty()) return stack;
		ItemStack current = stacks.get(slot);
		if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, stack)) return stack;
		int moved = Math.min(getCapacity(slot, ItemResource.of(stack)) - current.getCount(), stack.getCount());
		if (moved <= 0) return stack;
		if (!simulate) setStackInSlot(slot, stack.copyWithCount(current.getCount() + moved));
		return stack.copyWithCount(stack.getCount() - moved);
	}

	/** Takes up to {@code amount} items out of {@code slot}; returns what was taken. */
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack current = stacks.get(slot);
		int taken = Math.min(amount, current.getCount());
		if (taken <= 0) return ItemStack.EMPTY;
		if (!simulate) setStackInSlot(slot, current.copyWithCount(current.getCount() - taken));
		return current.copyWithCount(taken);
	}
}
