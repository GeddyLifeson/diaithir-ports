package net.diaithir.relics;

import java.util.List;
import net.diaithir.relics.DiaithirRelics.Def;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** The passive powers. hit: wielder strikes (returns the new damage); hurt: wearer is struck (new damage, 0 or less cancels); tick: once a second. */
public final class Passives {
    private Passives() {}

    /** Returns the damage after the relic's say. Side effects (fire, poison, arcs) happen here. */
    static float hit(Def d, ServerPlayer atk, LivingEntity victim, float amount, ItemStack stack) {
        ServerLevel level = atk.level();
        switch (d.passive()) {
            case "ignite" -> victim.igniteForSeconds(3);
            case "stagger" -> Abilities.effect(victim, MobEffects.SLOWNESS, 30, 1);
            case "poison" -> Abilities.effect(victim, MobEffects.POISON, 100, 0);
            case "chain" -> {
                if (atk.getRandom().nextFloat() < 0.2F) {
                    int n = 0;
                    for (LivingEntity e : Abilities.foes(atk, victim.position(), 4)) {
                        if (e == victim || n >= 3) continue;
                        n++;
                        Abilities.hurt(atk, e, 6);
                        Vec3 a = victim.getBoundingBox().getCenter(), b = e.getBoundingBox().getCenter();
                        for (int i = 0; i <= 6; i++) {
                            Vec3 at = a.lerp(b, i / 6.0);
                            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, 1, 0.05, 0.05, 0.05, 0);
                        }
                    }
                    Abilities.sound(level, victim.position(), SoundEvents.LIGHTNING_BOLT_THUNDER, 0.4F, 2.0F);
                }
            }
            case "backstab" -> {
                Vec3 toAtk = atk.position().subtract(victim.position());
                if (toAtk.lengthSqr() > 1.0E-4 && victim.getLookAngle().dot(toAtk.normalize()) < 0) {
                    amount *= 1.5F;
                    Abilities.burst(level, ParticleTypes.CRIT, victim.getBoundingBox().getCenter(), 8, 0.3, 0.2);
                }
            }
            case "tidebonus" -> {
                if (atk.isInWater() || level.isRainingAt(atk.blockPosition())) {
                    amount *= 1.3F;
                    Abilities.burst(level, ParticleTypes.SPLASH, victim.getBoundingBox().getCenter(), 10, 0.3, 0.1);
                }
            }
            case "skyhunter" -> {
                if (!victim.onGround()) {
                    amount *= 1.4F;
                    Abilities.burst(level, ParticleTypes.CLOUD, victim.position(), 6, 0.3, 0.05);
                }
            }
            case "starcrit" -> {
                if (victim.getHealth() >= victim.getMaxHealth()) {
                    amount *= 1.25F;
                    Abilities.burst(level, ParticleTypes.END_ROD, victim.getBoundingBox().getCenter(), 8, 0.3, 0.05);
                }
            }
            case "plunge" -> {
                if (atk.fallDistance > 1.0F) {
                    amount += Math.min(10.0F, atk.fallDistance);
                    Abilities.burst(level, ParticleTypes.CRIT, victim.getBoundingBox().getCenter(), 10, 0.3, 0.2);
                }
            }
            case "lifesteal" -> {
                atk.heal(amount * 0.15F);
                Abilities.burst(level, ParticleTypes.HEART, atk.position().add(0, 1.6, 0), 2, 0.3, 0.0);
            }
            case "breaker" -> amount += 2.0F * (victim.getArmorValue() / 5);
            default -> { }
        }
        return amount;
    }

    /** Returns the damage after the relic's say; 0 or less means the blow is turned aside. blocking: the shield is up. */
    static float hurt(Def d, ServerPlayer p, DamageSource src, float amount, ItemStack stack, boolean blocking) {
        ServerLevel level = p.level();
        boolean melee = src.getDirectEntity() instanceof LivingEntity && !src.is(DamageTypeTags.IS_PROJECTILE);
        LivingEntity attacker = melee ? (LivingEntity) src.getDirectEntity() : null;
        switch (d.passive()) {
            case "arrowward" -> {
                if (src.is(DamageTypeTags.IS_PROJECTILE)) amount *= 0.75F;
            }
            case "bearhide" -> {
                if (melee) amount *= 0.85F;
            }
            case "mistveil" -> {
                if (p.getHealth() + p.getAbsorptionAmount() - amount < p.getMaxHealth() * 0.35F && !p.getCooldowns().isOnCooldown(stack)) {
                    Abilities.effect(p, MobEffects.INVISIBILITY, 100, 0);
                    Abilities.effect(p, MobEffects.SPEED, 100, 1);
                    Abilities.burst(level, ParticleTypes.CLOUD, p.position().add(0, 1, 0), 30, 0.6, 0.05);
                    Abilities.sound(level, p.position(), SoundEvents.BELL_RESONATE, 1.0F, 0.8F);
                    p.getCooldowns().addCooldown(stack, 1200);
                }
            }
            case "reflect" -> {
                if (blocking && attacker != null) {
                    Abilities.hurt(p, attacker, amount * 0.3F);
                    Abilities.burst(level, ParticleTypes.CRIT, attacker.getBoundingBox().getCenter(), 8, 0.3, 0.2);
                }
            }
            case "frost" -> {
                if (blocking && attacker != null) {
                    Abilities.effect(attacker, MobEffects.SLOWNESS, 60, 2);
                    attacker.setTicksFrozen(140);
                    Abilities.burst(level, ParticleTypes.SNOWFLAKE, attacker.getBoundingBox().getCenter(), 14, 0.4, 0.05);
                }
            }
            case "ward" -> {
                if (blocking) {
                    if (src.is(DamageTypeTags.IS_PROJECTILE)) {
                        Abilities.burst(level, ParticleTypes.END_ROD, p.position().add(0, 1.4, 0), 10, 0.4, 0.05);
                        return 0;
                    }
                    if (attacker != null) Abilities.effect(attacker, MobEffects.BLINDNESS, 60, 0);
                }
            }
            case "dragonward" -> {
                if (src.is(DamageTypeTags.IS_FIRE)) {
                    p.clearFire();
                    return 0;
                }
            }
            default -> { }
        }
        return amount;
    }

    /** Once a second for every worn trinket (and the held weapon). */
    static void tick(Def d, ServerPlayer p, ItemStack stack) {
        ServerLevel level = p.level();
        switch (d.passive()) {
            case "wellspring" -> {
                if (p.getHealth() < p.getMaxHealth() * 0.5F) p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false));
            }
            case "sunwarmth" -> {
                if (level.isBrightOutside() && level.canSeeSky(p.blockPosition())) p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false));
            }
            case "moonsight" -> {
                if (!level.isBrightOutside()) {
                    p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false));
                    p.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, 0, true, false));
                }
            }
            case "ravensight" -> {
                List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(20),
                    e -> e instanceof net.minecraft.world.entity.monster.Enemy && e.isAlive());
                for (LivingEntity e : list) e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, true, false));
            }
            case "giantsbulk" -> p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 80, 1, true, false));
            case "feast" -> {
                if (p.tickCount % 60 == 0 && p.getFoodData().getFoodLevel() < 20) p.getFoodData().eat(1, 0.2F);
            }
            case "dragonward" -> p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, true, false));
            case "aqua" -> {
                if (p.isInWater()) p.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 60, 0, true, false));
            }
            default -> { }
        }
    }

    /** A fatal blow with the Ring of Fate on: stands the wearer up at 3 hearts. Returns true if it saved. */
    static boolean fate(Def d, ServerPlayer p, ItemStack stack) {
        if (!d.passive().equals("fatesave") || p.getCooldowns().isOnCooldown(stack)) return false;
        p.setHealth(6.0F);
        p.removeAllEffects();
        p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
        p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1));
        p.getCooldowns().addCooldown(stack, 12000);
        Abilities.burst(p.level(), ParticleTypes.TOTEM_OF_UNDYING, p.position().add(0, 1, 0), 40, 0.6, 0.4);
        Abilities.sound(p.level(), p.position(), SoundEvents.TOTEM_USE, 1.0F, 1.0F);
        return true;
    }
}
