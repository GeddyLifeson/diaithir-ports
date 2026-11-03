/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.item;

import li.cil.oc2.api.API;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;

public final class HardDriveItem extends AbstractStorageItem {
    public HardDriveItem(final int capacity, final DyeColor defaultColor) {
        super(
            new Item.Properties()
                .component(DataComponents.DYED_COLOR, new DyedItemColor(defaultColor.getTextureDiffuseColor()))
                .overrideDescription(Util.makeDescriptionId("item", Identifier.fromNamespaceAndPath(API.MOD_ID, "hard_drive"))),
            capacity
        );
    }
}
