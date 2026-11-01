package org.valkyrienskies.mod.mixin.world.level;

import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.VS2ChunkAllocator;

/**
 * 26.1 added ChunkPos.MAX_COORDINATE_VALUE and rejects chunks beyond it (GenerationChunkHolder, tickets, ...).
 * VS shipyard claims live at x ~ -1.79M chunks, so shipyard chunks must count as valid.
 */
@Mixin(ChunkPos.class)
public abstract class MixinChunkPosBounds {
    @Inject(method = "isValid(II)Z", at = @At("HEAD"), cancellable = true)
    private static void vs$shipyardIsValid(int x, int z, CallbackInfoReturnable<Boolean> cir) {
        if (VS2ChunkAllocator.INSTANCE.isChunkInShipyardCompanion(x, z)) cir.setReturnValue(true);
    }
}
