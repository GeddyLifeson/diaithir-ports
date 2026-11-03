package net.diaithir.combat;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Registers the simple items described by the consolidated bundles and aliases migrated ids to them. */
public final class ConsolidatedItems {
    private static final Logger LOG = LoggerFactory.getLogger("diaithircombat/items");
    private static final String BASE = "/data/diaithircombat/consolidated/";
    private static final Set<String> DATA_ONLY = Set.of("spell", "skill", "enchant", "event");
    private static final Set<String> STACK_ONE = Set.of("weapon", "armor", "relic");

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DiaithirCombat.ID);
    private static final Set<String> REGISTERED = new HashSet<>();
    private static final Map<String, DeferredItem<Item>> BY_PATH = new java.util.HashMap<>();

    private ConsolidatedItems() {}

    static void register(IEventBus bus) {
        Gson gson = new Gson();
        String[] names = read(gson, "index.json", String[].class);
        int count = 0;
        if (names != null) {
            for (String n : names) {
                JsonObject bundle = read(gson, n, JsonObject.class);
                if (bundle == null || !bundle.has("items") || !bundle.get("items").isJsonArray()) {
                    continue;
                }
                for (JsonElement el : bundle.getAsJsonArray("items")) {
                    if (!el.isJsonObject()) {
                        continue;
                    }
                    if (add(el.getAsJsonObject())) {
                        count++;
                    }
                }
            }
        }
        ITEMS.register(bus);
        bus.addListener(ConsolidatedItems::commonSetup);
        LOG.info("registered {} consolidated items", count);
    }

    private static boolean add(JsonObject o) {
        String kind = o.has("kind") && !o.get("kind").isJsonNull() ? o.get("kind").getAsString() : "";
        if (DATA_ONLY.contains(kind) || !o.has("id") || o.get("id").isJsonNull()) {
            return false;
        }
        String path = sanitize(o.get("id").getAsString());
        if (path.isEmpty() || !REGISTERED.add(path)) {
            return false;
        }
        int stack = STACK_ONE.contains(kind) ? 1 : "rune".equals(kind) ? 16 : 64;
        DeferredItem<Item> holder = ITEMS.registerSimpleItem(path, p -> p.stacksTo(stack));
        BY_PATH.put(path, holder);
        return true;
    }

    private static void commonSetup(FMLCommonSetupEvent e) {
        e.enqueueWork(ConsolidatedItems::aliases);
    }

    private static void aliases() {
        int done = 0;
        for (Map.Entry<String, String> en : Consolidated.ID_MIGRATION.entrySet()) {
            String oldId = en.getKey();
            if (!oldId.contains(":")) {
                continue;
            }
            String newPath = sanitize(en.getValue());
            if (!BY_PATH.containsKey(newPath)) {
                continue;
            }
            int colon = oldId.indexOf(':');
            String oldNs = sanitize(oldId.substring(0, colon));
            String oldPath = sanitize(oldId.substring(colon + 1));
            if (oldNs.isEmpty() || oldPath.isEmpty()) {
                continue;
            }
            try {
                Identifier from = Identifier.fromNamespaceAndPath(oldNs, oldPath);
                Identifier to = Identifier.fromNamespaceAndPath(DiaithirCombat.ID, newPath);
                if (BuiltInRegistries.ITEM instanceof net.minecraft.core.MappedRegistry<Item> mapped) {
                    mapped.addAlias(from, to);
                    done++;
                } else {
                    LOG.info("alias unsupported, {} -> {}", from, to);
                }
            } catch (Throwable t) {
                LOG.info("alias {} -> {} skipped: {}", oldId, newPath, t.toString());
            }
        }
        LOG.info("{} item aliases", done);
    }

    private static String sanitize(String raw) {
        String s = raw == null ? "" : raw;
        int colon = s.indexOf(':');
        if (colon >= 0) {
            s = s.substring(colon + 1);
        }
        s = s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "_");
        return s;
    }

    private static <T> T read(Gson gson, String path, Class<T> type) {
        try (var in = ConsolidatedItems.class.getResourceAsStream(BASE + path)) {
            return in == null ? null : gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), type);
        } catch (Exception e) {
            LOG.warn("bad bundle {}: {}", path, e.toString());
            return null;
        }
    }
}
