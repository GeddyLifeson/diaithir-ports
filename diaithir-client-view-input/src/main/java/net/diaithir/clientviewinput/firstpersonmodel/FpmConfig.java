package net.diaithir.clientviewinput.firstpersonmodel;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

final class FpmConfig {
    static final ModConfigSpec SPEC;
    static final ModConfigSpec.BooleanValue ENABLED;
    static final ModConfigSpec.BooleanValue SHOW_HELMET;
    static final ModConfigSpec.BooleanValue SHOW_HEAD;
    static final ModConfigSpec.DoubleValue OFFSET_X;
    static final ModConfigSpec.DoubleValue OFFSET_Z;
    static final ModConfigSpec.DoubleValue LOOK_DOWN_OFFSET;
    static final ModConfigSpec.BooleanValue IN_VEHICLES;
    static final ModConfigSpec.BooleanValue WHILE_SLEEPING;
    static final ModConfigSpec.BooleanValue WHILE_GLIDING;
    static final ModConfigSpec.BooleanValue WHILE_SWIMMING;
    static final ModConfigSpec.BooleanValue HIDE_IN_SPECTATOR;
    static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_SCREENS;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        ENABLED = b.define("enabled", true);
        SHOW_HELMET = b.define("showHelmet", false);
        SHOW_HEAD = b.define("showHead", false);
        OFFSET_X = b.defineInRange("bodyOffsetX", 0.0, -1.0, 1.0);
        OFFSET_Z = b.defineInRange("bodyOffsetZ", 0.0, -1.0, 1.0);
        LOOK_DOWN_OFFSET = b.defineInRange("lookDownOffset", 0.25, 0.0, 2.0);
        IN_VEHICLES = b.define("renderInVehicles", true);
        WHILE_SLEEPING = b.define("renderWhileSleeping", false);
        WHILE_GLIDING = b.define("renderWhileGliding", true);
        WHILE_SWIMMING = b.define("renderWhileSwimming", true);
        HIDE_IN_SPECTATOR = b.define("hideInSpectator", true);
        BLACKLISTED_SCREENS = b.defineListAllowEmpty("modBlacklistedScreens", List.of(), () -> "", o -> o instanceof String);
        SPEC = b.build();
    }

    private FpmConfig() {}
}
