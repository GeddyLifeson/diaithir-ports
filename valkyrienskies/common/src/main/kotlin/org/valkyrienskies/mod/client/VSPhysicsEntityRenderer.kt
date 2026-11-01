package org.valkyrienskies.mod.client

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.resources.Identifier
import org.joml.Quaternionf
import org.valkyrienskies.core.internal.world.VsiClientShipWorld
import org.valkyrienskies.mod.client.VSPhysicsEntityModel.Companion.LAYER_LOCATION
import org.valkyrienskies.mod.common.IShipObjectWorldClientProvider
import org.valkyrienskies.mod.common.ValkyrienSkiesMod
import org.valkyrienskies.mod.common.entity.VSPhysicsEntity

/**
 * Render state for a physics entity. Carries the ship-space render rotation captured during extract,
 * since 26.2's submit() has no access to the entity. A null rotation means the ship transform wasn't
 * available this frame and the entity should not be drawn.
 */
class VSPhysicsEntityRenderState : LivingEntityRenderState() {
    var shipToWorldRotation: Quaternionf? = null
}

// 26.2: EntityRenderer/MobRenderer render via extractRenderState(entity, state, partialTick) + a
// stateless submit(state, ...), replacing the old render(entity, ..., MultiBufferSource, ...). The
// ship rotation (previously read from the entity inside render()) is captured into the render state
// during extract and applied to the PoseStack in submit before delegating to the base model draw.
class VSPhysicsEntityRenderer(context: Context) :
    MobRenderer<VSPhysicsEntity, VSPhysicsEntityRenderState, VSPhysicsEntityModel>(
        context,
        // Bake the layer
        VSPhysicsEntityModel(context.bakeLayer(LAYER_LOCATION)),
        // Shadow size
        0.0F
    ) {

    override fun createRenderState(): VSPhysicsEntityRenderState = VSPhysicsEntityRenderState()

    override fun extractRenderState(
        entity: VSPhysicsEntity, state: VSPhysicsEntityRenderState, partialTick: Float
    ) {
        super.extractRenderState(entity, state, partialTick)
        val renderTransform = entity.getRenderTransform(
            ((Minecraft.getInstance() as IShipObjectWorldClientProvider).shipObjectWorld as VsiClientShipWorld)
        )
        state.shipToWorldRotation = renderTransform?.let { Quaternionf(it.shipToWorldRotation) }
    }

    override fun submit(
        state: VSPhysicsEntityRenderState, poseStack: PoseStack, collector: SubmitNodeCollector,
        cameraState: CameraRenderState
    ) {
        // Ship transform not available this frame - don't render (matches the old early return).
        val rotation = state.shipToWorldRotation ?: return

        // Rotate with the ship
        poseStack.mulPose(rotation)
        // Offset model from hitbox
        poseStack.translate(0.0, -0.5, 0.0)

        super.submit(state, poseStack, collector, cameraState)
    }

    override fun getTextureLocation(state: VSPhysicsEntityRenderState): Identifier {
        return Identifier.fromNamespaceAndPath(ValkyrienSkiesMod.MOD_ID, "textures/test_sphere.png")
    }
}
