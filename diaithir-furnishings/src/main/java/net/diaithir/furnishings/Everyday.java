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

    private Everyday() {}

    static void register(DeferredRegister.Items items) {
        try (InputStream in = Everyday.class.getResourceAsStream(FILE)) {
            JsonArray arr = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("items");
            for (JsonElement e : arr) {
                JsonObject o = e.getAsJsonObject();
                int dur = o.get("durability").getAsInt();
                ALL.add(items.registerSimpleItem(o.get("id").getAsString(), p -> {
                    Item.Properties q = p.stacksTo(o.get("stack").getAsInt());
                    return dur > 0 ? q.durability(dur) : q;
                }));
            }
        } catch (Exception ex) {
            throw new IllegalStateException("cannot read " + FILE, ex);
        }
    }
}
