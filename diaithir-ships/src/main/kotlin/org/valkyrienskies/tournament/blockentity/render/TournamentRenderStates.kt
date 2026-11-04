package org.valkyrienskies.tournament.blockentity.render

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import org.valkyrienskies.tournament.TournamentModels

class TournamentRenderState : BlockEntityRenderState() {
    var facing: net.minecraft.core.Direction = net.minecraft.core.Direction.UP
    var rotationDeg = 0.0
    var fillLevel = 0f
    var neighborsTransparent: java.util.BitSet = java.util.BitSet(6)
}

fun SubmitNodeCollector.submitTournamentModel(
    model: TournamentModels.Model,
    pose: PoseStack,
    state: BlockEntityRenderState,
    renderType: RenderType
) {
    val part = model.part() ?: return
    submitBlockModel(pose, renderType, listOf(part), IntArray(0), state.lightCoords, OverlayTexture.NO_OVERLAY, 0)
}
