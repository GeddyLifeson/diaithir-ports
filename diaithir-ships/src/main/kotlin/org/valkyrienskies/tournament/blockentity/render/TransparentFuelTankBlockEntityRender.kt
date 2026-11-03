package org.valkyrienskies.tournament.blockentity.render

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState
import net.minecraft.client.renderer.feature.ModelFeatureRenderer
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3
import org.valkyrienskies.tournament.TournamentModels
import org.valkyrienskies.tournament.blockentity.FuelTankBlockEntity
import org.valkyrienskies.tournament.util.extension.pose

class TransparentFuelTankBlockEntityRender : BlockEntityRenderer<FuelTankBlockEntity, TournamentRenderState> {

    override fun createRenderState() = TournamentRenderState()

    override fun extractRenderState(
        be: FuelTankBlockEntity, state: TournamentRenderState, partialTicks: Float, cameraPosition: Vec3,
        breakProgress: ModelFeatureRenderer.CrumblingOverlay?
    ) {
        BlockEntityRenderState.extractBase(be, state, breakProgress)
        state.fillLevel = be.wholeShipFillLevelSynced
        state.neighborsTransparent = be.neighborsTransparent.clone() as java.util.BitSet
    }

    override fun submit(state: TournamentRenderState, pose: PoseStack, collector: SubmitNodeCollector, camera: CameraRenderState) {
        if (state.fillLevel > 0.05f) {
            val byDir = List(Direction.entries.size) { index -> if (state.neighborsTransparent[index]) 0.0 else 0.01 }

            val x = byDir[Direction.WEST.ordinal]
            val y = byDir[Direction.DOWN.ordinal]
            val z = byDir[Direction.NORTH.ordinal]

            val nx = byDir[Direction.EAST.ordinal]
            val ny = byDir[Direction.UP.ordinal]
            val nz = byDir[Direction.SOUTH.ordinal]

            pose.pose {
                translate(x, y, z)
                scale(
                    1.0f - (x + nx).toFloat(),
                    (1.0f - (y + ny).toFloat()) * state.fillLevel,
                    1.0f - (z + nz).toFloat()
                )
                collector.submitTournamentModel(TournamentModels.SOLID_FUEL, pose, state, RenderTypes.cutoutMovingBlock())
            }
        }

        // the glass shell; drawn translucent, both faces (the model is rendered once, vanilla cull state is managed by the render type)
        collector.submitTournamentModel(TournamentModels.FUEL_TANK_FULL_TRANSPARENT, pose, state, RenderTypes.translucentMovingBlock())
    }
}
