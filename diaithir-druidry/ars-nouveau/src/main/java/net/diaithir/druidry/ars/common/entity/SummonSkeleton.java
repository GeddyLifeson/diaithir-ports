package net.diaithir.druidry.ars.common.entity;

import net.diaithir.druidry.ars.setup.registry.DataSerializers;
import net.diaithir.druidry.ars.api.entity.ISummon;
import net.diaithir.druidry.ars.common.entity.goal.FollowSummonerGoal;
import net.diaithir.druidry.ars.setup.registry.ModEntities;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class SummonSkeleton extends Skeleton implements IFollowingSummon, ISummon {
    public static EntityDataAccessor<Optional<UUID>> OWNER_UNIQUE_ID = SynchedEntityData.defineId(SummonSkeleton.class, DataSerializers.OPTIONAL_UUID.get());

    private final RangedBowAttackGoal<SummonSkeleton> bowGoal = new RangedBowAttackGoal<>(this, 1.0D, 20, 15.0F);

    private final MeleeAttackGoal meleeGoal = new MeleeAttackGoal(this, 2.2D, true) {
        /**
         * Reset the task's internal state. Called when this task is interrupted by another one
         */
        public void stop() {
            super.stop();
            SummonSkeleton.this.setAggressive(false);
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start() {
            super.start();
            SummonSkeleton.this.setAggressive(true);
        }
    };

    private LivingEntity owner;
    @Nullable
    private BlockPos boundOrigin;
    private boolean limitedLifespan;
    private int limitedLifeTicks;

    public SummonSkeleton(EntityType<? extends Skeleton> entityType, Level level) {
        super(entityType, level);
    }

    public SummonSkeleton(Level level, LivingEntity owner, ItemStack item) {
        super(ModEntities.SUMMON_SKELETON.get(), level);
        this.setWeapon(item);
        this.owner = owner;
        this.limitedLifespan = true;
        setOwnerID(owner.getUUID());

    }

    @Override
    public EntityType<?> getType() {
        return ModEntities.SUMMON_SKELETON.get();
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, EntitySpawnReason pSpawnType, @Nullable SpawnGroupData pSpawnGroupData) {
        this.populateDefaultEquipmentSlots(getRandom(), pDifficulty);
        this.populateDefaultEquipmentEnchantments(pLevel, getRandom(), pDifficulty);
        return super.finalizeSpawn(pLevel, pDifficulty, pSpawnType, pSpawnGroupData);
    }

    /**
     * Gives armor or weapon for entity based on given DifficultyInstance
     */
    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance pDifficulty) {

    }

    @Override
    protected void dropAllDeathLoot(ServerLevel p_348524_, DamageSource p_21192_) {

    }

    @Override
    protected boolean shouldDropLoot(ServerLevel level) {return false;}

    @Override
    protected void dropCustomDeathLoot(ServerLevel p_348477_, DamageSource p_33574_, boolean p_33576_) {

    }

    @Override
    protected void dropEquipment(ServerLevel level) {}

    @Override
    protected void registerGoals() {

        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.goalSelector.addGoal(2, new FollowSummonerGoal(this, this.owner, 1.0, 9.0f, 3.0f));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this, SummonSkeleton.class){
            @Override
            protected boolean canAttack(@Nullable LivingEntity pPotentialTarget, TargetingConditions pTargetPredicate) {
                return pPotentialTarget != null && super.canAttack(pPotentialTarget, pTargetPredicate) && !pPotentialTarget.getUUID().equals(getOwnerUUID()) ;
            }
        });
        this.targetSelector.addGoal(1, new CopyOwnerTargetGoal<>(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<Mob>(this, Mob.class, 10, false, true,
                (LivingEntity entity, ServerLevel serverLevel) ->
                        (entity instanceof Mob mob && mob.getTarget() != null && mob.getTarget().equals(this.owner))
                                || (entity != null && entity.getKillCredit() != null && entity.getKillCredit().equals(this.owner))
        ));
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
    }

    public void setWeapon(ItemStack item) {
        this.setItemSlot(EquipmentSlot.MAINHAND, item);
        this.reassessWeaponGoal();
    }

    @Override
    public void reassessWeaponGoal() {
        if (this.level() instanceof ServerLevel && this.getItemInHand(InteractionHand.MAIN_HAND) != ItemStack.EMPTY) {
            this.goalSelector.removeGoal(this.meleeGoal);
            this.goalSelector.removeGoal(this.bowGoal);
            ItemStack itemstack = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, item -> item instanceof net.minecraft.world.item.BowItem));
            if (itemstack.is(Items.BOW)) {
                this.bowGoal.setMinAttackInterval(20);
                this.goalSelector.addGoal(4, this.bowGoal);
            } else {
                this.goalSelector.addGoal(4, this.meleeGoal);
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource pSource, float pAmount) {
        if (pSource.is(DamageTypes.MOB_ATTACK) && pSource.getEntity() instanceof ISummon summon){
            if (summon.getOwnerUUID() != null && summon.getOwnerUUID().equals(this.getOwnerUUID())) return false;
        }
        return super.hurtServer(serverLevel, pSource, pAmount);
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick() {
        super.tick();
        if (--this.limitedLifeTicks <= 0) {
            this.limitedLifeTicks = 20;
            if (this.level() instanceof ServerLevel serverLevel) {
                this.hurtServer(serverLevel, serverLevel.damageSources().starve(), 20.0F);
            }
        }
    }

    @Override
    public PlayerTeam getTeam() {
        if (this.getSummoner() != null) return getSummoner().getTeam();
        return super.getTeam();
    }


    @Override
    public boolean canAttack(LivingEntity target) {
        LivingEntity summoner = this.getSummoner();
        if (summoner != null) {
            if (target instanceof ISummon summon && summon.getOwnerUUID() != null && summon.getOwnerUUID().equals(this.getOwnerUUID())) return false;
            if (target == summoner || summoner.isAlliedTo(target)) return false;
        }
        return super.canAttack(target);
    }

    @Override
    public net.minecraft.world.entity.EntityReference<LivingEntity> getOwnerReference() {
        UUID uuid = this.getOwnerUUID();
        return uuid == null ? null : net.minecraft.world.entity.EntityReference.of(uuid);
    }

    @Override
    public Level getWorld() {
        return this.level();
    }

    @Override
    public PathNavigation getPathNav() {
        return this.getNavigation();
    }

    @Override
    public Mob getSelfEntity() {
        return this;
    }

    public LivingEntity getSummoner() {
        return this.getOwnerFromID();
    }

    public LivingEntity getActualOwner() {
        return owner;
    }

    @Override
    public int getBaseExperienceReward(ServerLevel level) {
        return 0;
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readAdditionalSaveData(ValueInput compound) {
        super.readAdditionalSaveData(compound);
        if (compound.getInt("BoundX").isPresent()) {
            this.boundOrigin = new BlockPos(compound.getIntOr("BoundX", 0), compound.getIntOr("BoundY", 0), compound.getIntOr("BoundZ", 0));
        }

        compound.getInt("LifeTicks").ifPresent(this::setLimitedLife);

        UUID s = compound.read("OwnerUUID", UUIDUtil.CODEC).orElse(null);

        if (s != null) {
            try {
                this.setOwnerID(s);

            } catch (Throwable ignored) {
            }
        }

    }

    public void setLimitedLife(int lifeTicks) {
        this.limitedLifeTicks = lifeTicks;
    }

    public LivingEntity getOwnerFromID() {
        try {
            UUID uuid = this.getOwnerUUID();

            return uuid == null ? null : this.level().getPlayerByUUID(uuid);
        } catch (IllegalArgumentException var2) {
            return null;
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(OWNER_UNIQUE_ID, Optional.of(Util.NIL_UUID));
    }

    @Override
    public void addAdditionalSaveData(ValueOutput compound) {
        super.addAdditionalSaveData(compound);
        if (this.boundOrigin != null) {
            compound.putInt("BoundX", this.boundOrigin.getX());
            compound.putInt("BoundY", this.boundOrigin.getY());
            compound.putInt("BoundZ", this.boundOrigin.getZ());
        }

        if (this.limitedLifespan) {
            compound.putInt("LifeTicks", this.limitedLifeTicks);
        }
        if (this.getOwnerUUID() == null) {
            compound.store("OwnerUUID", UUIDUtil.CODEC, Util.NIL_UUID);
        } else {
            compound.store("OwnerUUID", UUIDUtil.CODEC, this.getOwnerUUID());
        }

    }

    /** Summoned skeletons do not burn in daylight (AbstractSkeleton's sun check is private in 26.1, so sun ignition is filtered here). */
    @Override
    public void setRemainingFireTicks(int ticks) {
        if (ticks > 0 && !isInLava() && level().isBrightOutside() && level().canSeeSky(blockPosition()) && getLastDamageSource() == null) return;
        super.setRemainingFireTicks(ticks);
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        onSummonDeath(level(), cause, false);
    }

    @Override
    public int getTicksLeft() {
        return limitedLifeTicks;
    }

    @Override
    public void setTicksLeft(int ticks) {
        this.limitedLifeTicks = ticks;
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_UNIQUE_ID).orElse(null);
    }

    @Override
    public void setOwnerID(UUID uuid) {
        this.entityData.set(OWNER_UNIQUE_ID, Optional.ofNullable(uuid));
    }

}
