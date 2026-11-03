package net.diaithir.druidry.ars.common.block.tile;

import net.diaithir.druidry.ars.api.client.ITooltipProvider;
import net.diaithir.druidry.ars.api.entity.IDispellable;
import net.diaithir.druidry.ars.api.mob_jar.JarBehavior;
import net.diaithir.druidry.ars.api.registry.JarBehaviorRegistry;
import net.diaithir.druidry.ars.common.block.ITickable;
import net.diaithir.druidry.ars.common.block.MobJar;
import net.diaithir.druidry.ars.common.items.data.MobJarData;
import net.diaithir.druidry.ars.common.lib.EntityTags;
import net.diaithir.druidry.ars.common.util.Log;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.diaithir.druidry.ars.setup.registry.DataComponentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class MobJarTile extends ModdedTile implements ITickable, IDispellable, ITooltipProvider {
    @Nullable
    public Entity cachedEntity;

    private CompoundTag entityTag;

    private CompoundTag extraDataTag;

    public boolean isVisible = true;

    public MobJarTile(BlockPos pos, BlockState state) {
        super(BlockRegistry.MOB_JAR_TILE, pos, state);
    }

    @Override
    public void tick() {
        try {
            if (level.isClientSide() && this.cachedEntity != null) {
                if (this.cachedEntity.isRemoved()) {
                    this.removeEntity();
                    return;
                }

                if (cachedEntity instanceof Mob mob) {
                    mob.getLookControl().tick();
                }
                // Camera-occlusion culling (isVisible) is client rendering logic; it was dropped in the 26.1 port.
                this.isVisible = true;
            }
            dispatchBehavior((behavior) -> {
                behavior.tick(this);
            });
            setChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean setEntityData(@NotNull Entity entity) {
        CompoundTag tag = saveEntityToTag(entity);
        if (entity.shouldBeSaved() && tag.contains("id")) {
            this.cachedEntity = loadEntityFromTag(level, tag);
            this.cachedEntity.setBoundingBox(new AABB(0, 0, 0, 0, 0, 0));
            this.cachedEntity.setPos(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);
            this.extraDataTag = null;
            this.entityTag = tag;
            if (!level.isClientSide()) {
                this.invalidateCapabilities();
                level.setBlockAndUpdate(worldPosition, this.getBlockState().setValue(MobJar.LIGHT_LEVEL, calculateLight()));
                updateBlock();
            }
            return true;
        } else {
            try {
                writeSimple(entity);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.entityTag = this.saveEntityToTag(this.getEntity());
        if (this.getEntity() instanceof Entity e) {
            e.remove(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        }
    }

    public void writeSimple(Entity e) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", EntityType.getKey(e.getType()).toString());
        if (level == null) return;
        this.cachedEntity = e.getType().create(level, EntitySpawnReason.LOAD);
        assert cachedEntity != null;
        this.cachedEntity.setBoundingBox(new AABB(0, 0, 0, 0, 0, 0));
        this.cachedEntity.setPos(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);
        this.extraDataTag = null;
        this.entityTag = tag;
        if (!level.isClientSide()) {
            level.setBlockAndUpdate(worldPosition, this.getBlockState().setValue(MobJar.LIGHT_LEVEL, calculateLight()));
            updateBlock();
        }
    }

    public int calculateLight() {
        if (getEntity() == null)
            return 0;

        if (getEntity().isOnFire()) {
            return 15;
        }
        if (getEntity() instanceof LightningBolt) {
            return 15;
        }
        AtomicInteger light = new AtomicInteger();
        JarBehaviorRegistry.forEach(getEntity(), (behavior) -> {
            light.set(Math.max(light.get(), behavior.lightLevel(this)));
        });
        return light.get();
    }

    public @Nullable Entity getEntity() {
        if (entityTag != null && cachedEntity == null) {
            cachedEntity = loadEntityFromTag(level, entityTag);
            if (cachedEntity == null) {
                return null;
            }
            cachedEntity.setBoundingBox(new AABB(0, 0, 0, 0, 0, 0));
            cachedEntity.setPos(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5);
        }
        return cachedEntity;
    }

    @Override
    public boolean onDispel(@NotNull LivingEntity caster) {
        if (entityTag == null || level == null)
            return false;
        Entity entity = loadEntityFromTag(level, entityTag);
        if (entity == null || entity.getType().builtInRegistryHolder().is(EntityTags.JAR_RELEASE_BLACKLIST))
            return false;
        entity.setPos(getBlockPos().getX() + 0.5, getBlockPos().getY() + 1.0, getBlockPos().getZ() + 0.5);
        level.addFreshEntity(entity);
        removeEntity();
        return true;
    }

    public void removeEntity() {
        if (this.getEntity() instanceof Entity e) {
            e.remove(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        }
        this.entityTag = null;
        this.cachedEntity = null;
        this.extraDataTag = null;
        this.invalidateCapabilities();
        updateBlock();
    }

    public CompoundTag getExtraDataTag() {
        return extraDataTag == null ? new CompoundTag() : extraDataTag;
    }

    public void setExtraDataTag(CompoundTag tag) {
        this.extraDataTag = tag;
        setChanged();
    }

    @Override
    public void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);

        // Check both conditions because the entity may have never been loaded on the server side.
        if (entityTag != null || cachedEntity != null) {
            // We cannot run getEntity when the level is null
            if (level == null) {
                if (entityTag != null) {
                    output.store("entityTag", CompoundTag.CODEC, entityTag);
                }
            } else {
                cachedEntity = getEntity();
                try {
                    if (cachedEntity != null) {
                        CompoundTag saved = saveEntityToTag(cachedEntity);
                        output.store("entityTag", CompoundTag.CODEC, saved);
                        saved.getString("id").ifPresent(id -> output.putString("entityId", id));
                    }
                } catch (Exception e) {
                    if (entityTag != null) {
                        output.store("entityTag", CompoundTag.CODEC, entityTag);
                    }
                    Log.getLogger().warn("Failed to save entity in MobJarTile", e);
                }
            }
        }
        if (extraDataTag != null) {
            output.store("extraMobData", CompoundTag.CODEC, extraDataTag);
        }
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        var saved = input.read("entityTag", CompoundTag.CODEC);
        if (saved.isPresent()) {
            this.entityTag = saved.get();
            this.cachedEntity = null;
        }
        this.extraDataTag = input.read("extraMobData", CompoundTag.CODEC).orElse(new CompoundTag());
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        this.cachedEntity = null;
        this.entityTag = null;
        super.handleUpdateTag(input);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ValueInput input) {
        this.cachedEntity = null;
        this.entityTag = null;
        super.onDataPacket(net, input);
    }

    public void dispatchBehavior(Consumer<JarBehavior<? extends Entity>> consumer) {
        Entity entity = getEntity();
        if (entity == null) {
            return;
        }
        JarBehaviorRegistry.forEach(entity, consumer);
    }

    public static Entity loadEntityFromTag(Level level, CompoundTag tag) {
        Entity entity = EntityType.loadEntityRecursive(tag, level, EntitySpawnReason.LOAD, EntityProcessor.NOP);
        if (entity == null) {
            String id = tag.getStringOr("id", "");
            Optional<EntityType<?>> type = EntityType.byString(id);
            if (type.isPresent()) {
                entity = type.get().create(level, EntitySpawnReason.LOAD);
            }
        }
        return entity;
    }

    public CompoundTag saveEntityToTag(Entity entity) {
        CompoundTag tag = new CompoundTag();
        if (entity != null) {
            TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, entity.level().registryAccess());
            entity.save(output);
            tag = output.buildResult();
            if (tag.isEmpty()) {
                tag.putString("id", EntityType.getKey(entity.getType()).toString());
            }
        }
        return tag;
    }

    @Override
    public void getTooltip(List<Component> tooltip) {
        Entity entity = getEntity();
        if (entity != null) {
            JarBehaviorRegistry.forEach(entity, (behavior) -> {
                behavior.getTooltip(this, tooltip);
            });
        }
    }


    @Override
    protected void applyImplicitComponents(@NotNull DataComponentGetter pComponentInput) {
        super.applyImplicitComponents(pComponentInput);
        var jar = pComponentInput.getOrDefault(DataComponentRegistry.MOB_JAR, new MobJarData(Optional.empty(), Optional.empty()));
        this.entityTag = jar.entityTag().orElse(null);
        this.extraDataTag = jar.extraDataTag().orElse(null);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.@NotNull Builder pComponents) {
        super.collectImplicitComponents(pComponents);
        if ((this.entityTag != null && !this.entityTag.isEmpty()) || (this.extraDataTag != null && !this.extraDataTag.isEmpty())) {
            pComponents.set(DataComponentRegistry.MOB_JAR, new MobJarData(Optional.ofNullable(this.entityTag), Optional.ofNullable(this.extraDataTag)));
        }
    }

    public <T, C> T getEntityCapability(EntityCapability<T, C> type, C context) {
        var entity = this.getEntity();
        if (entity == null) {
            return null;
        }

        return entity.getCapability(type, context);
    }

    /**
     * Wraps the jar entity's item handler so the entity tag is re-saved whenever items move.
     * (Ported to the 26.1 resource-transfer API; saves on any non-zero transfer, even if the transaction later rolls back.)
     */
    public static class SavingItemHandler extends DelegatingResourceHandler<ItemResource> {
        MobJarTile tile;

        private SavingItemHandler(MobJarTile tile, ResourceHandler<ItemResource> inner) {
            super(inner);
            this.tile = tile;
        }

        public static SavingItemHandler of(MobJarTile tile, ResourceHandler<ItemResource> inner) {
            return inner == null ? null : new SavingItemHandler(tile, inner);
        }

        private void save() {
            var entity = tile.getEntity();
            if (entity != null) {
                tile.entityTag = tile.saveEntityToTag(entity);
            }
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            int result = super.insert(index, resource, amount, transaction);
            if (result > 0) save();
            return result;
        }

        @Override
        public int insert(ItemResource resource, int amount, TransactionContext transaction) {
            int result = super.insert(resource, amount, transaction);
            if (result > 0) save();
            return result;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            int result = super.extract(index, resource, amount, transaction);
            if (result > 0) save();
            return result;
        }

        @Override
        public int extract(ItemResource resource, int amount, TransactionContext transaction) {
            int result = super.extract(resource, amount, transaction);
            if (result > 0) save();
            return result;
        }
    }
}
