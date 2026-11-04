// Ported from Food Effect Tooltips (LGPL-3.0-only) https://github.com/DaFuqs/FoodEffectTooltips
// Modifications: Listener registered manually on NeoForge.EVENT_BUS instead of @EventBusSubscriber; uses the feature's config class.
package net.diaithir.tooltips.foodeffecttooltips.client;

import java.util.ArrayList;
import java.util.List;
import net.diaithir.tooltips.foodeffecttooltips.config.FoodEffectsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public final class FoodEffectTooltipsClient {
    private FoodEffectTooltipsClient() {}

    public static void init() {
        NeoForge.EVENT_BUS.addListener(FoodEffectTooltipsClient::onTooltip);
    }

    private static float getTickRate() {
        ClientLevel world = Minecraft.getInstance().level;
        return world == null ? 20.0F : world.tickRateManager().tickrate();
    }

    private static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        Consumable consumable = stack.get(DataComponents.CONSUMABLE);
        if (consumable != null && shouldShowTooltip(stack)) {
            TooltipHelper.addFoodComponentEffectTooltip(stack, consumable, event.getToolTip(), getTickRate());
        }

        if (FoodEffectsConfig.SHOW_SUSPICIOUS_STEW_TOOLTIPS.getAsBoolean() && !event.getFlags().isCreative()) {
            SuspiciousStewEffects sus = stack.get(DataComponents.SUSPICIOUS_STEW_EFFECTS);
            if (sus != null && !sus.effects().isEmpty()) {
                List<MobEffectInstance> list = new ArrayList<>();
                for (SuspiciousStewEffects.Entry stewEffect : sus.effects()) {
                    list.add(stewEffect.createEffectInstance());
                }
                PotionContents.addPotionTooltip(list, event.getToolTip()::add, 1.0F, getTickRate());
            }
        }

        DeathProtection deathProtection = stack.get(DataComponents.DEATH_PROTECTION);
        if (deathProtection != null && shouldShowTooltip(stack)) {
            List<ConsumeEffect> consumeEffects = deathProtection.deathEffects();
            TooltipHelper.addConsumeEffectsTooltip(consumeEffects, event.getToolTip(), getTickRate(),
                    Component.translatable("foodeffecttooltips.food.whenTriggered"));
        }
    }

    public static boolean shouldShowTooltip(ItemStack stack) {
        Item item = stack.getItem();
        Identifier identifier = BuiltInRegistries.ITEM.getKey(item);

        boolean isWhitelist = FoodEffectsConfig.USE_AS_WHITELIST_INSTEAD.getAsBoolean();
        if (FoodEffectsConfig.BLACKLISTED_ITEM_IDENTIFIERS.get().contains(identifier.toString())) {
            return isWhitelist;
        }
        if (FoodEffectsConfig.BLACKLISTED_MOD_IDS.get().contains(identifier.getNamespace())) {
            return isWhitelist;
        }
        return !isWhitelist;
    }
}
