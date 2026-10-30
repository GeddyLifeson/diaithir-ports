package org.valkyrienskies.eureka.blockentity.renderer

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart
import net.minecraft.client.resources.model.ModelDebugName
import net.minecraft.resources.Identifier
import net.neoforged.neoforge.client.event.ModelEvent
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey
import org.valkyrienskies.eureka.EurekaMod
import org.valkyrienskies.eureka.block.IWoodType
import org.valkyrienskies.eureka.block.WoodType

/** Standalone (non block-bound) baked models of the ship helm wheel, one per wood type. */
object WheelModels {
    private val keys: Map<WoodType, StandaloneModelKey<BlockStateModelPart>> = WoodType.entries.associateWith { wood ->
        StandaloneModelKey(ModelDebugName { "vs_eureka:" + wood.serializedName.lowercase() + "_ship_helm_wheel" })
    }

    fun register(event: ModelEvent.RegisterStandalone) {
        for ((wood, key) in keys) {
            event.register(
                key,
                SimpleUnbakedStandaloneModel.simpleModelWrapper(
                    Identifier.fromNamespaceAndPath(EurekaMod.MOD_ID, "block/" + wood.serializedName.lowercase() + "_ship_helm_wheel")
                )
            )
        }
    }

    fun get(type: IWoodType): BlockStateModelPart? {
        val key = keys[type as? WoodType] ?: return null
        return Minecraft.getInstance().modelManager.getStandaloneModel(key)
    }
}
