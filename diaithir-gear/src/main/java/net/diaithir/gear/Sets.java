package net.diaithir.gear;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLPaths;

/** One Age-gated bonus per set family. Families: data/diaithirgear/set_bonuses/&lt;id&gt;.json (override: config/diaithirgear/set_bonuses). */
public final class Sets {
    public record Mod(Identifier attribute, AttributeModifier.Operation op, double amount) {}
    public record Tier(int pieces, List<Mod> mods) {}

    public static final class Family {
        public final String id;
        public final TagKey<Item> tag;
        public final int age;
        public final boolean curios;
        public final List<Tier> tiers = new ArrayList<>();

        Family(JsonObject o) {
            id = o.get("id").getAsString();
            tag = TagKey.create(Registries.ITEM, Identifier.parse(o.get("tag").getAsString()));
            age = o.get("age").getAsInt();
            curios = o.has("curios") && o.get("curios").getAsBoolean();
            for (var e : o.getAsJsonArray("tiers")) {
                JsonObject t = e.getAsJsonObject();
                List<Mod> ms = new ArrayList<>();
                for (var a : t.getAsJsonArray("attributes")) {
                    JsonObject m = a.getAsJsonObject();
                    ms.add(new Mod(Identifier.parse(m.get("attribute").getAsString()), op(m.get("operation").getAsString()), m.get("amount").getAsDouble()));
                }
                tiers.add(new Tier(t.get("pieces").getAsInt(), List.copyOf(ms)));
            }
            tiers.sort((x, y) -> Integer.compare(x.pieces, y.pieces));
        }

        public int count(LivingEntity e) {
            int n = 0;
            for (EquipmentSlot s : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                if (e.getItemBySlot(s).is(tag)) n++;
            }
            if (curios) n += Curios.worn(e, st -> st.is(tag)).size();
            return n;
        }

        /** Highest tier index reached (-1 none). Tiers are cumulative: one bonus, each tier replaces the lower one. */
        public int tierFor(int pieces) {
            int r = -1;
            for (int i = 0; i < tiers.size(); i++) if (pieces >= tiers.get(i).pieces) r = i;
            return r;
        }
    }

    private static AttributeModifier.Operation op(String s) {
        return switch (s) {
            case "add_multiplied_base" -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
            case "add_multiplied_total" -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
            default -> AttributeModifier.Operation.ADD_VALUE;
        };
    }

    private static final Map<String, Family> ALL = new LinkedHashMap<>();
    private static final Map<UUID, Map<String, Integer>> APPLIED = new ConcurrentHashMap<>();
    private static final java.util.Set<Identifier> MISSING = ConcurrentHashMap.newKeySet();

    private Sets() {}

    public static Map<String, Family> all() { return ALL; }

    public static void load() {
        ALL.clear();
        Gson g = new Gson();
        String base = "/data/" + DiaithirGear.ID + "/set_bonuses/";
        try (Reader r = new InputStreamReader(Sets.class.getResourceAsStream(base + "index.json"))) {
            JsonArray idx = g.fromJson(r, JsonArray.class);
            Path dir = FMLPaths.CONFIGDIR.get().resolve("diaithirgear/set_bonuses");
            for (var e : idx) {
                String id = e.getAsString();
                JsonObject o;
                Path ov = dir.resolve(id + ".json");
                if (Files.exists(ov)) {
                    try (Reader rr = Files.newBufferedReader(ov)) { o = g.fromJson(rr, JsonObject.class); }
                } else {
                    try (Reader rr = new InputStreamReader(Sets.class.getResourceAsStream(base + id + ".json"))) { o = g.fromJson(rr, JsonObject.class); }
                }
                ALL.put(id, new Family(o));
            }
        } catch (Exception ex) {
            throw new IllegalStateException("diaithirgear set_bonuses", ex);
        }
    }

    public static int ageOf(Player p) {
        int age = GearConfig.DEFAULT_AGE.getAsInt();
        try {
            var sb = p.level().getScoreboard();
            var obj = sb.getObjective(GearConfig.AGE_OBJECTIVE.get());
            if (obj != null) {
                var info = sb.getPlayerScoreInfo(p, obj);
                if (info != null && info.value() > 0) age = info.value();
            }
        } catch (RuntimeException ignored) { }
        return Mth.clamp(age, 1, 9);
    }

    static Identifier modId(String family, int attrIndex) {
        return Identifier.fromNamespaceAndPath(DiaithirGear.ID, "set/" + family + "/" + attrIndex);
    }

    private static AttributeInstance inst(LivingEntity e, Identifier id) {
        var h = BuiltInRegistries.ATTRIBUTE.get(id);
        if (h.isEmpty()) {
            if (MISSING.add(id)) DiaithirGear.LOG.info("Set bonus attribute {} is not registered, skipped", id);
            return null;
        }
        Holder<Attribute> hh = h.get();
        return e.getAttribute(hh);
    }

    /** Recompute the bonuses of one player. Returns the tier index per family it ended on. */
    public static Map<String, Integer> update(Player p) {
        Map<String, Integer> now = new LinkedHashMap<>();
        boolean on = GearConfig.SETS_ON.getAsBoolean();
        int age = GearConfig.IGNORE_AGE.getAsBoolean() ? 9 : ageOf(p);
        Map<String, Integer> prev = APPLIED.getOrDefault(p.getUUID(), Map.of());
        for (Family f : ALL.values()) {
            int tier = on && age >= f.age ? f.tierFor(f.count(p)) : -1;
            now.put(f.id, tier);
            if (prev.getOrDefault(f.id, -2) == tier) continue;
            for (Tier t : f.tiers) {
                for (int i = 0; i < t.mods.size(); i++) {
                    AttributeInstance ai = inst(p, t.mods.get(i).attribute);
                    if (ai != null) ai.removeModifier(modId(f.id, f.tiers.indexOf(t) * 16 + i));
                }
            }
            if (tier >= 0) {
                Tier t = f.tiers.get(tier);
                for (int i = 0; i < t.mods.size(); i++) {
                    Mod m = t.mods.get(i);
                    AttributeInstance ai = inst(p, m.attribute);
                    if (ai != null) ai.addOrUpdateTransientModifier(new AttributeModifier(modId(f.id, tier * 16 + i), m.amount, m.op));
                }
            }
        }
        APPLIED.put(p.getUUID(), now);
        return now;
    }

    public static void forget(Player p) { APPLIED.remove(p.getUUID()); }
}
