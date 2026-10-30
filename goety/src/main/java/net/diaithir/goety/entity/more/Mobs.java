package net.diaithir.goety.entity.more;

import java.util.EnumSet;
import net.diaithir.goety.GoetyEntities;
import net.diaithir.goety.GoetySounds;
import net.diaithir.goety.entity.BoltEntity;
import net.diaithir.goety.entity.Servant;
import net.diaithir.goety.entity.ServantCore;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/** The hostile creatures, bosses and bound servants beyond the first seven. Vanilla models, textures of the original. */
public final class Mobs {
    private Mobs() {}

    // ---------------------------------------------------------------- servants on other vanilla bodies
    public static class EvokerServant extends Evoker implements Servant {
        private ServantCore core;
        public EvokerServant(EntityType<? extends Evoker> t, Level l) { super(t, l); setPersistenceRequired(); }
        @Override public ServantCore core() { if (core == null) core = new ServantCore(this); return core; }
        @Override protected void registerGoals() { super.registerGoals(); core().bindGoals(this.goalSelector, this.targetSelector); }
        @Override public boolean canAttack(LivingEntity t) { return !core().isFriend(t) && super.canAttack(t); }
        @Override protected boolean shouldDropLoot(ServerLevel l) { return false; }
        @Override public boolean shouldDropExperience() { return false; }
        @Override public void tick() { super.tick(); core().tick(); }
        @Override protected void addAdditionalSaveData(ValueOutput o) { super.addAdditionalSaveData(o); core().save(o); }
        @Override protected void readAdditionalSaveData(ValueInput i) { super.readAdditionalSaveData(i); core().load(i); }
    }

    public static class VindicatorServant extends Vindicator implements Servant {
        private ServantCore core;
        public VindicatorServant(EntityType<? extends Vindicator> t, Level l) { super(t, l); setPersistenceRequired(); }
        @Override public ServantCore core() { if (core == null) core = new ServantCore(this); return core; }
        @Override protected void registerGoals() { super.registerGoals(); core().bindGoals(this.goalSelector, this.targetSelector); }
        @Override public boolean canAttack(LivingEntity t) { return !core().isFriend(t) && super.canAttack(t); }
        @Override protected boolean shouldDropLoot(ServerLevel l) { return false; }
        @Override public boolean shouldDropExperience() { return false; }
        @Override public void tick() { super.tick(); core().tick(); }
        @Override protected void addAdditionalSaveData(ValueOutput o) { super.addAdditionalSaveData(o); core().save(o); }
        @Override protected void readAdditionalSaveData(ValueInput i) { super.readAdditionalSaveData(i); core().load(i); }
    }

    public static class GolemServant extends IronGolem implements Servant {
        private ServantCore core;
        public GolemServant(EntityType<? extends IronGolem> t, Level l) { super(t, l); setPersistenceRequired(); }
        @Override public ServantCore core() { if (core == null) core = new ServantCore(this); return core; }
        @Override protected void registerGoals() { super.registerGoals(); core().bindGoals(this.goalSelector, this.targetSelector); }
        @Override public boolean canAttack(LivingEntity t) { return !core().isFriend(t) && super.canAttack(t); }
        @Override protected boolean shouldDropLoot(ServerLevel l) { return false; }
        @Override public boolean shouldDropExperience() { return false; }
        @Override public void tick() { super.tick(); core().tick(); }
        @Override protected void addAdditionalSaveData(ValueOutput o) { super.addAdditionalSaveData(o); core().save(o); }
        @Override protected void readAdditionalSaveData(ValueInput i) { super.readAdditionalSaveData(i); core().load(i); }
    }

    public static class WraithServant extends Vex implements Servant {
        private ServantCore core;
        public WraithServant(EntityType<? extends Vex> t, Level l) { super(t, l); setPersistenceRequired(); }
        @Override public ServantCore core() { if (core == null) core = new ServantCore(this); return core; }
        @Override protected void registerGoals() { super.registerGoals(); core().bindGoals(this.goalSelector, this.targetSelector); }
        @Override public boolean canAttack(LivingEntity t) { return !core().isFriend(t) && super.canAttack(t); }
        @Override protected boolean shouldDropLoot(ServerLevel l) { return false; }
        @Override public boolean shouldDropExperience() { return false; }
        @Override public void tick() { super.tick(); core().tick(); }
        @Override protected void addAdditionalSaveData(ValueOutput o) { super.addAdditionalSaveData(o); core().save(o); }
        @Override protected void readAdditionalSaveData(ValueInput i) { super.readAdditionalSaveData(i); core().load(i); }
    }

    // ---------------------------------------------------------------- hostile creatures
    public static class HostileZombie extends Zombie {
        public HostileZombie(EntityType<? extends Zombie> t, Level l) { super(t, l); }
        @Override protected boolean isSunSensitive() { return false; }
    }

    public static class HostileSkeleton extends Skeleton {
        public HostileSkeleton(EntityType<? extends Skeleton> t, Level l) { super(t, l); }
    }

    public static class HostileVindicator extends Vindicator {
        public HostileVindicator(EntityType<? extends Vindicator> t, Level l) { super(t, l); }
    }

    public static class HostileEvoker extends Evoker {
        public HostileEvoker(EntityType<? extends Evoker> t, Level l) { super(t, l); }
    }

    /** A wraith: a vex that is not bound to an evoker and does not crumble. */
    public static class Wraith extends Vex {
        public Wraith(EntityType<? extends Vex> t, Level l) { super(t, l); }
        @Override protected net.minecraft.sounds.SoundEvent getAmbientSound() { return GoetySounds.get("wraith_ambient", super.getAmbientSound()); }
        @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource s) { return GoetySounds.get("wraith_hurt", super.getHurtSound(s)); }
        @Override protected net.minecraft.sounds.SoundEvent getDeathSound() { return GoetySounds.get("wraith_death", super.getDeathSound()); }
        @Override public void tick() {
            super.tick();
            if (level() instanceof ServerLevel sl && tickCount % 10 == 0) sl.sendParticles(ParticleTypes.SOUL, getX(), getY() + 0.5, getZ(), 1, 0.2, 0.2, 0.2, 0.01);
        }
    }

    /** A necromancer: keeps its distance, flings soul bolts and calls its dead (minion type per variant). */
    public static class Necromancer extends Zombie {
        private String minion = "frayed";
        public Necromancer(EntityType<? extends Zombie> t, Level l) { super(t, l); }
        @Override protected net.minecraft.sounds.SoundEvent getAmbientSound() { return GoetySounds.get("necromancer_ambient", super.getAmbientSound()); }
        @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource s) { return GoetySounds.get("necromancer_hurt", super.getHurtSound(s)); }
        @Override protected net.minecraft.sounds.SoundEvent getDeathSound() { return GoetySounds.get("necromancer_death", super.getDeathSound()); }
        public Necromancer minion(String id) { this.minion = id; return this; }
        @Override protected boolean isSunSensitive() { return false; }
        @Override protected void registerGoals() {
            goalSelector.addGoal(0, new FloatGoal(this));
            goalSelector.addGoal(2, new CastGoal(this));
            goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
            goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
            targetSelector.addGoal(1, new HurtByTargetGoal(this));
            targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        }
        @Override protected boolean convertsInWater() { return false; }
        @Override protected void addAdditionalSaveData(ValueOutput o) { super.addAdditionalSaveData(o); o.putString("Minion", minion); }
        @Override protected void readAdditionalSaveData(ValueInput i) { super.readAdditionalSaveData(i); minion = i.getStringOr("Minion", minion); }

        static final class CastGoal extends Goal {
            private final Necromancer n;
            private int cd = 40;
            CastGoal(Necromancer n) { this.n = n; setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
            @Override public boolean canUse() { LivingEntity t = n.getTarget(); return t != null && t.isAlive() && n.distanceToSqr(t) < 24 * 24; }
            @Override public boolean requiresUpdateEveryTick() { return true; }
            @Override public void tick() {
                LivingEntity t = n.getTarget();
                if (t == null || !(n.level() instanceof ServerLevel sl)) return;
                n.getLookControl().setLookAt(t);
                double d = n.distanceTo(t);
                if (d < 7) n.getNavigation().moveTo(n.getX() + (n.getX() - t.getX()), n.getY(), n.getZ() + (n.getZ() - t.getZ()), 1.1);
                else if (d > 14) n.getNavigation().moveTo(t, 1.0);
                else n.getNavigation().stop();
                if (--cd > 0) return;
                if (n.tickCount % 3 == 0 && sl.getRandom().nextInt(4) == 0) {
                    cd = 120;
                    int have = sl.getEntitiesOfClass(Mob.class, n.getBoundingBox().inflate(16), m -> m != n && m.getType().equals(GoetyEntities.MOBS.get(n.minion).get())).size();
                    if (have < 4) for (int k = 0; k < 2; k++) {
                        Mob m = GoetyEntities.MOBS.get(n.minion).get().create(sl, EntitySpawnReason.MOB_SUMMONED);
                        if (m == null) continue;
                        m.snapTo(n.getX() + sl.getRandom().nextInt(5) - 2, n.getY(), n.getZ() + sl.getRandom().nextInt(5) - 2, 0, 0);
                        m.setTarget(t);
                        sl.addFreshEntity(m);
                        sl.sendParticles(ParticleTypes.SOUL, m.getX(), m.getY() + 0.5, m.getZ(), 10, 0.3, 0.4, 0.3, 0.02);
                    }
                } else if (n.tickCount % 3 == 0) {
                    cd = 50;
                    Vec3 dir = t.getEyePosition().subtract(n.getEyePosition()).normalize();
                    BoltEntity b = new BoltEntity(GoetyEntities.PROJECTILES.get("soul_bolt").get(), n, dir.scale(0.8), sl);
                    b.setDamage(4.0F);
                    b.setPos(n.getEyePosition().add(dir));
                    sl.addFreshEntity(b);
                }
            }
        }
    }

    /** The Skull Lord: a boss skeleton that rains wither skulls and raises rattled skeletons as it weakens. */
    public static class SkullLord extends Skeleton {
        private final ServerBossEvent bar = new ServerBossEvent(java.util.UUID.randomUUID(), getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
        private int skullCd = 60;
        public SkullLord(EntityType<? extends Skeleton> t, Level l) { super(t, l); xpReward = 50; setPersistenceRequired(); }
        @Override protected net.minecraft.sounds.SoundEvent getAmbientSound() { return GoetySounds.get("skull_lord_ambient", super.getAmbientSound()); }
        @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource s) { return GoetySounds.get("skull_lord_hurt", super.getHurtSound(s)); }
        @Override protected net.minecraft.sounds.SoundEvent getDeathSound() { return GoetySounds.get("skull_lord_death", super.getDeathSound()); }

        @Override public void startSeenByPlayer(ServerPlayer p) { super.startSeenByPlayer(p); bar.addPlayer(p); }
        @Override public void stopSeenByPlayer(ServerPlayer p) { super.stopSeenByPlayer(p); bar.removePlayer(p); }
        @Override public boolean canUsePortal(boolean b) { return false; }
        @Override public void aiStep() {
            super.aiStep();
            bar.setName(getDisplayName());
            bar.setProgress(getHealth() / getMaxHealth());
            LivingEntity t = getTarget();
            if (t != null && level() instanceof ServerLevel sl && --skullCd <= 0) {
                skullCd = 60 + getRandom().nextInt(40);
                Vec3 dir = t.getEyePosition().subtract(getEyePosition()).normalize();
                var skull = new net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull(sl, this, dir.scale(0.5));
                skull.setPos(getEyePosition().add(dir));
                sl.addFreshEntity(skull);
                if (getHealth() < getMaxHealth() / 2 && getRandom().nextInt(3) == 0) {
                    Mob m = GoetyEntities.MOBS.get("rattled").get().create(sl, EntitySpawnReason.MOB_SUMMONED);
                    if (m != null) { m.snapTo(getX() + 1, getY(), getZ() + 1, 0, 0); m.setTarget(t); sl.addFreshEntity(m); }
                }
            }
        }
    }
}
