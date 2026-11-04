package net.diaithir.ships.yard

import net.diaithir.yards.YardRegistry
import net.minecraft.resources.Identifier
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.AddServerReloadListenersEvent
import org.valkyrienskies.mod.common.assembly.ShipAssembler
import org.valkyrienskies.mod.common.shipObjectWorld
import org.valkyrienskies.mod.common.dimensionId

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
            // apply the summed part mass to the VS2 ship's inertia (block masses are replaced by the part masses)
            if (stats.massKg() > 0) applyMass(level, ship, stats.massKg())
            // apply the summed part stats to the new VS2 ship
            val c = YardShipControl()
            c.yardId = be.yardId; c.statMassKg = stats.massKg(); c.powerW = stats.powerW(); c.thrustN = stats.thrustN(); c.buoyancyN = stats.buoyancyN()
            be.build?.installed()?.firstNotNullOfOrNull { ArcaneFuel.GATED[it.item()] }?.let { c.engineId = it; c.engineEfficiency = ArcaneFuel.efficiency(it) }
            (level.shipObjectWorld.loadedShips.getById(ship.id) as? org.valkyrienskies.core.api.ships.LoadedServerShip)
                ?.let { it.setAttachment(c) } ?: YardShipControl.pending.put(ship.id, c)
            ship
        }
    } }

    /**
     * Sets the ship's total mass to [target] kg by scaling every block's mass by the same factor (ShipInertiaDataImpl.onSetBlock via
     * reflection), so the centre of mass stays where the hull's blocks put it. Any rounding remainder goes in at the centre of mass.
     */
    fun applyMass(level: net.minecraft.server.level.ServerLevel, ship: org.valkyrienskies.core.api.ships.ServerShip, target: Double) {
        try {
            val inertia = ship.inertiaData
            val before = inertia.shipMass
            if (before <= 0.0) return
            val ratio = target / before
            val m = inertia.javaClass.getMethod("onSetBlock", Int::class.java, Int::class.java, Int::class.java, Double::class.java, Double::class.java)
            val bb = ship.shipAABB ?: return
            val pos = net.minecraft.core.BlockPos.MutableBlockPos()
            for (x in bb.minX()..bb.maxX()) for (y in bb.minY()..bb.maxY()) for (z in bb.minZ()..bb.maxZ()) {
                val state = level.getBlockState(pos.set(x, y, z))
                if (state.isAir) continue
                val mass = org.valkyrienskies.mod.common.BlockStateInfo.get(state)?.first ?: continue
                m.invoke(inertia, x, y, z, mass, mass * ratio)
            }
            val rest = target - inertia.shipMass
            if (Math.abs(rest) > 1e-6) {
                val com = inertia.centerOfMassInShip
                m.invoke(inertia, Math.floor(com.x()).toInt(), Math.floor(com.y()).toInt(), Math.floor(com.z()).toInt(), if (rest < 0) -rest else 0.0, if (rest > 0) rest else 0.0)
            }
            org.slf4j.LoggerFactory.getLogger("vs_yard").info("applyMass: {} -> {} com={}", before, inertia.shipMass, inertia.centerOfMassInShip)
        } catch (e: Throwable) { org.slf4j.LoggerFactory.getLogger("vs_yard").warn("cannot apply part mass: {}", e.toString()) }
    }

    /** Per game tick: throttle follows the seated player's forward input (Eureka helm). */
    private fun driveThrottle(server: net.minecraft.server.MinecraftServer) {
        for (level in server.allLevels) for (ship in level.shipObjectWorld.loadedShips) {
            if (ship.chunkClaimDimension != level.dimensionId) continue
            val c = ship.getAttachment(YardShipControl::class.java) ?: continue
            val bb = ship.worldAABB
            val cx = Math.floor((bb.minX() + bb.maxX()) / 2).toInt(); val cz = Math.floor((bb.minZ() + bb.maxZ()) / 2).toInt()
            val y0 = Math.floor(bb.minY()).toInt(); val y1 = Math.ceil(bb.maxY()).toInt()
            var wet = 0
            for (y in y0 until y1) if (!level.getFluidState(net.minecraft.core.BlockPos(cx, y, cz)).isEmpty) wet++
            c.cover = if (y1 > y0) wet.toDouble() / (y1 - y0) else 0.0
            c.throttle = ship.getAttachment(org.valkyrienskies.mod.api.SeatedControllingPlayer::class.java)?.forwardImpulse?.toDouble()?.coerceIn(0.0, 1.0) ?: 0.0
            if (c.engineId.isNotEmpty() && c.fuelStored > 0.0) {
                val t = maxOf(c.throttle, c.manualThrottle).coerceIn(0.0, 1.0)
                if (t > 0) c.fuelStored = maxOf(0.0, c.fuelStored - ArcaneFuel.burnPerTick(c.fuelId, c.powerW * t, c.engineEfficiency))
            }
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
