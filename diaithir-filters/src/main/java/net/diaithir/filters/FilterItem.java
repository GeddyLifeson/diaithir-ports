package net.diaithir.filters;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/** The filter and the filter card. The card is the stackable variant meant to be slotted into other modules' machines. */
public final class FilterItem extends Item {
    private final boolean card;

    public FilterItem(Properties properties, boolean card) {
        super(properties);
        this.card = card;
    }

    public boolean card() {
        return card;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            sp.openMenu(new SimpleMenuProvider((id, inv, p) -> new FilterMenu(id, inv, hand), Component.translatable(getDescriptionId())),
                    buf -> buf.writeEnum(hand));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        FilterData d = Filters.data(stack);
        if (d.rules().isEmpty()) {
            lines.accept(Component.translatable("tooltip.diaithirfilters.empty"));
        } else {
            for (Rule r : d.rules()) lines.accept(Component.literal(Rule.describe(r)));
        }
    }
}
