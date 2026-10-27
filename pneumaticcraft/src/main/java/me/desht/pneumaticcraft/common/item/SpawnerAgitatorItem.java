package me.desht.pneumaticcraft.common.item;

import net.minecraft.world.item.Item;
import me.desht.pneumaticcraft.common.semiblock.SemiblockItem;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.fml.ModList;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class SpawnerAgitatorItem extends SemiblockItem {
    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay__, java.util.function.Consumer<Component> tooltipConsumer__, TooltipFlag pIsAdvanced) {
        List<Component> pTooltipComponents = me.desht.pneumaticcraft.compat.TooltipList.of(tooltipConsumer__);
        super.appendHoverText(pStack, context, tooltipDisplay__, tooltipConsumer__, pIsAdvanced);

        if (ModList.get().isLoaded(ModIds.APOTHIC_SPAWNERS)) {
            pTooltipComponents.add(xlate("gui.tooltip.item.pneumaticcraft.spawner_agitator.apotheosis").withStyle(ChatFormatting.GOLD));
        }
    }
}
