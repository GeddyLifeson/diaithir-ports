package com.otectus.immersivetanning.client;

import com.otectus.immersivetanning.ImmersiveTanning;
import com.otectus.immersivetanning.config.ServerConfig;
import com.otectus.immersivetanning.effect.LeatherworkEffects;
import com.otectus.immersivetanning.hide.HideItems;
import com.otectus.immersivetanning.leather.FieldRepair;
import com.otectus.immersivetanning.leather.Finish;
import com.otectus.immersivetanning.leather.LeatherworkData;
import com.otectus.immersivetanning.util.Scores;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The leatherwork tooltip. The important information is the word identity: the name reads "Fine Supple Leather
 * Boots", the next line says what Supple means. Scores and effect details follow; Shift shows the numbers.
 */
@EventBusSubscriber(modid = ImmersiveTanning.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Optional<LeatherworkData> found = LeatherworkData.get(stack);
        List<Component> tooltip = event.getToolTip();
        if (found.isEmpty()) {
            HideItems.mounting(stack).ifPresent(recipe -> tooltip.add(Math.min(1, tooltip.size()),
                    Component.translatable("tooltip.immersive_tanning.mountable", recipe.size().displayName()).withStyle(ChatFormatting.DARK_GRAY)));
            if (FieldRepair.isRepairable(stack) && Minecraft.getInstance().hasShiftDown()) {
                tooltip.add(Math.min(1, tooltip.size()), Component.translatable("tooltip.immersive_tanning.field_repairable").withStyle(ChatFormatting.DARK_GRAY));
            }
            return;
        }
        LeatherworkData data = found.get();
        Finish finish = data.finish();
        if (!tooltip.isEmpty() && !stack.has(DataComponents.CUSTOM_NAME)) {
            tooltip.set(0, Component.translatable("tooltip.immersive_tanning.named", data.quality().displayName(), finish.displayName(),
                    stack.getHoverName()).withStyle(stack.getRarity().getStyleModifier()));
        }
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("tooltip.immersive_tanning.finish_line", finish.displayName(), finish.tagline())
                .withStyle(s -> s.withColor(finish.textColor())));
        lines.add(Component.translatable("tooltip.immersive_tanning.integrity", Scores.label(data.integrity())).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.immersive_tanning.workmanship", Scores.label(data.workmanship())).withStyle(ChatFormatting.GRAY));
        if (data.trait() != null) {
            lines.add(Component.translatable("tooltip.immersive_tanning.trait", data.trait().displayName()).withStyle(ChatFormatting.GRAY));
        }
        if (Minecraft.getInstance().hasShiftDown()) {
            double efficacy = LeatherworkEffects.efficacy(data);
            lines.add(Component.translatable("tooltip.immersive_tanning.durability", percent(LeatherworkEffects.durability(data))).withStyle(ChatFormatting.DARK_GRAY));
            boolean wearable = stack.has(DataComponents.EQUIPPABLE);
            switch (finish) {
                case SUPPLE -> {
                    if (wearable) {
                        lines.add(Component.translatable("tooltip.immersive_tanning.effect.supple_speed",
                                oneDecimal(ServerConfig.get(ServerConfig.SUPPLE_SPEED_PER_PIECE) * efficacy * 100)).withStyle(ChatFormatting.DARK_GRAY));
                        lines.add(Component.translatable("tooltip.immersive_tanning.effect.supple_quiet",
                                Math.round(ServerConfig.get(ServerConfig.SUPPLE_VISIBILITY_PER_PIECE) * efficacy * 100)).withStyle(ChatFormatting.DARK_GRAY));
                        if (stack.get(DataComponents.EQUIPPABLE).slot() == EquipmentSlot.FEET
                                && ServerConfig.get(ServerConfig.SUPPLE_BOOTS_MUFFLE_STEPS)) {
                            lines.add(Component.translatable("tooltip.immersive_tanning.effect.supple_steps").withStyle(ChatFormatting.DARK_GRAY));
                        }
                    }
                }
                case HARNESS -> lines.add(Component.translatable("tooltip.immersive_tanning.effect.harness_repair",
                        oneDecimal(ServerConfig.get(ServerConfig.HARNESS_REPAIR_MULTIPLIER))).withStyle(ChatFormatting.DARK_GRAY));
                case HARDENED -> {
                    if (wearable) {
                        lines.add(Component.translatable("tooltip.immersive_tanning.effect.hardened",
                                Math.round(ServerConfig.get(ServerConfig.HARDENED_ARMOR_FACTOR) * efficacy * 100),
                                oneDecimal(ServerConfig.get(ServerConfig.HARDENED_TOUGHNESS_PER_PIECE) * efficacy)).withStyle(ChatFormatting.DARK_GRAY));
                    }
                }
            }
            if (data.trait() != null) lines.add(data.trait().effectDescription());
            if (wearable) lines.add(Component.translatable("tooltip.immersive_tanning.per_piece").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            if (FieldRepair.isRepairable(stack)) lines.add(Component.translatable("tooltip.immersive_tanning.field_repairable").withStyle(ChatFormatting.DARK_GRAY));
            if (data.crafter() != null) {
                lines.add(Component.translatable("tooltip.immersive_tanning.crafted_by", data.crafter()).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }
        } else {
            lines.add(Component.translatable("tooltip.immersive_tanning.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.addAll(Math.min(1, tooltip.size()), lines);
    }

    private static int percent(double multiplier) {
        return (int) Math.round(multiplier * 100.0);
    }

    private static String oneDecimal(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private ClientEvents() {}
}
