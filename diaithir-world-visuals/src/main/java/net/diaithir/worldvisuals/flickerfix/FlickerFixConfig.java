package net.diaithir.worldvisuals.flickerfix;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class FlickerFixConfig {
    public enum Mode { HOLD, FADE }
    public enum Curve { LINEAR, SMOOTH }

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.EnumValue<Mode> MODE;
    public static final ModConfigSpec.IntValue FADE_TICKS;
    public static final ModConfigSpec.EnumValue<Curve> FADE_CURVE;
    public static final ModConfigSpec.BooleanValue AFFECT_INFINITE;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        ENABLED = b.comment("Master switch").define("enabled", true);
        MODE = b.comment("Behaviour in the final window").defineEnum("mode", Mode.FADE);
        FADE_TICKS = b.comment("Fade length in ticks (FADE mode)").defineInRange("fadeTicks", 40, 1, 200);
        FADE_CURVE = b.comment("Fade interpolation").defineEnum("fadeCurve", Curve.SMOOTH);
        AFFECT_INFINITE = b.comment("Treat infinite-duration effects as constant 1.0").define("affectInfinite", true);
        SPEC = b.build();
    }

    private FlickerFixConfig() {}
}
