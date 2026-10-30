package com.kyanite.deeperdarker.world.otherside;

import com.kyanite.deeperdarker.DeeperDarker;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public class OthersideBiomes {
    public static final ResourceKey<Biome> DEEPLANDS = createKey("deeplands");
    public static final ResourceKey<Biome> ECHOING_FOREST = createKey("echoing_forest");
    public static final ResourceKey<Biome> BLOOMING_CAVERNS = createKey("blooming_caverns");
    public static final ResourceKey<Biome> OVERCAST_COLUMNS = createKey("overcast_columns");

    public static ResourceKey<Biome> createKey(String name) {
        return ResourceKey.create(Registries.BIOME, DeeperDarker.rl(name));
    }
}
