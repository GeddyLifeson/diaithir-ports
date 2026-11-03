package net.diaithir.ships.yard

import net.diaithir.yards.YardRegistry
import net.minecraft.resources.Identifier
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.AddServerReloadListenersEvent
import org.valkyrienskies.mod.common.assembly.ShipAssembler

/** Ship side of the Assembly Yard API (net.diaithir.yards). Registers under namespace vs_yard. */
object ShipYardMod {
    const val MOD_ID = "vs_yard"

    /** RolloutHook: lays the hull blocks into a VS2 ship through ShipAssembler. */
    val SHIP_HOOK: YardRegistry.RolloutHook by lazy { YardRegistry.RolloutHook { level, pos, _, _, _ ->
        level as net.minecraft.server.level.ServerLevel; pos as net.minecraft.core.BlockPos
        val be = level.getBlockEntity(pos) as SlipwayBlockEntity
        val blocks = be.hullBlocks()
        if (blocks.isEmpty()) null else ShipAssembler.assembleToShipFull(level, blocks).ship
    } }

    var apiPresent = false

    @JvmStatic
    fun init(bus: IEventBus) {
        // the shared API (net.diaithir.yards) lives in diaithir-carts; without it the yard blocks still register but nothing is hooked up
        apiPresent = try { Class.forName("net.diaithir.yards.YardRegistry"); true } catch (e: Throwable) { false }
        ShipYardBlocks.register()
        if (!apiPresent) return
        NeoForge.EVENT_BUS.addListener<AddServerReloadListenersEvent> {
            it.addListener(Identifier.fromNamespaceAndPath(MOD_ID, "yard_data"), YardData.Listener())
        }
    }
}
