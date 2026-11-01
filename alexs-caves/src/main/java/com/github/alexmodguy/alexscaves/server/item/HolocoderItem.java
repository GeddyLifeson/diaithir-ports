package com.github.alexmodguy.alexscaves.server.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HolocoderItem extends Item {
    public HolocoderItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag flagIn) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.isEmpty()) {
            Tag entity = tag.get("BoundEntityTag");
            if (entity instanceof CompoundTag) {
                Optional<EntityType<?>> optional = com.github.alexmodguy.alexscaves.server.misc.ACProps.entityTypeOf((CompoundTag) entity);
                if (optional.isPresent()) {
                    Component untranslated = optional.get().getDescription().copy().withStyle(ChatFormatting.GRAY);
                    tooltip.accept(untranslated);
                }
            }
        }
        super.appendHoverText(stack, context, display, tooltip, flagIn);
    }

    public static UUID getBoundEntityUUID(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.isEmpty() && tag.contains("BoundEntityUUID")) {
            return tag.read("BoundEntityUUID", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
        } else {
            return null;
        }
    }

    public static boolean isBound(ItemStack stack) {
        return getBoundEntityUUID(stack) != null;
    }
}
