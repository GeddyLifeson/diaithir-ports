package net.diaithir.goety.magic;

import java.util.List;
import net.diaithir.goety.GoetyEntities;
import net.diaithir.goety.entity.BoltEntity;
import net.diaithir.goety.entity.Servant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Interprets the spell archetypes of spells.json (projectile, ray, cone, aoe, buff, heal, summon, ...). */
public final class SpellEngine {
    public static final int SERVANT_CAP = 16;

    private SpellEngine() {}

    /** Casts the spell; false means it could not be cast (nothing is spent by the caller then). */
    public static boolean cast(SpellDef d, ServerLevel level, LivingEntity caster) {
        return switch (d.kind()) {
            case "projectile" -> projectile(d, level, caster);
            case "ray" -> ray(d, level, caster);
            case "cone" -> cone(d, level, caster);
            case "aoe" -> aoe(d, level, caster);
            case "buff" -> buff(d, caster);
            case "heal" -> heal(d, level, caster);
            case "summon" -> summon(d, level, caster);
            case "lightning" -> lightning(d, level, caster);
            case "fangs" -> fangs(d, level, caster);
            case "rain" -> rain(d, level, caster);
            case "launch" -> launch(d, level, caster);
            case "teleport" -> teleport(d, level, caster);
            default -> false;
        };
    }

    // ------------------------------------------------------------------ helpers
    static Holder<MobEffect> effect(String name) {
        return BuiltInRegistries.MOB_EFFECT.get(Identifier.withDefaultNamespace(name)).orElseThrow();
    }

    static void applyEffects(SpellDef d, LivingEntity target, @Nullable LivingEntity caster) {
        for (Object[] e : d.effects()) target.addEffect(new MobEffectInstance(effect((String) e[0]), (Integer) e[1], (Integer) e[2]), caster);
    }

    static ParticleOptions particle(String name) {
        if (name.equals("block")) return new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState());
        var pt = BuiltInRegistries.PARTICLE_TYPE.getValue(Identifier.withDefaultNamespace(name));
        return pt instanceof SimpleParticleType s ? s : ParticleTypes.POOF;
    }

    static void puff(ServerLevel level, String name, Vec3 at, int n, double spread) {
        level.sendParticles(particle(name), at.x, at.y, at.z, n, spread, spread, spread, 0.02);
    }

    static void sound(ServerLevel level, Entity e, net.minecraft.sounds.SoundEvent s, float pitch) {
        level.playSound(null, e.getX(), e.getY(), e.getZ(), s, SoundSource.PLAYERS, 1.0F, pitch);
    }

    static DamageSource source(ServerLevel level, LivingEntity caster) {
        return level.damageSources().indirectMagic(caster, caster);
    }

    static boolean isFoe(LivingEntity caster, Entity e) {
        if (e == caster || !(e instanceof LivingEntity le) || !le.isAlive()) return false;
        if (e instanceof Servant s && s.core().isFriend(caster)) return false;
        if (caster instanceof Servant cs && cs.core().isFriend(e)) return false;
        return !(e instanceof net.minecraft.world.entity.player.Player p && (p.isCreative() || p.isSpectator()));
    }

    static void damage(SpellDef d, ServerLevel level, LivingEntity caster, LivingEntity target, double amount) {
        if (amount > 0) target.hurtServer(level, source(level, caster), (float) amount);
        int fire = d.i("fire", 0);
        if (fire > 0) target.igniteForSeconds(fire);
        int freeze = d.i("freeze", 0);
        if (freeze > 0) target.setTicksFrozen(Math.min(target.getTicksFrozen() + freeze, target.getTicksRequiredToFreeze() + 140));
        applyEffects(d, target, caster);
    }

    static void knock(LivingEntity caster, LivingEntity target, double strength, double up) {
        if (strength == 0 && up == 0) return;
        Vec3 away = target.position().subtract(caster.position()).multiply(1, 0, 1);
        away = away.lengthSqr() < 1.0E-4 ? caster.getLookAngle().multiply(1, 0, 1) : away.normalize();
        target.push(away.x * strength, up, away.z * strength);
        target.hurtMarked = true;
    }

    @Nullable
    static LivingEntity rayEntity(LivingEntity caster, double range) {
        Vec3 from = caster.getEyePosition(), look = caster.getViewVector(1.0F), to = from.add(look.scale(range));
        AABB box = caster.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(caster.level(), caster, from, to, box, e -> e instanceof LivingEntity le && le != caster && le.isAlive(), 0.3F);
        return hit != null && hit.getEntity() instanceof LivingEntity le ? le : null;
    }

    static Vec3 rayEnd(LivingEntity caster, double range) {
        Vec3 from = caster.getEyePosition(), to = from.add(caster.getViewVector(1.0F).scale(range));
        BlockHitResult b = caster.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        return b.getType() == HitResult.Type.MISS ? to : b.getLocation();
    }

    // ------------------------------------------------------------------ archetypes
    private static boolean projectile(SpellDef d, ServerLevel level, LivingEntity caster) {
        int count = d.i("count", 1);
        double speed = d.d("speed", 1.2), spread = d.d("spread", 0.0);
        Vec3 eye = caster.getEyePosition();
        for (int n = 0; n < count; n++) {
            Vec3 dir = caster.getViewVector(1.0F);
            if (spread > 0 || count > 1) {
                double off = count > 1 ? (n - (count - 1) / 2.0) * spread : 0;
                dir = dir.add(level.getRandom().nextGaussian() * spread * 0.2 + off, level.getRandom().nextGaussian() * spread * 0.2, level.getRandom().nextGaussian() * spread * 0.2).normalize();
            }
            Vec3 at = eye.add(dir.scale(0.8));
            Entity e;
            switch (d.s("proj", "soul_bolt")) {
                case "soul_bolt", "magic_bolt" -> {
                    @SuppressWarnings("unchecked")
                    EntityType<BoltEntity> t = GoetyEntities.PROJECTILES.get(d.s("proj", "soul_bolt")).get();
                    BoltEntity b = new BoltEntity(t, caster, dir.scale(speed * 0.5), level);
                    b.setDamage((float) d.d("damage", 4.0));
                    e = b;
                }
                case "small_fireball" -> e = new SmallFireball(level, caster, dir.scale(speed * 0.4));
                case "large_fireball" -> e = new LargeFireball(level, caster, dir.scale(speed * 0.4), d.i("power", 1));
                case "wither_skull" -> e = new WitherSkull(level, caster, dir.scale(speed * 0.4));
                default -> { return false; }
            }
            e.setPos(at);
            level.addFreshEntity(e);
        }
        sound(level, caster, d.s("proj", "").contains("fireball") ? SoundEvents.BLAZE_SHOOT : SoundEvents.EVOKER_CAST_SPELL, 1.0F);
        return true;
    }

    private static boolean ray(SpellDef d, ServerLevel level, LivingEntity caster) {
        double range = d.d("range", 16);
        LivingEntity t = rayEntity(caster, range);
        Vec3 from = caster.getEyePosition().add(0, -0.2, 0), end = t != null ? t.getBoundingBox().getCenter() : rayEnd(caster, range);
        String p = d.s("particle", "witch");
        int steps = (int) Math.max(2, from.distanceTo(end) * 2);
        for (int i = 0; i <= steps; i++) puff(level, p, from.lerp(end, i / (double) steps), 1, 0.02);
        sound(level, caster, SoundEvents.EVOKER_CAST_SPELL, 1.2F);
        if (t == null || !isFoe(caster, t) && d.d("damage", 0) > 0) return true;
        damage(d, level, caster, t, d.d("damage", 0));
        knock(caster, t, d.d("knock", 0), d.d("up", 0));
        double heal = d.d("heal_self", 0);
        if (heal > 0) caster.heal((float) heal);
        puff(level, p, t.position().add(0, t.getBbHeight() / 2, 0), 6, 0.3);
        return true;
    }

    private static boolean cone(SpellDef d, ServerLevel level, LivingEntity caster) {
        double range = d.d("range", 6);
        Vec3 from = caster.getEyePosition().add(0, -0.25, 0), look = caster.getViewVector(1.0F);
        String p = d.s("particle", "poof");
        for (int i = 0; i < 10; i++) {
            double dist = 1 + level.getRandom().nextDouble() * (range - 1);
            Vec3 at = from.add(look.scale(dist)).add((level.getRandom().nextDouble() - 0.5) * dist * 0.4, (level.getRandom().nextDouble() - 0.5) * dist * 0.3, (level.getRandom().nextDouble() - 0.5) * dist * 0.4);
            puff(level, p, at, 1, 0.01);
        }
        AABB box = caster.getBoundingBox().expandTowards(look.scale(range)).inflate(2.0);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, x -> isFoe(caster, x))) {
            Vec3 to = e.getBoundingBox().getCenter().subtract(from);
            double dist = to.length();
            if (dist > range || dist < 0.01 || to.normalize().dot(look) < 0.8) continue;
            damage(d, level, caster, e, d.d("damage", 1));
            knock(caster, e, d.d("knock", 0), d.d("up", 0));
        }
        if (level.getRandom().nextInt(4) == 0) sound(level, caster, d.d("fire", 0) > 0 ? SoundEvents.FIRECHARGE_USE : SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F);
        return true;
    }

    private static boolean aoe(SpellDef d, ServerLevel level, LivingEntity caster) {
        double r = d.d("radius", 5);
        Vec3 c = caster.position();
        String p = d.s("particle", "poof");
        for (int i = 0; i < 28; i++) {
            double a = i / 28.0 * Math.PI * 2;
            puff(level, p, c.add(Math.cos(a) * r * 0.9, 0.2, Math.sin(a) * r * 0.9), 1, 0.05);
            puff(level, p, c.add(Math.cos(a) * r * 0.45, 0.3, Math.sin(a) * r * 0.45), 1, 0.05);
        }
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(r), x -> isFoe(caster, x) && x.distanceToSqr(caster) <= r * r)) {
            damage(d, level, caster, e, d.d("damage", 0));
            knock(caster, e, d.d("knock", 0), d.d("up", 0));
        }
        sound(level, caster, SoundEvents.GENERIC_EXPLODE.value(), 1.4F);
        return true;
    }

    private static boolean buff(SpellDef d, LivingEntity caster) {
        applyEffects(d, caster, caster);
        if (caster.level() instanceof ServerLevel sl) {
            puff(sl, "happy_villager", caster.position().add(0, 1, 0), 8, 0.4);
            sound(sl, caster, SoundEvents.EVOKER_CAST_SPELL, 1.4F);
        }
        return true;
    }

    private static boolean heal(SpellDef d, ServerLevel level, LivingEntity caster) {
        LivingEntity t = caster.isShiftKeyDown() ? caster : rayEntity(caster, d.d("range", 12));
        if (t == null) t = caster;
        if (t.getHealth() >= t.getMaxHealth()) return false;
        t.heal((float) d.d("amount", 6));
        puff(level, "heart", t.position().add(0, t.getBbHeight() + 0.2, 0), 5, 0.3);
        sound(level, t, SoundEvents.EVOKER_CAST_SPELL, 1.6F);
        return true;
    }

    private static boolean summon(SpellDef d, ServerLevel level, LivingEntity caster) {
        List<String> mobs = d.list("mobs");
        int count = d.i("count", 1);
        int mine = level.getEntitiesOfClass(Mob.class, caster.getBoundingBox().inflate(64), m -> m instanceof Servant s && caster.getUUID().equals(s.core().ownerId())).size();
        if (mine + count > SERVANT_CAP) return false;
        for (int n = 0; n < count; n++) {
            String id = mobs.get(level.getRandom().nextInt(mobs.size()));
            Mob m = GoetyEntities.MOBS.get(id).get().create(level, EntitySpawnReason.MOB_SUMMONED) instanceof Mob mm ? mm : null;
            if (m == null) continue;
            double a = level.getRandom().nextDouble() * Math.PI * 2, r = 1.5 + level.getRandom().nextDouble() * d.d("spread", 3);
            BlockPos pos = BlockPos.containing(caster.getX() + Math.cos(a) * r, caster.getY(), caster.getZ() + Math.sin(a) * r);
            m.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.getRandom().nextFloat() * 360F, 0);
            ((Servant) m).core().setOwner(caster);
            if (d.i("life", 0) > 0) ((Servant) m).core().setLife(d.i("life", 0));
            level.addFreshEntity(m);
            puff(level, "soul", m.position().add(0, 0.5, 0), 12, 0.4);
        }
        sound(level, caster, SoundEvents.EVOKER_PREPARE_SUMMON, 1.0F);
        return true;
    }

    private static boolean lightning(SpellDef d, ServerLevel level, LivingEntity caster) {
        double range = d.d("range", 24);
        LivingEntity t = rayEntity(caster, range);
        Vec3 at = t != null ? t.position() : rayEnd(caster, range);
        int strikes = d.s("mode", "target").equals("storm") ? d.i("count", 5) : 1;
        double radius = d.d("radius", 0);
        for (int i = 0; i < strikes; i++) {
            Vec3 p = at.add((level.getRandom().nextDouble() - 0.5) * 2 * radius, 0, (level.getRandom().nextDouble() - 0.5) * 2 * radius);
            var bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
            if (bolt == null) return false;
            bolt.snapTo(p.x, p.y, p.z);
            if (caster instanceof net.minecraft.server.level.ServerPlayer sp) bolt.setCause(sp);
            level.addFreshEntity(bolt);
        }
        return true;
    }

    private static boolean fangs(SpellDef d, ServerLevel level, LivingEntity caster) {
        int count = d.i("count", 8);
        Vec3 look = caster.getLookAngle().multiply(1, 0, 1).normalize();
        float yaw = (float) Math.atan2(look.z, look.x);
        if (d.s("mode", "line").equals("line")) {
            for (int i = 0; i < count; i++) {
                double dist = 1.5 + i * 1.3;
                level.addFreshEntity(new EvokerFangs(level, caster.getX() + look.x * dist, caster.getY(), caster.getZ() + look.z * dist, yaw, i * 2, caster));
            }
        } else {
            for (int i = 0; i < count; i++) {
                double a = i * Math.PI * 2 / count, r = 2.0 + (i % 2);
                level.addFreshEntity(new EvokerFangs(level, caster.getX() + Math.cos(a) * r, caster.getY(), caster.getZ() + Math.sin(a) * r, (float) a, i, caster));
            }
        }
        sound(level, caster, SoundEvents.EVOKER_CAST_SPELL, 1.0F);
        return true;
    }

    private static boolean rain(SpellDef d, ServerLevel level, LivingEntity caster) {
        double range = d.d("range", 24);
        LivingEntity t = rayEntity(caster, range);
        Vec3 at = t != null ? t.position() : rayEnd(caster, range);
        double radius = d.d("radius", 4), height = d.d("height", 12);
        for (int i = 0; i < d.i("count", 10); i++) {
            Vec3 p = at.add((level.getRandom().nextDouble() - 0.5) * 2 * radius, height + level.getRandom().nextDouble() * 3, (level.getRandom().nextDouble() - 0.5) * 2 * radius);
            Entity e;
            switch (d.s("proj", "arrow")) {
                case "arrow" -> { Arrow a = new Arrow(level, caster, new ItemStack(Items.ARROW), null); a.setBaseDamage(2.0); a.setDeltaMovement(0, -1.6, 0); e = a; }
                case "snowball" -> { Snowball s = new Snowball(level, caster, new ItemStack(Items.SNOWBALL)); s.setDeltaMovement(0, -1.2, 0); e = s; }
                case "small_fireball" -> e = new SmallFireball(level, caster, new Vec3(0, -0.6, 0));
                case "large_fireball" -> e = new LargeFireball(level, caster, new Vec3(0, -0.6, 0), 1);
                default -> { return false; }
            }
            e.setPos(p);
            level.addFreshEntity(e);
        }
        sound(level, caster, SoundEvents.EVOKER_CAST_SPELL, 0.8F);
        return true;
    }

    private static boolean launch(SpellDef d, ServerLevel level, LivingEntity caster) {
        Vec3 look = caster.getLookAngle().multiply(1, 0, 1).normalize();
        caster.push(look.x * d.d("forward", 0), d.d("up", 0), look.z * d.d("forward", 0));
        caster.hurtMarked = true;
        caster.resetFallDistance();
        puff(level, "cloud", caster.position(), 10, 0.3);
        sound(level, caster, SoundEvents.WIND_CHARGE_BURST.value(), 1.0F);
        return true;
    }

    private static boolean teleport(SpellDef d, ServerLevel level, LivingEntity caster) {
        Vec3 end = rayEnd(caster, d.d("range", 20));
        Vec3 dest = end.subtract(caster.getLookAngle().scale(0.6));
        puff(level, "portal", caster.position().add(0, 1, 0), 24, 0.4);
        caster.teleportTo(dest.x, dest.y, dest.z);
        caster.resetFallDistance();
        puff(level, "portal", dest.add(0, 1, 0), 24, 0.4);
        sound(level, caster, SoundEvents.ENDERMAN_TELEPORT, 1.0F);
        return true;
    }
}
