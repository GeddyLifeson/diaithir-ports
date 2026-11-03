package net.diaithir.druidry.ars.common.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FireEssence extends ModItem {

    public FireEssence() {
        super();
        withTooltip(Component.translatable("tooltip.ars_nouveau.essences"));
    }

    @Override
    public int getBurnTime(@NotNull ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return 200 * 20; // 200 seconds
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<net.minecraft.network.chat.Component> tooltip2, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltip2, flagIn);
        tooltip2.accept(Component.translatable("ars_nouveau.fire_essence.tooltip").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
    }
}
