package net.diaithir.relics;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
 * Registers the simple items described by the consolidated content bundles (everything that is not data only and not already
 * registered by {@link DiaithirRelics}) and wires the old-id to new-id aliases from {@link Consolidated#ID_MIGRATION}.
 */
public final class ConsolidatedItems {
    private static final Logger LOG = LoggerFactory.getLogger("diaithirrelics/consolidated-items");
    private static final String BASE = "/data/diaithirrelics/consolidated/";
    private static final Set<String> DATA_ONLY = Set.of("spell", "skill", "enchant", "event");
    private static final Set<String> SINGLE = Set.of("weapon", "armor", "relic");

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DiaithirRelics.ID);
    private static final Map<String, DeferredItem<Item>> REGISTERED = new LinkedHashMap<>();

    private ConsolidatedItems() {}

    static void register(IEventBus bus) {
        Gson gson = new Gson();
        Set<String> seen = new HashSet<>();
        for (DiaithirRelics.Def d : DiaithirRelics.DATA.items()) {
            seen.add(d.id());
        }
        String[] names = read(gson, "index.json", String[].class);
        if (names != null) {
            for (String n : names) {
                JsonObject bundle = read(gson, n, JsonObject.class);
                if (bundle == null || !bundle.has("items") || !bundle.get("items").isJsonArray()) {
                    continue;
                }
                for (JsonElement el : bundle.getAsJsonArray("items")) {
                    if (el.isJsonObject()) {
                        registerOne(el.getAsJsonObject(), seen);
                    }
                }
            }
        }
        ITEMS.register(bus);
        bus.addListener(FMLCommonSetupEvent.class, e -> e.enqueueWork(ConsolidatedItems::registerAliases));
        LOG.info("registered {} consolidated items", REGISTERED.size());
    }

    private static void registerOne(JsonObject o, Set<String> seen) {
        String rawId = str(o, "id");
        if (rawId == null) {
            return;
        }
        String kind = str(o, "kind");
        if (kind == null) {
            kind = "";
        }
        kind = kind.toLowerCase(Locale.ROOT);
        if (DATA_ONLY.contains(kind)) {
            return;
        }
        String id = sanitize(rawId);
        if (id.isEmpty() || !seen.add(id)) {
            if (!id.isEmpty()) {
                LOG.debug("skipping duplicate id {}", id);
            }
            return;
        }
        int stack = SINGLE.contains(kind) ? 1 : kind.equals("rune") ? 16 : 64;
        DeferredItem<Item> item = ITEMS.registerSimpleItem(id, p -> p.stacksTo(stack));
        REGISTERED.put(id, item);
    }

    private static void registerAliases() {
        if (!(BuiltInRegistries.ITEM instanceof MappedRegistry<Item> reg)) {
            LOG.info("item registry is not a MappedRegistry, skipping {} id aliases", Consolidated.ID_MIGRATION.size());
            return;
        }
        int ok = 0;
        for (Map.Entry<String, String> e : Consolidated.ID_MIGRATION.entrySet()) {
            String old = e.getKey();
            if (old == null || old.indexOf(':') < 0) {
                continue;
            }
            String target = e.getValue();
            if (target == null) {
                continue;
            }
            int c = target.indexOf(':');
            String targetPath = sanitize(c >= 0 ? target.substring(c + 1) : target);
            if (!REGISTERED.containsKey(targetPath) && !isMainItem(targetPath)) {
                LOG.debug("alias target {} not registered, skipping {}", targetPath, old);
                continue;
            }
            int oc = old.indexOf(':');
            String oldNs = sanitize(old.substring(0, oc));
            String oldPath = sanitize(old.substring(oc + 1));
            if (oldNs.isEmpty() || oldPath.isEmpty()) {
                continue;
            }
            try {
                reg.addAlias(Identifier.fromNamespaceAndPath(oldNs, oldPath), Identifier.fromNamespaceAndPath(DiaithirRelics.ID, targetPath));
                ok++;
            } catch (Exception ex) {
                LOG.warn("alias {} -> {} failed: {}", old, targetPath, ex.toString());
            }
        }
        LOG.info("{} item aliases registered", ok);
    }

    private static boolean isMainItem(String path) {
        for (DiaithirRelics.Def d : DiaithirRelics.DATA.items()) {
            if (d.id().equals(path)) {
                return true;
            }
        }
        return false;
    }

    private static String sanitize(String s) {
        String lower = s.toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder(lower.length());
        for (int i = 0; i < lower.length(); i++) {
            char ch = lower.charAt(i);
            sb.append((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || ch == '_' ? ch : '_');
        }
        return sb.toString();
    }

    private static String str(JsonObject o, String key) {
        JsonElement e = o.get(key);
        return e != null && e.isJsonPrimitive() ? e.getAsString() : null;
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
