// Ported from Extreme Sound Muffler (LGPL-3.0-only) https://github.com/LeoBeliik/ExtremeSoundMuffler
// Modifications: Rewritten as ModConfigSpec; anchors/inventory-button options dropped, muffled list persisted in config.
package net.diaithir.audio.soundmuffler.client;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class MufflerConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> FORBIDDEN_SOUNDS;
    public static final ModConfigSpec.BooleanValue LAWFUL_ALL_LIST;
    public static final ModConfigSpec.DoubleValue DEFAULT_MUTE_VOLUME;
    public static final ModConfigSpec.BooleanValue LEFT_BUTTONS;
    public static final ModConfigSpec.BooleanValue USE_DARK_THEME;
    public static final ModConfigSpec.BooleanValue MUFFLING_ENABLED;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MUFFLED;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        FORBIDDEN_SOUNDS = b.comment("Blacklisted sounds (substring match): never recorded in the Recent list and hidden from the All list")
                .defineListAllowEmpty("forbiddenSounds", List.of("ui.", "music.", "ambient."), () -> "", o -> o instanceof String);
        LAWFUL_ALL_LIST = b.comment("Allow the All list to include blacklisted sounds")
                .define("lawfulAllList", false);
        DEFAULT_MUTE_VOLUME = b.comment("Volume set when pressing the mute button")
                .defineInRange("defaultMuteVolume", 0.0D, 0.0D, 0.9D);
        LEFT_BUTTONS = b.comment("Move the muffle and play buttons to the left side of the GUI")
                .define("leftButtons", false);
        USE_DARK_THEME = b.comment("Use the dark theme for the GUI")
                .define("useDarkTheme", false);
        MUFFLING_ENABLED = b.comment("Master switch for muffling (persisted toggle)")
                .define("mufflingEnabled", true);
        MUFFLED = b.comment("Muffled sounds as 'namespace:sound=volume' entries (managed by the GUI)")
                .defineListAllowEmpty("muffledSounds", List.of(), () -> "", o -> o instanceof String);
        SPEC = b.build();
    }

    private MufflerConfig() {}
}
