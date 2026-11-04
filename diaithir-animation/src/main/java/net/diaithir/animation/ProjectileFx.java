package net.diaithir.animation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * Client-only projectile animation for every projectile entity of every mod: a launch burst along the flight line, a helix around the flight path (the
 * flight spin), a trail, and an impact or despawn burst where it ends. The look comes from the entity id (fire, frost, venom, lightning, arcane, blood,
 * light, water, shaft-like missiles) with a plain spark default. Particles only, so nothing depends on any mod's renderer.
 */
final class ProjectileFx {
    private static final class T { Vec3 last, vel; int age; ParticleOptions fx; boolean shaft; }
    private static final Map<Integer, T> TRACK = new ConcurrentHashMap<>();
    private static final double RANGE = 64 * 64;

    static void init() {
        NeoForge.EVENT_BUS.addListener(EntityJoinLevelEvent.class, ProjectileFx::join);
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, e -> tick());
    }

    static ParticleOptions look(String id) {
        String s = id.toLowerCase();
        if (s.matches(".*(fire|flame|lava|ember|blaze|molten|magma|dragon_charge|explo).*")) return ParticleTypes.FLAME;
        if (s.matches(".*(ice|frost|snow|arctic|cold|freez).*")) return ParticleTypes.SNOWFLAKE;
        if (s.matches(".*(poison|venom|toxic|acid|slime|spore).*")) return ParticleTypes.HAPPY_VILLAGER;
        if (s.matches(".*(lightning|spark|electr|tesla|storm|thunder).*")) return ParticleTypes.ELECTRIC_SPARK;
        if (s.matches(".*(arcane|magic|star|shimmer|aureal|mana|spell|rune|crystal).*")) return ParticleTypes.END_ROD;
        if (s.matches(".*(blood|vengeance|vampir|crimson).*")) return ParticleTypes.CRIMSON_SPORE;
        if (s.matches(".*(holy|sun|light|radiant|glow).*")) return ParticleTypes.GLOW;
        if (s.matches(".*(water|aqua|bubble|wave).*")) return ParticleTypes.BUBBLE;
        return ParticleTypes.CRIT;
    }

    private static void join(EntityJoinLevelEvent e) {
        if (!e.getLevel().isClientSide() || !(e.getEntity() instanceof Projectile) || e.getEntity() instanceof FireworkRocketEntity) return;
        Entity en = e.getEntity();
        T t = new T();
        t.last = en.position();
        t.vel = en.getDeltaMovement();
        t.fx = look(BuiltInRegistries.ENTITY_TYPE.getKey(en.getType()).getPath());
        TRACK.put(en.getId(), t);
        Vec3 d = t.vel.lengthSqr() > 1e-4 ? t.vel.normalize() : new Vec3(0, 0, 1);
        for (int i = 0; i < 6; i++) e.getLevel().addParticle(t.fx, en.getX(), en.getY(), en.getZ(), d.x * 0.15 + (i - 3) * 0.01, d.y * 0.15, d.z * 0.15 + (3 - i) * 0.01);
    }

    private static void tick() {
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.isPaused()) { return; }
        for (var it = TRACK.entrySet().iterator(); it.hasNext(); ) {
            var me = it.next();
            T t = me.getValue();
            Entity en = mc.level.getEntity(me.getKey());
            if (en == null || en.isRemoved()) {
                it.remove();
                if (mc.player.distanceToSqr(t.last) > RANGE) continue;
                // ended: hit if it was young or moving fast, otherwise it timed out
                boolean timeout = t.age > 1100;
                int n = timeout ? 4 : 14;
                for (int i = 0; i < n; i++) {
                    double a = i * 6.283 / n;
                    mc.level.addParticle(timeout ? ParticleTypes.SMOKE : t.fx, t.last.x, t.last.y, t.last.z, Math.cos(a) * 0.12, 0.05 + 0.04 * (i % 3), Math.sin(a) * 0.12);
                }
                if (!timeout) mc.level.addParticle(ParticleTypes.POOF, t.last.x, t.last.y, t.last.z, 0, 0.02, 0);
                continue;
            }
            t.age++;
            Vec3 now = en.position();
            Vec3 v = now.subtract(t.last);
            t.last = now;
            if (v.lengthSqr() < 1e-5 || mc.player.distanceToSqr(now) > RANGE) continue; // stuck in a block or far away
            // trail, plus a two-strand helix around the flight line for the spin
            mc.level.addParticle(t.fx, now.x, now.y, now.z, 0, 0, 0);
            Vec3 d = v.normalize();
            Vec3 u = Math.abs(d.y) > 0.9 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
            Vec3 a1 = d.cross(u).normalize(), a2 = d.cross(a1);
            double ph = t.age * 0.9;
            for (int s = 0; s < 2; s++) {
                double ang = ph + s * Math.PI;
                Vec3 o = a1.scale(Math.cos(ang) * 0.22).add(a2.scale(Math.sin(ang) * 0.22));
                mc.level.addParticle(t.fx, now.x + o.x, now.y + o.y, now.z + o.z, 0, 0, 0);
            }
        }
    }

    private ProjectileFx() {}
}
