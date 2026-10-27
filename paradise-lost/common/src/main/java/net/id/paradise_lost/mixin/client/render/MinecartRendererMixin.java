package net.id.paradise_lost.mixin.client.render;

import net.id.paradise_lost.util.MinecartFloatingHelper;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Overrides the extracted render yaw/pitch of floating carts. In 26.1 the pose is part of the render state.
 */
@Mixin(AbstractMinecartRenderer.class)
public class MinecartRendererMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"), require = 0)
    private void paradiseLost$floatPose(AbstractMinecart entity, MinecartRenderState state, float partialTicks, CallbackInfo ci) {
        float xRot = state.xRot;
        float yRot = state.yRot;
        if (MinecartFloatingHelper.shouldUseFloatRenderPose(entity)) {
            float yaw = MinecartFloatingHelper.getFloatRenderYaw(entity);
            // old-style rendering applies (180 - yRot), new-style applies yRot directly
            float wanted = state.isNewRender ? 180.0F - yaw : yaw;
            yRot = wanted;
            state.posOnRail = null;
            state.frontPos = null;
            state.backPos = null;
            xRot = MinecartFloatingHelper.getFloatRenderPitch(entity);
            Float scale = MinecartFloatingHelper.railSlopeScale(entity,
                    Mth.lerp(partialTicks, entity.xOld, entity.getX()),
                    Mth.lerp(partialTicks, entity.yOld, entity.getY()),
                    Mth.lerp(partialTicks, entity.zOld, entity.getZ()));
            if (scale != null) {
                xRot *= scale;
            }
        }
        float smoothedYaw = MinecartFloatingHelper.smoothRenderYawDegrees(entity, state.isNewRender ? yRot : 180.0F - yRot, partialTicks);
        state.yRot = state.isNewRender ? smoothedYaw : 180.0F - smoothedYaw;
        state.xRot = xRot * MinecartFloatingHelper.smoothRenderPitchSign(entity);
    }
}
