package net.diaithir.voiceinteraction;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class InteractionConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue VIBRATIONS;
    public static final ModConfigSpec.BooleanValue CREATURES;
    public static final ModConfigSpec.BooleanValue SNEAK_QUIETENS;
    public static final ModConfigSpec.BooleanValue WHISPER_COUNTS;
    public static final ModConfigSpec.BooleanValue GROUP_COUNTS;
    public static final ModConfigSpec.DoubleValue THRESHOLD_DB;
    public static final ModConfigSpec.DoubleValue LOUD_DB;
    public static final ModConfigSpec.DoubleValue NOTICE_RADIUS;
    public static final ModConfigSpec.IntValue INTERVAL_TICKS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> RULES;

    static {
        var b = new ModConfigSpec.Builder();
        VIBRATIONS = b.comment("Speech makes a vibration that sculk sensors and wardens can pick up.").define("vibrations", true);
        CREATURES = b.comment("Creatures of the wild notice speech.").define("creatures", true);
        SNEAK_QUIETENS = b.comment("Speaking while sneaking is not noticed.").define("sneakQuietens", true);
        WHISPER_COUNTS = b.comment("Whispered speech is noticed too, at a third of the distance.").define("whisperCounts", false);
        GROUP_COUNTS = b.comment("Speech inside a private voice group is noticed too (Simple Voice Chat only).").define("groupCounts", false);
        THRESHOLD_DB = b.comment("Quietest level that is noticed, in dB below full scale.").defineInRange("thresholdDb", -45.0, -96.0, 0.0);
        LOUD_DB = b.comment("Level at which speech carries the whole radius.").defineInRange("loudDb", -20.0, -96.0, 0.0);
        NOTICE_RADIUS = b.comment("Blocks over which loud speech is noticed by creatures.").defineInRange("noticeRadius", 16.0, 1.0, 64.0);
        INTERVAL_TICKS = b.comment("Most often one speaker is noticed, in ticks. Keeps a long speech from flooding the game.").defineInRange("intervalTicks", 10, 1, 200);
        RULES = b.comment("Creature rules, entity id = look | flee | none. Wolves, cats, villagers and the like.")
            .defineListAllowEmpty("rules", List.of(
                "minecraft:wolf=look", "minecraft:cat=look", "minecraft:villager=look", "minecraft:wandering_trader=look",
                "minecraft:iron_golem=look", "minecraft:horse=look", "minecraft:donkey=look", "minecraft:parrot=look",
                "minecraft:fox=flee", "minecraft:ocelot=flee", "minecraft:rabbit=flee", "minecraft:chicken=flee",
                "minecraft:bat=flee", "minecraft:warden=none"), () -> "minecraft:wolf=look", o -> o instanceof String s && Hearing.parseRule(s) != null);
        SPEC = b.build();
    }

    private InteractionConfig() {}
}
