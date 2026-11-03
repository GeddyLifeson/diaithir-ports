package net.diaithir.classes;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers the simple placeholder-free items described by the consolidated content bundles (weapons, armour, relics, runes,
 * blocks-as-items and anything else that is not data-only) and wires up registry aliases for the old-id to new-id migration map.
 */
public final class ConsolidatedItems {
    private static final Logger LOG = LoggerFactory.getLogger("diaithir_classes/consolidated_items");
    private static final String BASE = "/data/diaithir_classes/consolidated/";
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DiaithirClasses.ID);
    private static final Set<String> REGISTERED = new HashSet<>();
    private static final Map<String, DeferredItem<Item>> BUNDLE_ITEMS = new java.util.LinkedHashMap<>();
    private static final Set<String> DATA_ONLY = Set.of("spell", "skill", "enchant", "event");

    private ConsolidatedItems() {}

    static void register(IEventBus bus) {
        List<JsonObject> bundles = readBundles();
        Set<String> taken = new HashSet<>();
        for (var spec : DiaithirClasses.DATA.items()) {
            taken.add(sanitize(spec.id()));
        }
        for (String part : DiaithirClasses.DATA.parts()) {
            taken.add(sanitize(part));
        }
        for (var rune : DiaithirClasses.DATA.runes()) {
            taken.add(sanitize(rune.id()));
        }
        for (var ring : DiaithirClasses.DATA.rings()) {
            taken.add(sanitize(ring.id()));
        }
        for (JsonObject bundle : bundles) {
            if (!bundle.has("items") || !bundle.get("items").isJsonArray()) {
                continue;
            }
            for (JsonElement el : bundle.getAsJsonArray("items")) {
                if (!el.isJsonObject()) {
                    continue;
                }
                JsonObject o = el.getAsJsonObject();
                if (!o.has("id") || !o.has("kind")) {
                    continue;
                }
                String kind = o.get("kind").getAsString().toLowerCase(Locale.ROOT);
                if (DATA_ONLY.contains(kind)) {
                    continue;
                }
                String id = sanitize(o.get("id").getAsString());
                if (id.isEmpty()) {
                    continue;
                }
                if (taken.contains(id) || !REGISTERED.add(id)) {
                    LOG.debug("skipping duplicate item id {}", id);
                    continue;
                }
                int stack = switch (kind) {
                    case "weapon", "armor", "relic" -> 1;
                    case "rune" -> 16;
                    default -> 64;
                };
                BUNDLE_ITEMS.put(id, ITEMS.registerItem(id, Item::new, p -> p.stacksTo(stack)));
            }
        }
        ITEMS.register(bus);
        bus.addListener(ConsolidatedItems::onCommonSetup);
        LOG.info("registered {} consolidated items", BUNDLE_ITEMS.size());
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ConsolidatedItems::registerAliases);
    }

    private static void registerAliases() {
        int added = 0;
        for (Map.Entry<String, String> e : Consolidated.ID_MIGRATION.entrySet()) {
            String oldRaw = e.getKey();
            if (oldRaw.indexOf(':') < 0) {
                continue;
            }
            String newPath = sanitize(stripNamespace(e.getValue()));
            if (newPath.isEmpty() || !BUNDLE_ITEMS.containsKey(newPath)) {
                continue;
            }
            int colon = oldRaw.indexOf(':');
            String oldNs = sanitize(oldRaw.substring(0, colon));
            String oldPath = sanitize(oldRaw.substring(colon + 1));
            if (oldNs.isEmpty() || oldPath.isEmpty()) {
                continue;
            }
            Identifier from = Identifier.fromNamespaceAndPath(oldNs, oldPath);
            Identifier to = Identifier.fromNamespaceAndPath(DiaithirClasses.ID, newPath);
            if (from.equals(to)) {
                continue;
            }
            try {
                if (BuiltInRegistries.ITEM instanceof MappedRegistry<Item> mapped) {
                    mapped.addAlias(from, to);
                    added++;
                } else {
                    LOG.info("item registry is not a MappedRegistry, alias {} -> {} not added", from, to);
                }
            } catch (Exception ex) {
                LOG.warn("alias {} -> {} failed: {}", from, to, ex.toString());
            }
        }
        LOG.info("registered {} item aliases", added);
    }

    private static String stripNamespace(String s) {
        int i = s.indexOf(':');
        return i < 0 ? s : s.substring(i + 1);
    }

    /** Lower-cases, strips diacritics and reduces the id to [a-z0-9_]. */
    static String sanitize(String raw) {
        String n = Normalizer.normalize(raw, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        StringBuilder sb = new StringBuilder(n.length());
        for (char c : n.toLowerCase(Locale.ROOT).toCharArray()) {
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_') {
                sb.append(c);
            } else if (c == ' ' || c == '-' || c == '.' || c == '/') {
                sb.append('_');
            }
        }
        return sb.toString();
    }

    private static List<JsonObject> readBundles() {
        List<JsonObject> out = new ArrayList<>();
        Gson gson = new Gson();
        String[] names = read(gson, "index.json", String[].class);
        if (names == null) {
            return out;
        }
        for (String n : names) {
            JsonObject o = read(gson, n, JsonObject.class);
            if (o != null) {
                out.add(o);
            }
        }
        return out;
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
