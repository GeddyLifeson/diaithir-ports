package net.diaithir.relics;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.diaithir.relics.DiaithirRelics.Def;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** The active powers: each takes the player and the relic stack, does its work on the server and says whether it fired (so a miss costs no cooldown). */
public final class Abilities {
    private Abilities() {}

    /** True while an ability or passive is itself dealing damage, so hit effects do not trigger on it. */
    static boolean busy = false;

    static boolean isFoe(ServerPlayer p, LivingEntity e) {
        if (e == p || !e.isAlive() || e instanceof Player || e instanceof net.minecraft.world.entity.decoration.ArmorStand) return false;
        return e instanceof Enemy || (e instanceof Mob m && m.getTarget() == p);
    }

    static List<LivingEntity> foes(ServerPlayer p, Vec3 centre, double r) {
        ServerLevel level = p.level();
        AABB box = new AABB(centre, centre).inflate(r);
        List<LivingEntity> out = new ArrayList<>();
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, x -> isFoe(p, x))) {
            if (e.position().distanceTo(centre) <= r + e.getBbWidth()) out.add(e);
        }
        return out;
    }

    static List<LivingEntity> cone(ServerPlayer p, double range, double minDot) {
        Vec3 look = p.getLookAngle();
        List<LivingEntity> out = new ArrayList<>();
        for (LivingEntity e : foes(p, p.position(), range)) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(p.getEyePosition());
            if (to.lengthSqr() < 1.0E-4 || to.normalize().dot(look) >= minDot) out.add(e);
        }
        return out;
    }

    static void hurt(ServerPlayer p, LivingEntity t, float dmg) {
        boolean old = busy;
        busy = true;
        try {
            t.hurtServer(p.level(), p.level().damageSources().indirectMagic(p, p), dmg);
        } finally {
            busy = old;
        }
    }

    static void sound(ServerLevel l, Vec3 at, SoundEvent s, float vol, float pitch) {
        l.playSound(null, at.x, at.y, at.z, s, SoundSource.PLAYERS, vol, pitch);
    }

    static void burst(ServerLevel l, ParticleOptions t, Vec3 at, int n, double spread, double speed) {
        l.sendParticles(t, at.x, at.y, at.z, n, spread, spread * 0.5, spread, speed);
    }

    /** First foe along the gaze within range, or null. */
    static LivingEntity aimed(ServerPlayer p, double range) {
        Vec3 from = p.getEyePosition();
        Vec3 look = p.getLookAngle();
        Vec3 to = from.add(look.scale(range));
        LivingEntity best = null;
        double bd = Double.MAX_VALUE;
        for (LivingEntity e : p.level().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().expandTowards(look.scale(range)).inflate(1.5), x -> isFoe(p, x))) {
            Optional<Vec3> hit = e.getBoundingBox().inflate(0.4).clip(from, to);
            if (hit.isPresent()) {
                double d = from.distanceToSqr(hit.get());
                if (d < bd) { bd = d; best = e; }
            }
        }
        return best;
    }

    static void effect(LivingEntity e, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> eff, int ticks, int amp) {
        e.addEffect(new MobEffectInstance(eff, ticks, amp));
    }

    public static boolean fire(Def d, ServerPlayer p, ItemStack stack) {
        ServerLevel level = p.level();
        Vec3 pos = p.position();
        Vec3 look = p.getLookAngle();
        switch (d.active()) {
            case "sunburst" -> {
                for (LivingEntity e : foes(p, pos, 5)) { e.igniteForSeconds(6); hurt(p, e, 6); }
                for (int i = 0; i < 24; i++) {
                    double a = i / 24.0 * Math.PI * 2;
                    level.sendParticles(ParticleTypes.FLAME, pos.x + Math.cos(a) * 4.5, pos.y + 0.3, pos.z + Math.sin(a) * 4.5, 2, 0.1, 0.2, 0.1, 0.02);
                }
                sound(level, pos, SoundEvents.BLAZE_SHOOT, 1.0F, 0.8F);
                return true;
            }
            case "lunge" -> {
                Vec3 flat = new Vec3(look.x, 0, look.z).normalize();
                for (LivingEntity e : foes(p, pos.add(flat.scale(3.5)), 4.0)) hurt(p, e, 8);
                p.setDeltaMovement(flat.x * 1.8, 0.25, flat.z * 1.8);
                p.hurtMarked = true;
                burst(level, ParticleTypes.SWEEP_ATTACK, pos.add(flat).add(0, 1, 0), 1, 0.2, 0);
                burst(level, ParticleTypes.BUBBLE_POP, pos.add(0, 1, 0), 12, 0.5, 0.05);
                sound(level, pos, SoundEvents.TRIDENT_RIPTIDE_3.value(), 0.9F, 1.1F);
                return true;
            }
            case "skybolt" -> {
                LivingEntity t = aimed(p, 24);
                if (t == null) return false;
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
                if (bolt == null) return false;
                bolt.setPos(t.position());
                bolt.setCause(p);
                level.addFreshEntity(bolt);
                return true;
            }
            case "shadowstep" -> {
                Vec3 target = null;
                for (int dist = 9; dist >= 1; dist--) {
                    Vec3 dest = p.position().add(look.x * dist, look.y * dist, look.z * dist);
                    if (level.noCollision(p, p.getBoundingBox().move(dest.subtract(p.position())))) { target = dest; break; }
                }
                if (target == null) return false;
                burst(level, ParticleTypes.PORTAL, pos.add(0, 1, 0), 30, 0.4, 0.3);
                p.teleportTo(target.x, target.y, target.z);
                burst(level, ParticleTypes.PORTAL, target.add(0, 1, 0), 30, 0.4, 0.3);
                effect(p, MobEffects.INVISIBILITY, 60, 0);
                sound(level, target, SoundEvents.ENDERMAN_TELEPORT, 0.8F, 1.3F);
                return true;
            }
            case "slam" -> {
                for (LivingEntity e : foes(p, pos, 4)) {
                    hurt(p, e, 8);
                    e.push(0, 0.9, 0);
                    e.hurtMarked = true;
                }
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), pos.x, pos.y + 0.2, pos.z, 60, 2.0, 0.2, 2.0, 0.1);
                sound(level, pos, SoundEvents.ANVIL_USE, 1.0F, 0.6F);
                return true;
            }
            case "tidal" -> {
                Vec3 flat = new Vec3(look.x, 0, look.z).normalize();
                for (LivingEntity e : cone(p, 6, 0.5)) {
                    hurt(p, e, 4);
                    e.push(flat.x * 2.2, 0.5, flat.z * 2.2);
                    e.hurtMarked = true;
                }
                for (int i = 1; i <= 6; i++) {
                    burst(level, ParticleTypes.SPLASH, pos.add(flat.scale(i)).add(0, 0.8, 0), 14, 0.7, 0.2);
                    burst(level, ParticleTypes.BUBBLE_POP, pos.add(flat.scale(i)).add(0, 1.2, 0), 4, 0.5, 0.05);
                }
                effect(p, MobEffects.WATER_BREATHING, 600, 0);
                sound(level, pos, SoundEvents.TRIDENT_RIPTIDE_3.value(), 1.0F, 0.7F);
                return true;
            }
            case "beam" -> {
                LivingEntity t = aimed(p, 24);
                if (t == null) return false;
                Vec3 from = p.getEyePosition().add(0, -0.2, 0);
                Vec3 to = t.getBoundingBox().getCenter();
                int steps = (int) from.distanceTo(to) * 2;
                for (int i = 0; i <= steps; i++) {
                    Vec3 at = from.lerp(to, i / (double) Math.max(1, steps));
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, 1, 0.05, 0.05, 0.05, 0.0);
                }
                hurt(p, t, 14);
                sound(level, to, SoundEvents.LIGHTNING_BOLT_THUNDER, 0.6F, 1.8F);
                return true;
            }
            case "starfall" -> {
                for (LivingEntity e : cone(p, 8, 0.3)) {
                    hurt(p, e, 12);
                    effect(e, MobEffects.GLOWING, 200, 0);
                    burst(level, ParticleTypes.END_ROD, e.getBoundingBox().getCenter(), 20, 0.5, 0.1);
                }
                burst(level, ParticleTypes.END_ROD, p.getEyePosition().add(look.scale(3)), 30, 1.5, 0.05);
                sound(level, pos, SoundEvents.AMETHYST_BLOCK_CHIME, 1.2F, 0.5F);
                return true;
            }
            case "venom" -> {
                for (LivingEntity e : foes(p, pos, 5)) {
                    effect(e, MobEffects.POISON, 160, 0);
                    effect(e, MobEffects.WEAKNESS, 160, 0);
                }
                burst(level, ParticleTypes.ITEM_SLIME, pos.add(0, 0.8, 0), 40, 2.5, 0.05);
                burst(level, ParticleTypes.SNEEZE, pos.add(0, 0.8, 0), 20, 2.0, 0.02);
                sound(level, pos, SoundEvents.EVOKER_CAST_SPELL, 1.0F, 0.6F);
                return true;
            }
            case "leap" -> {
                Vec3 flat = new Vec3(look.x, 0, look.z).normalize();
                p.setDeltaMovement(flat.x * 0.9, 1.1, flat.z * 0.9);
                p.hurtMarked = true;
                effect(p, MobEffects.SLOW_FALLING, 120, 0);
                burst(level, ParticleTypes.CLOUD, pos, 14, 0.4, 0.05);
                sound(level, pos, SoundEvents.EVOKER_CAST_SPELL, 0.7F, 1.6F);
                return true;
            }
            case "oathcry" -> {
                for (Player a : level.getEntitiesOfClass(Player.class, p.getBoundingBox().inflate(8))) {
                    effect(a, MobEffects.STRENGTH, 400, 1);
                    effect(a, MobEffects.RESISTANCE, 400, 0);
                    burst(level, ParticleTypes.HAPPY_VILLAGER, a.position().add(0, 1, 0), 10, 0.5, 0.0);
                }
                for (LivingEntity e : foes(p, pos, 8)) effect(e, MobEffects.WEAKNESS, 200, 1);
                sound(level, pos, SoundEvents.RAID_HORN.value(), 1.5F, 1.0F);
                return true;
            }
            case "mend" -> {
                for (EquipmentSlot s : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET,
                    EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}) {
                    ItemStack it = p.getItemBySlot(s);
                    if (!it.isEmpty() && it.isDamaged()) {
                        it.setDamageValue(Math.max(0, it.getDamageValue() - it.getMaxDamage() / 4));
                    }
                }
                burst(level, ParticleTypes.LAVA, pos.add(0, 1, 0), 12, 0.5, 0.0);
                burst(level, ParticleTypes.CRIT, pos.add(0, 1.2, 0), 24, 0.6, 0.3);
                sound(level, pos, SoundEvents.ANVIL_USE, 1.0F, 1.2F);
                return true;
            }
            default -> {
                return false;
            }
        }
    }
}
