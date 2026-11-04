// Ported from GUI Scaler (LGPL-3.0-only) https://github.com/zendiik/gui-scaler
// Modifications: same options (enableAutoScale, mode, customRules); list uses defineListAllowEmpty with a format validator.
package net.diaithir.clientviewinput.guiscaler.config;

import java.util.List;
import net.diaithir.clientviewinput.guiscaler.core.ScaleMode;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class GuiScalerConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLE_AUTO_SCALE;
    public static final ModConfigSpec.EnumValue<ScaleMode> MODE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> CUSTOM_RULES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("GUI Scaler Configuration").push("general");

        ENABLE_AUTO_SCALE = builder
                .comment("Enable automatic GUI scaling")
                .define("enableAutoScale", true);

        MODE = builder
                .comment("Scaling mode: AUTO (intelligent) or CUSTOM (user-defined rules)")
                .defineEnum("mode", ScaleMode.AUTO);

        CUSTOM_RULES = builder
                .comment("Custom scaling rules in format 'width:scale' (e.g., '2560:3' means scale 3 for width >= 2560)")
                .defineListAllowEmpty("customRules",
                        List.of("2560:3", "1920:2", "1280:1"),
                        () -> "1280:1",
                        o -> o instanceof String s && s.matches("\\d+:\\d+"));

        builder.pop();
        SPEC = builder.build();
    }

    private GuiScalerConfig() {}
}
