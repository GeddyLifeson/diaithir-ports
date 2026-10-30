package net.diaithir.chunkloaders;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public final class CartSealItem extends Item {
    public CartSealItem(Properties properties) { super(properties); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        int side = 2 * Config.CART_RADIUS.get() + 1;
        lines.accept(Component.literal("Use on a minecart: keeps " + side + "x" + side + " chunks around it loaded").withStyle(net.minecraft.ChatFormatting.GRAY));
        lines.accept(Component.literal(Config.CART_MINUTES_PER_SEAL.get() + " minutes per seal, stacks up to " + Config.CART_MAX_MINUTES.get()).withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
    }
}
