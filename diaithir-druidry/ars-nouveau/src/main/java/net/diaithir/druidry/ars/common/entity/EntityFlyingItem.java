package net.diaithir.druidry.ars.common.entity;

import net.diaithir.druidry.ars.api.util.BlockUtil;
import net.diaithir.druidry.ars.api.util.NearbyPlayerCache;
import net.diaithir.druidry.ars.common.util.EasingManager;
import net.diaithir.druidry.ars.common.util.EasingType;
import net.diaithir.druidry.ars.setup.registry.DataSerializers;
import net.diaithir.druidry.ars.setup.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class EntityFlyingItem extends ColoredProjectile {
    public static final EntityDataAccessor<Vec3> to = SynchedEntityData.defineId(EntityFlyingItem.class, DataSerializers.VEC.get());
    public static final EntityDataAccessor<Vec3> from = SynchedEntityData.defineId(EntityFlyingItem.class, DataSerializers.VEC.get());
    public static final EntityDataAccessor<Boolean> SPAWN_TOUCH = SynchedEntityData.defineId(EntityFlyingItem.class, EntityDataSerializers.BOOLEAN);

    public int age;
    int maxAge;

    public static final EntityDataAccessor<ItemStack> HELD_ITEM = SynchedEntityData.defineId(EntityFlyingItem.class, EntityDataSerializers.ITEM_STACK);
    public static final EntityDataAccessor<Float> OFFSET = SynchedEntityData.defineId(EntityFlyingItem.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Boolean> DIDOFFSET = SynchedEntityData.defineId(EntityFlyingItem.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_BUBBLE = SynchedEntityData.defineId(EntityFlyingItem.class, EntityDataSerializers.BOOLEAN);

    public EntityFlyingItem(Level worldIn, Vec3 from, Vec3 to) {
        this(worldIn, from, to, 255, 25, 180);
    }

    public EntityFlyingItem(Level worldIn, Vec3 from, Vec3 to, int r, int g, int b) {
        this(ModEntities.ENTITY_FLYING_ITEM.get(), worldIn);
        this.entityData.set(EntityFlyingItem.to, to);
        this.entityData.set(EntityFlyingItem.from, from);
        this.maxAge = (int) Math.floor(from.subtract(to).length() * 5);
        this.entityData.set(RED, r);
        this.entityData.set(GREEN, g);
        this.entityData.set(BLUE, b);
        setPos(from);
    }

    public EntityFlyingItem(Level worldIn, BlockPos from, BlockPos to) {
        this(worldIn, new Vec3(from.getX() + 0.5, from.getY(), from.getZ() + 0.5), new Vec3(to.getX() + 0.5, to.getY(), to.getZ() + 0.5), 255, 25, 180);
    }

    public EntityFlyingItem(Level worldIn, BlockPos from, BlockPos to, int r, int g, int b) {
        this(worldIn, new Vec3(from.getX() + 0.5, from.getY(), from.getZ() + 0.5), new Vec3(to.getX() + 0.5, to.getY(), to.getZ() + 0.5), r, g, b);
    }

    public EntityFlyingItem(EntityType<EntityFlyingItem> entityAOEProjectileEntityType, Level world) {
        super(entityAOEProjectileEntityType, world);
    }

    public static @NotNull EntityFlyingItem spawn(ServerLevel level, Vec3 from, Vec3 to, int r, int g, int b) {
        boolean canSpawn = NearbyPlayerCache.isPlayerNearby(BlockPos.containing(from), level, 64);
        EntityFlyingItem entity = new EntityFlyingItem(level, from, to, r, g, b);
        if (canSpawn && level.isLoaded(BlockPos.containing(to))) {
            level.addFreshEntity(entity);
        }
        return entity;
    }

    public static @NotNull EntityFlyingItem spawn(ServerLevel level, BlockPos from, BlockPos to, int r, int g, int b) {
        return spawn(from, level, from, to, r, g, b);
    }

    public static @NotNull EntityFlyingItem spawn(BlockPos checkCachePos, ServerLevel level, BlockPos from, BlockPos to, int r, int g, int b) {
        boolean canSpawn = NearbyPlayerCache.isPlayerNearby(checkCachePos, level, 64);
        EntityFlyingItem entity = new EntityFlyingItem(level, from, to, r, g, b);
        if (canSpawn && level.isLoaded(to)) {
            level.addFreshEntity(entity);
        }
        return entity;
    }

    public static @NotNull EntityFlyingItem spawn(BlockPos checkCachePos, ServerLevel level, BlockPos from, BlockPos to) {
        return spawn(checkCachePos, level, from, to, 255, 25, 180);
    }

    public static @NotNull EntityFlyingItem spawn(ServerLevel level, BlockPos from, BlockPos to) {
        return spawn(from, level, from, to);
    }

    public EntityFlyingItem setStack(ItemStack stack) {
        this.entityData.set(HELD_ITEM, stack.copy());
        return this;
    }

    /**
     * This is the actual function that smoothly interpolates (lerp) between keyframes
     *
     * @param startValue The animation's start value
     * @param endValue   The animation's end value
     * @return The interpolated value
     */
    public static float lerp(double percentCompleted, double startValue, double endValue, EasingType type) {
        if (percentCompleted >= 1) {
            return (float) endValue;
        }
        percentCompleted = EasingManager.ease(percentCompleted, type, null);
        // current tick / position should be between 0 and 1 and represent the percentage of the lerping that has completed
        return (float) lerpInternal(percentCompleted, startValue,
                endValue);
    }

    public static double lerpInternal(double pct, double start, double end) {
        return start + pct * (end - start);
    }

    /**
     * Calculates a value between 0 and 1, given the precondition that value
     * is between min and max. 0 means value = max, and 1 means value = min.
     */
    public double normalize(double value, double min, double max) {
        return 1.0 - ((value - min) / (max - min));
    }

    boolean wentUp;

    @Override
    public void tick() {
        super.tick();
        this.age++;


        if (age > 400)
            this.remove(RemovalReason.DISCARDED);

        Vec3 start = entityData.get(from);
        Vec3 end = entityData.get(to);
        if (BlockUtil.distanceFrom(level(), end, this.position()) <= 1 || this.age > 1000 || BlockUtil.distanceFrom(level(), end, this.position()) > 16) {
            this.remove(RemovalReason.DISCARDED);
            return;
        }

        double posX = getX();
        double posY = getY();
        double posZ = getZ();


        double time = 1 - normalize(age, 0.0, 80);

        EasingType type = EasingType.NONE;

        double startY = start.y();
        double endY = end.y() + getDistanceAdjustment(start, end);
        double lerpX = lerp(time, start.x(), end.x(), type);
        double lerpY = lerp(time, lerp(time, startY, endY, type), lerp(time, endY, startY, type), type);
        double lerpZ = lerp(time, start.z(), end.z(), type);

        Vec3 adjustedPos = new Vec3(posX, end.y(), posZ);
        if (BlockUtil.distanceFrom(level(), end, adjustedPos) <= 0.5) {
            posY = getY() - 0.05;
            this.setPos(lerpX, posY, lerpZ);
        } else {
            this.setPos(lerpX, lerpY, lerpZ);
        }
    }

    public EntityFlyingItem withNoTouch() {
        this.entityData.set(SPAWN_TOUCH, false);
        return this;
    }

    public void setDistanceAdjust(float offset) {
        this.entityData.set(OFFSET, offset);
        this.entityData.set(DIDOFFSET, true);
    }

    private double getDistanceAdjustment(Vec3 start, Vec3 end) {
        if (this.entityData.get(DIDOFFSET))
            return this.entityData.get(OFFSET);

        double distance = BlockUtil.distanceFrom(level(), start, end);
        if (distance <= 1.5)
            return 2.5;

        return 3;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(HELD_ITEM, input.read("item", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
        this.age = input.getIntOr("age", 0);
        this.entityData.set(DIDOFFSET, input.getBooleanOr("didoffset", false));
        this.entityData.set(OFFSET, input.getFloatOr("offset", 0f));
        this.entityData.set(EntityFlyingItem.from, input.read("from", Vec3.CODEC).orElse(Vec3.ZERO));
        this.entityData.set(EntityFlyingItem.to, input.read("to", Vec3.CODEC).orElse(Vec3.ZERO));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (getStack() != null) {
            output.store("item", ItemStack.OPTIONAL_CODEC, getStack());
        }
        output.putInt("age", age);
        output.putBoolean("didoffset", this.entityData.get(DIDOFFSET));
        output.putFloat("offset", this.entityData.get(OFFSET));
        output.store("from", Vec3.CODEC, this.entityData.get(EntityFlyingItem.from));
        output.store("to", Vec3.CODEC, this.entityData.get(EntityFlyingItem.to));
    }

    public ItemStack getStack() {
        return this.entityData.get(HELD_ITEM);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(HELD_ITEM, ItemStack.EMPTY);
        pBuilder.define(OFFSET, 0.0f);
        pBuilder.define(DIDOFFSET, false);
        pBuilder.define(to, new Vec3(0, 0, 0));
        pBuilder.define(from, new Vec3(0, 0, 0));
        pBuilder.define(SPAWN_TOUCH, true);
        pBuilder.define(IS_BUBBLE, false);
    }

    @Override
    public EntityType<?> getType() {
        return ModEntities.ENTITY_FLYING_ITEM.get();
    }
}
