// Ported from Food Effect Tooltips (LGPL-3.0-only) https://github.com/DaFuqs/FoodEffectTooltips
// Modifications: Replaced Guava/DFU helpers with a local record, explicit imports, dropped jetbrains annotations.
package net.diaithir.tooltips.foodeffecttooltips.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.TeleportRandomlyConsumeEffect;

public final class TooltipHelper {
    private TooltipHelper() {}

    private record Mod(Holder<Attribute> attribute, AttributeModifier modifier) {}

    public static void addFoodComponentEffectTooltip(ItemStack stack, Consumable consumable, List<Component> tooltip, float tickRate) {
        if (consumable.onConsumeEffects().isEmpty()) {
            return;
        }
        MutableComponent c = stack.getUseAnimation() == ItemUseAnimation.DRINK
                ? Component.translatable("potion.whenDrank")
                : Component.translatable("foodeffecttooltips.food.whenEaten");
        addConsumeEffectsTooltip(consumable.onConsumeEffects(), tooltip, tickRate, c);
    }

    public static void addConsumeEffectsTooltip(List<ConsumeEffect> consumeEffects, List<Component> tooltip, float tickRate, MutableComponent header) {
        build(tooltip, consumeEffects, tickRate, header);
    }

    private static void build(List<Component> tooltip, List<ConsumeEffect> effects, float tickRate, MutableComponent header) {
        List<Mod> modifiers = new ArrayList<>();

        for (ConsumeEffect entry : effects) {
            if (entry instanceof ClearAllStatusEffectsConsumeEffect) {
                tooltip.add(Component.translatable("foodeffecttooltips.food.clearsAllStatusEffects"));
                continue;
            }
            if (entry instanceof TeleportRandomlyConsumeEffect tp) {
                tooltip.add(Component.translatable("foodeffecttooltips.food.teleportRandomly", tp.diameter()));
                continue;
            }
            if (entry instanceof RemoveStatusEffectsConsumeEffect rm) {
                tooltip.add(Component.translatable("foodeffecttooltips.food.removesStatusEffects"));
                for (var i : rm.effects()) {
                    tooltip.add(Component.literal("- ").append(i.value().getDisplayName()));
                }
                continue;
            }
            if (!(entry instanceof ApplyStatusEffectsConsumeEffect apply)) {
                continue;
            }

            for (MobEffectInstance inst : apply.effects()) {
                MutableComponent text = Component.translatable(inst.getDescriptionId());
                Holder<MobEffect> holder = inst.getEffect();
                holder.value().createModifiers(inst.getAmplifier(), (attribute, modifier) -> modifiers.add(new Mod(attribute, modifier)));
                if (inst.getAmplifier() > 0) {
                    text = Component.translatable("potion.withAmplifier", text, Component.translatable("potion.potency." + inst.getAmplifier()));
                }
                if (!inst.endsWithin(20)) {
                    text = Component.translatable("potion.withDuration", text, MobEffectUtil.formatDuration(inst, 1.0F, tickRate));
                }
                if (apply.probability() < 1.0F) {
                    text = Component.translatable("foodeffecttooltips.food.withChance", text, Math.round(apply.probability() * 100));
                }
                tooltip.add(text.withStyle(holder.value().getCategory().getTooltipFormatting()));
            }
        }

        if (!modifiers.isEmpty()) {
            tooltip.add(CommonComponents.EMPTY);
            tooltip.add(header.withStyle(ChatFormatting.DARK_PURPLE));

            for (Mod m : modifiers) {
                AttributeModifier am = m.modifier();
                double d = am.amount();
                double e;
                if (am.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_BASE && am.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                    e = am.amount();
                } else {
                    e = am.amount() * 100.0;
                }

                if (d > 0.0) {
                    tooltip.add(Component.translatable("attribute.modifier.plus." + am.operation().id(),
                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(e),
                            Component.translatable(m.attribute().value().getDescriptionId())).withStyle(ChatFormatting.BLUE));
                } else if (d < 0.0) {
                    e *= -1.0;
                    tooltip.add(Component.translatable("attribute.modifier.take." + am.operation().id(),
                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(e),
                            Component.translatable(m.attribute().value().getDescriptionId())).withStyle(ChatFormatting.RED));
                }
            }
        }
    }
}
