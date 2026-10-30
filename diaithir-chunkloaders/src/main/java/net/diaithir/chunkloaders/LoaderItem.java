package net.diaithir.chunkloaders;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public final class LoaderItem extends BlockItem {
    private final int tier;

    public LoaderItem(Block block, Properties properties, int tier) {
        super(block, properties);
        this.tier = tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        int r = Config.RADIUS[tier].get(), side = 2 * r + 1;
        lines.accept(Component.literal("Keeps " + side + "x" + side + " chunks loaded").withStyle(net.minecraft.ChatFormatting.GRAY));
        lines.accept(Component.literal("Burns " + Config.COST[tier].get() * 20 + " fuel ticks a second").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
        lines.accept(Component.literal("Use with fuel to feed it, empty-handed to read it").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
    }
}
