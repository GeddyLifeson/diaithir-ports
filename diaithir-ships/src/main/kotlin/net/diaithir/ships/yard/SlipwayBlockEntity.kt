package net.diaithir.ships.yard

import net.diaithir.yards.BuildState
import net.diaithir.yards.PartDef
import net.diaithir.yards.Stats
import net.diaithir.yards.YardDef
import net.diaithir.yards.YardRegistry
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.slf4j.LoggerFactory
import kotlin.math.floor

/** Controller of one slipway / dry dock. Holds the BuildState, grows the hull blocks, and launches the finished ship. */
class SlipwayBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(ShipYardBlocks.SLIPWAY_BE.get(), pos, state) {
    var yardId: String = ""
    var build: BuildState? = null
    /** Hull cells this yard actually placed; only these go into the ship (an existing block in a hull cell is left alone). */
    private val placedBlocks = HashMap<BlockPos, String>()
    /** Saved data kept verbatim while the carts API is absent, so it survives until the API is back. */
    private var rawSaved: Triple<Map<String, String>, Int, Int>? = null
    val footprint: YardDef.Footprint get() = def?.footprint() ?: YardDef.Footprint(5, 10, 5)
    var lastStats: Stats? = null
    var lastShipId: Long = -1
    /** Every installed part with the stage it went into (BuildState keeps only an unordered list). */
    private val log = ArrayList<Pair<Int, PartDef>>()
    private val logger = LoggerFactory.getLogger("vs_yard")

    val def: YardDef? get() = YardRegistry.yard(yardId).orElse(null)

    fun chooseYard(id: String): Boolean {
        if (rawSaved != null) return false // a retained unresolved build is only dropped by discardSaved()
        val d = YardRegistry.yard(id).orElse(null) ?: return false
        yardId = id
        rawSaved = null
        build = BuildState.start(d); log.clear(); placedBlocks.clear(); setChanged(); return true
    }

    private fun inStage(stage: Int) = log.filter { it.first == stage }.map { it.second }

    private fun freeSlots(kind: String): Int {
        val b = build ?: return 0
        val st = def!!.stages().getOrNull(b.stageIndex()) ?: return 0
        return st.slots().filter { it.kind() == kind }.sumOf { it.count() } - inStage(b.stageIndex()).count { it.kind() == kind }
    }

    fun slotsFull(): Boolean {
        val b = build ?: return false
        val st = def!!.stages().getOrNull(b.stageIndex()) ?: return true
        return st.slots().all { freeSlots(it.kind()) <= 0 }
    }

    val holdsSavedBuild: Boolean get() = rawSaved != null

    /** Explicit player action: drops a retained build whose yard or parts are not loaded. */
    fun discardSaved() {
        rawSaved = null; build = null; log.clear(); placedBlocks.clear(); yardId = ""; setChanged()
    }

    /** Empty-hand use: picks a yard type when idle, launches when complete, else reports status. */
    fun onUse(sneak: Boolean): String {
        if (rawSaved != null) {
            if (!sneak) return "This slipway holds a saved build whose yard or parts are not loaded. Sneak-use with an empty hand to discard it."
            discardSaved()
            return "Saved build discarded."
        }
        val b = build
        if (b == null || (sneak && b.stageIndex() == 0 && log.isEmpty())) {
            val ids = YardData.yardIds()
            if (ids.isEmpty()) return "No yard types loaded."
            val next = ids[(ids.indexOf(yardId) + 1).mod(ids.size)]
            chooseYard(next)
            return "Yard type: $next ${siteCheck()?.let { " (site problem: $it)" } ?: ""}"
        }
        if (b.complete()) return launch()?.let { "Launched ship $it" } ?: "Launch failed (see log)"
        val st = def!!.stages()[b.stageIndex()]
        return "Stage ${b.stageIndex() + 1}/${def!!.stages().size} ${st.id()}: needs ${st.slots().joinToString { "${it.kind()} x${freeSlots(it.kind())}" }}, work ${(b.stageProgress() * 100).toInt()}%"
    }

    /** Null when the footprint suits the yard's site type, else a reason. */
    fun siteCheck(): String? {
        val lvl = level ?: return "no level"
        val d = def ?: return "no yard"
        if (!d.allowedIn(lvl.dimension().identifier().toString())) return "wrong dimension"
        for (i in 0 until footprint.l()) {
            val below = lvl.getBlockState(worldPos(Cell(0, -1, 1 + i, "")))
            val water = !below.fluidState.isEmpty
            when (d.site()) {
                YardDef.Site.WATER -> if (!water) return "slipway needs water along the centre line"
                YardDef.Site.GROUND -> if (water || !below.isSolid) return "dry dock needs solid ground along the centre line"
                else -> return "site ${d.site()} not supported by the ship yard"
            }
        }
        return null
    }

    private fun worldPos(c: Cell) = worldPosition.offset(c.dx, c.dy, c.dz)

    /** Install a part; heavy parts need a crane (lift_kg >= mass) within reach. Age gating is left to the Age thread (age passed as max). */
    fun install(part: PartDef): BuildState.InstallResult {
        val b = build ?: return BuildState.InstallResult.NO_SLOT
        val lift = ShipYardBlocks.craneAt(level!!, worldPosition).second
        val stage = b.stageIndex()
        val r = b.install(part, AgeLookup.current((level as ServerLevel).server), lift)
        if (r == BuildState.InstallResult.OK) { log.add(Pair(stage, part)); syncBlocks(); setChanged() }
        return r
    }

    /** Work for [ticks] at the in-reach crane speed; true when a stage finished. */
    fun work(ticks: Int): Boolean {
        val b = build ?: return false
        val lvl = level ?: return false
        val crane = ShipYardBlocks.craneAt(lvl, worldPosition).first
        val done = b.advance(ticks, crane)
        syncBlocks(); setChanged()
        return done
    }

    fun serverTick() {
        val b = build ?: return
        val lvl = level as? ServerLevel ?: return
        if (b.complete() || lvl.gameTime % 4L != 0L || !slotsFull()) return
        work(4)
    }

    /** Places finished stages' blocks, the current stage's blocks up to its progress, and installed fittings. Idempotent. */
    fun syncBlocks() {
        val b = build ?: return
        val lvl = level as? ServerLevel ?: return
        val d = def ?: return
        for ((idx, st) in d.stages().withIndex()) {
            if (st.ghost() == "fittings") {
                if (idx > b.stageIndex()) continue
                val seen = HashMap<String, Int>()
                for (p in inStage(idx)) { val k = seen.merge(p.kind(), 1, Int::plus)!! - 1; HullGen.fitting(footprint, p, k)?.let { put(lvl, it) } }
            } else {
                val upto = when { idx < b.stageIndex() -> 1.0; idx == b.stageIndex() -> if (slotsFull()) b.stageProgress() else 0.0; else -> 0.0 }
                val cells = HullGen.cells(footprint, st.ghost())
                val n = if (upto >= 1.0) cells.size else floor(cells.size * upto).toInt()
                for (i in 0 until n) put(lvl, cells[i])
            }
        }
    }

    private fun put(lvl: ServerLevel, c: Cell) {
        val pos = worldPos(c)
        val cur = lvl.getBlockState(pos)
        if (!cur.isAir && cur.block !is LiquidBlock) { if (placedBlocks[pos] == null) logger.warn("vs_yard: hull cell {} obstructed by {}", pos, BuiltInRegistries.BLOCK.getKey(cur.block)); return }
        val blk = BuiltInRegistries.BLOCK.getValue(Identifier.parse(c.block))
        if (blk === Blocks.AIR) return
        if (lvl.setBlock(pos, blk.defaultBlockState(), 3)) { placedBlocks[pos.immutable()] = c.block; setChanged() }
    }

    private fun allCells(): List<Cell> {
        val d = def ?: return emptyList()
        val out = ArrayList<Cell>()
        for ((idx, st) in d.stages().withIndex()) {
            if (st.ghost() == "fittings") { val seen = HashMap<String, Int>(); for (p in inStage(idx)) { val k = seen.merge(p.kind(), 1, Int::plus)!! - 1; HullGen.fitting(footprint, p, k)?.let { out.add(it) } } }
            else out.addAll(HullGen.cells(footprint, st.ghost()))
        }
        return out
    }

    /** True while the block at [pos] is still the one this yard placed there (a replaced block is not ours to move into the ship). */
    private fun isPlaced(lvl: net.minecraft.world.level.Level, pos: BlockPos): Boolean {
        val id = placedBlocks[pos] ?: return false
        return BuiltInRegistries.BLOCK.getKey(lvl.getBlockState(pos).block).toString() == id
    }

    /** Hull cells that are not placed: obstructed by another block, replaced after placing, or not laid yet. */
    fun missingCells(): List<BlockPos> {
        val lvl = level ?: return emptyList()
        return allCells().filter { BuiltInRegistries.BLOCK.getValue(Identifier.parse(it.block)) !== Blocks.AIR }.map { worldPos(it) }.filter { !isPlaced(lvl, it) }
    }

    /** The finished hull's blocks that exist in the world, for rollout. */
    fun hullBlocks(): Set<BlockPos> {
        val lvl = level ?: return emptySet()
        return allCells().map { worldPos(it) }.filter { isPlaced(lvl, it) }.filter { val s = lvl.getBlockState(it); !s.isAir && s.block !is LiquidBlock }.toSet()
    }

    /** Roll out through the registered RolloutHook (ShipAssembler -> VS2 ship). Returns the ship id. */
    fun launch(): Long? {
        val b = build ?: return null
        val lvl = level as? ServerLevel ?: return null
        if (!b.complete()) return null
        syncBlocks() // lays cells whose obstruction was cleared after completion
        missingCells().let { if (it.isNotEmpty()) { logger.warn("vs_yard: launch refused, {} hull cell(s) not placed (obstructed or replaced), first at {}", it.size, it.first()); return null } }
        siteCheck()?.let { logger.warn("vs_yard: launching despite site check: {}", it) }
        val ship = YardRegistry.finish(yardId, lvl, worldPosition, b) as? org.valkyrienskies.core.api.ships.ServerShip ?: return null
        lastStats = b.stats(); lastShipId = ship.id
        chooseYard(yardId)
        return ship.id
    }

    override fun saveAdditional(output: ValueOutput) {
        super.saveAdditional(output)
        rawSaved?.let { r -> r.first.forEach { (k, v) -> output.putString(k, v) }; output.putInt("Stage", r.second); output.putInt("Ticks", r.third); return } // carts API absent: write back what was loaded
        output.putString("Yard", yardId)
        output.putString("Placed", placedBlocks.entries.joinToString(";") { "${it.key.x},${it.key.y},${it.key.z},${it.value}" })
        val b = build ?: return
        output.putInt("Stage", b.stageIndex()); output.putInt("Ticks", b.progressTicks())
        output.putString("Log", log.joinToString(",") { "${it.first}:${it.second.item()}" })
    }

    override fun loadAdditional(input: ValueInput) {
        super.loadAdditional(input)
        if (!ShipYardMod.apiPresent) {
            rawSaved = Triple(listOf("Yard", "Placed", "Log").associateWith { input.getStringOr(it, "") }, input.getIntOr("Stage", 0), input.getIntOr("Ticks", 0))
            return
        }
        rawSaved = null
        yardId = input.getStringOr("Yard", "")
        placedBlocks.clear()
        input.getStringOr("Placed", "").split(";").forEach { e ->
            val p = e.split(",", limit = 4)
            if (p.size == 4) { val x = p[0].toIntOrNull(); val y = p[1].toIntOrNull(); val z = p[2].toIntOrNull(); if (x != null && y != null && z != null) placedBlocks[BlockPos(x, y, z)] = p[3] }
        }
        val retain = { rawSaved = Triple(listOf("Yard", "Placed", "Log").associateWith { input.getStringOr(it, "") }, input.getIntOr("Stage", 0), input.getIntOr("Ticks", 0)); build = null }
        val d = def
        if (d == null) { retain(); return } // yard definition unavailable: keep the saved fields so the next save does not drop progress
        val stage = input.getIntOr("Stage", 0)
        val entries = input.getStringOr("Log", "").split(",").filter { it.isNotEmpty() }
        val parts = entries.mapNotNull { e ->
            val (st, item) = e.split(":", limit = 2).let { Pair(it[0].toInt(), it[1]) }
            YardRegistry.part(item).orElse(null)?.let { Pair(st, it) }
        }
        if (parts.size != entries.size) { retain(); return } // a saved part is unresolved: hold the build (no work, no launch) and keep the raw data until it resolves on a later load
        log.clear(); log.addAll(parts)
        val b = BuildState.restore(d, stage, input.getIntOr("Ticks", 0), parts.filter { it.first < stage }.map { it.second }, parts.filter { it.first >= stage }.map { it.second })
        build = b
    }
}
