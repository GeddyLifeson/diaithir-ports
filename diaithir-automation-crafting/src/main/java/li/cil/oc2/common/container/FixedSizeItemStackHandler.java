/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.container;

import li.cil.oc2.common.util.NBTTagIds;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.items.ItemStackHandler;

public class FixedSizeItemStackHandler extends ItemStackHandler {
    private static final String SIZE_TAG_NAME = "Size";

    ///////////////////////////////////////////////////////////////////

    public FixedSizeItemStackHandler(final int size) {
        super(size);
    }

    public FixedSizeItemStackHandler(final NonNullList<ItemStack> stacks) {
        super(stacks);
    }

    ///////////////////////////////////////////////////////////////////

    public boolean isEmpty() {
        for (int slot = 0; slot < getSlots(); slot++) {
            if (!getStackInSlot(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void deserialize(final ValueInput input) {
        // Our size is fixed, don't trust NBT data we're loading.
        final int fixedSize = getSlots();
        super.deserialize(input);

        final int loadedSize = getSlots();
        if (loadedSize != fixedSize) {
            final ItemStack[] loaded = new ItemStack[loadedSize];
            for (int slot = 0; slot < loadedSize; slot++) {
                loaded[slot] = getStackInSlot(slot);
            }

            setSize(fixedSize);

            final int count = Math.min(fixedSize, loadedSize);
            for (int slot = 0; slot < count; slot++) {
                setStackInSlot(slot, loaded[slot]);
            }
        }
    }
}
