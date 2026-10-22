package org.valkyrienskies.mod.mixin.client.renderer;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.mod.client.IVSCamera;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer {
    @Shadow
    @Final
    private SectionOcclusionGraph sectionOcclusionGraph;

    @Unique
    private ShipTransform valkyrienskies$prevShipMountedToTransform = null;

    /**
     * 26.2: {@code renderLevel(...)} was replaced by
     * {@code render(GraphicsResourceAllocator, DeltaTracker, boolean, CameraRenderState, Matrix4fc,
     * GpuBufferSlice, Vector4f, boolean)}, and the live {@code Camera} is no longer passed in - only its
     * extracted {@link CameraRenderState}. We read the ship-mounted render transform straight off the
     * live main camera (which {@code MixinCamera} tagged during {@code Camera.update}) and invalidate the
     * occlusion graph when the ship the player is riding rotates enough, so ship sections get re-culled.
     *
     * <p>The old {@code disableBlockDamageDistanceCheck} (ModifyExpressionValue on the {@code 1024}
     * block-damage-distance constant) is dropped: that constant no longer lives in the level render
     * method in 26.2 (block breaking moved onto the SubmitNodeCollector path).
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void valkyrienskies$preRender(final GraphicsResourceAllocator graphicsResourceAllocator,
        final DeltaTracker deltaTracker, final boolean bl, final CameraRenderState cameraRenderState,
        final Matrix4fc matrix4fc, final GpuBufferSlice gpuBufferSlice, final Vector4f vector4f,
        final boolean bl2, final CallbackInfo ci) {
        final Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (camera == null) {
            return;
        }
        final ShipTransform shipMountedRenderTransform = ((IVSCamera) camera).getShipMountedRenderTransform();
        if (valkyrienskies$prevShipMountedToTransform != shipMountedRenderTransform) {
            if (valkyrienskies$prevShipMountedToTransform != null && shipMountedRenderTransform != null) {
                // Compute the angle between rotations
                double rotDot = Math.abs(valkyrienskies$prevShipMountedToTransform.getShipToWorldRotation()
                    .dot(shipMountedRenderTransform.getShipToWorldRotation()));
                rotDot = Math.min(rotDot, 1.0);
                final double angle = 2.0 * Math.acos(rotDot);
                if (Math.toDegrees(angle) > 1.0) {
                    valkyrienskies$prevShipMountedToTransform = shipMountedRenderTransform;
                    sectionOcclusionGraph.invalidate();
                }
            } else {
                valkyrienskies$prevShipMountedToTransform = shipMountedRenderTransform;
                sectionOcclusionGraph.invalidate();
            }
        }
    }
}
