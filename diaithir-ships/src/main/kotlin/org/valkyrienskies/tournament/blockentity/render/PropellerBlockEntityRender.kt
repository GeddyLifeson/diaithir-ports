package org.valkyrienskies.tournament.blockentity.render

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState
import net.minecraft.client.renderer.feature.ModelFeatureRenderer
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.world.level.block.DirectionalBlock
import net.minecraft.world.phys.Vec3
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import org.valkyrienskies.tournament.TournamentModels
import org.valkyrienskies.tournament.blockentity.PropellerBlockEntity
import org.valkyrienskies.tournament.util.extension.pose

class PropellerBlockEntityRender<T : PropellerBlockEntity<T>>(
    val model: TournamentModels.Model
) : BlockEntityRenderer<T, TournamentRenderState> {

    override fun createRenderState() = TournamentRenderState()

    override fun extractRenderState(
        be: T, state: TournamentRenderState, partialTicks: Float, cameraPosition: Vec3,
        breakProgress: ModelFeatureRenderer.CrumblingOverlay?
    ) {
        BlockEntityRenderState.extractBase(be, state, breakProgress)
        state.facing = be.blockState.getValue(DirectionalBlock.FACING)
        state.rotationDeg = be.rotation
    }

    override fun submit(state: TournamentRenderState, pose: PoseStack, collector: SubmitNodeCollector, camera: CameraRenderState) {
        pose.pose {
            translate(0.5, 0.5, 0.5)
            mulPose(state.facing.opposite.rotation)
            mulPose(Quaternionf(AxisAngle4f(Math.toRadians(state.rotationDeg).toFloat(), 0f, 1f, 0f)))
            translate(-0.5, -0.5, -0.5)
            collector.submitTournamentModel(model, pose, state, RenderTypes.cutoutMovingBlock())
        }
    }
}
