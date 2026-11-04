// SPDX-License-Identifier: GPL-3.0-only
// Folded from MrCrayfish's Vehicle Mod (Unofficial 1.20.1 port by bay4lly; original by MrCrayfish, GPL). Rewritten for NeoForge 26.1.2; art is not used.
package net.diaithir.vehicles.crayfish;

/** Vehicle health: 100 by default, hits and hard landings take it down, a hammer gives 5 back, below zero the vehicle is destroyed. Fall damage starts at 4 blocks while falling faster than 1 block per tick, and is half the distance. */
public final class VehicleHealth {
    public static final float DEFAULT_MAX = 100F;
    public static final float REPAIR_STEP = 5F;

    private float max;
    private float health;

    public VehicleHealth(float max) {
        this.max = max;
        this.health = max;
    }

    public float health() {
        return health;
    }

    public float max() {
        return max;
    }

    public void set(float h) {
        health = Math.min(max, h);
    }

    /** Applies damage; true when the vehicle is now destroyed (health below zero). */
    public boolean hurt(float amount) {
        health = Math.min(max, health - amount);
        return health < 0F;
    }

    /** Fall damage for a landing, 0 if it was too soft. */
    public static float fallDamage(double distance, double verticalSpeed) {
        return distance >= 4.0 && verticalSpeed < -1.0 ? (float) (distance / 2.0) : 0F;
    }

    /** One hammer blow; returns true if it healed anything. */
    public boolean repair() {
        if (health >= max) return false;
        set(health + REPAIR_STEP);
        return true;
    }

    /** 0 (undamaged) to 9 (nearly gone): drives the crack overlay. */
    public int destroyedStage() {
        return 10 - (int) Math.max(1.0F, (int) Math.ceil(10.0F * (health / max)));
    }
}
