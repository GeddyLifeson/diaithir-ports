package org.valkyrienskies.eureka.blockentity.renderer

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState
import net.minecraft.client.renderer.feature.ModelFeatureRenderer
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.phys.Vec3
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import org.valkyrienskies.eureka.block.IWoodType
import org.valkyrienskies.eureka.block.ShipHelmBlock
import org.valkyrienskies.eureka.blockentity.ShipHelmBlockEntity
import org.valkyrienskies.mod.common.getShipManagingPos

/**
 * Extract-side snapshot of everything the helm wheel needs at submit time.
 */
class ShipHelmRenderState : BlockEntityRenderState() {
    var facingYRot: Float = 0f
    var wheelRot: Float = 0f
    var woodType: IWoodType? = null
}

// 26.2: BlockEntityRenderer<T> (direct render with MultiBufferSource) became
// BlockEntityRenderer<T, S : BlockEntityRenderState> with the extract/submit split:
// createRenderState() + extractRenderState(game data -> state) + submit(state -> SubmitNodeCollector).
class ShipHelmBlockEntityRenderer(val ctx: BlockEntityRendererProvider.Context) :
    BlockEntityRenderer<ShipHelmBlockEntity, ShipHelmRenderState> {

    override fun createRenderState(): ShipHelmRenderState = ShipHelmRenderState()

    override fun extractRenderState(
        blockEntity: ShipHelmBlockEntity,
        state: ShipHelmRenderState,
        partialTick: Float,
        cameraPos: Vec3,
        crumblingOverlay: ModelFeatureRenderer.CrumblingOverlay?,
    ) {
        super.extractRenderState(blockEntity, state, partialTick, cameraPos, crumblingOverlay)
        state.facingYRot = blockEntity.blockState.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot()
        state.woodType = (blockEntity.blockState.block as? ShipHelmBlock)?.woodType
        // Rotate the wheel based on the ship's angular velocity
        val ship = blockEntity.level?.getShipManagingPos(blockEntity.blockPos)
        state.wheelRot = (ship?.omega?.y() ?: 0.0).toFloat()
    }

    override fun submit(
        state: ShipHelmRenderState,
        poseStack: PoseStack,
        collector: SubmitNodeCollector,
        cameraRenderState: CameraRenderState,
    ) {
        val woodType = state.woodType ?: return

        poseStack.pushPose()
        // Wheel offset of the base
        poseStack.translate(0.5, 0.60, 0.5)
        // Rotate wheel towards the direction its facing
        poseStack.mulPose(
            Quaternionf(
                AxisAngle4f(
                    (-state.facingYRot * Math.PI / 180.0).toFloat(), 0.0f, 1.0f, 0.0f
                )
            )
        )
        // Add offset of the base based of rotation
        poseStack.translate(0.0, 0.0, 0.19)
        // Rotate the wheel based of the ship omega
        poseStack.mulPose(Quaternionf(AxisAngle4f((state.wheelRot / 20f * Math.PI.toFloat()), 0.0f, 0.0f, 1.0f)))
        // Render the wheel
        WheelModels.render(poseStack, woodType, state.lightCoords, collector)

        poseStack.popPose()
    }
}
