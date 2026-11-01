package org.valkyrienskies.eureka.forge

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.resources.model.ModelDebugName
import net.minecraft.resources.Identifier
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import net.neoforged.neoforge.client.event.ModelEvent
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey
import org.valkyrienskies.eureka.EurekaBlockEntities
import org.valkyrienskies.eureka.EurekaMod
import org.valkyrienskies.eureka.block.IWoodType
import org.valkyrienskies.eureka.block.WoodType
import org.valkyrienskies.eureka.blockentity.renderer.ShipHelmBlockEntityRenderer
import org.valkyrienskies.eureka.blockentity.renderer.WheelModels
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

// 26.2/NeoForge: extra ("standalone") models are no longer registered by bare Identifier via
// ModelEvent.RegisterAdditional + looked up in bakedTopLevelModels - each model gets a typed
// StandaloneModelKey registered in ModelEvent.RegisterStandalone and resolved through
// ModelManager.getStandaloneModel once baking completes. BakedModel is gone; the wheel models
// bake to BlockStateModelParts (see WheelModels).
object EurekaModForgeClient {
    private var happendClientSetup = false

    private val wheelModelKeys: Map<WoodType, StandaloneModelKey<BlockStateModelPart>> =
        WoodType.entries.associateWith { woodType ->
            StandaloneModelKey(ModelDebugName { "eureka wheel model ${woodType.serializedName}" })
        }

    private fun wheelModelId(woodType: IWoodType): Identifier = Identifier.fromNamespaceAndPath(
        EurekaMod.MOD_ID, "block/" + woodType.serializedName.lowercase() + "_ship_helm_wheel"
    )

    fun registerClient() {
        MOD_BUS.addListener { event: ModelEvent.BakingCompleted ->
            clientSetup(event)
        }
        MOD_BUS.addListener { event: ModelEvent.RegisterStandalone ->
            onModelRegistry(event)
        }
        MOD_BUS.addListener { event: EntityRenderersEvent.RegisterRenderers ->
            entityRenderers(event)
        }
    }

    fun clientSetup(event: ModelEvent.BakingCompleted) {
        if (!happendClientSetup) {
            happendClientSetup = true
            EurekaMod.initClient()
        }
        // Re-point the wheel models at the freshly baked standalone models (fires on every
        // resource reload; WheelModels drops its cache internally).
        val modelManager = event.modelManager
        WheelModels.setModelGetter { woodType: IWoodType ->
            wheelModelKeys[woodType]?.let { modelManager.getStandaloneModel(it) }
                ?: throw IllegalStateException("Wheel model missing for wood type ${woodType.serializedName}")
        }
    }

    fun entityRenderers(event: EntityRenderersEvent.RegisterRenderers) {
        event.registerBlockEntityRenderer(EurekaBlockEntities.SHIP_HELM.get()) { ctx: BlockEntityRendererProvider.Context ->
            ShipHelmBlockEntityRenderer(ctx)
        }
    }

    fun onModelRegistry(event: ModelEvent.RegisterStandalone) {
        for ((woodType, key) in wheelModelKeys) {
            event.register(key, SimpleUnbakedStandaloneModel.simpleModelWrapper(wheelModelId(woodType)))
        }
    }
}
