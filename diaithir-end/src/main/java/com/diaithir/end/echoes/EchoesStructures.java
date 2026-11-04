package com.diaithir.end.echoes;

import java.util.List;

public final class EchoesStructures {
    public static final String NS = "diaithir_end";
    public static final String LEGACY_NS = "echoes_of_the_end";
    public static final List<String> NAMES = List.of(
        "shattered_spire", "void_outpost", "chorus_grove_shrine", "sunken_vault", "broken_bridge",
        "obsidian_obelisk", "watchers_ruin", "floating_remnant", "echo_chamber");
    /** Age gate per structure (index matches NAMES): diaithir:age/<n>. */
    public static final int[] AGES = {5, 5, 5, 6, 5, 5, 6, 6, 7};

    private EchoesStructures() {}

    public static String legacyToNew(String id) {
        return id.startsWith(LEGACY_NS + ":") ? NS + id.substring(LEGACY_NS.length()) : id;
    }
}
