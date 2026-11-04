// Ported from Sound Physics Perfected (MIT) https://github.com/FalseMSP/SoundPhysicsPerfected
// Modifications: Lombok removed.
package net.diaithir.audio.soundphysics.data;

import net.minecraft.world.phys.Vec3;

public final class BlueRayResult {
    public final boolean arrived;
    public final Vec3 directionFromPlayer;
    public final double distance;

    public BlueRayResult(boolean arrived, Vec3 directionFromPlayer, double distance) {
        this.arrived = arrived;
        this.directionFromPlayer = directionFromPlayer;
        this.distance = distance;
    }
}
