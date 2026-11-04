package net.diaithir.creatureutilities.bees;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A bee of one of the species in species.json. It is a normal bee in every way (hives, flowers, pollination, anger) and
 * also makes a comb of its species: each time it unloads nectar in a hive, and now and then while it carries nectar.
 */
public class SpeciesBee extends Bee {
    private static final EntityDataAccessor<String> SPECIES = SynchedEntityData.defineId(SpeciesBee.class, EntityDataSerializers.STRING);
    private int combTimer;

    public SpeciesBee(EntityType<? extends Bee> type, Level level) {
        super(type, level);
        this.combTimer = 2400 + this.random.nextInt(1200);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SPECIES, BeeSpecies.DEFAULT);
    }

    public String species() {
        return this.entityData.get(SPECIES);
    }

    public void setSpecies(String id) {
        this.entityData.set(SPECIES, Bees.SPECIES.containsKey(id) ? id : BeeSpecies.DEFAULT);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Species", species());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setSpecies(input.getStringOr("Species", BeeSpecies.DEFAULT));
    }

    @Override
    protected Component getTypeName() {
        return Component.translatable("entity." + Bees.ID + ".species_bee." + species());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide() && this.hasNectar() && --combTimer <= 0) {
            combTimer = 2400 + this.random.nextInt(1200);
            dropComb();
        }
    }

    @Override
    public void dropOffNectar() {
        if (!this.level().isClientSide() && this.hasNectar()) {
            dropComb();
        }
        super.dropOffNectar();
    }

    private void dropComb() {
        var item = Bees.COMBS.get(species());
        if (item != null) {
            this.spawnAtLocation((ServerLevel) this.level(), new ItemStack(item.get()));
        }
    }

    @Override
    public @Nullable Bee getBreedOffspring(ServerLevel level, AgeableMob partner) {
        SpeciesBee child = Bees.SPECIES_BEE.get().create(level, EntitySpawnReason.BREEDING);
        if (child != null) {
            String other = partner instanceof SpeciesBee sb ? sb.species() : null;
            child.setSpecies(Bees.offspring(species(), other, this.random));
        }
        return child;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        if (reason != EntitySpawnReason.BREEDING && reason != EntitySpawnReason.LOAD) {
            List<String> here = new ArrayList<>();
            var biome = level.getBiome(BlockPos.containing(this.getX(), this.getY(), this.getZ()));
            for (BeeSpecies s : Bees.SPECIES.values()) {
                if (s.wild() && biome.is(TagKey.<Biome>create(Registries.BIOME, Identifier.parse(s.biome())))) {
                    here.add(s.id());
                }
            }
            if (!here.isEmpty()) {
                setSpecies(here.get(this.random.nextInt(here.size())));
            }
        }
        return result;
    }
}
