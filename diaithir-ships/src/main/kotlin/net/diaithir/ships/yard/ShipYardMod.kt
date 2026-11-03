package net.diaithir.ships.yard

import net.diaithir.yards.YardRegistry
import net.minecraft.resources.Identifier
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.AddServerReloadListenersEvent
import org.valkyrienskies.mod.common.assembly.ShipAssembler
import org.valkyrienskies.mod.common.shipObjectWorld

/** Ship side of the Assembly Yard API (net.diaithir.yards). Registers under namespace vs_yard. */
object ShipYardMod {
    const val MOD_ID = "vs_yard"

    /** RolloutHook: lays the hull blocks into a VS2 ship through ShipAssembler. */
    val SHIP_HOOK: YardRegistry.RolloutHook by lazy { YardRegistry.RolloutHook { level, pos, _, stats, _ ->
        level as net.minecraft.server.level.ServerLevel; pos as net.minecraft.core.BlockPos
        val be = level.getBlockEntity(pos) as SlipwayBlockEntity
        val blocks = be.hullBlocks()
        if (blocks.isEmpty()) null else {
            val ship = ShipAssembler.assembleToShipFull(level, blocks).ship
            // apply the summed part stats to the new VS2 ship
            val c = YardShipControl()
            c.yardId = be.yardId; c.statMassKg = stats.massKg(); c.powerW = stats.powerW(); c.thrustN = stats.thrustN(); c.buoyancyN = stats.buoyancyN()
            (level.shipObjectWorld.loadedShips.getById(ship.id) as? org.valkyrienskies.core.api.ships.LoadedServerShip)
                ?.let { it.setAttachment(c) } ?: YardShipControl.pending.put(ship.id, c)
            ship
        }
    } }

    /** Per game tick: throttle follows the seated player's forward input (Eureka helm). */
    private fun driveThrottle(server: net.minecraft.server.MinecraftServer) {
        for (level in server.allLevels) for (ship in level.shipObjectWorld.loadedShips) {
            val c = ship.getAttachment(YardShipControl::class.java) ?: continue
            c.throttle = ship.getAttachment(org.valkyrienskies.mod.api.SeatedControllingPlayer::class.java)?.forwardImpulse?.toDouble()?.coerceIn(0.0, 1.0) ?: 0.0
        }
    }

    var apiPresent = false

    @JvmStatic
    fun init(bus: IEventBus) {
        // the shared API (net.diaithir.yards) lives in diaithir-carts; without it the yard blocks still register but nothing is hooked up
        apiPresent = try { Class.forName("net.diaithir.yards.YardRegistry"); true } catch (e: Throwable) { false }
        ShipYardBlocks.register()
        if (!apiPresent) return
        org.valkyrienskies.mod.common.ValkyrienSkiesMod.vsCore.registerAttachment(YardShipControl::class.java)
        org.valkyrienskies.mod.common.ValkyrienSkiesMod.vsCore.shipLoadEvent.on { e ->
            YardShipControl.pending.remove(e.ship.id)?.let { if (e.ship.getAttachment(YardShipControl::class.java) == null) e.ship.setAttachment(it) }
        }
        NeoForge.EVENT_BUS.addListener<net.neoforged.neoforge.event.tick.ServerTickEvent.Post> { driveThrottle(it.server) }
        NeoForge.EVENT_BUS.addListener<AddServerReloadListenersEvent> {
            it.addListener(Identifier.fromNamespaceAndPath(MOD_ID, "yard_data"), YardData.Listener())
        }
    }
}
