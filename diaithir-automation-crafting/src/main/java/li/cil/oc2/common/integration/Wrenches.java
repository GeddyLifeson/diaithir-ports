/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.integration;

import li.cil.oc2.common.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public final class Wrenches {
    public static boolean isWrench(@Nullable final ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.is(ItemTags.WRENCHES);
    }

    public static boolean isHoldingWrench(final Entity entity) {
        if (entity instanceof final LivingEntity livingEntity) {
            if (isWrench(livingEntity.getMainHandItem())) {
                return true;
            }
            if (isWrench(livingEntity.getOffhandItem())) {
                return true;
            }
        }

        return false;
    }
}
