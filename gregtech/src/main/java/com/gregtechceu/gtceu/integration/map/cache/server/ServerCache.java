package com.gregtechceu.gtceu.integration.map.cache.server;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/**
 * PORT: the minimap (JourneyMap/Xaero) prospecting cache is not ported. This keeps the call sites working as no-ops.
 */
public class ServerCache {

    public static final ServerCache instance = new ServerCache();

    public void maybeInitWorld(ServerLevel world) {}

    public void invalidateWorld(ServerLevel world) {}

    public boolean addVein(ResourceKey<Level> dim, int gridX, int gridZ, GeneratedVeinMetadata name) {
        return false;
    }

    public void clear() {}

    public void prospectSurfaceRockMaterial(ResourceKey<Level> dim, Material material, BlockPos pos,
                                            ServerPlayer player) {}

    public void prospectByOreMaterial(ResourceKey<Level> dim, Material material, BlockPos origin, ServerPlayer player,
                                      int radius) {}

    public void prospectByDepositName(ResourceKey<Level> dim, ResourceKey<GTOreDefinition> veinId, BlockPos origin,
                                      ServerPlayer player, int radius) {}

    public void prospectAllInChunk(ResourceKey<Level> dim, ChunkPos pos, ServerPlayer player) {}

    public void removeAllInChunk(ResourceKey<Level> dim, ChunkPos pos) {}
}
