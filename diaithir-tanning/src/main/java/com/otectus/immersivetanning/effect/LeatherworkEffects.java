package com.otectus.immersivetanning.effect;

import com.otectus.immersivetanning.ImmersiveTanning;
import com.otectus.immersivetanning.api.TraitEffect;
import com.otectus.immersivetanning.config.ServerConfig;
import com.otectus.immersivetanning.leather.Finish;
import com.otectus.immersivetanning.leather.LeatherworkData;
import com.otectus.immersivetanning.trait.TraitEffects;
import com.otectus.immersivetanning.trait.TraitRef;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.List;
import java.util.Optional;

/**
 * What finished leatherwork does when worn. Every effect is per piece, so each piece contributes and a full set only
 * stacks the contributions (no set threshold). Workmanship scales armor and the finish's own effect; Integrity scales
 * durability (see {@link #applyDurability}).
 * <ul>
 *     <li>Supple - movement speed, a smaller detection range, and boots that keep footsteps from sculk.</li>
 *     <li>Harness - much more durability, and the best field repair (see FieldRepair).</li>
 *     <li>Hardened - extra armor and some toughness.</li>
 * </ul>
 */
@EventBusSubscriber(modid = ImmersiveTanning.MOD_ID)
public final class LeatherworkEffects {
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private static Identifier modifierId(String attribute, EquipmentSlot slot) {
        return ImmersiveTanning.id("leatherwork/" + attribute + "/" + slot.getName());
    }

    /** Workmanship as a multiplier of armor and finish effects. */
    public static double efficacy(LeatherworkData data) {
        return lerp(ServerConfig.get(ServerConfig.MINIMUM_EFFICACY_MULTIPLIER), ServerConfig.get(ServerConfig.MAXIMUM_EFFICACY_MULTIPLIER), data.workmanship());
    }

    /** Integrity, finish and trait as one durability multiplier. */
    public static double durability(LeatherworkData data) {
        double multiplier = lerp(ServerConfig.get(ServerConfig.MINIMUM_DURABILITY_MULTIPLIER), ServerConfig.get(ServerConfig.MAXIMUM_DURABILITY_MULTIPLIER), data.integrity());
        if (data.finish() == Finish.HARNESS) multiplier *= ServerConfig.get(ServerConfig.HARNESS_DURABILITY_MULTIPLIER);
        TraitEffect effect = traitEffect(data.trait());
        if (effect != null) multiplier *= effect.durabilityMultiplier(data.trait());
        return multiplier;
    }

    /**
     * Durability conversion: the stack's own maximum durability becomes the item's base value times the multiplier, so
     * wear, Unbreaking and Mending run unchanged on top and the damage bar stays truthful. Worn damage is kept.
     */
    public static void applyDurability(ItemStack stack, LeatherworkData data) {
        Integer base = stack.getItem().components().get(DataComponents.MAX_DAMAGE);
        if (base == null || base <= 0) return;
        int max = Math.max(1, (int) Math.round(base * Math.max(0.01, durability(data))));
        if (max == base) stack.remove(DataComponents.MAX_DAMAGE);
        else stack.set(DataComponents.MAX_DAMAGE, max);
        if (stack.getDamageValue() >= max) stack.setDamageValue(max - 1);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onAttributeModifiers(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        Optional<LeatherworkData> found = LeatherworkData.get(stack);
        if (found.isEmpty()) return;
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || !equippable.slot().isArmor()) return;
        EquipmentSlot slot = equippable.slot();
        EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(slot);
        LeatherworkData data = found.get();
        try {
            double efficacy = efficacy(data);
            double armorMultiplier = efficacy;
            if (data.finish() == Finish.HARDENED) armorMultiplier += ServerConfig.get(ServerConfig.HARDENED_ARMOR_FACTOR) * efficacy;
            if (Math.abs(armorMultiplier - 1.0) > 1.0E-6) {
                // Rescale the piece's own armor in place (same ids), so the tooltip shows one armor line.
                for (ItemAttributeModifiers.Entry entry : List.copyOf(event.getModifiers())) {
                    AttributeModifier m = entry.modifier();
                    if (!entry.attribute().is(Attributes.ARMOR) || m.operation() != AttributeModifier.Operation.ADD_VALUE) continue;
                    event.replaceModifier(entry.attribute(), new AttributeModifier(m.id(), m.amount() * armorMultiplier, m.operation()), entry.slot());
                }
            }
            if (data.finish() == Finish.HARDENED) {
                double toughness = ServerConfig.get(ServerConfig.HARDENED_TOUGHNESS_PER_PIECE) * efficacy;
                if (toughness > 0) {
                    event.addModifier(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(modifierId("armor_toughness", slot), toughness, AttributeModifier.Operation.ADD_VALUE), group);
                }
            }
            if (data.finish() == Finish.SUPPLE) {
                double speed = ServerConfig.get(ServerConfig.SUPPLE_SPEED_PER_PIECE) * efficacy;
                if (speed > 0) {
                    event.addModifier(Attributes.MOVEMENT_SPEED, new AttributeModifier(modifierId("movement_speed", slot), speed, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), group);
                }
            }
            TraitEffect effect = traitEffect(data.trait());
            if (effect != null) effect.addModifiers(event, slot, data.trait());
        } catch (RuntimeException e) {
            ImmersiveTanning.LOGGER.debug("Could not apply leatherwork effects to {}: {}", stack, e.toString());
        }
    }

    /** Supple pieces shrink the range at which mobs notice the wearer. */
    @SubscribeEvent
    public static void onVisibility(LivingEvent.LivingVisibilityEvent event) {
        double per = ServerConfig.get(ServerConfig.SUPPLE_VISIBILITY_PER_PIECE);
        if (per <= 0) return;
        double multiplier = 1.0;
        for (EquipmentSlot slot : ARMOR) {
            Optional<LeatherworkData> data = LeatherworkData.get(event.getEntity().getItemBySlot(slot));
            if (data.isPresent() && data.get().finish() == Finish.SUPPLE) multiplier *= 1.0 - Mth.clamp(per * efficacy(data.get()), 0.0, 0.5);
        }
        if (multiplier < 1.0) event.modifyVisibility(multiplier);
    }

    /** Supple boots keep footsteps and landings from reaching sculk sensors and wardens, like wool. */
    @SubscribeEvent
    public static void onGameEvent(VanillaGameEvent event) {
        var type = event.getVanillaEvent();
        if (!type.is(GameEvent.STEP) && !type.is(GameEvent.HIT_GROUND) && !type.is(GameEvent.SWIM)) return;
        Entity cause = event.getCause();
        if (!(cause instanceof LivingEntity living) || !ServerConfig.get(ServerConfig.SUPPLE_BOOTS_MUFFLE_STEPS)) return;
        Optional<LeatherworkData> boots = LeatherworkData.get(living.getItemBySlot(EquipmentSlot.FEET));
        if (boots.isPresent() && boots.get().finish() == Finish.SUPPLE) event.setCanceled(true);
    }

    /** Innate hide traits reduce the damage types they resist, per piece. */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onHurt(LivingIncomingDamageEvent event) {
        float multiplier = 1F;
        for (EquipmentSlot slot : ARMOR) {
            Optional<LeatherworkData> data = LeatherworkData.get(event.getEntity().getItemBySlot(slot));
            if (data.isEmpty() || data.get().trait() == null) continue;
            TraitEffect effect = traitEffect(data.get().trait());
            if (effect != null) multiplier *= effect.damageMultiplier(event.getSource(), data.get().trait());
        }
        if (multiplier < 1F) event.setAmount(event.getAmount() * Math.max(0F, multiplier));
    }

    private static TraitEffect traitEffect(TraitRef trait) {
        return trait == null ? null : TraitEffects.get(trait.effect());
    }

    public static double lerp(double min, double max, int score) {
        return min + (max - min) * Mth.clamp(score, 0, 100) / 100.0;
    }

    private LeatherworkEffects() {}
}
