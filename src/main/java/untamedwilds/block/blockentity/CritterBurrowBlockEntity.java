package untamedwilds.block.blockentity;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import untamedwilds.util.NbtUtil;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import java.util.ArrayList;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import untamedwilds.config.ConfigMobControl;
import untamedwilds.entity.ComplexMob;
import untamedwilds.init.ModBlock;
import untamedwilds.util.EntityUtils;

import java.util.List;

public class CritterBurrowBlockEntity extends BlockEntity {

    private final List<Inhabitants> inhabitants = new ArrayList<>();
    private EntityType<?> entityType;
    private int variant;
    private int count;

    public CritterBurrowBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlock.TILE_ENTITY_BURROW.get(), pos, state);
    }

    public int getSumMobs() { return this.inhabitants.size() + this.count; }
    public boolean hasNoMobs() {
        return this.getSumMobs() == 0;
    }

    public void tryEnterBurrow(LivingEntity entityIn) {
        entityIn.stopRiding();
        entityIn.ejectPassengers();
        CompoundTag CompoundTag = EntityUtils.writeEntityToNBT(entityIn, true);
        this.inhabitants.add(new Inhabitants(CompoundTag));
        if (this.getLevel() != null) {
            BlockPos blockpos = this.getBlockPos();
            this.getLevel().playSound(null, blockpos.getX(), blockpos.getY(), blockpos.getZ(), SoundEvents.BEEHIVE_ENTER, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (this.getLevel() instanceof ServerLevel serverLevel) {
                EntityUtils.spawnParticlesOnEntity(serverLevel, entityIn, ParticleTypes.POOF, 3, 6);
            }
        }
        entityIn.remove(Entity.RemovalReason.DISCARDED);
        setChanged();
    }

    public void releaseOrCreateMob(ServerLevel worldIn) {
        if (!this.hasNoMobs() && this.getEntityType() != null && this.getVariant() >= 0 && worldIn.getRandom().nextFloat() < 0.1D * (this.getSumMobs() * this.getSumMobs())) {
            BlockPos blockpos = this.getBlockPos();
            if (worldIn.hasNearbyAlivePlayer((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D, ConfigMobControl.critterSpawnRange.get())) {
                if (!this.getInhabitants().isEmpty()) {
                    int i = worldIn.getRandom().nextInt(this.inhabitants.size());
                    Entity spawn = this.getEntityType().create(worldIn, (e) -> {
                        if (this.inhabitants.get(i).entityData != null && this.inhabitants.get(i).entityData.contains("EntityTag")) {
                            e.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, worldIn.registryAccess(), this.inhabitants.get(i).entityData.getCompoundOrEmpty("EntityTag")));
                        }
                    }, blockpos, EntitySpawnReason.DISPENSER, true, false);
                    if (spawn != null) {
                        worldIn.addFreshEntityWithPassengers(spawn);
                        this.inhabitants.remove(i);
                        setChanged();
                    }
                }
                else if (this.getCount() > 0 && this.getEntityType() != null) {
                    // Turns out that calling EntityType.create(...) will fucking crash the game if it pulls an invalid variant
                    //Entity spawn = this.getEntityType().create(worldIn, null, null, null, blockpos, EntitySpawnReason.CHUNK_GENERATION, true, false);
                    Entity spawn = this.getEntityType().create(worldIn, EntitySpawnReason.CHUNK_GENERATION);
                    if (spawn != null) {
                        spawn.snapTo(blockpos.getX() + 0.5D, blockpos.getY(), blockpos.getZ() + 0.5D, Mth.wrapDegrees(worldIn.getRandom().nextFloat() * 360.0F), 0.0F);
                        if (spawn instanceof Mob mobSpawn) {
                            mobSpawn.finalizeSpawn(worldIn, worldIn.getCurrentDifficultyAt(blockpos), EntitySpawnReason.CHUNK_GENERATION, null);
                        }
                        if (spawn instanceof ComplexMob entitySpawn) {
                            entitySpawn.setVariant(EntityUtils.getClampedNumberOfSpecies(this.variant, this.entityType));
                            entitySpawn.setHome(this.getBlockPos());
                        }
                        worldIn.addFreshEntityWithPassengers(spawn);
                        this.setCount(this.getCount() - 1);
                        setChanged();
                    }
                }
                if (worldIn.getRandom().nextInt(ConfigMobControl.burrowRepopulationChance.get()) == 0 && this.getCount() < 20) {
                    this.setCount(this.getCount() + 1);
                }
            }
        }
    }

    public void setCount(int newCount) {
        this.count = newCount;
    }
    public int getCount() { return this.count; }

    public void setVariant(int variant) {
        this.variant = variant;
    }
    public int getVariant() { return this.variant; }

    public void setEntityType(EntityType<?> type) {
        this.entityType = type;
    }
    public EntityType<?> getEntityType() { return this.entityType; }

    @Override
    protected void loadAdditional(ValueInput compound) {
        super.loadAdditional(compound);
        this.inhabitants.clear();
        java.util.List<CompoundTag> listnbt = compound.read("Inhabitants", CompoundTag.CODEC.listOf()).orElse(java.util.List.of());
        this.setVariant(compound.getIntOr("Variant", 0));
        this.setCount(compound.getIntOr("Count", 0));
        if (NbtUtil.has(compound, "entityType")) {
            this.setEntityType(EntityType.byString(compound.getStringOr("entityType", "")).orElse(null));
        }

        for(int i = 0; i < listnbt.size(); ++i) {
            CompoundTag CompoundTag = listnbt.get(i);
            Inhabitants beehivetileentity$bee = new Inhabitants(CompoundTag.getCompoundOrEmpty("EntityData"));
            this.inhabitants.add(beehivetileentity$bee);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput compound) {
        super.saveAdditional(compound);
        compound.store("Inhabitants", CompoundTag.CODEC.listOf(), this.getInhabitants());
        compound.putInt("Count", this.getCount());
        compound.putInt("Variant", this.getVariant());
        if (this.getEntityType() != null) {
            compound.putString("entityType", this.getEntityType().builtInRegistryHolder().key().identifier().toString());
        }
    }

    public java.util.List<CompoundTag> getInhabitants() {
        java.util.List<CompoundTag> inhabitants = new java.util.ArrayList<>();

        for(Inhabitants inhabitant : this.inhabitants) {
            CompoundTag CompoundTag = new CompoundTag();
            CompoundTag.put("EntityData", inhabitant.entityData);
            inhabitants.add(CompoundTag);
        }

        return inhabitants;
    }

    static class Inhabitants {
        private final CompoundTag entityData;

        private Inhabitants(CompoundTag nbt) {
            nbt.remove("UUID");
            this.entityData = nbt;
        }
    }
}