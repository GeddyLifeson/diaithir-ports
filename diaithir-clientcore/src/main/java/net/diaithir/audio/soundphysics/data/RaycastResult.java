// Ported from Sound Physics Perfected (MIT) https://github.com/FalseMSP/SoundPhysicsPerfected
// Modifications: Lombok removed.
package net.diaithir.audio.soundphysics.data;

import net.minecraft.world.phys.Vec3;

public final class RaycastResult {
    public final double totalDistance;
    public final Vec3 initialDirection;
    public final SoundData hitEntity;

    public RaycastResult(double totalDistance, Vec3 initialDirection, SoundData hitEntity) {
        this.totalDistance = totalDistance;
        this.initialDirection = initialDirection;
        this.hitEntity = hitEntity;
    }
}
