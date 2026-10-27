package net.id.paradise_lost.mixin.client.render;

import net.id.paradise_lost.world.dimension.ParadiseLostDimension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fades water out towards the bottom of the Paradise Lost dimension.
 */
@Mixin(FluidRenderer.class)
public class FluidRendererMixin {

    @Unique
    private float paradiseLost$fadeAlpha = 1F;

    @Inject(method = "tesselate", at = @At("HEAD"), require = 0)
    private void paradiseLost$computeFade(BlockAndTintGetter level, BlockPos pos, FluidRenderer.Output output, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
        this.paradiseLost$fadeAlpha = 1F;
        var clientLevel = Minecraft.getInstance().level;
        if (clientLevel != null && clientLevel.dimension() == ParadiseLostDimension.PARADISE_LOST_WORLD_KEY) {
            if (fluidState.getType().isSame(Fluids.WATER)) {
                this.paradiseLost$fadeAlpha = Math.min((pos.getY() - level.getMinY()) / 32F, 1);
            }
        }
    }

    @ModifyVariable(method = "vertex", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
    private int paradiseLost$applyFade(int color) {
        if (this.paradiseLost$fadeAlpha >= 1F) {
            return color;
        }
        return ARGB.color(Math.round(ARGB.alpha(color) * Math.max(this.paradiseLost$fadeAlpha, 0F)), color);
    }
}
