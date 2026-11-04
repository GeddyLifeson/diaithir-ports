/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.item;

import li.cil.oc2.api.API;
import li.cil.oc2.common.util.ColorUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

public final class HardDriveWithExternalDataItem extends AbstractBlockDeviceItem {
    public HardDriveWithExternalDataItem(final Identifier defaultData, final DyeColor defaultColor) {
        super(
            new Item.Properties()
                .component(DataComponents.DYED_COLOR, new DyedItemColor(defaultColor.getTextureDiffuseColor()))
                .overrideDescription(Util.makeDescriptionId("item", Identifier.fromNamespaceAndPath(API.MOD_ID, "hard_drive"))),
            defaultData
        );
    }
}
