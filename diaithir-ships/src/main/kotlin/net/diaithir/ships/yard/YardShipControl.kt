package net.diaithir.ships.yard

import com.fasterxml.jackson.annotation.JsonAutoDetect
import org.joml.Vector3d
import org.valkyrienskies.core.api.ships.PhysShip
import org.valkyrienskies.core.api.ships.ShipPhysicsListener
import org.valkyrienskies.core.api.world.PhysLevel

/**
 * Ship attachment carrying the summed yard part stats of a launched ship (mass/power/thrust/buoyancy from net.diaithir.yards.Stats).
 * Buoyancy lifts the hull in proportion to how much of it is in liquid; thrust pushes along the ship's +z while throttle > 0
 * (throttle follows the Eureka helm's forward input, or [manualThrottle] for tests).
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@Suppress("DEPRECATION")
class YardShipControl : ShipPhysicsListener {
    var yardId: String = ""
    var statMassKg = 0.0
    var powerW = 0.0
    var thrustN = 0.0
    var buoyancyN = 0.0

    @Volatile var throttle = 0.0
    @Volatile var manualThrottle = 0.0
    @Volatile var lastBuoyancyApplied = 0.0
    @Volatile var lastThrustApplied = 0.0
    @Volatile var lastOverlap = 0.0
    @Volatile var physMass = 0.0
    @Volatile var physSpeed = 0.0
    @Volatile var physStatic = false

    override fun physTick(physShip: PhysShip, physLevel: PhysLevel) {
        val cover = physShip.liquidOverlap.coerceIn(0.0, 1.0)
        lastOverlap = cover; physMass = physShip.mass; physSpeed = physShip.velocity.length(); physStatic = physShip.isStatic
        lastBuoyancyApplied = buoyancyN * cover
        if (lastBuoyancyApplied > 0) physShip.applyInvariantForce(Vector3d(0.0, lastBuoyancyApplied, 0.0))
        val t = maxOf(throttle, manualThrottle).coerceIn(0.0, 1.0)
        lastThrustApplied = thrustN * t
        if (lastThrustApplied > 0) physShip.applyInvariantForce(physShip.transform.shipToWorld.transformDirection(Vector3d(0.0, 0.0, lastThrustApplied), Vector3d()))
    }

    companion object {
        @JvmField val pending = java.util.concurrent.ConcurrentHashMap<Long, YardShipControl>()
    }
}
