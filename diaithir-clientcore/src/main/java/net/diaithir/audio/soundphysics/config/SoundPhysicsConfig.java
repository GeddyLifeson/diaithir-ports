// Ported from Sound Physics Perfected (MIT) https://github.com/FalseMSP/SoundPhysicsPerfected
// Modifications: YACL config replaced by ModConfigSpec; all upstream options kept with same defaults.
package net.diaithir.audio.soundphysics.config;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class SoundPhysicsConfig {
    public enum DebugType { OFF, CHAT, ACTION_BAR }
    public enum LegacyReverb { MODERN, VERSION140, VERSION100 }
    public enum AttenuationType { INVERSE_SQUARE, LINEAR, VERCIDIUM_INVERSE_SQUARE, VERCIDIUM_LINEAR }

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue RAYS_CAST;
    public static final ModConfigSpec.IntValue RAYS_BOUNCED;
    public static final ModConfigSpec.IntValue MAX_RAY_LENGTH;
    public static final ModConfigSpec.IntValue PROC_RANGE;
    public static final ModConfigSpec.DoubleValue VOLUME_MULTIPLIER;
    public static final ModConfigSpec.IntValue TICK_RATE;
    public static final ModConfigSpec.BooleanValue PERMEATION;
    public static final ModConfigSpec.DoubleValue PERMEATION_STEP_SIZE;
    public static final ModConfigSpec.BooleanValue USE_EXPLOSION_RESISTANCE;
    public static final ModConfigSpec.EnumValue<AttenuationType> ATTENUATION_TYPE;
    public static final ModConfigSpec.DoubleValue MAX_BLOCKS_PERMEATED;
    public static final ModConfigSpec.DoubleValue PERMEATION_ABSORPTION;
    public static final ModConfigSpec.BooleanValue BARRIER_AS_AIR;
    public static final ModConfigSpec.BooleanValue DISABLE_FX_ON_UI;
    public static final ModConfigSpec.DoubleValue BOUNCE_ABSORPTION_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue SHORTCUT_DIRECTIONALITY;
    public static final ModConfigSpec.IntValue MAX_SOUNDS;
    public static final ModConfigSpec.EnumValue<DebugType> DEBUG;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> SOUND_BLACKLIST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> SOUND_TICK_BLACKLIST;

    public static final ModConfigSpec.BooleanValue REVERB;
    public static final ModConfigSpec.EnumValue<DebugType> REVERB_TUNING;
    public static final ModConfigSpec.EnumValue<LegacyReverb> LEGACY_REVERB;
    public static final ModConfigSpec.BooleanValue EXPERIMENTAL_REVERB;
    public static final ModConfigSpec.DoubleValue GLOBAL_REVERB_INTENSITY;
    public static final ModConfigSpec.DoubleValue INDOOR_BIAS;
    public static final ModConfigSpec.DoubleValue OUTDOOR_BIAS;
    public static final ModConfigSpec.DoubleValue SMALL_ROOM_EMPHASIS;
    public static final ModConfigSpec.DoubleValue LARGE_ROOM_EMPHASIS;
    public static final ModConfigSpec.DoubleValue DISTANCE_ATTENUATION_LINEAR;
    public static final ModConfigSpec.DoubleValue DISTANCE_ATTENUATION_QUADRATIC;
    public static final ModConfigSpec.DoubleValue AIR_ABSORPTION_RATE;
    public static final ModConfigSpec.DoubleValue MIN_AIR_ABSORPTION;
    public static final ModConfigSpec.DoubleValue ROOM_VOLUME_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue SURFACE_AREA_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue ROOM_COMPLEXITY_DIVISOR;
    public static final ModConfigSpec.DoubleValue LATE_REVERB_DELAY_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue SOUND_SPEED;
    public static final ModConfigSpec.DoubleValue BASE_REVERB_GAIN;
    public static final ModConfigSpec.DoubleValue REVERB_GAIN_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue MAX_OVERALL_GAIN;
    public static final ModConfigSpec.DoubleValue SEND_FILTER_HF_REDUCTION;
    public static final ModConfigSpec.DoubleValue MIN_DIFFUSION;
    public static final ModConfigSpec.DoubleValue MAX_DIFFUSION;
    public static final ModConfigSpec.DoubleValue MIN_DENSITY;
    public static final ModConfigSpec.DoubleValue DENSITY_ROOM_SIZE_FACTOR;
    public static final ModConfigSpec.DoubleValue DYNAMIC_ABSORPTION_LF_FACTOR;
    public static final ModConfigSpec.DoubleValue DECAY_LF_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue OUTDOOR_HF_LEAK;
    public static final ModConfigSpec.DoubleValue ECHO_TIME_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue ECHO_DEPTH_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue MODULATION_TIME_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue MODULATION_DEPTH_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue BASE_HF_REFERENCE;
    public static final ModConfigSpec.DoubleValue HF_ROOM_SIZE_FACTOR;
    public static final ModConfigSpec.DoubleValue BASE_LF_REFERENCE;
    public static final ModConfigSpec.DoubleValue LF_ROOM_SIZE_FACTOR;
    public static final ModConfigSpec.DoubleValue ROOM_ROLLOFF_SIZE_FACTOR;
    public static final ModConfigSpec.DoubleValue RT60_SABINE_CONSTANT;
    public static final ModConfigSpec.DoubleValue MIN_TOTAL_ABSORPTION;
    public static final ModConfigSpec.DoubleValue DIFFUSION_COMPLEXITY_WEIGHT;
    public static final ModConfigSpec.DoubleValue DIFFUSION_ENCLOSURE_WEIGHT;
    public static final ModConfigSpec.DoubleValue DIFFUSION_REVERB_WEIGHT;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.push("general");
        RAYS_CAST = b.comment("# of rays to cast from the player. PERFORMANCE IMPACT: HIGH").defineInRange("raysCast", 200, 0, 2000);
        RAYS_BOUNCED = b.comment("Max # of bounces before a ray terminates. PERFORMANCE IMPACT: HIGH").defineInRange("raysBounced", 3, 0, 16);
        MAX_RAY_LENGTH = b.comment("Max length of a single ray in chunks. PERFORMANCE IMPACT: MEDIUM").defineInRange("maxRayLength", 4, 2, 16);
        PROC_RANGE = b.comment("Max distance in blocks that sounds are processed; -1 = infinite").defineInRange("procRange", 200, -1, 1000);
        VOLUME_MULTIPLIER = b.comment("Global volume multiplier for processed sounds").defineInRange("volumeMultiplier", 1.0, 0.0, 10.0);
        TICK_RATE = b.comment("How often tickable sounds are re-positioned, in ticks; 0 = off. PERFORMANCE IMPACT: MEDIUM").defineInRange("tickRate", 10, 0, 20);
        ATTENUATION_TYPE = b.comment("Attenuation mode").defineEnum("attenuationType", AttenuationType.VERCIDIUM_LINEAR);
        BOUNCE_ABSORPTION_MULTIPLIER = b.comment("How much survives a bounce (1.0 = no absorption)").defineInRange("bounceAbsorptionMultiplier", 0.9, 0.001, 1.0);
        SHORTCUT_DIRECTIONALITY = b.comment("With direct line of sight use the real direction instead of the ray traced one").define("shortcutDirectionality", true);
        MAX_SOUNDS = b.comment("Maximum # of sounds the mod is allowed to process").defineInRange("maxSounds", 100, 0, Integer.MAX_VALUE);
        DISABLE_FX_ON_UI = b.comment("Disable effects on the MASTER sound category (UI sounds)").define("disableFXOnUI", false);
        DEBUG = b.comment("Print sound names to chat / action bar").defineEnum("debug", DebugType.OFF);
        b.pop();

        b.push("permeation");
        PERMEATION = b.comment("Cast permeating rays and muffle sounds through blocks. PERFORMANCE IMPACT: MEDIUM").define("permeation", true);
        PERMEATION_STEP_SIZE = b.comment("Permeation step size in blocks; lower = slower. PERFORMANCE IMPACT: HIGH").defineInRange("permeationStepSize", 0.0625, 0.01, 2.0);
        USE_EXPLOSION_RESISTANCE = b.comment("Use block explosion resistance for absorption").define("useExplosionResistance", true);
        MAX_BLOCKS_PERMEATED = b.comment("Max blocks permeated in the calculation").defineInRange("maxBlocksPermeated", 3.0, 1.0, 10.0);
        PERMEATION_ABSORPTION = b.comment("Volume is multiplied by x^blocksTraveled").defineInRange("permeationAbsorption", 0.4, 0.001, 1.0);
        BARRIER_AS_AIR = b.comment("Barriers do not absorb sound").define("barrierAsAir", true);
        b.pop();

        b.push("blacklist");
        SOUND_BLACKLIST = b.comment("Sounds containing any of these strings are not processed").defineListAllowEmpty("soundBlacklist", List.of(), () -> "", o -> o instanceof String);
        SOUND_TICK_BLACKLIST = b.comment("Sounds containing any of these strings are not ticked").defineListAllowEmpty("soundTickBlacklist", List.of(), () -> "", o -> o instanceof String);
        b.pop();

        b.push("reverb");
        REVERB = b.comment("Cast reverb detecting rays and apply reverb dynamically. PERFORMANCE IMPACT: MEDIUM").define("reverb", true);
        REVERB_TUNING = b.comment("Debug output of reverb values").defineEnum("reverbTuning", DebugType.OFF);
        LEGACY_REVERB = b.comment("Switch reverb mode to older versions").defineEnum("legacyReverb", LegacyReverb.MODERN);
        EXPERIMENTAL_REVERB = b.comment("Experimental reverb allowing louder than vanilla attenuation").define("experimentalReverb", true);
        GLOBAL_REVERB_INTENSITY = b.defineInRange("globalReverbIntensity", 2.0, 0.0, 10000.0);
        INDOOR_BIAS = b.defineInRange("indoorBias", 2.0, 0.0, 10000.0);
        OUTDOOR_BIAS = b.defineInRange("outdoorBias", 0.2, 0.0, 10000.0);
        SMALL_ROOM_EMPHASIS = b.defineInRange("smallRoomEmphasis", 1.0, 0.0, 10000.0);
        LARGE_ROOM_EMPHASIS = b.defineInRange("largeRoomEmphasis", 1.0, 0.0, 10000.0);
        DISTANCE_ATTENUATION_LINEAR = b.defineInRange("distanceAttenuationLinear", 0.0, 0.0, 10000.0);
        DISTANCE_ATTENUATION_QUADRATIC = b.defineInRange("distanceAttenuationQuadratic", 0.0, 0.0, 10000.0);
        AIR_ABSORPTION_RATE = b.defineInRange("airAbsorptionRate", 0.003, 0.0, 10000.0);
        MIN_AIR_ABSORPTION = b.defineInRange("minAirAbsorption", 0.1, 0.0, 10000.0);
        ROOM_VOLUME_MULTIPLIER = b.defineInRange("roomVolumeMultiplier", 0.7, 0.0, 10000.0);
        SURFACE_AREA_MULTIPLIER = b.defineInRange("surfaceAreaMultiplier", 6.0, 0.0, 10000.0);
        ROOM_COMPLEXITY_DIVISOR = b.defineInRange("roomComplexityDivisor", 2.0, 0.0001, 10000.0);
        LATE_REVERB_DELAY_MULTIPLIER = b.defineInRange("lateReverbDelayMultiplier", 1.5, 0.0, 10000.0);
        SOUND_SPEED = b.defineInRange("soundSpeed", 343.0, 1.0, 10000.0);
        BASE_REVERB_GAIN = b.defineInRange("baseReverbGain", 0.1, 0.0, 10000.0);
        REVERB_GAIN_MULTIPLIER = b.defineInRange("reverbGainMultiplier", 0.4, 0.0, 10000.0);
        MAX_OVERALL_GAIN = b.defineInRange("maxOverallGain", 1.0, 0.0, 10000.0);
        SEND_FILTER_HF_REDUCTION = b.defineInRange("sendFilterHfReduction", 0.7, 0.0, 10000.0);
        MIN_DIFFUSION = b.defineInRange("minDiffusion", 0.3, 0.0, 1.0);
        MAX_DIFFUSION = b.defineInRange("maxDiffusion", 0.95, 0.0, 1.0);
        MIN_DENSITY = b.defineInRange("minDensity", 0.3, 0.0, 1.0);
        DENSITY_ROOM_SIZE_FACTOR = b.defineInRange("densityRoomSizeFactor", 100.0, 0.0001, 10000.0);
        DYNAMIC_ABSORPTION_LF_FACTOR = b.defineInRange("dynamicAbsorptionLfFactor", 3.0, 0.0, 10000.0);
        DECAY_LF_MULTIPLIER = b.defineInRange("decayLfMultiplier", 1.2, 0.0, 10000.0);
        OUTDOOR_HF_LEAK = b.defineInRange("outdoorHfLeak", 0.7, 0.0, 10000.0);
        ECHO_TIME_MULTIPLIER = b.defineInRange("echoTimeMultiplier", 0.002, 0.0, 10000.0);
        ECHO_DEPTH_MULTIPLIER = b.defineInRange("echoDepthMultiplier", 0.1, 0.0, 10000.0);
        MODULATION_TIME_MULTIPLIER = b.defineInRange("modulationTimeMultiplier", 0.01, 0.0, 10000.0);
        MODULATION_DEPTH_MULTIPLIER = b.defineInRange("modulationDepthMultiplier", 0.1, 0.0, 10000.0);
        BASE_HF_REFERENCE = b.defineInRange("baseHfReference", 5000.0, 0.0, 10000.0);
        HF_ROOM_SIZE_FACTOR = b.defineInRange("hfRoomSizeFactor", 20.0, 0.0, 10000.0);
        BASE_LF_REFERENCE = b.defineInRange("baseLfReference", 250.0, 0.0, 10000.0);
        LF_ROOM_SIZE_FACTOR = b.defineInRange("lfRoomSizeFactor", 2.0, 0.0, 10000.0);
        ROOM_ROLLOFF_SIZE_FACTOR = b.defineInRange("roomRolloffSizeFactor", 50.0, 0.0001, 10000.0);
        RT60_SABINE_CONSTANT = b.defineInRange("rt60SabineConstant", 0.161, 0.0, 10000.0);
        MIN_TOTAL_ABSORPTION = b.defineInRange("minTotalAbsorption", 0.1, 0.0001, 10000.0);
        DIFFUSION_COMPLEXITY_WEIGHT = b.defineInRange("diffusionComplexityWeight", 1.0, 0.0, 10000.0);
        DIFFUSION_ENCLOSURE_WEIGHT = b.defineInRange("diffusionEnclosureWeight", 1.0, 0.0, 10000.0);
        DIFFUSION_REVERB_WEIGHT = b.defineInRange("diffusionReverbWeight", 1.0, 0.0, 10000.0);
        b.pop();

        SPEC = b.build();
    }

    private SoundPhysicsConfig() {}
}
