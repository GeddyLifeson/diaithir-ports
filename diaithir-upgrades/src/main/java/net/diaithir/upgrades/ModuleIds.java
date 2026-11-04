package net.diaithir.upgrades;

/** Module naming and Age gating, kept free of Minecraft types so tests can load it. */
public final class ModuleIds {
    private ModuleIds() {}

    public static String idOf(Theme theme, Kind kind, int tier) {
        String t = switch (theme) { case TECH -> "circuit"; case MAGIC -> "ogham"; case FOOD -> "spice"; };
        return t + "_" + kind.name().toLowerCase() + "_" + tier;
    }

    public static int ageOf(Theme theme, int tier) {
        return tier >= 2 ? 7 : theme == Theme.TECH ? 6 : 3;
    }
}
