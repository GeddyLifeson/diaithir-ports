package net.id.paradise_lost.mixin.client.render;

import net.id.paradise_lost.block.natural.cloud.ParadiseLostCloudBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes cloud blocks count as the screen overlay block when the player's eyes are inside one.
 */
@Mixin(ScreenEffectRenderer.class)
public abstract class InGameOverlayRendererMixin {

    @Inject(method = "getOverlayBlock", at = @At("HEAD"), cancellable = true, require = 0)
    private static void paradiseLost$cloudOverlay(Player player, CallbackInfoReturnable<Pair<BlockState, BlockPos>> cir) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int i = 0; i < 8; ++i) {
            double d = player.getX() + (double) (((float) ((i) % 2) - 0.5F) * player.getBbWidth() * 0.8F);
            double e = player.getEyeY() + (double) (((float) ((i >> 1) % 2) - 0.5F) * 0.1F);
            double f = player.getZ() + (double) (((float) ((i >> 2) % 2) - 0.5F) * player.getBbWidth() * 0.8F);
            mutable.set(d, e, f);
            BlockState blockState = player.level().getBlockState(mutable);
            if (blockState.getBlock() instanceof ParadiseLostCloudBlock) {
                cir.setReturnValue(Pair.of(blockState, mutable.immutable()));
                return;
            }
        }
    }
}
