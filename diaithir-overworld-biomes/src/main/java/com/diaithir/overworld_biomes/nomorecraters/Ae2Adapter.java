package com.diaithir.overworld_biomes.nomorecraters;

import net.neoforged.fml.ModList;

/** Single isolation point for all AE2-specific coupling (soft dependency). */
public final class Ae2Adapter {
    public static final String AE2_ID = "ae2";
    private static Boolean present;

    private Ae2Adapter() {}

    public static boolean isPresent() {
        if (present == null) {
            present = ModList.get().isLoaded(AE2_ID);
        }
        return present;
    }

    /** AE2 feature registry path whose placement we intercept. */
    public static boolean isMeteoriteFeature(String namespace, String path) {
        return AE2_ID.equals(namespace) && path.contains("meteorite");
    }
}
