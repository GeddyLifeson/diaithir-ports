package net.diaithir.goety.magic;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** The Totem of Souls: carried in the inventory, it holds the souls the wands spend. */
public class TotemItem extends Item {
    public TotemItem(Properties props) {
        super(props);
    }

    @Override public boolean isBarVisible(ItemStack s) { return Souls.get(s) > 0; }
    @Override public int getBarWidth(ItemStack s) { return Mth.clamp(Math.round(13.0F * Souls.get(s) / Souls.MAX), 0, 13); }
    @Override public int getBarColor(ItemStack s) { return 0x8FE6DF; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> out, TooltipFlag flag) {
        out.accept(Component.translatable("goety.tooltip.souls", Souls.get(stack), Souls.MAX).withStyle(ChatFormatting.AQUA));
    }
}
