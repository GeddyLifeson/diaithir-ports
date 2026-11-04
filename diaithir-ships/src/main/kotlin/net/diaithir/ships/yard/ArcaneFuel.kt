package net.diaithir.ships.yard

import net.diaithir.units.fuel.Fuels

/** Link to the shared fuel table (diaithir-units): which fuels an engine class accepts and how fast it burns them. */
object ArcaneFuel {
    /** Part item to engine class id in the fuel table. Only the arcane motor is fuel-gated; the steam engine keeps Eureka's item burning. */
    val GATED = mapOf("vs_yard:arcane_motor" to "arcane_motor")

    fun accepts(engineId: String, fuelId: String): Boolean = try { Fuels.engine(engineId)?.accepts(fuelId) == true } catch (e: Throwable) { false }

    fun efficiency(engineId: String): Double = try { Fuels.engine(engineId)?.efficiency() ?: 0.0 } catch (e: Throwable) { 0.0 }

    /** Fuel units burned in one game tick at [powerW] shaft power. */
    fun burnPerTick(fuelId: String, powerW: Double, efficiency: Double): Double =
        try { Fuels.get(fuelId)?.let { Fuels.flowPerSecond(it, powerW, efficiency) / 20.0 } ?: 0.0 } catch (e: Throwable) { 0.0 }
}
