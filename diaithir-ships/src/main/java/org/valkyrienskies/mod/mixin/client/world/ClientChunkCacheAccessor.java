package org.valkyrienskies.mod.mixin.client.world;

import net.minecraft.client.multiplayer.ClientChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(ClientChunkCache.class)
public interface ClientChunkCacheAccessor {
    /**
     * {@code ClientChunkCache.Storage} is private, so the return type is coerced to Object.
     */
    @Accessor("storage")
    @Coerce
    Object vs$getStorage();
}
