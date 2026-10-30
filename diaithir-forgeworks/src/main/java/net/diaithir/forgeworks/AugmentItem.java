package net.diaithir.forgeworks;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public final class AugmentItem extends Item {
    public final Augment augment;

    public AugmentItem(Augment augment, Properties properties) {
        super(properties);
        this.augment = augment;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        String text = switch (augment) {
            case SPEED -> "Machine works faster, at a small extra FE cost per job";
            case EFFICIENCY -> "Each job costs less FE";
            case CHANCE -> "Bonus outputs turn up more often";
        };
        lines.accept(Component.literal(text).withStyle(net.minecraft.ChatFormatting.GRAY));
        lines.accept(Component.literal("Put in the augment slot, up to " + Config.MAX_AUGMENTS.get()).withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
    }
}
