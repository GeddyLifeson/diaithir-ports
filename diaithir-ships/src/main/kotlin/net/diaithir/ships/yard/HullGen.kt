package net.diaithir.ships.yard

import net.diaithir.yards.PartDef
import net.diaithir.yards.YardDef.Footprint
import kotlin.math.max
import kotlin.math.roundToInt

/** A block at an offset from the slipway controller. The hull runs along +z, its centre line at dx = 0. */
data class Cell(val dx: Int, val dy: Int, val dz: Int, val block: String)

/** Deterministic hull geometry for a yard footprint; stage `ghost` ids pick which part of the hull a stage lays down. */
object HullGen {
    fun deckLayer(def: Footprint) = max(1, def.h() - 2)

    private fun halfWidth(def: Footprint, i: Int, j: Int): Int {
        val t = if (def.l() <= 1) 0.0 else i / (def.l() - 1.0)
        val taper = if (t < 0.65) 1.0 else max(0.15, (1.0 - t) / 0.35)
        val lift = 0.5 + 0.5 * (j.toDouble() / deckLayer(def))
        return ((def.w() / 2.0) * taper * lift).roundToInt()
    }

    /** Hull cells for a ghost id (keel, ribs, planking, deck); fittings are placed per installed part instead. */
    fun cells(def: Footprint, ghost: String): List<Cell> {
        val out = ArrayList<Cell>()
        val deck = deckLayer(def)
        for (i in 0 until def.l()) {
            val z = 1 + i
            when (ghost) {
                "keel" -> out.add(Cell(0, 0, z, "minecraft:spruce_log"))
                "ribs" -> if (i % 3 == 0) {
                    for (j in 0 until deck) {
                        val hw = halfWidth(def, i, j)
                        if (j == 0) for (x in -hw..hw) { if (x != 0) out.add(Cell(x, 0, z, "minecraft:spruce_planks")) }
                        else if (hw > 0) { out.add(Cell(-hw, j, z, "minecraft:spruce_planks")); out.add(Cell(hw, j, z, "minecraft:spruce_planks")) }
                    }
                }
                "planking" -> for (j in 0 until deck) {
                    val hw = halfWidth(def, i, j)
                    val ribRow = i % 3 == 0
                    if (j == 0) { if (!ribRow) for (x in -hw..hw) { if (x != 0) out.add(Cell(x, 0, z, "minecraft:oak_planks")) } }
                    else if (hw > 0 && !ribRow) { out.add(Cell(-hw, j, z, "minecraft:oak_planks")); out.add(Cell(hw, j, z, "minecraft:oak_planks")) }
                }
                "deck" -> { val hw = halfWidth(def, i, deck); for (x in -hw..hw) out.add(Cell(x, deck, z, "minecraft:oak_planks")) }
            }
        }
        return out.distinctBy { Triple(it.dx, it.dy, it.dz) }.sortedWith(compareBy({ it.dz }, { it.dy }, { it.dx }))
    }

    /** Where the k-th installed part of a kind sits, from its numeric extras anchor_x / anchor_y / anchor_row (0..1 along the hull). */
    fun fitting(def: Footprint, part: PartDef, k: Int): Cell? {
        val block = part.block().ifEmpty { return null }
        val ax = (part.extra()["anchor_x"] ?: 0.0).roundToInt()
        val ay = (part.extra()["anchor_y"] ?: 1.0).roundToInt().coerceIn(1, def.h() - 1)
        val row = (part.extra()["anchor_row"] ?: 0.5)
        val i = (row * (def.l() - 1)).roundToInt() + (k / 2) * 2
        val x = if (k % 2 == 0) ax else -ax
        return Cell(x, ay, 1 + i.coerceIn(0, def.l() - 1), block)
    }
}
