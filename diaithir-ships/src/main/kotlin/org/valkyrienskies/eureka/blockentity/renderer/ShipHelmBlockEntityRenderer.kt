package org.valkyrienskies.eureka.blockentity.renderer

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState
import net.minecraft.client.renderer.feature.ModelFeatureRenderer
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.phys.Vec3
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import org.valkyrienskies.eureka.block.IWoodType
import org.valkyrienskies.eureka.block.ShipHelmBlock
import org.valkyrienskies.eureka.blockentity.ShipHelmBlockEntity
import org.valkyrienskies.mod.common.getShipManagingPos

class ShipHelmRenderState : BlockEntityRenderState() {
    var facingYRot = 0f
    var shipOmegaY = 0.0
    var woodType: IWoodType? = null
}

class ShipHelmBlockEntityRenderer(val ctx: BlockEntityRendererProvider.Context) :
    BlockEntityRenderer<ShipHelmBlockEntity, ShipHelmRenderState> {

    override fun createRenderState() = ShipHelmRenderState()

    override fun extractRenderState(
        blockEntity: ShipHelmBlockEntity,
        state: ShipHelmRenderState,
        partialTicks: Float,
        cameraPosition: Vec3,
        breakProgress: ModelFeatureRenderer.CrumblingOverlay?
    ) {
        BlockEntityRenderState.extractBase(blockEntity, state, breakProgress)
        state.facingYRot = blockEntity.blockState.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot()
        state.shipOmegaY = blockEntity.level?.getShipManagingPos(blockEntity.blockPos)?.omega?.y() ?: 0.0
        state.woodType = (blockEntity.blockState.block as? ShipHelmBlock)?.woodType
    }

    override fun submit(
        state: ShipHelmRenderState,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        camera: CameraRenderState
    ) {
        val model = state.woodType?.let { WheelModels.get(it) } ?: return
        poseStack.pushPose()
        // Wheel offset of the base
        poseStack.translate(0.5, 0.60, 0.5)
        // Rotate wheel towards the direction its facing
        poseStack.mulPose(Quaternionf(AxisAngle4f((-state.facingYRot * Math.PI / 180.0).toFloat(), 0.0f, 1.0f, 0.0f)))
        // Add offset of the base based of rotation
        poseStack.translate(0.0, 0.0, 0.19)
        // Rotate the wheel based of the ship omega
        poseStack.mulPose(Quaternionf(AxisAngle4f((state.shipOmegaY / 20f * Math.PI.toFloat()).toFloat(), 0.0f, 0.0f, 1.0f)))
        // Model isn't centered, calculated offsets
        poseStack.translate(-0.5, -0.625, -0.25)
        submitNodeCollector.submitBlockModel(
            poseStack, RenderTypes.cutoutMovingBlock(), listOf(model), IntArray(0),
            state.lightCoords, OverlayTexture.NO_OVERLAY, 0
        )
        poseStack.popPose()
    }
}
