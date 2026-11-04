package net.diaithir.crafting.foundry;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** Watts and job seconds per Foundry machine, read from data/diaithircrafting/machine_stats.json (keys are progression node ids). */
public final class MachineStats {
    private MachineStats() {}

    public record Stat(double watts, double seconds) {}

    private static Map<String, Stat> table;

    private static synchronized Map<String, Stat> table() {
        if (table == null) {
            Map<String, Stat> t = new HashMap<>();
            try (Reader r = new InputStreamReader(MachineStats.class.getResourceAsStream("/data/" + DiaithirCrafting.ID + "/machine_stats.json"), StandardCharsets.UTF_8)) {
                JsonObject machines = JsonParser.parseReader(r).getAsJsonObject().getAsJsonObject("machines");
                for (var e : machines.entrySet()) {
                    JsonObject o = e.getValue().getAsJsonObject();
                    t.put(e.getKey(), new Stat(o.has("W") ? o.get("W").getAsDouble() : 0, o.has("s") ? o.get("s").getAsDouble() : 0));
                }
            } catch (Exception ex) {
                throw new IllegalStateException("machine_stats.json unreadable", ex);
            }
            table = Map.copyOf(t);
        }
        return table;
    }

    /** The stat row for a progression node id such as {@code tech.assembler}, or null when the machine has none. */
    public static Stat of(String nodeId) { return table().get(nodeId); }

    public static int size() { return table().size(); }
}
