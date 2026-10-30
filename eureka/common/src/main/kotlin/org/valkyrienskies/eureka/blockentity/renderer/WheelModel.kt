package org.valkyrienskies.eureka.blockentity.renderer

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.texture.OverlayTexture
import org.valkyrienskies.eureka.block.IWoodType
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function

/**
 * Holds the per-wood-type wheel models and submits them through the 26.2 render pipeline.
 *
 * 26.2 rewrite: BakedModel is gone (block models are now [BlockStateModelPart]s baked through the
 * NeoForge standalone-model system), MultiBufferSource/RenderType.cutout/tesselateWithoutAO are
 * gone (models are submitted via [SubmitNodeCollector.submitBlockModel]), and the old
 * StateHolder-per-wood-type hack is unnecessary - a plain lazy map does the job. The platform
 * entry point supplies the model lookup via [setModelGetter] once baking completes.
 */
object WheelModels {
    private var getter: (IWoodType) -> BlockStateModelPart =
        { throw IllegalStateException("Wheel model getter not set") }

    private val models = ConcurrentHashMap<IWoodType, BlockStateModelPart>()

    fun setModelGetter(getter: Function<IWoodType, BlockStateModelPart>) {
        this.getter = getter::apply
        // Models are re-baked on resource reload; drop stale entries.
        models.clear()
    }

    fun render(
        poseStack: PoseStack,
        woodType: IWoodType,
        lightCoords: Int,
        collector: SubmitNodeCollector,
    ) {
        val model = models.getOrPut(woodType) { getter(woodType) }

        poseStack.pushPose()
        // Model isn't centered calculated and need to use 0.625 on y and z 0.25
        poseStack.translate(-0.5, -0.625, -0.25)
        collector.submitBlockModel(
            poseStack,
            RenderTypes.cutoutMovingBlock(),
            listOf(model),
            IntArray(0), // no tinted quads in the wheel models
            lightCoords,
            OverlayTexture.NO_OVERLAY,
            -1, // white tint color
        )
        poseStack.popPose()
    }
}
