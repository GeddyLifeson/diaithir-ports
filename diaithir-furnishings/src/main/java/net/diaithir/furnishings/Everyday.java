package net.diaithir.furnishings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Everyday objects (hand tools, kitchenware, household items) registered from data/diaithirfurnishings/everyday.json. */
final class Everyday {
    static final String FILE = "/data/" + DiaithirFurnishings.ID + "/everyday.json";
    static final List<DeferredItem<Item>> ALL = new ArrayList<>();

    static final String FARM = "/data/" + DiaithirFurnishings.ID + "/farm.json";

    private Everyday() {}

    private static JsonObject machines() throws Exception {
        try (InputStream in = Everyday.class.getResourceAsStream(FARM)) {
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("machines");
        }
    }

    static void register(DeferredRegister.Items items) {
        try (InputStream in = Everyday.class.getResourceAsStream(FILE)) {
            JsonObject mech = machines();
            JsonArray arr = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("items");
            for (JsonElement e : arr) {
                JsonObject o = e.getAsJsonObject();
                int dur = o.get("durability").getAsInt();
                String id = o.get("id").getAsString();
                JsonObject m = mech.has(id) ? mech.getAsJsonObject(id) : null;
                ALL.add(items.registerItem(id, p -> {
                    Item.Properties q = p.stacksTo(o.get("stack").getAsInt());
                    q = dur > 0 ? q.durability(dur) : q;
                    return m == null ? new Item(q) : new FarmItem(q, m.get("kind").getAsString(), m.get("width").getAsInt(),
                            m.get("power_w").getAsInt(), m.get("mode").getAsString());
                }));
            }
        } catch (Exception ex) {
            throw new IllegalStateException("cannot read " + FILE, ex);
        }
    }
}
