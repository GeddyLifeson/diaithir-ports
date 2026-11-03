/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.item;

import li.cil.oc2.api.API;
import net.minecraft.util.Util;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

public final class MemoryItem extends AbstractStorageItem {
    @Nullable private String descriptionId;

    ///////////////////////////////////////////////////////////////////

    public MemoryItem(final int defaultCapacity) {
        super(defaultCapacity);
    }

    ///////////////////////////////////////////////////////////////////

    // Item#getOrCreateDescriptionId no longer exists in 26.1, so this no longer overrides anything.
    protected String getOrCreateDescriptionId() {
        if (descriptionId == null) {
            descriptionId = Util.makeDescriptionId("item", Identifier.fromNamespaceAndPath(API.MOD_ID, "memory"));
        }
        return descriptionId;
    }
}
