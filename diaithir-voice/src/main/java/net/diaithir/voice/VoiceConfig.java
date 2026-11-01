package net.diaithir.voice;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class VoiceConfig {
    public static final ModConfigSpec COMMON;
    public static final ModConfigSpec.DoubleValue RANGE;
    public static final ModConfigSpec.DoubleValue NEAR;
    public static final ModConfigSpec.DoubleValue MAX_FRAMES_PER_SECOND;
    public static final ModConfigSpec CLIENT;
    public static final ModConfigSpec.DoubleValue VOLUME;
    public static final ModConfigSpec.IntValue JITTER_FRAMES;
    public static final ModConfigSpec.ConfigValue<String> MIC_NAME;

    static {
        var b = new ModConfigSpec.Builder();
        RANGE = b.comment("Blocks over which speech carries. Beyond this nobody hears it.").defineInRange("range", 32.0, 2.0, 128.0);
        NEAR = b.comment("Inside this many blocks speech is at full volume.").defineInRange("nearDistance", 4.0, 0.0, 64.0);
        MAX_FRAMES_PER_SECOND = b.comment("Most frames the server relays per second for one speaker (a talker sends 50).").defineInRange("maxFramesPerSecond", 60.0, 10.0, 200.0);
        COMMON = b.build();
        b = new ModConfigSpec.Builder();
        VOLUME = b.comment("Playback volume multiplier.").defineInRange("volume", 1.0, 0.0, 3.0);
        JITTER_FRAMES = b.comment("Frames of 20 ms held before playback starts. More is steadier and later.").defineInRange("jitterFrames", 3, 1, 25);
        MIC_NAME = b.comment("Part of the microphone name to use. Empty means the system default.").define("microphone", "");
        CLIENT = b.build();
    }

    private VoiceConfig() {}
}
