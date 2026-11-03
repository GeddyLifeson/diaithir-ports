// Ported from Sound Physics Perfected (MIT) https://github.com/FalseMSP/SoundPhysicsPerfected
// Modifications: Lombok removed.
package net.diaithir.audio.soundphysics.data;

import net.minecraft.world.phys.Vec3;

public final class RayHitData {
    public final RaycastResult rayResult;
    public final Vec3 direction;
    public final double weight;
    public final double muffleFac;
    public final int bounces;

    public RayHitData(RaycastResult rayResult, Vec3 direction, double weight, double muffleFac, int bounces) {
        this.rayResult = rayResult;
        this.direction = direction;
        this.weight = weight;
        this.muffleFac = muffleFac;
        this.bounces = bounces;
    }
}
