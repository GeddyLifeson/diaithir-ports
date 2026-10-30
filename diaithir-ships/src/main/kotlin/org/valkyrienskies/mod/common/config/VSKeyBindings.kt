package org.valkyrienskies.mod.common.config

import net.minecraft.client.KeyMapping
import net.minecraft.resources.Identifier
import org.lwjgl.glfw.GLFW
import java.util.function.Consumer
import java.util.function.Supplier

object VSKeyBindings {
    /** Must be registered with `RegisterKeyMappingsEvent.registerCategory(VSKeyBindings.CATEGORY)`. */
    @JvmField
    val CATEGORY = KeyMapping.Category(Identifier.fromNamespaceAndPath("valkyrienskies", "driving"))

    // TODO when making the addon utils for registering... this too
    private val toBeRegistered = mutableListOf<Consumer<Consumer<KeyMapping>>>()

    // val shipUp = register("key.valkyrienskies.ship_up", 32, CATEGORY)
    val shipDown = register("key.valkyrienskies.ship_down", GLFW.GLFW_KEY_V, CATEGORY)
    val shipCruise = register("key.valkyrienskies.ship_cruise", GLFW.GLFW_KEY_C, CATEGORY)

    // val shipForward = register("key.valkyrienskies.ship_forward", 87, CATEGORY)
    // val shipBack = register("key.valkyrienskies.ship_back", 83, CATEGORY)
    // val shipLeft = register("key.valkyrienskies.ship_left", 65, CATEGORY)
    // val shipRight = register("key.valkyrienskies.ship_right", 68, CATEGORY)

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
