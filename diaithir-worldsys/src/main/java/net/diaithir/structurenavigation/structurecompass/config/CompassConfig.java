// Ported from Structure Compass (MIT) https://github.com/Mrbysco/StructureCompass
// Modifications: Flattened into static fields, logging listeners removed.
package net.diaithir.structurenavigation.structurecompass.config;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class CompassConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue COMPASS_RANGE;
    public static final ModConfigSpec.BooleanValue LOCATE_UNEXPLORED;
    public static final ModConfigSpec.BooleanValue LOCATE_ASYNC;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> STRUCTURE_BLACKLIST;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("General settings").push("general");

        COMPASS_RANGE = builder
                .comment("Sets the range in blocks in which the structure compasses can locate structures [default: 10000]")
                .defineInRange("compassRange", 10000, 0, Integer.MAX_VALUE);

        LOCATE_UNEXPLORED = builder
                .comment("Defines if the structure compass should only locate unexplored structures. A structure is tagged as explored when the compass is used to find it. [default: false]")
                .define("locateUnexplored", false);

        LOCATE_ASYNC = builder
                .comment("Defines if the structure compass should locate structures asynchronously [default: false]")
                .define("locateAsync", false);

        STRUCTURE_BLACKLIST = builder
                .comment("Defines which structures can't be searched with the Structure Compass\n"
                        + "(Supports wildcard *, Example: 'minecraft:*' will blacklist anything in the minecraft domain)")
                .defineListAllowEmpty("structureBlacklist", () -> List.of(""), () -> "", o -> o instanceof String);

        builder.pop();
        SPEC = builder.build();
    }

    private CompassConfig() {}
}
