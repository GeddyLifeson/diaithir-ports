package net.diaithir.druidry.ars.common.entity;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.ANFakePlayer;
import net.diaithir.druidry.ars.api.client.IVariantColorProvider;
import net.diaithir.druidry.ars.api.entity.IDispellable;
import net.diaithir.druidry.ars.api.util.SummonUtil;
import net.diaithir.druidry.ars.client.particle.ParticleUtil;
import net.diaithir.druidry.ars.common.block.tile.IAnimationListener;
import net.diaithir.druidry.ars.common.block.tile.WixieCauldronTile;
import net.diaithir.druidry.ars.common.entity.goal.wixie.CompleteCraftingGoal;
import net.diaithir.druidry.ars.common.entity.goal.wixie.FindNextItemGoal;
import net.diaithir.druidry.ars.common.entity.goal.wixie.FindPotionGoal;
import net.diaithir.druidry.ars.setup.registry.ItemsRegistry;
import net.diaithir.druidry.ars.setup.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.Tags;
import net.diaithir.druidry.ars.anim.GeoAnimatable;
import net.diaithir.druidry.ars.anim.GeoEntity;
import net.diaithir.druidry.ars.anim.AnimatableInstanceCache;
import net.diaithir.druidry.ars.anim.*;
import net.diaithir.druidry.ars.anim.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.Arrays;

public class EntityWixie extends AbstractFlyingCreature implements GeoEntity, IAnimationListener, IDispellable, IVariantColorProvider<EntityWixie> {
    AnimatableInstanceCache manager = GeckoLibUtil.createInstanceCache(this);
    public static final EntityDataAccessor<String> COLOR = SynchedEntityData.defineId(EntityWixie.class, EntityDataSerializers.STRING);

    public BlockPos cauldronPos;
    public int inventoryBackoff;

    private <P extends GeoAnimatable> PlayState idlePredicate(AnimationState<P> event) {
        if (getNavigation().isInProgress())
            return PlayState.STOP;
        event.getController().setAnimation(RawAnimation.begin().thenPlay("idle"));
        return PlayState.CONTINUE;
    }

    private <P extends GeoAnimatable> PlayState castPredicate(AnimationState<P> event) {
        return PlayState.CONTINUE;
    }

    private <P extends GeoAnimatable> PlayState summonPredicate(AnimationState<P> event) {
        return PlayState.CONTINUE;
    }

    @Override
    public int getBaseExperienceReward(ServerLevel level) {
        return 0;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource pSource, float pAmount) {
        return SummonUtil.canSummonTakeDamage(pSource) && super.hurtServer(level, pSource, pAmount);
    }

    AnimationController<?> summonController;
    AnimationController<?> castController;

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar animatableManager) {
        animatableManager.add(new AnimationController<>(this, "idleController", 20, this::idlePredicate));
        castController = new AnimationController<>(this, "castController", 1, this::castPredicate);
        summonController = new AnimationController<>(this, "summonController", 1, this::summonPredicate);
        animatableManager.add(castController);
        animatableManager.add(summonController);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return manager;
    }

    public EntityWixie(EntityType<? extends AbstractFlyingCreature> type, Level worldIn) {
        super(type, worldIn);
        this.moveControl = new FlyingMoveControl(this, 10, true);
    }

    @SuppressWarnings("unchecked")
    private static EntityType<? extends AbstractFlyingCreature> wixieType() {
        return (EntityType<? extends AbstractFlyingCreature>) (EntityType<?>) BuiltInRegistries.ENTITY_TYPE.getValue(ArsNouveau.prefix("wixie"));
    }

    private static ItemStack charmStack() {
        return new ItemStack(BuiltInRegistries.ITEM.getValue(ArsNouveau.prefix("wixie_charm")));
    }

    public EntityWixie(Level world, BlockPos pos) {
        this(wixieType(), world);
        this.cauldronPos = pos;
    }

    public static String[] COLORS = {"white", "green", "blue", "black", "red"};

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide() || hand != InteractionHand.MAIN_HAND)
            return InteractionResult.SUCCESS;
        ItemStack stack = player.getItemInHand(hand);

        if (player.getMainHandItem().is(Tags.Items.DYES)) {
            DyeColor color = DyeColor.getColor(stack);
            if (color == null || this.entityData.get(COLOR).equals(color.getName()) || !Arrays.asList(COLORS).contains(color.getName()))
                return InteractionResult.SUCCESS;
            this.entityData.set(COLOR, color.getName());
            player.getMainHandItem().shrink(1);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void tick() {
        super.tick();
        SummonUtil.healOverTime(this);
        if (!level().isClientSide() && (cauldronPos == null || !(level().getBlockEntity(cauldronPos) instanceof WixieCauldronTile)))
            this.hurtServer((ServerLevel) level(), level().damageSources().playerAttack(ANFakePlayer.getPlayer((ServerLevel) level())), 99);
        if (!level().isClientSide() && inventoryBackoff > 0) {
            inventoryBackoff--;
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        goalSelector.addGoal(2, new FindNextItemGoal(this));
        goalSelector.addGoal(2, new FindPotionGoal(this));
        goalSelector.addGoal(1, new CompleteCraftingGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(COLOR, "blue");
    }

    @Override
    public boolean removeWhenFarAway(double p_213397_1_) {
        return false;
    }

    @Override
    protected PathNavigation createNavigation(Level world) {
        FlyingPathNavigation flyingpathnavigator = new FlyingPathNavigation(this, world);
        flyingpathnavigator.setCanOpenDoors(false);
        flyingpathnavigator.setCanFloat(true);
        flyingpathnavigator.getNodeEvaluator().setCanPassDoors(true);
        return flyingpathnavigator;
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.FLYING_SPEED, Attributes.FLYING_SPEED.value().getDefaultValue())
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        if (tag.getInt("summoner_x").isPresent())
            cauldronPos = new BlockPos(tag.getIntOr("summoner_x", 0), tag.getIntOr("summoner_y", 0), tag.getIntOr("summoner_z", 0));
        tag.getString("color").ifPresent(c -> this.entityData.set(COLOR, c));
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        if (cauldronPos != null) {
            tag.putInt("summoner_x", cauldronPos.getX());
            tag.putInt("summoner_y", cauldronPos.getY());
            tag.putInt("summoner_z", cauldronPos.getZ());
        }
        tag.putString("color", this.entityData.get(COLOR));
    }

    @Override
    public void startAnimation(int arg) {
        if (arg == Animations.CAST.ordinal() && castController != null) {
            castController.setAnimation(RawAnimation.begin().thenPlay("cast"));
        } else if (arg == Animations.SUMMON_ITEM.ordinal() && summonController != null) {
            summonController.setAnimation(RawAnimation.begin().thenPlay("summon_item"));
        }
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide()) {
            ItemStack stack = charmStack();
            level().addFreshEntity(new ItemEntity(level(), getX(), getY(), getZ(), stack));

        }
        super.die(source);
    }

    @Override
    public boolean onDispel(@Nullable LivingEntity caster) {
        if (this.isRemoved())
            return false;

        if (!level().isClientSide()) {
            ItemStack stack = charmStack();
            level().addFreshEntity(new ItemEntity(level(), getX(), getY(), getZ(), stack.copy()));
            ParticleUtil.spawnPoof((ServerLevel) level(), blockPosition());
            this.remove(RemovalReason.DISCARDED);
        }
        return true;
    }

    @Override
    public Identifier getTexture(EntityWixie entity) {
        String color = getColor(entity).toLowerCase();
        if (color.isEmpty())
            color = "blue";
        return ArsNouveau.prefix( "textures/entity/wixie_" + color + ".png");
    }

    @Override
    public String getColor(EntityWixie entityWixie) {
        return this.getEntityData().get(COLOR);
    }

    @Override
    public void setColor(String color, EntityWixie entityWixie) {
        this.getEntityData().set(COLOR, color);
    }


    public enum Animations {
        CAST,
        SUMMON_ITEM
    }
}
