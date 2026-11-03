// Ported from Dynamic Surroundings Rebirth (MIT) https://github.com/uawyiegrfv/DynamicSurroundingsRebirth
// Modifications: Configuration.java subset (soundOptions/entityEffects/footstepAccents) mapped to ModConfigSpec.
package net.diaithir.audio.surroundings.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class SurroundingsConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLE_BIOME_SOUNDS;
    public static final ModConfigSpec.BooleanValue ENABLE_BACKGROUND_THUNDER;
    public static final ModConfigSpec.BooleanValue ENABLE_WIND_SOUNDS;
    public static final ModConfigSpec.BooleanValue ENABLE_FOOTSTEP_SOUNDS;
    public static final ModConfigSpec.BooleanValue ENABLE_SNEAK_FOOTSTEPS;
    public static final ModConfigSpec.BooleanValue ENABLE_JUMP_LAND;
    public static final ModConfigSpec.BooleanValue ENABLE_FOOTSTEP_ACCENTS;
    public static final ModConfigSpec.BooleanValue ENABLE_WET_ACCENTS;
    public static final ModConfigSpec.DoubleValue AMBIENT_VOLUME;
    public static final ModConfigSpec.DoubleValue BIOME_VOLUME;
    public static final ModConfigSpec.DoubleValue FOOTSTEP_VOLUME;
    public static final ModConfigSpec.DoubleValue PLAYER_EFFECT_VOLUME;
    public static final ModConfigSpec.DoubleValue AMBIENT_CHANCE;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        ENABLE_BIOME_SOUNDS = b.comment("Ambient biome sounds (forest, plains, crickets, ...)").define("enableBiomeSounds", true);
        ENABLE_BACKGROUND_THUNDER = b.comment("Distant background thunder during storms").define("enableBackgroundThunder", true);
        ENABLE_WIND_SOUNDS = b.comment("Wind bed that rises with rain").define("enableWindSounds", true);
        ENABLE_FOOTSTEP_SOUNDS = b.comment("Extra footstep layer for the player").define("enableFootstepSounds", true);
        ENABLE_SNEAK_FOOTSTEPS = b.comment("Footstep layer while sneaking").define("enableSneakFootstepSounds", true);
        ENABLE_JUMP_LAND = b.comment("Jump and landing sounds").define("enablePlayerJumpLandSounds", true);
        ENABLE_FOOTSTEP_ACCENTS = b.comment("Armor / surface accents on footsteps").define("enableAccents", true);
        ENABLE_WET_ACCENTS = b.comment("Accents when raining or in water").define("enableWetSurfaceAccents", true);
        AMBIENT_VOLUME = b.comment("Scales all ambient sounds").defineInRange("ambientVolumeScaling", 1.0D, 0.0D, 4.0D);
        BIOME_VOLUME = b.comment("Scales biome ambience").defineInRange("biomeVolume", 1.0D, 0.0D, 2.0D);
        FOOTSTEP_VOLUME = b.comment("Scales footstep layer").defineInRange("footstepVolume", 1.0D, 0.0D, 2.0D);
        PLAYER_EFFECT_VOLUME = b.comment("Scales jump/land effects").defineInRange("playerEffectVolume", 1.0D, 0.0D, 2.0D);
        AMBIENT_CHANCE = b.comment("Per-tick chance of an ambient biome sound").defineInRange("additionalSoundChance", 0.008D, 0.0D, 1.0D);
        SPEC = b.build();
    }

    private SurroundingsConfig() {}
}
