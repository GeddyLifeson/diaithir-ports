package net.diaithir.upgrades;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Parses data/&lt;ns&gt;/upgrades/targets/*.json:
 * {"rules":[{"pattern":"ns:block|ns:*|#ns:tag","priority":0,"slots":2,"allowed":["speed"],"max_quality":4}]}
 * Bad rows are skipped, never fatal, so one mod's typo cannot break another's rules.
 */
public final class RuleLoader {
    private RuleLoader() {}

    public static List<TargetRegistry.Rule> parse(JsonObject file) {
        List<TargetRegistry.Rule> out = new ArrayList<>();
        if (!file.has("rules") || !file.get("rules").isJsonArray()) return out;
        for (JsonElement el : file.getAsJsonArray("rules")) {
            if (!el.isJsonObject()) continue;
            JsonObject o = el.getAsJsonObject();
            try {
                Set<Kind> kinds = EnumSet.noneOf(Kind.class);
                for (JsonElement k : o.getAsJsonArray("allowed")) {
                    kinds.add(Kind.valueOf(k.getAsString().toUpperCase(Locale.ROOT)));
                }
                int slots = o.get("slots").getAsInt();
                if (slots < 0 || slots > 16) continue;
                out.add(new TargetRegistry.Rule(o.get("pattern").getAsString(),
                        o.has("priority") ? o.get("priority").getAsInt() : 0, slots, kinds,
                        o.has("max_quality") ? o.get("max_quality").getAsInt() : 4));
            } catch (RuntimeException ignored) {
                // skip bad row
            }
        }
        return out;
    }
}
