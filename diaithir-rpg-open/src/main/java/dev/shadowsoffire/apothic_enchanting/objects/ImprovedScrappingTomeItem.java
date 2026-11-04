package dev.shadowsoffire.apothic_enchanting.objects;

import java.util.Random;
import java.util.function.Consumer;

import dev.shadowsoffire.apothic_enchanting.util.TooltipUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

public class ImprovedScrappingTomeItem extends Item {

    static Random rand = new Random();

    public ImprovedScrappingTomeItem(Item.Properties properties) {
        super(properties);
    }


    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
        if (stack.isEnchanted()) return;
        tooltip.accept(TooltipUtil.lang("info", "improved_scrap_tome").withStyle(ChatFormatting.GRAY));
        tooltip.accept(TooltipUtil.lang("info", "improved_scrap_tome2").withStyle(ChatFormatting.GRAY));
    }

    public static boolean updateAnvil(AnvilUpdateEvent ev) {
        ItemStack weapon = ev.getLeft();
        ItemStack book = ev.getRight();
        if (!(book.getItem() instanceof ImprovedScrappingTomeItem) || book.isEnchanted() || !weapon.isEnchanted()) return false;

        ItemEnchantments wepEnch = EnchantmentHelper.getEnchantmentsForCrafting(weapon);
        ItemStack out = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantmentHelper.setEnchantments(out, wepEnch);
        ev.setMaterialCost(1);
        ev.setXpCost(wepEnch.size() * 10);
        ev.setOutput(out);
        return true;
    }
}
