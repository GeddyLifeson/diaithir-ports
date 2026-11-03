/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;

public final class FloppyItem extends AbstractStorageItem {
    // TODO(oc2-restore): value mirrors CustomItemColors.BROWN from li.cil.oc2.client.item, which is excluded from this compile stage. Verify the value when the client package is restored.
    private static final int BROWN = 0x664C33;

    public FloppyItem(final int capacity) {
        super(
            new Item.Properties()
                .component(DataComponents.DYED_COLOR, new DyedItemColor(BROWN)),
            capacity
        );
    }
}
