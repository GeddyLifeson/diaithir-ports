package com.github.ysbbbbbb.kaleidoscopetavern.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class PaintingBlockItem extends BlockItem {
    private @Nullable String tooltipKey;

    public PaintingBlockItem(Identifier id, Supplier<? extends Block> block) {
        super(block.get(), new Properties()
                .setId(ResourceKey.create(Registries.ITEM, id))
                .overrideDescription("item.kaleidoscope_tavern.painting")
        );
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        if (this.tooltipKey == null) {
            this.tooltipKey = Util.makeDescriptionId("tooltip", BuiltInRegistries.BLOCK.getKey(this.getBlock()));
        }
        builder.accept(Component.translatable(this.tooltipKey).withStyle(ChatFormatting.GRAY));
    }
}
