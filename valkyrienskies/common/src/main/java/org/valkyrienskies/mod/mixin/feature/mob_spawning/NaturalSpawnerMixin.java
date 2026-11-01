package org.valkyrienskies.mod.mixin.feature.mob_spawning;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NaturalSpawner.SpawnState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.config.VSGameConfig;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {

    // 26.2: spawnForChunk(ServerLevel, LevelChunk, SpawnState, boolean, boolean, boolean) became
    // spawnForChunk(ServerLevel, LevelChunk, SpawnState, List<MobCategory>) - the three spawn-type
    // booleans were replaced by a pre-filtered list of categories (see getFilteredSpawningCategories).
    @Inject(method = "spawnForChunk", at = @At("HEAD"), cancellable = true)
    private static void determineSpawningOnShips(final ServerLevel level, final LevelChunk chunk,
        final SpawnState spawnState, final List<MobCategory> categories, final CallbackInfo ci) {
        if (VSGameUtilsKt.isChunkInShipyard(level, chunk.getPos().x, chunk.getPos().z)) {
            if (!VSGameConfig.SERVER.getAllowMobSpawns()) {
                ci.cancel();
            }
        }
    }
}
