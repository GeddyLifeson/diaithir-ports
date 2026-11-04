package net.diaithir.druidry.ars.common.entity;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.diaithir.druidry.ars.api.entity.IDispellable;
import net.diaithir.druidry.ars.api.util.SummonUtil;
import net.diaithir.druidry.ars.common.entity.goal.lily.WagGoal;
import net.diaithir.druidry.ars.setup.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.diaithir.druidry.ars.anim.GeoEntity;
import net.diaithir.druidry.ars.anim.AnimatableInstanceCache;
import net.diaithir.druidry.ars.anim.AnimatableManager;
import net.diaithir.druidry.ars.anim.AnimationController;
import net.diaithir.druidry.ars.anim.PlayState;
import net.diaithir.druidry.ars.anim.RawAnimation;
import net.diaithir.druidry.ars.anim.GeckoLibUtil;

import java.util.UUID;

public class Lily extends TamableAnimal implements GeoEntity, IDispellable {
    // Owner UUID to Lily UUID
    public static BiMap<UUID, UUID> ownerLilyMap = HashBiMap.create();

    private static final EntityDataAccessor<Boolean> SIT = SynchedEntityData.defineId(Lily.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> WAG = SynchedEntityData.defineId(Lily.class, EntityDataSerializers.BOOLEAN);

    private static final SoundEvent LILY_WHINE = SoundEvent.createVariableRangeEvent(Identifier.withDefaultNamespace("entity.wolf.whine"));
    private static final SoundEvent LILY_PANT = SoundEvent.createVariableRangeEvent(Identifier.withDefaultNamespace("entity.wolf.pant"));
    private static final SoundEvent LILY_AMBIENT = SoundEvent.createVariableRangeEvent(Identifier.withDefaultNamespace("entity.wolf.ambient"));
    private static final SoundEvent LILY_HURT = SoundEvent.createVariableRangeEvent(Identifier.withDefaultNamespace("entity.wolf.hurt"));
    private static final SoundEvent LILY_DEATH = SoundEvent.createVariableRangeEvent(Identifier.withDefaultNamespace("entity.wolf.death"));

    public int wagTicks;

    public Lily(EntityType<? extends TamableAnimal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public Lily(Level level){
        this(ModEntities.LILY.get(), level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F));
        this.goalSelector.addGoal(7, new WagGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
    }

    @Override
    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        if (this.level().isClientSide()) {
            return InteractionResult.CONSUME;
        }
        if (this.isOwnedBy(pPlayer)) {
            this.setOrderedToSit(!this.isOrderedToSit());
            this.jumping = false;
            this.navigation.stop();
            this.setTarget(null);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(pPlayer, pHand);
    }

    @Override
    public void tick() {
        super.tick();
        SummonUtil.healOverTime(this);
        if(!level().isClientSide()){
            if(level().getGameTime() % 20 == 0 && !ownerLilyMap.containsValue(this.getUUID())){
                this.remove(RemovalReason.DISCARDED);
            }
            if( wagTicks > 0 && isWagging()){
                wagTicks--;
                if(wagTicks <= 0){
                    setWagging(false);
                }
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(SIT, false);
        pBuilder.define(WAG, false);
    }

    @Override
    public boolean isOrderedToSit() {
        return this.entityData.get(SIT);
    }

    @Override
    public boolean isTame() {
        return true;
    }

    @Override
    public void setOrderedToSit(boolean pOrderedToSit) {
        this.entityData.set(SIT, pOrderedToSit);
    }

    public boolean isWagging() {
        return this.entityData.get(WAG);
    }

    public void setWagging(boolean pWagging) {
        this.entityData.set(WAG, pWagging);
    }

    @Nullable
    public UUID getOwnerUUID() {
        EntityReference<LivingEntity> ref = this.getOwnerReference();
        return ref == null ? null : ref.getUUID();
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        this.playSound(SoundEvents.WOLF_STEP.value(), 0.15F, 1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.3F).add(Attributes.MAX_HEALTH, 40f).add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    protected SoundEvent getAmbientSound() {
        if (this.getRandom().nextInt(3) == 0) {
            return this.isTame() && this.getHealth() < 10.0F ? LILY_WHINE : LILY_PANT;
        } else {
            return LILY_AMBIENT;
        }
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return LILY_HURT;
    }

    protected SoundEvent getDeathSound() {
        return LILY_DEATH;
    }

    @Override
    public boolean hurtServer(ServerLevel pLevel, DamageSource pSource, float pAmount) {
        if(!(pSource.getEntity() instanceof Player)){
            return false;
        }
        return super.hurtServer(pLevel, pSource, pAmount);
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
    protected float getSoundVolume() {
        return 0.4F;
    }

    /**
     * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
     * the animal type)
     */
    public boolean isFood(ItemStack pStack) {
        return pStack.is(ItemTags.WOLF_FOOD);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
        return null;
    }

    public boolean isLookingAtMe(Player pPlayer) {
        Vec3 vec3 = pPlayer.getViewVector(1.0F).normalize();
        Vec3 vec31 = new Vec3(this.getX() - pPlayer.getX(), this.getEyeY() - pPlayer.getEyeY(), this.getZ() - pPlayer.getZ());
        double d0 = vec31.length();
        vec31 = vec31.normalize();
        double d1 = vec3.dot(vec31);
        return d1 > 1.0D - 0.025D / d0 && pPlayer.hasLineOfSight(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController(this, "walk", 1, (event) -> {
            if(event.isMoving()){
                event.getController().setAnimation(RawAnimation.begin().thenPlay("run"));
                return PlayState.CONTINUE;
            }
            return PlayState.STOP;
        }));
        data.add(new AnimationController(this, "idle", 1, (event) -> {
            if(!event.isMoving() && !this.isWagging()){
                event.getController().setAnimation(RawAnimation.begin().thenPlay("idle"));
                return PlayState.CONTINUE;
            }
            return PlayState.STOP;
        }));
        data.add(new AnimationController(this, "idle_wag", 1, (event) -> {
            if(!event.isMoving() && this.isWagging()){
                event.getController().setAnimation(RawAnimation.begin().thenPlay("idle_wagging"));
                return PlayState.CONTINUE;
            }
            return PlayState.STOP;
        }));

        data.add(new AnimationController(this, "rest", 1, (event) -> {
            if(!event.isMoving() && this.isOrderedToSit() && !this.isWagging()){
                event.getController().setAnimation(RawAnimation.begin().thenPlay("resting"));
                return PlayState.CONTINUE;
            }
            return PlayState.STOP;
        }));

        data.add(new AnimationController(this, "rest_wag", 1, (event) -> {
            if(!event.isMoving() && this.isOrderedToSit() && this.isWagging()){
                event.getController().setAnimation(RawAnimation.begin().thenPlay("resting_wagging"));
                return PlayState.CONTINUE;
            }
            return PlayState.STOP;
        }));

    }
    AnimatableInstanceCache factory = GeckoLibUtil.createInstanceCache(this);
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return factory;
    }

    @Override
    public boolean onDispel(@NotNull LivingEntity caster) {
        if(caster.getUUID().equals(this.getOwnerUUID())) {
            this.remove(RemovalReason.DISCARDED);
            return true;
        }
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput pInput) {
        super.readAdditionalSaveData(pInput);
        UUID ownerUUID = this.getOwnerUUID();
        if(ownerUUID != null && !ownerLilyMap.containsKey(ownerUUID)) {
            Lily.ownerLilyMap.put(ownerUUID, this.getUUID());
        }
    }
}
