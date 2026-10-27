package diaithir.myth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The one entity class behind every Myth Beast. A {@link Creature} row supplies the stats and the preset:
 * beast (walks), flyer (hovers and flees) or avatar (two-phase boss: below half health it speeds up and gains
 * one extra attack).
 */
public class MythBeast extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> ENRAGED = SynchedEntityData.defineId(MythBeast.class, EntityDataSerializers.BOOLEAN);
    private static final Identifier PHASE2_SPEED = Identifier.fromNamespaceAndPath(DiaithirMyth.MODID, "phase2_speed");
    private static final Identifier PHASE2_ATTACK = Identifier.fromNamespaceAndPath(DiaithirMyth.MODID, "phase2_attack");

    /** Hostile creatures count as monsters for the rest of the game (iron golems, peaceful difficulty). */
    public static class Hostile extends MythBeast implements Enemy {
        public Hostile(EntityType<? extends MythBeast> type, Level level, Creature c) {
            super(type, level, c);
        }
    }

    public final Creature def;

    /** The super constructor registers goals and navigation before our fields exist, so resolve the row from the type. */
    private Creature d() {
        return def != null ? def : Creature.byId(BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()).getPath());
    }

    private final @Nullable ServerBossEvent bossEvent;
    private int abilityCooldown = 100;

    public MythBeast(EntityType<? extends MythBeast> type, Level level, Creature def) {
        super(type, level);
        this.def = def;
        this.xpReward = def.xp;
        if (def.flyer) {
            this.moveControl = new FlyingMoveControl(this, 20, true);
            this.setPathfindingMalus(PathType.WATER, -1.0F);
            this.setPathfindingMalus(PathType.FENCE, -1.0F);
        }
        this.bossEvent = def.boss && !level.isClientSide()
                ? new ServerBossEvent(Mth.createInsecureUUID(this.random), this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS)
                : null;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ENRAGED, false);
    }

    public boolean isEnraged() {
        return this.entityData.get(ENRAGED);
    }

    private boolean fights() {
        return d().attack > 0.0D;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        if (d().flyer) {
            FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
            nav.setCanOpenDoors(false);
            nav.setCanFloat(false);
            return nav;
        }
        return super.createNavigation(level);
    }

    @Override
    protected void registerGoals() {
        if (!d().flyer) {
            this.goalSelector.addGoal(0, new FloatGoal(this));
        }
        if (fights()) {
            this.goalSelector.addGoal(2, new MeleeAttackGoal(this, d().hostile ? 1.2D : 1.4D, true));
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            if (d().hostile) {
                this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
            }
        } else {
            this.goalSelector.addGoal(1, new PanicGoal(this, 1.6D));
        }
        if (d().flyer) {
            this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        } else {
            this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        }
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
        return !def.flyer && super.causeFallDamage(fallDistance, damageModifier, damageSource);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !def.boss && super.removeWhenFarAway(distanceToClosestPlayer);
    }

    // ---- combat ----

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        if (hit && target instanceof LivingEntity living && "poisonhit".equals(def.ability)) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0), this);
        }
        return hit;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (def.boss) {
            if (bossEvent != null) {
                bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
            }
            if (!isEnraged() && this.getHealth() <= this.getMaxHealth() * 0.5F) {
                enrage(level);
            }
            if (isEnraged() && this.getTarget() != null && --abilityCooldown <= 0) {
                abilityCooldown = 90 + this.random.nextInt(40);
                useAbility(level, this.getTarget());
            }
        }
    }

    private void enrage(ServerLevel level) {
        this.entityData.set(ENRAGED, true);
        applyPhaseTwo();
        if (bossEvent != null) {
            bossEvent.setColor(BossEvent.BossBarColor.RED);
        }
        level.playSound(null, this.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 2.0F, 0.8F);
        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, getX(), getY() + getBbHeight() * 0.7, getZ(), 12, getBbWidth() * 0.4, 0.4, getBbWidth() * 0.4, 0.0);
    }

    private void applyPhaseTwo() {
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && !speed.hasModifier(PHASE2_SPEED)) {
            speed.addPermanentModifier(new AttributeModifier(PHASE2_SPEED, 0.35D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        AttributeInstance atk = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null && !atk.hasModifier(PHASE2_ATTACK)) {
            atk.addPermanentModifier(new AttributeModifier(PHASE2_ATTACK, 0.2D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }

    /** The extra attack of phase two, chosen by the creature's ability. */
    private void useAbility(ServerLevel level, LivingEntity target) {
        double dist = this.distanceTo(target);
        double dmg = this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        switch (def.ability) {
            case "charge" -> {
                if (dist > 3.0D && dist < 16.0D) {
                    Vec3 dir = target.position().subtract(this.position()).multiply(1, 0, 1).normalize();
                    this.setDeltaMovement(dir.x * 1.3D, 0.3D, dir.z * 1.3D);
                    this.hurtMarked = true;
                    level.playSound(null, this.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.0F, 1.2F);
                }
            }
            case "web" -> {
                if (dist < 12.0D) {
                    for (LivingEntity e : nearbyFoes(level, 12.0D)) {
                        e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 120, 2), this);
                        level.sendParticles(ParticleTypes.CLOUD, e.getX(), e.getY() + 0.3, e.getZ(), 8, 0.3, 0.2, 0.3, 0.01);
                    }
                    level.playSound(null, this.blockPosition(), SoundEvents.SPIDER_AMBIENT, SoundSource.HOSTILE, 1.5F, 0.6F);
                }
            }
            case "venom" -> {
                if (dist < 8.0D) {
                    for (LivingEntity e : nearbyFoes(level, 7.0D)) {
                        e.addEffect(new MobEffectInstance(MobEffects.POISON, 140, 1), this);
                        e.hurtServer(level, this.damageSources().mobAttack(this), (float) (dmg * 0.4D));
                    }
                    level.sendParticles(ParticleTypes.ITEM_SLIME, getX(), getY() + 0.5, getZ(), 24, 2.5, 0.4, 2.5, 0.0);
                }
            }
            default -> { // slam
                if (dist < 7.0D) {
                    for (LivingEntity e : nearbyFoes(level, 6.0D)) {
                        e.hurtServer(level, this.damageSources().mobAttack(this), (float) (dmg * 0.6D));
                        e.knockback(1.2D, this.getX() - e.getX(), this.getZ() - e.getZ());
                        e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1), this);
                    }
                    level.sendParticles(ParticleTypes.POOF, getX(), getY() + 0.2, getZ(), 30, 2.5, 0.1, 2.5, 0.05);
                    level.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.0F, 0.7F);
                }
            }
        }
    }

    private List<LivingEntity> nearbyFoes(ServerLevel level, double radius) {
        AABB box = this.getBoundingBox().inflate(radius, 2.0D, radius);
        return level.getEntitiesOfClass(LivingEntity.class, box, e -> e != this && !(e instanceof MythBeast) && e.isAlive()
                && (e instanceof Player p ? !p.isCreative() && !p.isSpectator() : e == this.getTarget()));
    }

    // ---- boss bar and saving ----

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        if (bossEvent != null) {
            bossEvent.setName(this.getDisplayName());
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (bossEvent != null) {
            bossEvent.addPlayer(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        if (bossEvent != null) {
            bossEvent.removePlayer(player);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Enraged", isEnraged());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        if (input.getBooleanOr("Enraged", false)) {
            this.entityData.set(ENRAGED, true);
            applyPhaseTwo();
            if (bossEvent != null) {
                bossEvent.setColor(BossEvent.BossBarColor.RED);
            }
        }
        if (this.hasCustomName() && bossEvent != null) {
            bossEvent.setName(this.getDisplayName());
        }
    }

    // ---- sounds ----

    private static @Nullable SoundEvent sound(String id) {
        return id == null ? null : BuiltInRegistries.SOUND_EVENT.getOptional(Identifier.parse(id)).orElse(null);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return sound(def.sounds.ambient);
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource source) {
        return sound(def.sounds.hurt);
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return sound(def.sounds.death);
    }

    @Override
    public int getMaxHeadYRot() {
        return 40;
    }

    // ---- spawning ----

    public static boolean checkSpawn(Creature c, EntityType<? extends MythBeast> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (reason == EntitySpawnReason.SPAWNER || reason == EntitySpawnReason.SPAWN_ITEM_USE || reason == EntitySpawnReason.COMMAND) {
            return true;
        }
        if (c.hostile) {
            return level.getDifficulty() != Difficulty.PEACEFUL && Monster.isDarkEnoughToSpawn(level, pos, random)
                    && net.minecraft.world.entity.Mob.checkMobSpawnRules(type, level, reason, pos, random);
        }
        return level.getRawBrightness(pos, 0) > 8 && net.minecraft.world.entity.Mob.checkMobSpawnRules(type, level, reason, pos, random);
    }
}
