package net.diaithir.druidry.ars.common.items.curios;

import net.diaithir.druidry.ars.api.mana.IManaDiscountEquipment;
import net.diaithir.druidry.ars.api.spell.Spell;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class DiscountRing extends AbstractManaCurio implements IManaDiscountEquipment {

    public abstract int getManaDiscount();

    @Override
    public int getMaxManaBoost(ItemStack i) {
        return 10;
    }

    @Override
    public int getManaRegenBonus(ItemStack i) {
        return 1;
    }

    @Override
    public int getManaDiscount(ItemStack i, Spell spell) {
        return getManaDiscount();
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<net.minecraft.network.chat.Component> tooltip2, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltip2, flagIn);
        tooltip2.accept(Component.translatable("tooltip.discount_item", getManaDiscount(stack)));
    }
}
