package de.ellpeck.actuallyadditions.mod.items;

import net.minecraft.world.item.Item;

import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

import de.ellpeck.actuallyadditions.mod.components.ActuallyComponents;
import de.ellpeck.actuallyadditions.mod.items.base.ItemBase;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
import java.util.List;

public class ItemTag extends ItemBase {
    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Item.TooltipContext pLevel, TooltipDisplay display, Consumer<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        var data = pStack.get(ActuallyComponents.ITEM_TAG);
/*        data.flatMap(ItemTagAttachment::getTag).ifPresent(itemTag -> {
            BuiltInRegistries.ITEM.get(itemTag).ifPresent(itemTagCollection -> {
                pTooltipComponents.accept(Component.literal("Valid Tag").withStyle(ChatFormatting.GREEN));
                pTooltipComponents.accept(Component.literal("Tag: " + itemTag.location()).withStyle(ChatFormatting.GRAY));
                pTooltipComponents.accept(Component.literal("Contains: " + itemTagCollection.stream().count() + " items").withStyle(ChatFormatting.GRAY));
            });
        });*/
        if(data != null) {
            pTooltipComponents.accept(Component.literal("Tag: " + data.toString()).withStyle(ChatFormatting.GRAY));
        } else {
            pTooltipComponents.accept(Component.translatable("tooltip.actuallyadditions.item_tag.no_tag").withStyle(ChatFormatting.RED));
        }

        super.appendHoverText(pStack, pLevel, display, pTooltipComponents, pIsAdvanced);
    }


/*    @Nonnull
    @Override
    public InteractionResult use(Level pLevel, @Nonnull Player pPlayer, @Nonnull InteractionHand pUsedHand) {
        if (!pLevel.isClientSide())
            pPlayer.openMenu(new SimpleMenuProvider((pId, pInventory, player) -> new ItemTagContainer(pId, pInventory), Component.translatable("container.actuallyadditions.item_tag")));

        return super.use(pLevel, pPlayer, pUsedHand);
    }*/
}
