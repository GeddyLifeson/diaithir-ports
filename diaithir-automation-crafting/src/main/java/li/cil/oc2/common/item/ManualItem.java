/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.item;

import li.cil.oc2.common.util.TooltipUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// TODO(oc2-restore): Re-extend AbstractManualItem once the manual API (li.cil.manual) and the client manual are restored.
public final class ManualItem extends Item {
    public ManualItem() {
        super(new Item.Properties());
    }

    ///////////////////////////////////////////////////////////////////

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(final ItemStack stack, final Item.TooltipContext context, final TooltipDisplay display, final Consumer<Component> builder, final TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        final List<Component> tooltip = new ArrayList<>();
        TooltipUtils.tryAddDescription(stack, tooltip);
        tooltip.forEach(builder);
    }

    ///////////////////////////////////////////////////////////////////

    // TODO(oc2-restore): getManualModel() returned Manuals.MANUAL.get()
    // TODO(oc2-restore): getManualStyle() returned ModManualStyle.INSTANCE
    // TODO(oc2-restore): getScreenStyle() returned ModManualScreenStyle.INSTANCE
}
