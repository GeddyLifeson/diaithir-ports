/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.item;

import li.cil.oc2.api.API;
import net.minecraft.resources.Identifier;

public final class CustomItemModelProperties {
    public static final Identifier COLOR_PROPERTY = Identifier.fromNamespaceAndPath(API.MOD_ID, "color");

    ///////////////////////////////////////////////////////////////////

    public static void initialize() {
        // TODO(oc2-restore): ItemProperties no longer exists in 26.1.2. Item model properties are now
        //  driven by data (item model definitions) and registered through dedicated events. The color
        //  property for the hard drives and floppies (CustomItemColors.getColor(stack)) used to be
        //  registered here for HARD_DRIVE_SMALL, HARD_DRIVE_MEDIUM, HARD_DRIVE_LARGE,
        //  HARD_DRIVE_EXTRA_LARGE, FLOPPY and FLOPPY_MODERN.
    }
}
