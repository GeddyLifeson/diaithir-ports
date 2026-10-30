package org.valkyrienskies.mod.mixin.feature.hit_outline;

import static org.valkyrienskies.mod.common.VSClientGameUtils.transformRenderWithShip;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {

    @Shadow
    @Nullable
    private ClientLevel level;

    /**
     * @reason The outline of a block on a ship has to be rendered with the ship render transform.
     */
    @Inject(
        method = "renderHitOutline",
        at = @At("HEAD"),
        cancellable = true
    )
    private void preRenderHitOutline(final PoseStack matrixStack, final VertexConsumer vertexConsumer,
        final double camX, final double camY, final double camZ, final BlockOutlineRenderState state,
        final int color, final float width, final CallbackInfo ci
    ) {
        if (this.level == null) {
            return;
        }
        final ClientShip ship = VSGameUtilsKt.getLoadedShipManagingPos(this.level, state.pos());
        if (ship != null) {
            matrixStack.pushPose();
            transformRenderWithShip(ship.getRenderTransform(), matrixStack, state.pos(), camX, camY, camZ);
            ShapeRenderer.renderShape(matrixStack, vertexConsumer, state.shape(), 0d, 0d, 0d, color, width);
            matrixStack.popPose();
            ci.cancel();
        }
    }

}
