package net.diaithir.animation.machines;

/** Motion families. Every block, machine and vehicle part maps to one of these. */
public enum Kind {
    /** Continuous spin about an axis (gears, fans, rotors, propellers, wheels, drills). */
    SPIN,
    /** Linear back-and-forth (pistons, presses, stampers, plungers). */
    PISTON,
    /** Hinged swing between closed and open (doors, trapdoors, lids, hatches, gates, levers). */
    SWING,
    /** Slide between closed and open (drawers, sliding doors, ramps, shutters). */
    SLIDE,
    /** Slow vertical float with turn (crystals, beacons, floating cores, balloons). */
    HOVER,
    /** Scale pulse (lamps, indicators, reactors, heartbeat of a working core). */
    PULSE,
    /** Aim at a target yaw and pitch (turrets, cannons, radar dishes). */
    AIM,
    /** Texture scroll rate for belts and conveyors; the renderer applies it as a UV offset. */
    SCROLL,
    /** Small random shake (grinders, crushers, vibrating screens, engines). */
    SHAKE,
    /** Rise and fall with a swell (hulls on water, hot-air rise, sails, flags). */
    SWAY;
}
