// Ported from Sound Physics Perfected (MIT) https://github.com/FalseMSP/SoundPhysicsPerfected
// Modifications: package moved, values unchanged, made final constants.
package net.diaithir.audio.soundphysics.reverb;

public final class ReverbConstants {
    public static final float MIN_WALL_DISTANCE = 0.5f;
    public static final float MAX_WALL_DISTANCE = 200.0f;
    public static final float MIN_RT60 = 0.1f;
    public static final float MAX_RT60 = 10.0f;
    public static final float MIN_CALCULATED_RT60 = 0.1f;
    public static final float MAX_CALCULATED_RT60 = 8.0f;
    public static final float MIN_ROOM_SIZE = 1.0f;
    public static final float MAX_ROOM_SIZE = 100.0f;
    public static final float MIN_ABSORPTION = 0.01f;
    public static final float MAX_ABSORPTION = 0.9f;
    public static final float MIN_EARLY_REFLECTION_DELAY = 0.001f;
    public static final float MAX_EARLY_REFLECTION_DELAY = 0.1f;
    public static final float MIN_LATE_REVERB_DELAY = 0.005f;
    public static final float MAX_LATE_REVERB_DELAY = 0.1f;
    public static final float MIN_SURFACE_TO_VOLUME_RATIO = 0.1f;
    public static final float MAX_SURFACE_TO_VOLUME_RATIO = 5.0f;
    public static final float MIN_EFFECTIVE_DECAY_TIME = 0.1f;
    public static final float MAX_EFFECTIVE_DECAY_TIME = 8.0f;
    public static final float MIN_DECAY_HF_RATIO = 0.1f;
    public static final float MAX_DECAY_HF_RATIO = 2.0f;
    public static final float MIN_REFLECTIONS_GAIN = 0.0f;
    public static final float MAX_REFLECTIONS_GAIN = 3f;
    public static final float MIN_LATE_REVERB_GAIN = 0.0f;
    public static final float MAX_LATE_REVERB_GAIN = 10.0f;
    public static final float MIN_AIR_ABSORPTION_HF = 0.892f;
    public static final float MAX_AIR_ABSORPTION_HF = 1.0f;
    public static final float MIN_ROOM_ROLLOFF = 0.0f;
    public static final float MAX_ROOM_ROLLOFF = 1.0f;
    public static final float MIN_ECHO_TIME = 0.075f;
    public static final float MAX_ECHO_TIME = 0.25f;
    public static final float MIN_ECHO_DEPTH = 0.0f;
    public static final float MAX_ECHO_DEPTH = 1.0f;
    public static final float MIN_MODULATION_TIME = 0.04f;
    public static final float MAX_MODULATION_TIME = 4.0f;
    public static final float MIN_MODULATION_DEPTH = 0.0f;
    public static final float MAX_MODULATION_DEPTH = 1.0f;
    public static final float MIN_HF_REFERENCE = 1000.0f;
    public static final float MAX_HF_REFERENCE = 20000.0f;
    public static final float MIN_LF_REFERENCE = 20.0f;
    public static final float MAX_LF_REFERENCE = 1000.0f;
    public static final float MIN_GAIN_LF = 0.1f;
    public static final float MAX_GAIN_LF = 1.0f;

    private ReverbConstants() {}
}
