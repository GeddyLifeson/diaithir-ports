/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.item;

import net.minecraft.util.Util;

public final class FlashMemoryItem extends AbstractStorageItem {
    public FlashMemoryItem(final int defaultCapacity) {
        super(createProperties().stacksTo(1)
            .overrideDescription(Util.makeDescriptionId("item", Items.FLASH_MEMORY.getId())), defaultCapacity);
    }
}
