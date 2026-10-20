package untamedwilds.world;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public abstract class FaunaHandler {

    private final static List<SpawnListEntry> spawnCritter = new ArrayList<>();
    private final static List<SpawnListEntry> spawnUndergroundLarge = new ArrayList<>();
    private final static List<SpawnListEntry> spawnLargePrey = new ArrayList<>();
    // This list is meant to populate rivers and water bodies with fish, and has higher density to compensate for the scarcity of water
    private final static List<SpawnListEntry> spawnDenseWater = new ArrayList<>();
    private final static List<SpawnListEntry> spawnOcean = new ArrayList<>();
    private final static List<SpawnListEntry> spawnSessile = new ArrayList<>();
    private final static List<SpawnListEntry> spawnApexPred = new ArrayList<>();

    public FaunaHandler() {
    }

    public static List<SpawnListEntry> getSpawnableList(animalType type) {
        return switch (type) {
            case CRITTER -> spawnCritter;
            case BENTHOS -> spawnSessile;
            case DENSE_WATER -> spawnDenseWater;
            case LARGE_OCEAN -> spawnOcean;
            case LARGE_HERB -> spawnLargePrey;
            case APEX_PRED -> spawnApexPred;
            case LARGE_UNDERGROUND -> spawnUndergroundLarge;
        };
    }

    public static List<SpawnListEntry> getSpawnableList(String type) {
        switch (type) {
            case "critter" -> {
                return spawnCritter;
            }
            case "benthos" -> {
                return spawnSessile;
            }
            case "water_river" -> {
                return spawnDenseWater;
            }
            case "water_ocean" -> {
                return spawnOcean;
            }
            case "herbivores" -> {
                return spawnLargePrey;
            }
            case "predators" -> {
                return spawnApexPred;
            }
            case "underground" -> {
                return spawnUndergroundLarge;
            }
        }
        return null;
    }

    public static void clearAllSpawnLists() {
        spawnCritter.clear();
        spawnUndergroundLarge.clear();
        spawnLargePrey.clear();
        spawnDenseWater.clear();
        spawnOcean.clear();
        spawnSessile.clear();
        spawnApexPred.clear();
    }

    public static class SpawnListEntry {
        public static final Codec<FaunaHandler.SpawnListEntry> CODEC = RecordCodecBuilder.create((p_237051_0_) -> p_237051_0_.group(
                Codec.STRING.fieldOf("type").orElse("").forGetter((p_237056_0_) -> p_237056_0_.entityName),
                Codec.INT.fieldOf("weight").orElse(0).forGetter((p_237054_0_) -> p_237054_0_.itemWeight),
                Codec.INT.fieldOf("size_min").orElse(0).forGetter((p_237055_0_) -> p_237055_0_.minGroupCount),
                Codec.INT.fieldOf("size_max").orElse(0).forGetter((p_237054_0_) -> p_237054_0_.maxGroupCount))
            .apply(p_237051_0_, FaunaHandler.SpawnListEntry::new));

        public String entityName;
        public EntityType<?> entityType;
        public int itemWeight;
        public int minGroupCount;
        public int maxGroupCount;

        public SpawnListEntry(String entityName, Integer weight, Integer minGroupCount, Integer maxGroupCount) {
            this.entityType = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.tryParse(entityName));
            this.itemWeight = weight;
            this.minGroupCount = minGroupCount;
            this.maxGroupCount = maxGroupCount;
        }

        public SpawnListEntry(EntityType<?> entityTypeIn, int weight, int minGroupCount, int maxGroupCount) {
            this.entityType = entityTypeIn;
            this.itemWeight = weight;
            this.minGroupCount = minGroupCount;
            this.maxGroupCount = maxGroupCount;
        }

        public String toString() {
            return EntityType.getKey(this.entityType) + "*(" + this.minGroupCount + "-" + this.maxGroupCount + "):" + this.itemWeight;
        }

        public Integer getGroupCount() {
            if (this.minGroupCount >= this.maxGroupCount)
                return this.minGroupCount;
            return this.minGroupCount + ThreadLocalRandom.current().nextInt(this.maxGroupCount - this.minGroupCount + 1);
        }
    }

    public enum animalType {
        CRITTER("critter"),
        BENTHOS("benthos"),
        DENSE_WATER("water_river"),
        LARGE_OCEAN("water_ocean"),
        LARGE_HERB("herbivores"),
        LARGE_UNDERGROUND("underground"),
        APEX_PRED("predators");

        public String name;

        animalType(String name) {
            this.name = name;
        }
    }
}
