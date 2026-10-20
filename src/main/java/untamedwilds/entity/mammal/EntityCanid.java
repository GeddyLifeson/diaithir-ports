package untamedwilds.entity.mammal;

import untamedwilds.citadel.animation.Animation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import untamedwilds.UntamedWilds;
import untamedwilds.config.ConfigGamerules;
import untamedwilds.entity.*;
import untamedwilds.entity.ai.*;
import untamedwilds.entity.ai.target.*;
import untamedwilds.init.ModEntity;
import untamedwilds.util.EntityUtils;

import javax.annotation.Nullable;

public class EntityCanid extends ComplexMobTerrestrial implements INewSkins, ISpecies, IPackEntity, INeedsPostUpdate {

    public EntityCanid(EntityType<? extends ComplexMob> type, Level worldIn) {
        super(type, worldIn);
        this.turn_speed = 0.15F;
    }

    public void registerGoals() {
        this.goalSelector.addGoal(1, new SmartSwimGoal_Land(this));
        this.goalSelector.addGoal(2, new FindItemsGoal(this, 12, true));
        this.goalSelector.addGoal(2, new SmartMeleeAttackGoal(this, 1.8D, false, 1, false, false));
        this.goalSelector.addGoal(3, new SmartAvoidGoal<>(this, LivingEntity.class, 16, 1.2D, 1.6D, input -> getEcoLevel(input) > getEcoLevel(this)));
        this.goalSelector.addGoal(4, new SmartMateGoal(this, 1D));
        this.goalSelector.addGoal(4, new GotoSleepGoal(this, 1D));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25D));
        this.goalSelector.addGoal(5, new SmartWanderGoal(this, 1D, true));
        this.goalSelector.addGoal(6, new SmartLookAtGoal(this, LivingEntity.class, 10.0F));
        this.targetSelector.addGoal(1, new HurtPackByTargetGoal(this).setAlertOthers(EntityCanid.class));
        this.targetSelector.addGoal(2, new ProtectChildrenTarget<>(this, LivingEntity.class, true, input -> !(input instanceof EntityCanid)));
        this.targetSelector.addGoal(3, new HuntPackMobTarget<>(this, LivingEntity.class, true, 30, false, input -> getEcoLevel(input) < getEcoLevel(this)));
        this.targetSelector.addGoal(4, new AngrySleeperTarget<>(this, LivingEntity.class, true));
    }

    protected void reassessTameGoals() {
        if (this.isTame()) {
            if (UntamedWilds.DEBUG) {
                UntamedWilds.LOGGER.info("Updating AI tasks for tamed mob");
            }
            this.goalSelector.addGoal(3, new SmartFollowOwnerGoal(this, 1.3D, 12.0F, 3.0F));
            this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
            this.targetSelector.addGoal(2, new SmartOwnerHurtTargetGoal(this));
        }
    }

    public static AttributeSupplier.Builder registerAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.8D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 28.0D)
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2D)
                .add(Attributes.ARMOR, 0.0D);
    }

    public boolean wantsToBreed() {
        if (ConfigGamerules.naturalBreeding.get() && this.age == 0) {
            return this.getHunger() >= 80;
        }
        return false;
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide()) {
            if (this.herd == null) {
                IPackEntity.initPack(this);
            }
            else {
                this.herd.tick();
            }
            if (this.level().getGameTime() % 1000 == 0) {
                this.addHunger(-10);
                if (!this.isStarving()) {
                    this.heal(1.0F);
                }
            }
            int i = this.random.nextInt(3000);
            if (i == 13 && !this.isInWater() && this.getTarget() == null && this.isNotMoving() && this.canMove()) {
                this.setSitting(true);
            }
            if (i == 14 && this.isSitting()) {
                this.setSitting(false);
            }
            this.setAngry(this.getTarget() != null);
        }
        super.aiStep();
    }

    public boolean hurtServer(net.minecraft.server.level.ServerLevel amLevel, DamageSource damageSource, float amount) {
        // Retaliate I: Mob will strike back when attacked by its current target
        performRetaliation(damageSource, this.getHealth(), amount, false);
        return super.hurtServer(amLevel, damageSource, amount);
    }

    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(SoundEvents.WOLF_STEP.value(), 0.15F, 1.0F);
    }

    @Nullable
    public EntityCanid getBreedOffspring(ServerLevel serverWorld, AgeableMob ageable) {
        return create_offspring(new EntityCanid(ModEntity.CANID.get(), this.level()));
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

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{NO_ANIMATION};
    }

    public Animation getAnimationEat() { return NO_ANIMATION; }

    @Override
    public void updateAttributes() {
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(getEntityData(this.getType()).getSpeciesData().get(this.getVariant()).getAttack());
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(getEntityData(this.getType()).getSpeciesData().get(this.getVariant()).getHealth());
        this.setHealth(this.getMaxHealth());
    }
}
