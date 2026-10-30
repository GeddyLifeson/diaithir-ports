package org.valkyrienskies.mod.mixin.feature.fix_render_chunk_sorting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(SectionRenderDispatcher.RenderSection.class)
public class MixinRenderChunk {

    @Shadow
    @Final
    private BlockPos.MutableBlockPos renderOrigin;

    // The vanilla chunk sorting by distance to the player that this mixin used to fix (getDistToPlayerSqr) no longer
    // exists in 26.1.

    // This fixes ship blocks not rendering for some reason...
    @Inject(method = "hasAllNeighbors", at = @At("HEAD"), cancellable = true)
    private void preHasAllNeighbors(final CallbackInfoReturnable<Boolean> cir) {
        final ClientLevel world = Minecraft.getInstance().level;
        if (world == null) {
            return;
        }

        if (VSGameUtilsKt.isBlockInShipyard(world, renderOrigin)) {
            cir.setReturnValue(true);
        }
    }
}
