package net.diaithir.bridges;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/** Server config (diaithirbridges-server.toml). Ratios are "output per 100 input". Read only after the config has loaded. */
public final class Config {
    private Config() {}

    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue EU_TO_FE = B
            .comment("FE made per 100 EU put into a converter set to EU to FE. 90 means a tenth is lost.")
            .defineInRange("euToFe", 90, 1, 10000);
    public static final ModConfigSpec.IntValue FE_TO_EU = B
            .comment("EU-side FE made per 100 FE put into a converter set to FE to EU. 50 means two in for one out.")
            .defineInRange("feToEu", 50, 1, 10000);
    public static final ModConfigSpec.IntValue FE_TO_AIR = B
            .comment("mL of compressed air made per 100 FE (air converter, FE to air). 20 means 5 FE per mL.")
            .defineInRange("feToAir", 20, 1, 10000);
    public static final ModConfigSpec.IntValue AIR_TO_FE = B
            .comment("FE made per 100 mL of compressed air (air converter, air to FE). 200 means 2 FE per mL.")
            .defineInRange("airToFe", 200, 1, 10000);
    public static final ModConfigSpec.IntValue BUFFER = B
            .comment("Buffer of each converter, in its output unit.")
            .defineInRange("buffer", 100_000, 1000, 10_000_000);
    public static final ModConfigSpec.IntValue RATE = B
            .comment("Most a converter takes in, or gives out, per tick.")
            .defineInRange("rate", 4000, 1, 1_000_000);
    public static final ModConfigSpec.DoubleValue MAX_BAR = B
            .comment("The air converter stops pushing air into a neighbour at this pressure (bar).")
            .defineInRange("maxBar", 4.5, 0.5, 20.0);
    public static final ModConfigSpec.ConfigValue<List<? extends String>> GUARD_COMPONENTS = B
            .comment("Data component ids whose presence marks a storage cell or disk as holding items. A recipe of type diaithirbridges:empty_cell_convert refuses any input stack carrying one with content.")
            .defineListAllowEmpty("guardComponents", List.of("ae2:storage_cell_inv", "refinedstorage:storage_reference"), () -> "", o -> o instanceof String);
    public static final ModConfigSpec.IntValue TANK_CAPACITY = B
            .comment("Carrier tank capacity in the fuel's own unit: litres for liquids, kg for solids and batteries, quanta for magic.")
            .defineInRange("tankCapacity", 8000, 100, 10_000_000);
    public static final ModConfigSpec.IntValue MAGIC_BUFFER = B
            .comment("Quanta the magic carrier holds before it stops taking reagents.")
            .defineInRange("magicBuffer", 4000, 100, 1_000_000);
    public static final ModConfigSpec.IntValue MAGIC_YIELD_PCT = B
            .comment("Percent of a reagent's joules that come out as magic carrier quanta. 90 means a tenth is lost. Balance value.")
            .defineInRange("magicYieldPct", 90, 1, 100);
    public static final ModConfigSpec.IntValue LOCO_BUFFER = B
            .comment("FE the locomotive charger holds.")
            .defineInRange("locoBuffer", 2_000_000, 1000, 100_000_000);
    public static final ModConfigSpec.IntValue LOCO_RATE = B
            .comment("Most FE the locomotive charger takes in, or pours into one locomotive, per tick (1 FE = 1 J; 20 000 FE/t is 400 kW).")
            .defineInRange("locoRate", 20_000, 1, 10_000_000);
    public static final ModConfigSpec SPEC = B.build();

    /** Value, or the default while the config is not loaded yet. */
    static int get(ModConfigSpec.IntValue v) {
        return SPEC.isLoaded() ? v.get() : v.getDefault();
    }
}
