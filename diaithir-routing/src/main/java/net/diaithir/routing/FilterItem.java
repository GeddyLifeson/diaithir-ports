package net.diaithir.routing;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** The shared filter. Use it on its own to flip mode; sneak-use with another item in the off hand to add or remove that item. */
public final class FilterItem extends Item {
    public FilterItem(Properties properties) {
        super(properties);
    }

    public static FilterData read(ItemStack stack) {
        return stack.getOrDefault(RoutingRefs.filterData(), FilterData.EMPTY);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack self = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        FilterData d = read(self);
        ItemStack other = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (player.isShiftKeyDown() && !other.isEmpty()) {
            FilterData n = d.withToggled(other);
            self.set(RoutingRefs.filterData(), n);
            player.sendOverlayMessage(Component.literal(n.entries().size() == d.entries().size() && d.entries().size() >= FilterData.MAX_ENTRIES
                    ? "Filter is full" : n.describe()));
        } else if (player.isShiftKeyDown()) {
            self.set(RoutingRefs.filterData(), d.withMatchTag(!d.matchTag()));
            player.sendOverlayMessage(Component.literal(read(self).describe()));
        } else {
            self.set(RoutingRefs.filterData(), d.withBlacklist(!d.blacklist()));
            player.sendOverlayMessage(Component.literal(read(self).describe()));
        }
        return InteractionResult.SUCCESS;
    }
}
