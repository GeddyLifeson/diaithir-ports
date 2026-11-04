package org.valkyrienskies.mod.mixin.client.world;

import net.minecraft.client.multiplayer.ClientChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientChunkCache.class)
public interface ClientChunkCacheAccessor {
    /**
     * Mixin matches accessor descriptors exactly (no coercion for fields), so the real
     * (access-widened) {@code ClientChunkCache.Storage} type must be returned.
     */
    @Accessor("storage")
    ClientChunkCache.Storage vs$getStorage();
}
