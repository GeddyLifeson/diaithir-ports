package com.github.ysbbbbbb.kaleidoscopetavern.item;

import com.github.ysbbbbbb.kaleidoscopetavern.block.deco.SandwichBoardBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SandwichBoardBlockItem extends BlockItem {
    private @Nullable List<String> transformItemNames;

    public SandwichBoardBlockItem(Identifier id, Supplier<? extends Block> block) {
        super(block.get(), new Properties()
                .setId(ResourceKey.create(Registries.ITEM, id))
                .overrideDescription("item.kaleidoscope_tavern.sandwich_board")
        );
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        if (this.getBlock() instanceof SandwichBoardBlock sandwichBoardBlock) {
            List<Item> transformItems = sandwichBoardBlock.getTransformItems();
            if (this.transformItemNames == null && !transformItems.isEmpty()) {
                this.transformItemNames = transformItems.stream()
                        .map(Item::getDescriptionId)
                        .toList();
            }
            if (this.transformItemNames != null) {
                this.transformItemNames.forEach(name -> builder.accept(Component.translatable(name).withStyle(ChatFormatting.GRAY)));
            }
        }
    }
}
