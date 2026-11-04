// Ported from Sound Physics Perfected (MIT) https://github.com/FalseMSP/SoundPhysicsPerfected
// Modifications: Lombok removed.
package net.diaithir.audio.soundphysics.reverb;

public final class ReverbSurfaceData {
    public double absorptionCoefficient;
    public final double reflectivityCoefficient;
    public final String surfaceType;

    public ReverbSurfaceData(double absorptionCoefficient, double reflectivityCoefficient, String surfaceType) {
        this.absorptionCoefficient = absorptionCoefficient;
        this.reflectivityCoefficient = reflectivityCoefficient;
        this.surfaceType = surfaceType;
    }
}
