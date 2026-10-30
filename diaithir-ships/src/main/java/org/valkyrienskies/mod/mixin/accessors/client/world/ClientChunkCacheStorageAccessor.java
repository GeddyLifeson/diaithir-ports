package org.valkyrienskies.mod.mixin.accessors.client.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.client.multiplayer.ClientChunkCache$Storage")
public interface ClientChunkCacheStorageAccessor {
    @Invoker(value = "inRange")
    boolean callInRange(int chunkX, int chunkZ);

    @Accessor("chunkCount")
    int getChunkCount();

    @Accessor("chunkCount")
    void setChunkCount(int chunkCount);
}
