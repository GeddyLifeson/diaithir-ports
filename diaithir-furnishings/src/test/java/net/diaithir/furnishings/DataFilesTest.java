package net.diaithir.furnishings;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DataFilesTest {
    private static JsonObject json(String path) throws Exception {
        var in = DataFilesTest.class.getResourceAsStream("/" + path);
        assertNotNull(in, path);
        return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    @Test
    void everyBlockHasItsFiles() throws Exception {
        List<String> names = new ArrayList<>(List.of("clay_jar", "glass_jar"));
        Woods.ALL.forEach(w -> names.add(w.id() + "_signpost"));
        JsonObject lang = json("assets/diaithirfurnishings/lang/en_us.json");
        for (String n : names) {
            json("assets/diaithirfurnishings/blockstates/" + n + ".json");
            json("assets/diaithirfurnishings/items/" + n + ".json");
            json("assets/diaithirfurnishings/models/block/" + n + ".json");
            json("data/diaithirfurnishings/loot_table/blocks/" + n + ".json");
            json("data/diaithirfurnishings/recipe/" + n + ".json");
            assertTrue(lang.has("block.diaithirfurnishings." + n), n);
            assertTrue(!lang.get("block.diaithirfurnishings." + n).getAsString().contains("—"), "no em dash");
        }
    }
}
