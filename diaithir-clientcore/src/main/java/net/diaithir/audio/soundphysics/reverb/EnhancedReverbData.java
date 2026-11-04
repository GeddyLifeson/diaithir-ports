// Ported from Sound Physics Perfected (MIT) https://github.com/FalseMSP/SoundPhysicsPerfected
// Modifications: Lombok removed, plain record-style class.
package net.diaithir.audio.soundphysics.reverb;

public final class EnhancedReverbData {
    public final double rt60;
    public final double earlyReflectionDelay;
    public final double lateReflectionStrength;
    public final double roomSize;
    public final double absorption;
    public final String acousticProfile;
    public final boolean isIndoors;

    public EnhancedReverbData(double rt60, double earlyReflectionDelay, double lateReflectionStrength,
                              double roomSize, double absorption, String acousticProfile, boolean isIndoors) {
        this.rt60 = rt60;
        this.earlyReflectionDelay = earlyReflectionDelay;
        this.lateReflectionStrength = lateReflectionStrength;
        this.roomSize = roomSize;
        this.absorption = absorption;
        this.acousticProfile = acousticProfile;
        this.isIndoors = isIndoors;
    }

    @Override
    public String toString() {
        return String.format("Reverb Data: rt60=%.3f, earlyReflectionDelay=%.3f, lateReflectionStrength=%.3f, roomSize=%.3f, absorption=%.3f, isIndoors=%s",
                rt60, earlyReflectionDelay, lateReflectionStrength, roomSize, absorption, isIndoors);
    }
}
