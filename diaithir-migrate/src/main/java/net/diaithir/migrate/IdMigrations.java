package net.diaithir.migrate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The single id-migration loader. Every module may ship {@code data/<modid>/id_migrations.json}, a list of
 * {@code {"registry": "minecraft:block", "old": "oldmod:thing", "new": "newmod:thing"}}.
 * NeoForge 26.1 has no missing-mappings event, so each entry becomes a registry alias
 * ({@code Registry#addAlias}) added while that registry is being filled, after every other mod has registered
 * (lowest priority). Saved worlds, recipes, loot tables and tags that still name the old id then resolve to the new one.
 * An alias is skipped (and logged) when the old id is still registered or the new id is not.
 */
@Mod("diaithirmigrate")
public final class IdMigrations {
    private static final Logger LOG = LoggerFactory.getLogger("diaithirmigrate");
    private static final Map<Identifier, List<Identifier[]>> BY_REGISTRY = new HashMap<>();
    private static boolean loaded;

    public IdMigrations(IEventBus modBus) {
        modBus.addListener(EventPriority.LOWEST, IdMigrations::onRegister);
    }

    private static synchronized void load() {
        if (loaded) return;
        loaded = true;
        int files = 0, entries = 0;
        for (var info : ModList.get().getMods()) {
            String path = "data/" + info.getModId() + "/id_migrations.json";
            var contents = info.getOwningFile().getFile().getContents();
            if (contents.findFile(path).isEmpty()) continue;
            files++;
            try (Reader r = new InputStreamReader(contents.openFile(path), StandardCharsets.UTF_8)) {
                JsonArray arr = JsonParser.parseReader(r).getAsJsonArray();
                for (JsonElement e : arr) {
                    var o = e.getAsJsonObject();
                    Identifier reg = Identifier.parse(o.get("registry").getAsString());
                    BY_REGISTRY.computeIfAbsent(reg, k -> new ArrayList<>())
                            .add(new Identifier[]{Identifier.parse(o.get("old").getAsString()), Identifier.parse(o.get("new").getAsString())});
                    entries++;
                }
            } catch (Exception ex) {
                LOG.error("id_migrations.json of {} is invalid: {}", info.getModId(), ex.toString());
            }
        }
        LOG.info("Loaded {} id migrations from {} files", entries, files);
    }

    private static void onRegister(RegisterEvent event) {
        load();
        Identifier key = event.getRegistryKey().identifier();
        List<Identifier[]> list = BY_REGISTRY.get(key);
        if (list == null) return;
        Registry<?> registry = event.getRegistry();
        int ok = 0;
        for (Identifier[] m : list) {
            if (registry.containsKey(m[0])) { LOG.warn("{}: old id {} is still registered, no alias to {}", key, m[0], m[1]); continue; }
            if (!registry.containsKey(m[1])) { LOG.error("{}: target {} of migration {} does not exist", key, m[1], m[0]); continue; }
            registry.addAlias(m[0], m[1]);
            ok++;
        }
        LOG.info("{}: {} of {} aliases added", key, ok, list.size());
    }
}
