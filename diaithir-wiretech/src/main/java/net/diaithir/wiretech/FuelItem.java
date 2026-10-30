package net.diaithir.wiretech;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/** A canister of liquid fuel for the diesel generator: burns for ticks at fePerTick. */
public final class FuelItem extends Item {
    public final int ticks;
    public final int fePerTick;

    public FuelItem(int ticks, int fePerTick, Properties properties) {
        super(properties);
        this.ticks = ticks;
        this.fePerTick = fePerTick;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.literal(fePerTick + " FE per tick for " + ticks / 20 + " s in a diesel generator").withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
