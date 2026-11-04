package net.diaithir.combat.survey;

import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Status build-up (survey part 2, typed damage), clean-room from the idea in Overhauled Damage: hits fill a meter per kind, the meter
 * drains while nothing adds to it, and a full meter (100) fires the kind's effect and empties. Kinds: bleeding (edged weapons), poison
 * (poisonous attackers), burning (fire), freezing (cold), shock (lightning, stun), stagger (blunt weapons). Effects use vanilla effects so
 * nothing needs a client. Meters live in memory only and are cleared when the entity leaves.
 */
public final class Buildup {
    public enum Kind { BLEEDING, POISON, BURNING, FREEZING, SHOCK, STAGGER }

    public static final float FULL = 100.0F;
    /** Meter points gained per point of damage dealt. */
    public static final float RATE = 4.0F;
    /** Meter points lost per second when idle. */
    public static final float DRAIN = 5.0F;
    private static final Map<LivingEntity, EnumMap<Kind, Float>> METERS = new WeakHashMap<>();

    public static float meter(LivingEntity e, Kind k) { EnumMap<Kind, Float> m = METERS.get(e); return m == null ? 0F : m.getOrDefault(k, 0F); }

    /** Which kind a hit fills, or null. */
    public static Kind kindOf(DamageSource s) {
        if (s.is(DamageTypeTags.IS_FIRE)) return Kind.BURNING;
        if (s.is(DamageTypeTags.IS_FREEZING)) return Kind.FREEZING;
        if (s.is(DamageTypeTags.IS_LIGHTNING)) return Kind.SHOCK;
        if (s.getDirectEntity() instanceof LivingEntity att) {
            ItemStack w = att.getMainHandItem();
            String p = w.isEmpty() ? "" : BuiltInRegistries.ITEM.getKey(w.getItem()).getPath();
            if (p.contains("hammer") || p.contains("mace") || p.contains("club") || p.contains("flail")) return Kind.STAGGER;
            if (p.endsWith("sword") || p.contains("axe") || p.contains("dagger") || p.contains("sabre") || p.contains("scimitar") || p.contains("rapier") || p.contains("spear") || p.contains("halberd")) return Kind.BLEEDING;
            var id = BuiltInRegistries.ENTITY_TYPE.getKey(att.getType()).getPath();
            if (id.contains("spider") || id.contains("bee") || id.contains("snake") || id.contains("viper")) return Kind.POISON;
        }
        return null;
    }

    /** Add points; fires the effect when the meter fills. Returns true if it fired. */
    public static boolean add(LivingEntity target, Kind k, float points) {
        if (target.level().isClientSide() || points <= 0) return false;
        EnumMap<Kind, Float> m = METERS.computeIfAbsent(target, x -> new EnumMap<>(Kind.class));
        float v = m.getOrDefault(k, 0F) + points;
        if (v < FULL) { m.put(k, v); return false; }
        m.put(k, 0F);
        fire(target, k);
        return true;
    }

    static void fire(LivingEntity t, Kind k) {
        switch (k) {
            case BLEEDING -> t.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
            case POISON -> t.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
            case BURNING -> t.setRemainingFireTicks(160);
            case FREEZING -> { t.setTicksFrozen(t.getTicksRequiredToFreeze() + 100); t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1)); }
            case SHOCK -> { t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 4)); t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1)); }
            case STAGGER -> { t.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 3)); t.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 60, 1)); }
        }
    }

    @SubscribeEvent
    public void damaged(LivingDamageEvent.Post e) {
        Kind k = kindOf(e.getSource());
        if (k != null) add(e.getEntity(), k, e.getInflictedDamage() * RATE);
    }

    @SubscribeEvent
    public void tick(EntityTickEvent.Post e) {
        if (!(e.getEntity() instanceof LivingEntity le) || le.tickCount % 20 != 0 || le.level().isClientSide()) return;
        EnumMap<Kind, Float> m = METERS.get(le);
        if (m == null) return;
        m.replaceAll((k, v) -> Math.max(0F, v - DRAIN));
    }
}
