package org.valkyrienskies.eureka.registry

import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.block.entity.FuelValues
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.neoforge.server.ServerLifecycleHooks

@Suppress("INAPPLICABLE_JVM_NAME")
object FuelRegistry {
    /** Kept so the original `FuelRegistry.INSTANCE.get(stack)` call sites still read naturally. */
    val INSTANCE: FuelRegistry get() = this

    private fun fuelValues(): FuelValues? {
        ServerLifecycleHooks.getCurrentServer()?.let { return it.fuelValues() }
        return if (FMLEnvironment.getDist().isClient) ClientFuel.get() else null
    }

    fun get(stack: ItemStack): Int {
        val fv = fuelValues() ?: return 0
        return stack.getBurnTime(RecipeType.SMELTING, fv)
    }

    private object ClientFuel {
        fun get(): FuelValues? = net.minecraft.client.Minecraft.getInstance().level?.fuelValues()
    }
}
