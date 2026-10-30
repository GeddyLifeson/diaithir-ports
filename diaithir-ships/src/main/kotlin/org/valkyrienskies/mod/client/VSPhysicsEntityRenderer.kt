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

class VSPhysicsEntityRenderState : LivingEntityRenderState() {
    /** Ship-to-world rotation of the physics entity, null if it has no render transform. */
    var shipToWorldRotation: Quaternionf? = null
}

class VSPhysicsEntityRenderer<T : VSPhysicsEntity>(context: Context) :
    MobRenderer<T, VSPhysicsEntityRenderState, VSPhysicsEntityModel>(
        context,
        // Bake the layer
        VSPhysicsEntityModel(context.bakeLayer(LAYER_LOCATION)),
        // Shadow size
        0.0F
    ) {

    override fun createRenderState(): VSPhysicsEntityRenderState = VSPhysicsEntityRenderState()

    override fun extractRenderState(entity: T, state: VSPhysicsEntityRenderState, partialTicks: Float) {
        super.extractRenderState(entity, state, partialTicks)
        val renderTransform = entity.getRenderTransform(
            ((Minecraft.getInstance() as IShipObjectWorldClientProvider).shipObjectWorld as VsiClientShipWorld)
        )
        state.shipToWorldRotation = renderTransform?.let { Quaternionf(it.shipToWorldRotation) }
    }

    override fun submit(
        state: VSPhysicsEntityRenderState, poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector, camera: CameraRenderState
    ) {
        val rotation = state.shipToWorldRotation ?: return

        poseStack.pushPose()
        // Rotate with the ship
        poseStack.mulPose(rotation)

        // Offset model from hitbox
        poseStack.translate(0.0, -0.5, 0.0)

        super.submit(state, poseStack, submitNodeCollector, camera)
        poseStack.popPose()
    }

    override fun getTextureLocation(state: VSPhysicsEntityRenderState): Identifier {
        return Identifier.fromNamespaceAndPath(ValkyrienSkiesMod.MOD_ID, "textures/test_sphere.png")
    }
}
