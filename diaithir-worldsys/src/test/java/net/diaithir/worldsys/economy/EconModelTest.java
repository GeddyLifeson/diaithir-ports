package net.diaithir.worldsys.economy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Replays the scenarios the KubeJS model produced (tools/economy/golden_model.js) and demands the same numbers. */
class EconModelTest {
    @Test
    void matchesTheKubeJsModel() {
        JsonObject root = JsonParser.parseReader(new InputStreamReader(getClass().getResourceAsStream("/econ_model_golden.json"))).getAsJsonObject();
        Map<String, EconModel.Good> goods = new HashMap<>();
        root.getAsJsonObject("goods").entrySet().forEach(e -> goods.put(e.getKey(), new EconModel.Good(e.getValue().getAsJsonArray().get(0).getAsDouble(), e.getValue().getAsJsonArray().get(1).getAsDouble())));
        Map<String, double[]> regions = new HashMap<>();
        root.getAsJsonObject("regions").entrySet().forEach(e -> regions.put(e.getKey(), new double[] {e.getValue().getAsJsonArray().get(0).getAsDouble(), e.getValue().getAsJsonArray().get(1).getAsDouble()}));
        int checked = 0;
        for (JsonElement run : root.getAsJsonArray("runs")) {
            EconModel.State st = new EconModel.State();
            for (JsonElement stepEl : run.getAsJsonObject().getAsJsonArray("steps")) {
                JsonObject o = stepEl.getAsJsonObject();
                JsonArray s = o.getAsJsonArray("step");
                double want = o.get("result").getAsDouble();
                String kind = s.get(0).getAsString(), region = s.get(1).getAsString(), id = s.get(2).getAsString();
                double got;
                if (kind.equals("mid")) got = EconModel.mid(st, regions, goods::get, region, id, null, 0);
                else {
                    int n = s.get(3).getAsInt(); boolean buying = s.get(4).getAsBoolean(); double scale = s.get(5).getAsDouble();
                    got = kind.equals("trade") ? EconModel.trade(st, regions, goods::get, region, id, n, buying, scale) : EconModel.quote(st, regions, goods::get, region, id, n, buying, scale);
                }
                assertEquals(want, got, 1e-9, run.getAsJsonObject().get("name").getAsString() + " " + s);
                checked++;
            }
        }
        assertEquals(25, checked);
    }
}
