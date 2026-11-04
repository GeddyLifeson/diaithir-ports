package net.diaithir.arms.immersive;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Armour effects of the Immersive Armors sets (GPL-3.0 package). As upstream, resistances compound per piece worn while the "primary"
 * effects (wither, spikes, fire, berserk) act once with the count of the set. State lives in memory only.
 */
public final class Effects {
    private static final Map<LivingEntity, Long> DIVINE = new WeakHashMap<>();

    /** Pure: damage left after n pieces each cutting a fraction. */
    public static float perPiece(float amount, float strength, int n) { return (float) (amount * Math.pow(1.0 - strength, n)); }

    /** Pure: berserk multiplier for set count, strength per piece and health fraction. */
    public static float berserk(int n, float strength, float healthFraction) { return 1.0F + n * strength * (1.0F - healthFraction); }

    static float num(Sets.Set s, String key, int idx) {
        for (var e : s.effects()) if (e.get(0).equals(key)) return ((Number) e.get(idx)).floatValue();
        return Float.NaN;
    }

    static boolean has(Sets.Set s, String key) { return s.effects().stream().anyMatch(e -> e.get(0).equals(key)); }

    @SubscribeEvent
    public void incoming(LivingIncomingDamageEvent e) {
        LivingEntity victim = e.getEntity();
        DamageSource src = e.getSource();
        float amount = e.getAmount();
        // attacker side
        if (src.getEntity() instanceof LivingEntity att && src.getDirectEntity() == att) {
            for (Sets.Set s : Sets.DATA.sets()) {
                int n = Sets.count(att, s);
                if (n == 0) continue;
                if (has(s, "berserk")) amount *= berserk(n, num(s, "berserk", 1), att.getHealth() / att.getMaxHealth());
                if (has(s, "axe_efficiency") && (att.getMainHandItem().is(ItemTags.AXES) || att.getOffhandItem().is(ItemTags.AXES))) amount *= 1.0F + n * num(s, "axe_efficiency", 1);
            }
        }
        // defender side
        for (Sets.Set s : Sets.DATA.sets()) {
            int n = Sets.count(victim, s);
            if (n == 0) continue;
            Entity attacker = src.getEntity();
            if (has(s, "fire_resistance") && src.is(DamageTypeTags.IS_FIRE)) amount = perPiece(amount, num(s, "fire_resistance", 1), n);
            if (has(s, "explosion_protection") && src.is(DamageTypeTags.IS_EXPLOSION)) amount = perPiece(amount, num(s, "explosion_protection", 1), n);
            if (has(s, "magic_protection") && (src.is(DamageTypes.MAGIC) || src.is(DamageTypes.INDIRECT_MAGIC))) amount = perPiece(amount, num(s, "magic_protection", 1), n);
            if (has(s, "wither")) {
                if (src.is(DamageTypes.WITHER)) amount *= Math.min(1.0F, 1.0F - num(s, "wither", 1) * n);
                if (attacker instanceof LivingEntity a && src.getDirectEntity() == a && !a.fireImmune()) a.addEffect(new MobEffectInstance(MobEffects.WITHER, (int) num(s, "wither", 2) * n, 1));
            }
            if (has(s, "fire_inflicting") && attacker != null && !attacker.fireImmune()) attacker.setRemainingFireTicks(attacker.getRemainingFireTicks() + (int) num(s, "fire_inflicting", 1) * n);
            if (has(s, "bouncing") && attacker != null && src.getDirectEntity() == attacker) {
                Vec3 push = attacker.position().subtract(victim.position()).normalize().scale(num(s, "bouncing", 1) * n);
                attacker.setDeltaMovement(attacker.getDeltaMovement().add(push));
            }
            if (has(s, "spikes") && attacker != null && src.getDirectEntity() == attacker && !src.is(DamageTypes.THORNS) && victim.level() instanceof ServerLevel sl)
                attacker.hurtServer(sl, victim.damageSources().thorns(victim), num(s, "spikes", 1) * n);
            if (has(s, "arrow_block") && src.is(DamageTypeTags.IS_PROJECTILE) && victim.getRandom().nextFloat() < n * num(s, "arrow_block", 1)) {
                victim.level().playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.SHIELD_BLOCK, victim.getSoundSource(), 0.5F, 1.25F);
                amount = 0;
            }
            if (has(s, "divine") && n == 4 && amount > 0) {
                long now = victim.level().getGameTime();
                Long last = DIVINE.get(victim);
                if (last == null || last + (long) num(s, "divine", 1) < now) {
                    DIVINE.put(victim, now);
                    victim.level().playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.ANVIL_LAND, victim.getSoundSource(), 0.5F, 1.25F);
                    amount = 0;
                }
            }
        }
        if (amount <= 0) e.setCanceled(true); else e.setAmount(amount);
    }

    @SubscribeEvent
    public void target(LivingChangeTargetEvent e) {
        if (!(e.getEntity() instanceof AbstractSkeleton) || e.getNewAboutToBeSetTarget() == null) return;
        LivingEntity t = e.getNewAboutToBeSetTarget();
        for (Sets.Set s : Sets.DATA.sets()) {
            if (!s.antiSkeleton()) continue;
            int n = Sets.count(t, s);
            if (n > 0 && e.getEntity().getRandom().nextFloat() < n * 0.25F) { e.setCanceled(true); return; }
        }
    }

    @SubscribeEvent
    public void tick(EntityTickEvent.Post e) {
        if (!(e.getEntity() instanceof LivingEntity le) || le.level().isClientSide()) return;
        Sets.Set s = Sets.of(le.getItemBySlot(EquipmentSlot.FEET));
        if (s != null && has(s, "steam_tech")) {
            le.fallDistance = Math.min(1, le.fallDistance);
            Vec3 v = le.getDeltaMovement();
            if (v.y < -0.75 && !le.isFallFlying()) le.setDeltaMovement(v.x, -0.75, v.z);
        }
        Sets.Set h = Sets.of(le.getItemBySlot(EquipmentSlot.HEAD));
        if (h != null && has(h, "steam_tech") && le.isShiftKeyDown() && le.tickCount % 20 == 0) {
            for (Entity o : le.level().getEntities(le, new AABB(le.position(), le.position()).inflate(16))) if (o instanceof Monster m) m.addEffect(new MobEffectInstance(MobEffects.GLOWING, 25));
        }
    }
}
