package org.valkyrienskies.tournament

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart
import net.minecraft.client.resources.model.ModelDebugName
import net.minecraft.resources.Identifier
import net.neoforged.neoforge.client.event.ModelEvent
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey

/** Standalone (not block-bound) models used by the block entity renderers. */
object TournamentModels {
    @JvmField val MODELS = mutableMapOf<Identifier, Model>()

    class Model(
        @JvmField val resourceLocation: Identifier,
    ) {
        val key: StandaloneModelKey<BlockStateModelPart> =
            StandaloneModelKey(ModelDebugName { "vs_tournament:" + resourceLocation.path })

        /** The baked model, or null if the resource reload has not finished / the model failed to load. */
        fun part(): BlockStateModelPart? = Minecraft.getInstance().modelManager.getStandaloneModel(key)
    }

    private fun model(name: String): Model {
        val rl = Identifier.fromNamespaceAndPath(TournamentMod.MOD_ID, name)
        if (rl in MODELS)
            error("Model $name already registered !")
        val model = Model(rl)
        MODELS[rl] = model
        return model
    }

    @JvmField val PROP_BIG = model("block/prop_big_prop")
    @JvmField val PROP_SMALL = model("block/prop_small_prop")
    @JvmField val SOLID_FUEL = model("block/solid_fuel")
    @JvmField val ROTATOR_ROTARY = model("block/rotator_rotary")
    @JvmField val FUEL_TANK_FULL_TRANSPARENT = model("block/fuel_tank_full_transparent")

    @JvmStatic
    fun register(event: ModelEvent.RegisterStandalone) {
        MODELS.values.forEach { event.register(it.key, SimpleUnbakedStandaloneModel.simpleModelWrapper(it.resourceLocation)) }
    }
}
