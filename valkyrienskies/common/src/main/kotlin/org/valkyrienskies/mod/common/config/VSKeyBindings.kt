package org.valkyrienskies.mod.common.config

import net.minecraft.client.KeyMapping
import net.minecraft.resources.Identifier
import org.lwjgl.glfw.GLFW
import org.valkyrienskies.mod.common.ValkyrienSkiesMod
import java.util.function.Consumer
import java.util.function.Supplier

object VSKeyBindings {
    // TODO when making the addon utils for registering... this too
    private val toBeRegistered = mutableListOf<Consumer<Consumer<KeyMapping>>>()

    // 26.2: KeyMapping's category parameter is now a registered KeyMapping.Category (record
    // wrapping an Identifier), not a raw translation-key String.
    private val DRIVING_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(ValkyrienSkiesMod.MOD_ID, "driving"))

    // val shipUp = register("key.valkyrienskies.ship_up", 32, DRIVING_CATEGORY)
    val shipDown = register("key.valkyrienskies.ship_down", GLFW.GLFW_KEY_V, DRIVING_CATEGORY)
    val shipCruise = register("key.valkyrienskies.ship_cruise", GLFW.GLFW_KEY_C, DRIVING_CATEGORY)

    // val shipForward = register("key.valkyrienskies.ship_forward", 87, DRIVING_CATEGORY)
    // val shipBack = register("key.valkyrienskies.ship_back", 83, DRIVING_CATEGORY)
    // val shipLeft = register("key.valkyrienskies.ship_left", 65, DRIVING_CATEGORY)
    // val shipRight = register("key.valkyrienskies.ship_right", 68, DRIVING_CATEGORY)

    private fun register(name: String, keyCode: Int, category: KeyMapping.Category): Supplier<KeyMapping> =
        object : Supplier<KeyMapping>, Consumer<Consumer<KeyMapping>> {
            lateinit var registered: KeyMapping

            // If this throws error ur on server
            override fun get(): KeyMapping = registered
            override fun accept(t: Consumer<KeyMapping>) {
                registered = KeyMapping(name, keyCode, category)
                t.accept(registered)
            }
        }.apply { toBeRegistered.add(this) }

    fun clientSetup(registerar: Consumer<KeyMapping>) {
        toBeRegistered.forEach { it.accept(registerar) }
    }

    fun isKeyMappingFromVS2(keyMapping: KeyMapping): Boolean {
        return keyMapping.name.startsWith("key.valkyrienskies")
    }
}
