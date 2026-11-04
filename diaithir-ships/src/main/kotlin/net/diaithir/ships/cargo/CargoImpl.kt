package net.diaithir.ships.cargo

import net.diaithir.freight.BayData
import net.diaithir.freight.CargoBay
import net.diaithir.freight.CargoCarrier
import net.diaithir.freight.Carriers
import net.diaithir.freight.Freight
import net.diaithir.freight.LoadType
import net.diaithir.freight.Loads
import net.diaithir.ships.yard.YardShipControl
import net.minecraft.core.BlockPos
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.TagParser
import net.minecraft.network.chat.Component
import net.minecraft.resources.RegistryOps
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import net.neoforged.neoforge.network.PacketDistributor
import org.joml.Vector3d
import org.slf4j.LoggerFactory
import org.valkyrienskies.core.api.ships.LoadedServerShip
import org.valkyrienskies.eureka.ship.EurekaShipControl
import org.valkyrienskies.mod.common.ValkyrienSkiesMod
import org.valkyrienskies.mod.common.dimensionId
import org.valkyrienskies.mod.common.getShipObjectManagingPos
import org.valkyrienskies.mod.common.shipObjectWorld

/**
 * Ships as cargo carriers for the containers API (net.diaithir.freight, in diaithir-carts). Touched only when that API is present
 * ([ShipCargoMod] checks first). A carrier ship is a yard ship or a Eureka ship; its deck is a [CargoBay] laid over the hull's footprint one cell
 * above the hull's highest block, saved in the ship attachment [ShipCargo] and mirrored to clients for drawing.
 */
object CargoImpl {
    private val log = LoggerFactory.getLogger("vs_yard")
    const val DECK_HEIGHT = 4
    const val MAX_SPAN = 48

    /** A ship's deck as a [CargoCarrier]. Stateless: the bay is rebuilt from the attachment on every use and written back by [changed]. */
    class ShipCarrier(val level: ServerLevel, val ship: LoadedServerShip, val data: ShipCargo) : CargoCarrier {
        private val bay = load(level, data)
        override fun bay(): CargoBay = bay

        override fun columnAt(world: Vec3): IntArray? {
            val p = ship.transform.worldToShip.transformPosition(Vector3d(world.x, world.y, world.z))
            return columnAtShip(p.x, p.z)
        }

        fun columnAtShip(sx: Double, sz: Double): IntArray? {
            val x = Math.floor(sx).toInt() - data.ox
            val z = Math.floor(sz).toInt() - data.oz
            return if (x in 0 until data.w && z in 0 until data.l) intArrayOf(x, z) else null
        }

        override fun changed() {
            save(level, data, bay)
            broadcast(level, ship, data, bay)
        }
    }

    fun init(bus: IEventBus) {
        val devOnly = !net.neoforged.fml.ModList.get().isLoaded("diaithircarts")
        if (devOnly) {
            if (System.getProperty("vs.cargodev") != "true") return // API classes present but the carts mod is not: nothing registers them
            Freight.init(bus) // dev/test runs: the API is compiled in, so register its blocks and items here
        }
        ValkyrienSkiesMod.vsCore.registerAttachment(ShipCargo::class.java)
        Carriers.SOURCES.add { level, box ->
            if (level is ServerLevel) level.shipObjectWorld.loadedShips.firstNotNullOfOrNull { s ->
                if (s.chunkClaimDimension == level.dimensionId && s.worldAABB.intersectsAABB(org.joml.primitives.AABBd(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ))) carrierFor(level, s) else null
            } else null
        }
        NeoForge.EVENT_BUS.addListener<PlayerInteractEvent.RightClickBlock> { handUse(it) }
        NeoForge.EVENT_BUS.addListener<PlayerEvent.PlayerLoggedInEvent> { e ->
            (e.entity as? ServerPlayer)?.let { p -> syncAll(p.level() as ServerLevel, p) }
        }
        NeoForge.EVENT_BUS.addListener<ServerTickEvent.Post> { e ->
            if (e.server.tickCount % 40 == 0) for (l in e.server.allLevels) syncAll(l, null)
        }
        log.info("ship cargo: carriers registered with the containers API")
    }

    /** The carrier for a ship, creating its deck on first use; null when the ship is not a yard or Eureka ship. */
    fun carrierFor(level: ServerLevel, ship: LoadedServerShip): ShipCarrier? {
        var data = ship.getAttachment(ShipCargo::class.java)
        if (data == null) {
            data = ShipCargo()
            ship.setAttachment(data)
        }
        if (!data.ready) {
            val kg = payloadKg(ship)
            val bb = ship.shipAABB
            if (kg <= 0.0 || bb == null) return null
            // the deck starts one above the highest block actually in the hull (the ship's AABB can be looser than its blocks)
            var top = bb.minY() - 1
            val mp = BlockPos.MutableBlockPos()
            for (x in bb.minX()..bb.maxX()) for (z in bb.minZ()..bb.maxZ()) for (y in bb.maxY() downTo maxOf(top + 1, bb.minY())) {
                if (!level.getBlockState(mp.set(x, y, z)).isAir) { top = y; break }
            }
            data.ox = bb.minX(); data.oz = bb.minZ(); data.oy = top + 1
            data.w = (bb.maxX() - bb.minX() + 1).coerceIn(1, MAX_SPAN); data.l = (bb.maxZ() - bb.minZ() + 1).coerceIn(1, MAX_SPAN); data.h = DECK_HEIGHT
            data.maxKg = kg
            data.ready = true
        }
        return ShipCarrier(level, ship, data)
    }

    /** Most cargo mass the hull may carry: the lift left over after its own mass (yard ships) or what its floaters and balloons give (Eureka ships). */
    fun payloadKg(ship: LoadedServerShip): Double {
        ship.getAttachment(YardShipControl::class.java)?.let { return maxOf(0.0, it.buoyancyN / 9.81 - it.statMassKg) }
        ship.getAttachment(EurekaShipControl::class.java)?.let { return (it.floaters / 15) * 400.0 + it.balloons * 300.0 }
        return 0.0
    }

    private fun ops(level: ServerLevel) = RegistryOps.create(NbtOps.INSTANCE, level.registryAccess())

    private fun load(level: ServerLevel, d: ShipCargo): CargoBay {
        val bay = CargoBay(maxOf(d.w, 1), maxOf(d.h, 1), maxOf(d.l, 1), d.maxKg)
        if (d.saved.isEmpty()) return bay
        try {
            val tag = TagParser.parseCompoundFully(d.saved)
            val top = BayData.CODEC.parse(ops(level), tag.get("loads")).result().orElse(BayData.EMPTY)
            val insides = tag.getListOrEmpty("inside")
            top.loads().forEachIndexed { i, e ->
                val t = LoadType.byId(e.type()) ?: return@forEachIndexed
                val p = bay.restore(t, e.turned(), e.x(), e.y(), e.z(), 0.0) ?: return@forEachIndexed
                if (t.container()) {
                    val inner = insides.get(i)?.let { BayData.CODEC.parse(ops(level), it).result().orElse(BayData.EMPTY) } ?: BayData.EMPTY
                    val ib = inner.toBay(t)
                    for (q in ib.loads()) { val r = p.inside.restore(q.type, q.turned, q.x, q.y, q.z, q.payloadKg); if (r != null) r.data = q.data }
                } else {
                    val items = NonNullList.withSize(t.itemSlots(), ItemStack.EMPTY)
                    e.items().copyInto(items)
                    p.data = items; p.payloadKg = Loads.kg(items)
                }
            }
        } catch (ex: Exception) { log.warn("ship cargo: cannot read saved loads: {}", ex.toString()) }
        return bay
    }

    private fun save(level: ServerLevel, d: ShipCargo, bay: CargoBay) {
        val loads = bay.loads()
        val top = BayData.CODEC.encodeStart(ops(level), BayData.of(bay)).result().orElse(null) ?: return
        val insides = ListTag()
        for (p in loads) {
            insides.add(if (p.inside != null) BayData.CODEC.encodeStart(ops(level), BayData.of(p.inside)).result().orElse(ListTag()) else ListTag())
        }
        val tag = CompoundTag()
        tag.put("loads", top); tag.put("inside", insides)
        d.saved = tag.toString()
    }

    private fun send(p: ServerPlayer, pkt: ShipCargoPacket) {
        if (p.connection.hasChannel(pkt.type())) PacketDistributor.sendToPlayer(p, pkt)
    }

    private fun packetFor(ship: LoadedServerShip, d: ShipCargo, bay: CargoBay) = ShipCargoPacket(ship.id, d.ox, d.oy, d.oz, d.w, d.h, d.l,
        bay.loads().map { ShipCargoPacket.Load(it.type.id(), it.x, it.y, it.z, it.turned) })

    private fun broadcast(level: ServerLevel, ship: LoadedServerShip, d: ShipCargo, bay: CargoBay) {
        val pkt = packetFor(ship, d, bay)
        for (p in level.players()) send(p, pkt)
    }

    /** Sends every carrier ship's manifest in a level, to one player or everyone in it. */
    fun syncAll(level: ServerLevel, only: ServerPlayer?) {
        val players = only?.let { listOf(it) } ?: level.players()
        if (players.isEmpty()) return
        for (ship in level.shipObjectWorld.loadedShips) {
            if (ship.chunkClaimDimension != level.dimensionId) continue
            val c = carrierFor(level, ship) ?: continue
            val pkt = packetFor(ship, c.data, c.bay())
            for (p in players) send(p, pkt)
        }
    }

    /** Client handler: turns the manifest into block states to draw. */
    fun receive(p: ShipCargoPacket) {
        val draws = p.loads.mapNotNull { l ->
            val t = LoadType.byId(l.type) ?: return@mapNotNull null
            val w = if (l.turned) t.l() else t.w(); val ln = if (l.turned) t.w() else t.l()
            val state = when {
                t.looseItems() -> Freight.LOADS[t.id()]?.get()?.defaultBlockState()
                t.kind() == LoadType.Kind.TANK -> Freight.TANK_VESSEL.get().defaultBlockState()
                else -> Freight.WALL.get().defaultBlockState()
            } ?: return@mapNotNull null
            ClientCargo.Draw(state, l.x, l.y, l.z, w, t.h(), ln)
        }
        ClientCargo.ships[p.shipId] = ClientCargo.Manifest(p.ox, p.oy, p.oz, p.w, p.h, p.l, draws)
    }

    /** Hand loading: a load item on a ship's deck puts it on the aimed column; sneaking with an empty hand takes the top load back. */
    private fun handUse(e: PlayerInteractEvent.RightClickBlock) {
        val level = e.level
        val pos: BlockPos = e.pos
        val ship = level.getShipObjectManagingPos(pos) ?: return
        val held = e.itemStack
        val put = Carriers.loadable(held)
        val take = held.isEmpty && e.entity.isShiftKeyDown && e.hand == net.minecraft.world.InteractionHand.MAIN_HAND
        if (!put && !take) return
        if (level.isClientSide) {
            // keep the client from placing the load block or opening a screen; the server does the work
            if (ClientCargo.isCarrier(ship.id) && (put || ClientCargo.ships[ship.id]!!.loads.isNotEmpty())) {
                e.cancellationResult = InteractionResult.SUCCESS; e.isCanceled = true
            }
            return
        }
        if (ship !is LoadedServerShip || level !is ServerLevel) return
        val carrier = carrierFor(level, ship) ?: return
        val loc = e.hitVec.location
        // the hit point is in ship space on a ship's blocks; map it from the world only when it is not
        val bb = ship.shipAABB
        val inShip = bb != null && loc.x >= bb.minX() - 2 && loc.x <= bb.maxX() + 3 && loc.z >= bb.minZ() - 2 && loc.z <= bb.maxZ() + 3
        val col = (if (inShip) carrier.columnAtShip(loc.x, loc.z) else carrier.columnAt(loc))
        e.cancellationResult = InteractionResult.SUCCESS; e.isCanceled = true
        if (col == null) { e.entity.sendOverlayMessage(Component.literal("Not on the deck")); return }
        if (put) {
            val r = Carriers.put(carrier, held, col[0], col[1])
            if (r != CargoBay.Result.OK) e.entity.sendOverlayMessage(Component.literal("Cannot load there: " + r.name.lowercase().replace('_', ' ')))
        } else {
            val out = Carriers.takeTop(carrier, col[0], col[1])
            if (!out.isEmpty && !e.entity.inventory.add(out)) e.entity.drop(out, false)
        }
    }
}
