package untamedwilds.entity.mammal;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import untamedwilds.citadel.animation.Animation;
import untamedwilds.config.ConfigGamerules;
import untamedwilds.entity.ComplexMob;
import untamedwilds.entity.ComplexMobTerrestrial;
import untamedwilds.entity.INeedsPostUpdate;
import untamedwilds.entity.INewSkins;
import untamedwilds.entity.IPackEntity;
import untamedwilds.entity.ISpecies;
import untamedwilds.entity.ai.FindItemsGoal;
import untamedwilds.entity.ai.FollowParentGoal;
import untamedwilds.entity.ai.GotoSleepGoal;
import untamedwilds.entity.ai.SmartAvoidGoal;
import untamedwilds.entity.ai.SmartFollowOwnerGoal;
import untamedwilds.entity.ai.SmartLookAtGoal;
import untamedwilds.entity.ai.SmartMateGoal;
import untamedwilds.entity.ai.SmartMeleeAttackGoal;
import untamedwilds.entity.ai.SmartSwimGoal_Land;
import untamedwilds.entity.ai.SmartWanderGoal;
import untamedwilds.entity.ai.target.AngrySleeperTarget;
import untamedwilds.entity.ai.target.HuntMobTarget;
import untamedwilds.entity.ai.target.ProtectChildrenTarget;
import untamedwilds.entity.ai.target.SmartHurtByTargetGoal;
import untamedwilds.entity.ai.target.SmartOwnerHurtTargetGoal;
import untamedwilds.init.ModEntity;
import untamedwilds.util.EntityUtils;

public class EntityBigCat extends ComplexMobTerrestrial implements ISpecies, INewSkins, INeedsPostUpdate, IPackEntity {
   private static final EntityDataAccessor<Boolean> DIMORPHISM = SynchedEntityData.defineId(EntityBigCat.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> FLUFFY_TAIL = SynchedEntityData.defineId(EntityBigCat.class, EntityDataSerializers.BOOLEAN);
   public static Animation ATTACK_MAUL;
   public static Animation ATTACK_POUNCE;
   public static Animation ANIMATION_ROAR;
   public static Animation ANIMATION_EAT;
   public static Animation IDLE_TALK;
   public static Animation IDLE_STRETCH;
   public int aggroProgress;

   public EntityBigCat(EntityType<? extends ComplexMob> type, Level worldIn) {
      super(type, worldIn);
      ATTACK_POUNCE = Animation.create(42);
      ATTACK_MAUL = Animation.create(22);
      IDLE_TALK = Animation.create(20);
      IDLE_STRETCH = Animation.create(110);
      this.turn_speed = 0.1F;
   }

   protected void defineSynchedData(Builder builder) {
      super.defineSynchedData(builder);
      builder.define(DIMORPHISM, false);
      builder.define(FLUFFY_TAIL, false);
   }

   public void registerGoals() {
      this.goalSelector.addGoal(1, new SmartSwimGoal_Land(this));
      this.goalSelector.addGoal(2, new FindItemsGoal(this, 12, true));
      this.goalSelector.addGoal(2, new SmartMeleeAttackGoal(this, 2.3, false, 1.0F, false, true));
      this.goalSelector.addGoal(3, new SmartAvoidGoal<LivingEntity>(this, LivingEntity.class, 16.0F, 1.2, 1.6, input -> getEcoLevel(input) > getEcoLevel(this)));
      this.goalSelector.addGoal(4, new SmartMateGoal(this, 1.0));
      this.goalSelector.addGoal(4, new GotoSleepGoal(this, 1.0));
      this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25));
      this.goalSelector.addGoal(5, new SmartWanderGoal(this, 1.0, true));
      this.goalSelector.addGoal(6, new SmartLookAtGoal(this, LivingEntity.class, 10.0F));
      this.targetSelector.addGoal(1, new SmartHurtByTargetGoal(this, new Class[0]));
      this.targetSelector.addGoal(2, new AngrySleeperTarget(this, LivingEntity.class, true));
      this.targetSelector.addGoal(3, new ProtectChildrenTarget(this, LivingEntity.class, true, input -> !(input instanceof EntityBigCat)));
      this.targetSelector.addGoal(4, new HuntMobTarget<LivingEntity>(this, LivingEntity.class, true, 30, false, input -> getEcoLevel(input) < getEcoLevel(this)));
   }

   public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder registerAttributes() {
      return LivingEntity.createLivingAttributes()
         .add(Attributes.ATTACK_DAMAGE, 8.0)
         .add(Attributes.ATTACK_KNOCKBACK, 0.8)
         .add(Attributes.MOVEMENT_SPEED, 0.16)
         .add(Attributes.FOLLOW_RANGE, 32.0)
         .add(Attributes.MAX_HEALTH, 40.0)
         .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
         .add(Attributes.ARMOR, 0.0);
   }

   public boolean wantsToBreed() {
      if (!super.wantsToBreed() || this.isSleeping() || this.getAge() != 0 || !EntityUtils.hasFullHealth(this) || this.getHunger() < 80) {
         return false;
      } else if ((Boolean)ConfigGamerules.hardcoreBreeding.get()) {
         List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(6.0, 4.0, 6.0));
         return list.size() < 3;
      } else {
         return true;
      }
   }

   @Nullable
   public EntityBigCat getBreedOffspring(ServerLevel serverWorld, AgeableMob ageable) {
      return (EntityBigCat)this.create_offspring(new EntityBigCat((EntityType<? extends ComplexMob>)ModEntity.BIG_CAT.get(), this.level()));
   }

   public boolean isPushedByFluid() {
      return false;
   }

   protected int calculateFallDamage(float distance, float damageMultiplier) {
      return Mth.ceil((distance * 0.5F - 3.0F) * damageMultiplier);
   }

   public void aiStep() {
      if (!this.level().isClientSide()) {
         if (this.herd == null && EntityUtils.getPackSize(this.getType(), this.getVariant()) > 1) {
            IPackEntity.initPack(this);
         } else if (EntityUtils.getPackSize(this.getType(), this.getVariant()) > 1) {
            this.herd.tick();
         }

         if (this.level().getGameTime() % 1000L == 0L) {
            this.addHunger(-3);
            if (!this.isStarving()) {
               this.heal(2.0F);
            }
         }

         if (this.getAnimation() == NO_ANIMATION && this.getTarget() == null && !this.isSleeping() && this.getCommandInt() == 0) {
            int i = this.random.nextInt(3000);
            if (i == 0 && !this.isInWater() && this.isNotMoving() && this.canMove() && this.isActive()) {
               this.getNavigation().stop();
               this.setSitting(true);
            }

            if ((i == 1 || this.isInWater()) && this.isSitting() && this.getCommandInt() < 2) {
               this.setSitting(false);
            }

            if (i == 2 && this.canMove() && !this.isInWater() && !this.isBaby()) {
               this.getNavigation().stop();
               this.setAnimation(IDLE_STRETCH);
            }

            if (i > 2980 && !this.isInWater() && !this.isBaby()) {
               this.setAnimation(IDLE_TALK);
            }
         }

         if (this.tickCount % 80 == 2 && this.getTarget() != null && this.getAnimation() == NO_ANIMATION) {
            this.setAnimation(ANIMATION_ROAR);
         }

         if (this.getAnimation() == ATTACK_POUNCE && this.getAnimationTick() == 10) {
            this.getMoveControl().strafe(2.0F, 0.0F);
            this.getJumpControl().jump();
         }

         this.setAngry(this.getTarget() != null);
      }

      if (this.getAnimation() == ANIMATION_EAT && (this.getAnimationTick() == 10 || this.getAnimationTick() == 20 || this.getAnimationTick() == 30)) {
         this.playSound(SoundEvents.HORSE_EAT, 1.5F, 0.8F);
      }

      if (this.getAnimation() == ATTACK_MAUL && this.getAnimationTick() == 10) {
         this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.5F, 0.8F);
      }

      if (this.getAnimation() == IDLE_TALK && this.getAnimationTick() == 1 && this.getAmbientSound() != null) {
         this.playSound(this.getAmbientSound(), this.getSoundVolume(), this.getVoicePitch());
      }

      if (this.level().isClientSide() && this.isAngry() && this.aggroProgress < 40) {
         this.aggroProgress++;
      } else if (this.level().isClientSide() && !this.isAngry() && this.aggroProgress > 0) {
         this.aggroProgress--;
      }

      super.aiStep();
   }

   protected void playStepSound(BlockPos pos, BlockState blockIn) {
      this.playSound((SoundEvent)SoundEvents.WOLF_STEP.value(), 0.15F, 1.0F);
   }

   protected void reassessTameGoals() {
      if (this.isTame()) {
         this.goalSelector.addGoal(3, new SmartFollowOwnerGoal(this, 2.3, 12.0F, 3.0F));
         this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
         this.targetSelector.addGoal(2, new SmartOwnerHurtTargetGoal(this));
      }
   }

   protected SoundEvent getAmbientSound() {
      return this.isBaby() ? SoundEvents.OCELOT_AMBIENT : super.getAmbientSound();
   }

   protected SoundEvent getHurtSound(@NotNull DamageSource source) {
      return this.isBaby() ? SoundEvents.OCELOT_HURT : super.getHurtSound(source);
   }

   protected SoundEvent getDeathSound() {
      return this.isBaby() ? SoundEvents.OCELOT_DEATH : super.getDeathSound();
   }

   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      ItemStack itemstack = player.getItemInHand(hand);
      if (hand == InteractionHand.MAIN_HAND && !this.isTame()) {
         if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
         }

         if (this.isFood(itemstack)) {
            this.playSound(SoundEvents.HORSE_EAT, 1.5F, 0.8F);
            if (this.getRandom().nextInt(3) == 0) {
               this.tame(player);
               EntityUtils.spawnParticlesOnEntity(this.level(), this, ParticleTypes.HEART, 3, 6);
            } else {
               EntityUtils.spawnParticlesOnEntity(this.level(), this, ParticleTypes.SMOKE, 3, 3);
            }

            return InteractionResult.SUCCESS;
         }
      }

      return super.mobInteract(player, hand);
   }

   public boolean doHurtTarget(ServerLevel amLevel, Entity entityIn) {
      boolean flag = super.doHurtTarget(amLevel, entityIn);
      if (flag && this.getAnimation() == NO_ANIMATION && !this.isBaby()) {
         Animation anim = this.chooseAttackAnimation(entityIn);
         this.setAnimation(anim);
         this.setAnimationTick(0);
      }

      return flag;
   }

   public boolean hurtServer(ServerLevel amLevel, DamageSource damageSource, float amount) {
      float f = this.getHealth();
      if (!this.level().isClientSide()
         && !this.isNoAi()
         && this.getTarget() == damageSource.getEntity()
         && amount < f
         && (damageSource.getEntity() != null || damageSource.getDirectEntity() != null)
         && damageSource.getEntity() instanceof LivingEntity
         && damageSource.getEntity() instanceof TamableAnimal tamable
         && tamable.getOwner() != null
         && this.hasLineOfSight(damageSource.getEntity())
         && !damageSource.getEntity().isInvulnerable()
         && this.getAnimation() == NO_ANIMATION) {
         this.doHurtTarget((ServerLevel)this.level(), damageSource.getEntity());
      }

      return super.hurtServer(amLevel, damageSource, amount);
   }

   private Animation chooseAttackAnimation(Entity target) {
      return target.getBbHeight() < this.getBbHeight() ? ATTACK_MAUL : ATTACK_POUNCE;
   }

   public Animation[] getAnimations() {
      return new Animation[]{NO_ANIMATION, ATTACK_POUNCE, ATTACK_MAUL, IDLE_TALK, IDLE_STRETCH};
   }

   protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
      return sizeIn.height() * 0.9F;
   }

   public void updateAttributes() {
      this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(getEntityData(this.getType()).getSpeciesData().get(this.getVariant()).getAttack().floatValue());
      this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(getEntityData(this.getType()).getSpeciesData().get(this.getVariant()).getHealth().floatValue());
      this.setHealth(this.getMaxHealth());
      this.setDimorphism(getEntityData(this.getType()).getFlags(this.getVariant(), "dimorphism") == 1);
      this.setFluffyTail(getEntityData(this.getType()).getFlags(this.getVariant(), "fluffyTail") == 1);
   }

   public boolean hasDimorphism() {
      return (Boolean)this.entityData.get(DIMORPHISM);
   }

   private void setDimorphism(boolean dimorphism) {
      this.entityData.set(DIMORPHISM, dimorphism);
   }

   public boolean hasFluffyTail() {
      return (Boolean)this.entityData.get(FLUFFY_TAIL);
   }

   private void setFluffyTail(boolean fluffy_tail) {
      this.entityData.set(FLUFFY_TAIL, fluffy_tail);
   }

   public void addAdditionalSaveData(ValueOutput compound) {
      super.addAdditionalSaveData(compound);
      compound.putBoolean("hasDimorphism", this.hasDimorphism());
      compound.putBoolean("fluffy", this.hasFluffyTail());
   }

   public void readAdditionalSaveData(ValueInput compound) {
      super.readAdditionalSaveData(compound);
      this.setDimorphism(compound.getBooleanOr("hasDimorphism", false));
      this.setFluffyTail(compound.getBooleanOr("fluffy", false));
   }

   public Identifier getTexture() {
      Identifier texture_path = EntityUtils.getSkinFromEntity(this);
      // Diaithir fix: species data is only loaded on the logical server; on a client connected to a
      // dedicated server getEntityData() returns null. The synched DIMORPHISM flag is already
      // authoritative, so only consult the data holder when it exists.
      untamedwilds.util.EntityDataHolder data = getEntityData(this.getType());
      boolean dimorphic = this.hasDimorphism() || (data != null && data.getFlags(this.getVariant(), "dimorphism") == 1);
      if (dimorphic) {
         String trimmed_path = texture_path.getPath().substring(0, texture_path.getPath().lastIndexOf(46));
         return Identifier.tryParse("untamedwilds:" + trimmed_path + "_" + this.getGenderString() + ".png");
      } else {
         return texture_path;
      }
   }
}
