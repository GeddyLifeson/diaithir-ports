package net.diaithir.bestiary;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/** A creature whose look, size, stats and spawn rules come from its CreatureDef. Covers the beast and flyer presets. */
public class Creature extends PathfinderMob implements GeoEntity {
    private static final EntityDataAccessor<String> KIND = SynchedEntityData.defineId(Creature.class, EntityDataSerializers.STRING);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle"), WALK = RawAnimation.begin().thenLoop("walk"),
            FLY = RawAnimation.begin().thenLoop("fly"), ATTACK = RawAnimation.begin().thenPlay("attack");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private @Nullable CreatureDef def;
    private @Nullable UUID owner;

    public Creature(EntityType<? extends Creature> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.FLYING_SPEED, 0.4).add(Attributes.ATTACK_KNOCKBACK);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(KIND, "");
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, false));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0) {
            @Override
            public boolean canUse() {
                return wanders() && !flying() && super.canUse();
            }
        });
        goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0) {
            @Override
            public boolean canUse() {
                return wanders() && flying() && super.canUse();
            }
        });
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, (target, level) -> hostile()));
    }

    public Identifier kind() {
        return Identifier.parse(entityData.get(KIND).isEmpty() ? DiaithirBestiary.ID + ":unknown" : entityData.get(KIND));
    }

    public @Nullable CreatureDef def() {
        if (def == null && !entityData.get(KIND).isEmpty()) def = DiaithirBestiary.creatures(level()).getValue(kind());
        return def;
    }

    /** Sets the kind on the server. A fresh creature also gets full health. */
    public void setKind(Identifier id, CreatureDef def, boolean fresh) {
        this.def = def;
        entityData.set(KIND, id.toString());
        CreatureDef.Stats s = def.stats();
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(s.health());
        getAttribute(Attributes.ARMOR).setBaseValue(s.armor());
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(s.damage());
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(s.speed());
        getAttribute(Attributes.FLYING_SPEED).setBaseValue(s.flyingSpeed());
        getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(s.followRange());
        getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(s.knockbackResistance());
        if (def.preset() == Preset.MECH) getAttribute(Attributes.STEP_HEIGHT).setBaseValue(1.1);
        if (fresh) setHealth(getMaxHealth());
        onKindSet();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (KIND.equals(accessor)) {
            def = null;
            if (def() != null) onKindSet();
        }
    }

    protected void onKindSet() {
        if (flying() && !(moveControl instanceof FlyingMoveControl)) {
            moveControl = new FlyingMoveControl(this, 20, true);
            FlyingPathNavigation nav = new FlyingPathNavigation(this, level());
            nav.setCanFloat(true);
            navigation = nav;
            setNoGravity(true);
        }
        refreshDimensions();
    }

    public boolean flying() {
        return def() != null && def.isFlying();
    }

    public boolean hostile() {
        return def() != null && def.isHostile() && owner == null;
    }

    protected boolean wanders() {
        return def() != null && !isVehicle() && owner == null;
    }

    public @Nullable UUID owner() {
        return owner;
    }

    public void tame(Player player) {
        owner = player.getUUID();
        setPersistenceRequired();
        setTarget(null);
        navigation.stop();
        if (level() instanceof ServerLevel server)
            server.sendParticles(ParticleTypes.HEART, getX(), getY() + getBbHeight(), getZ(), 7, 0.5, 0.5, 0.5, 0.02);
    }

    public boolean canBeRiddenBy(Player player) {
        return def() != null && def.isRideable() && !isVehicle() && player.getUUID().equals(owner);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        CreatureDef d = def();
        if (d == null || player.isSecondaryUseActive()) return super.mobInteract(player, hand);
        ItemStack stack = player.getItemInHand(hand);
        if (owner == null && d.tame().isPresent() && stack.is(d.tame().get().item())) {
            if (!level().isClientSide()) {
                stack.consume(1, player);
                if (random.nextFloat() < d.tame().get().chance()) tame(player);
            }
            return InteractionResult.SUCCESS;
        }
        if (canBeRiddenBy(player)) {
            if (!level().isClientSide()) player.startRiding(this);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        return def() != null && def.isRideable() && getFirstPassenger() instanceof Player player ? player : super.getControllingPassenger();
    }

    @Override
    protected void tickRidden(Player controller, Vec3 riddenInput) {
        super.tickRidden(controller, riddenInput);
        setRot(controller.getYRot(), controller.getXRot() * 0.5F);
        yRotO = yBodyRot = yHeadRot = getYRot();
    }

    @Override
    protected Vec3 getRiddenInput(Player controller, Vec3 selfInput) {
        float strafe = controller.xxa * 0.5F, forward = controller.zza < 0 ? controller.zza * 0.5F : controller.zza;
        if (!flying()) return new Vec3(strafe, 0, forward);
        float pitch = controller.getXRot() * Mth.DEG_TO_RAD;
        float up = -Mth.sin(pitch) * forward + (controller.isJumping() ? 0.5F : 0F);
        return new Vec3(strafe, up, Mth.cos(pitch) * forward);
    }

    @Override
    protected float getRiddenSpeed(Player controller) {
        return flying() ? (float) getAttributeValue(Attributes.FLYING_SPEED) * 0.25F : (float) getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    @Override
    public void travel(Vec3 input) {
        if (flying() && getControllingPassenger() instanceof Player) travelFlying(input, getSpeed());
        else super.travel(input);
    }

    @Override
    public boolean isFlyingVehicle() {
        return flying();
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
        if (!flying()) super.checkFallDamage(ya, onGround, onState, pos);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        triggerAnim("main", "attack");
        return super.doHurtTarget(level, target);
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return def() != null ? EntityDimensions.scalable(def.width(), def.height()) : super.getDefaultDimensions(pose);
    }

    @Override
    protected Component getTypeName() {
        return def() != null ? Component.translatable(def.nameKey(kind())) : super.getTypeName();
    }

    @Override
    protected void dropFromLootTable(ServerLevel level, DamageSource source, boolean playerKilled) {
        if (def() != null && def.lootTable().isPresent()) dropFromLootTable(level, source, playerKilled, def.lootTable().get());
        else super.dropFromLootTable(level, source, playerKilled);
    }

    @Override
    public void checkDespawn() {
        if (hostile() && level().getDifficulty() == Difficulty.PEACEFUL) discard();
        else super.checkDespawn();
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return owner == null && !isVehicle() && super.removeWhenFarAway(distSqr);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && tickCount > 1 && def() == null) discard();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Kind", entityData.get(KIND));
        output.storeNullable("Owner", UUIDUtil.CODEC, owner);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        owner = input.read("Owner", UUIDUtil.CODEC).orElse(null);
        Identifier id = Identifier.tryParse(input.getStringOr("Kind", ""));
        CreatureDef d = id == null ? null : DiaithirBestiary.creatures(level()).getValue(id);
        if (d != null) setKind(id, d, false);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<Creature>("main", 4, test -> {
            if (paused()) return PlayState.PAUSE;
            if (flying() && !onGround()) return test.setAndContinue(FLY);
            return test.setAndContinue(test.isMoving() ? WALK : IDLE);
        }).triggerableAnim("attack", ATTACK));
    }

    protected boolean paused() {
        return false;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
