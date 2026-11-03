package net.diaithir.woodvariants;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** The non-wood part of the Macaw-style set, read from data/diaithirwoodvariants/macaws.json: roof colours and paving patterns and palettes. */
public record MacawSet(List<String> roofColors, List<String> patterns, List<String> palettes) {
    private static final String DATA = "/data/" + DiaithirWoodVariants.ID + "/macaws.json";

    public static MacawSet load(Logger log) {
        try (Reader r = new InputStreamReader(MacawSet.class.getResourceAsStream(DATA), StandardCharsets.UTF_8)) {
            return parse(r);
        } catch (Exception e) {
            log.error("Could not read {}", DATA, e);
            return new MacawSet(List.of(), List.of(), List.of());
        }
    }

    public static MacawSet parse(Reader r) {
        JsonObject o = JsonParser.parseReader(r).getAsJsonObject();
        List<String> colors = new ArrayList<>(), patterns = new ArrayList<>(), palettes = new ArrayList<>();
        o.getAsJsonArray("roof_colors").forEach(e -> colors.add(e.getAsString()));
        o.getAsJsonArray("paving_patterns").forEach(e -> patterns.add(e.getAsString()));
        o.getAsJsonObject("paving_palettes").keySet().forEach(palettes::add);
        return new MacawSet(colors, patterns, palettes);
    }
}
