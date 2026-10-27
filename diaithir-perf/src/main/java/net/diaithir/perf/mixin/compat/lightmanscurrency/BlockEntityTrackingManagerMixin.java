package net.diaithir.perf.mixin.compat.lightmanscurrency;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Lightman's Currency 26.1-1.0.0.1 untracks trader block entities on every ChunkWatchEvent.UnWatch with
 * ServerLevel.getChunk(x, z), which is a blocking chunk fetch. After a far teleport the server drops thousands of
 * chunks from the player's view in one tick, and each call waits on the chunk system: single ticks of 149 to 257 s.
 * The chunk only matters while it is loaded (its block entities are the ones being untracked), so ask for it with
 * getChunkNow and treat an unloaded chunk as having nothing to untrack. Optional: a Lightman's update that renames
 * the method only turns this off.
 */
@Mixin(targets = "io.github.lightman314.lightmanscurrency.api.trader.tracking.managers.BlockEntityTrackingManager", remap = false)
abstract class BlockEntityTrackingManagerMixin {
    @Redirect(method = "untrackBlockEntity", require = 0, remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;hasChunk(II)Z"))
    private static boolean diaithirperf$loadedOnly(ServerLevel level, int x, int z) {
        return level.getChunkSource().getChunkNow(x, z) != null;
    }

    @Redirect(method = "untrackBlockEntity", require = 0, remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getChunk(II)Lnet/minecraft/world/level/chunk/LevelChunk;"))
    private static LevelChunk diaithirperf$noBlockingFetch(ServerLevel level, int x, int z) {
        return level.getChunkSource().getChunkNow(x, z);
    }
}
