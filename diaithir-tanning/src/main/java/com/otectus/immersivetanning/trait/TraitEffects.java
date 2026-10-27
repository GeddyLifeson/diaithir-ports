package com.otectus.immersivetanning.trait;

import com.otectus.immersivetanning.ImmersiveTanning;
import com.otectus.immersivetanning.api.TraitEffect;
import com.otectus.immersivetanning.registry.ModTags;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** The built-in trait effects and the registry that custom effects join. */
public final class TraitEffects {
    public static final Identifier HEATPROOF = ImmersiveTanning.id("heatproof");
    public static final Identifier INSULATING = ImmersiveTanning.id("insulating");
    public static final Identifier LIGHTWEIGHT = ImmersiveTanning.id("lightweight");
    public static final Identifier TOUGH = ImmersiveTanning.id("tough");
    public static final Identifier WATER_RESISTANT = ImmersiveTanning.id("water_resistant");

    private static final Map<Identifier, TraitEffect> EFFECTS = new ConcurrentHashMap<>();

    static {
        EFFECTS.put(HEATPROOF, new TraitEffect() {
            @Override
            public float damageMultiplier(DamageSource source, TraitRef trait) {
                return source.is(ModTags.RESISTED_BY_HEATPROOF) ? reduce(0.125F, trait) : 1.0F;
            }

            @Override
            public Object[] descriptionArgs(TraitRef trait) {
                return new Object[]{percent(0.125 * trait.strength())};
            }
        });
        EFFECTS.put(INSULATING, new TraitEffect() {
            @Override
            public float damageMultiplier(DamageSource source, TraitRef trait) {
                return source.is(ModTags.RESISTED_BY_INSULATING) ? reduce(0.25F, trait) : 1.0F;
            }

            @Override
            public Object[] descriptionArgs(TraitRef trait) {
                return new Object[]{percent(0.25 * trait.strength())};
            }
        });
        EFFECTS.put(LIGHTWEIGHT, new TraitEffect() {
            @Override
            public void addModifiers(ItemAttributeModifierEvent event, EquipmentSlot slot, TraitRef trait) {
                add(event, slot, Attributes.MOVEMENT_SPEED, "lightweight", 0.015 * trait.strength());
            }

            @Override
            public Object[] descriptionArgs(TraitRef trait) {
                return new Object[]{percent(0.015 * trait.strength())};
            }
        });
        EFFECTS.put(TOUGH, new TraitEffect() {
            @Override
            public double durabilityMultiplier(TraitRef trait) {
                return 1.0 + 0.25 * trait.strength();
            }

            @Override
            public Object[] descriptionArgs(TraitRef trait) {
                return new Object[]{percent(0.25 * trait.strength())};
            }
        });
        EFFECTS.put(WATER_RESISTANT, new TraitEffect() {
            @Override
            public void addModifiers(ItemAttributeModifierEvent event, EquipmentSlot slot, TraitRef trait) {
                add(event, slot, NeoForgeMod.SWIM_SPEED, "water_resistant", 0.05 * trait.strength());
            }

            @Override
            public float damageMultiplier(DamageSource source, TraitRef trait) {
                return source.is(ModTags.RESISTED_BY_WATER_RESISTANT) ? reduce(0.15F, trait) : 1.0F;
            }

            @Override
            public Object[] descriptionArgs(TraitRef trait) {
                return new Object[]{percent(0.05 * trait.strength()), percent(0.15 * trait.strength())};
            }
        });
    }

    public static void register(Identifier id, TraitEffect effect) {
        if (EFFECTS.putIfAbsent(id, effect) != null) {
            throw new IllegalStateException("Trait effect " + id + " is already registered");
        }
    }

    public static boolean isKnown(Identifier id) {
        return EFFECTS.containsKey(id);
    }

    @Nullable
    public static TraitEffect get(Identifier id) {
        return EFFECTS.get(id);
    }

    /** A share as a percentage for tooltips: 0.125 reads "12.5", 0.25 reads "25". */
    public static String percent(double share) {
        double value = Math.round(share * 1000.0) / 10.0;
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    private static float reduce(float perPiece, TraitRef trait) {
        return Math.max(0F, 1F - perPiece * trait.strength());
    }

    /** Adds a MULTIPLY_BASE modifier whose id is stable per effect and slot, so pieces in different slots stack. */
    private static void add(ItemAttributeModifierEvent event, EquipmentSlot slot, Holder<Attribute> attribute, String name, double amount) {
        Identifier id = ImmersiveTanning.id("trait/" + name + "/" + slot.getName());
        event.addModifier(attribute, new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.bySlot(slot));
    }

    private TraitEffects() {}
}
