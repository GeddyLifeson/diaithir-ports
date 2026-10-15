package me.desht.pneumaticcraft.common.item;

import net.minecraft.world.item.Item;

import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

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
    public void appendHoverText(ItemStack pStack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, context, display, pTooltipComponents, pIsAdvanced);

        if (ModList.get().isLoaded(ModIds.APOTHIC_SPAWNERS)) {
            pTooltipComponents.accept(xlate("gui.tooltip.item.pneumaticcraft.spawner_agitator.apotheosis").withStyle(ChatFormatting.GOLD));
        }
    }
}
