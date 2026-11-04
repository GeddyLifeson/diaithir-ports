package net.diaithir.pipes;

import java.util.Map;

/** Old-mod registry names mapped to ours. Applied by a Remap event hook in worlds that still hold the old ids; also documented here for data fixing by hand. */
public final class IdMigration {
    private static final Map<String, String> MAP = Map.ofEntries(
            Map.entry("classicpipes:item_pipe", "diaithirpipes:item_pipe_basic"),
            Map.entry("classicpipes:fluid_pipe", "diaithirpipes:fluid_pipe_basic"),
            Map.entry("pipez:item_pipe", "diaithirpipes:item_pipe_basic"),
            Map.entry("pipez:fluid_pipe", "diaithirpipes:fluid_pipe_basic"),
            Map.entry("pipez:energy_pipe", "diaithirpipes:energy_pipe_basic"),
            Map.entry("pipez:wrench", "diaithirpipes:wrench"),
            Map.entry("pipeconnector:pipe_connector", "diaithirpipes:connector"),
            Map.entry("tinypipes:item_pipe", "diaithirpipes:item_pipe_basic"),
            Map.entry("tinypipes:fluid_pipe", "diaithirpipes:fluid_pipe_basic"),
            Map.entry("tinypipes:energy_pipe", "diaithirpipes:energy_pipe_basic"));

    private IdMigration() {
    }

    public static Map<String, String> table() {
        return MAP;
    }

    public static String migrate(String old) {
        return MAP.getOrDefault(old, old);
    }
}
