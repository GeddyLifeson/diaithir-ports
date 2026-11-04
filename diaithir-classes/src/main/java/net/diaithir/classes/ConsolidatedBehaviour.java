package net.diaithir.classes;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data-driven server-side behaviour for the consolidated content bundles. Numeric attrs are read from the bundle
 * JSON (same index.json approach as ConsolidatedItems) and applied by lightweight event handlers that tolerate
 * missing items, missing attrs and null entities.
 *
 * Implemented here (server-side, no client rendering, no foreign APIs):
 *  - Holy damage bonus vs undead from the equipped main hand item (ligichi weapons).
 *  - Spell-power style damage scaling for staves, wands and robes (damage multiplier for magic damage dealt).
 *  - Armour set bonuses (2/4 piece) for the ligichi and robe sets, applied as transient health/absorption effects.
 *  - Overheal to absorption conversion (approximated via periodic regen detection on the player tick).
 *  - Rune fuel presence tracking (low-fuel actionbar warning when holding a staff with 3 or fewer matching runes).
 */
public final class ConsolidatedBehaviour {
    private static final Logger LOG = LoggerFactory.getLogger("diaithir_classes/consolidated_behaviour");
    private static final String BASE = "/data/diaithir_classes/consolidated/";

    /** item id (sanitised path) -> numeric attrs. */
    private static final Map<String, Map<String, Double>> NUMERIC = new HashMap<>();
    /** item id -> string attrs (school, set, slot ...). */
    private static final Map<String, Map<String, String>> TEXT = new HashMap<>();
    /** item id -> kind. */
    private static final Map<String, String> KINDS = new HashMap<>();
    /** set name -> member item ids. */
    private static final Map<String, Set<String>> SETS = new HashMap<>();
    /** Per-player last observed health, used for overheal detection. */
    private static final Map<UUID, Float> LAST_HEALTH = new ConcurrentHashMap<>();
    /** Per-player warning cooldown (game time of last warning). */
    private static final Map<UUID, Long> LAST_WARN = new ConcurrentHashMap<>();

    private static boolean registered;

    private ConsolidatedBehaviour() {}

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        try {
            load();
        } catch (Exception ex) {
            LOG.warn("behaviour data load failed: {}", ex.toString());
        }
        NeoForge.EVENT_BUS.addListener(ConsolidatedBehaviour::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(ConsolidatedBehaviour::onPlayerTick);
        LOG.info("consolidated behaviour registered for {} items, {} sets", NUMERIC.size(), SETS.size());
    }

    // ------------------------------------------------------------------ data loading

    private static void load() {
        Gson gson = new Gson();
        String[] names = read(gson, "index.json", String[].class);
        if (names == null) {
            return;
        }
        for (String n : names) {
            JsonObject bundle = read(gson, n, JsonObject.class);
            if (bundle == null || !bundle.has("items") || !bundle.get("items").isJsonArray()) {
                continue;
            }
            for (JsonElement el : bundle.getAsJsonArray("items")) {
                if (!el.isJsonObject()) {
                    continue;
                }
                JsonObject o = el.getAsJsonObject();
                if (!o.has("id") || !o.has("kind") || !o.has("attrs") || !o.get("attrs").isJsonObject()) {
                    continue;
                }
                String id = ConsolidatedItems.sanitize(o.get("id").getAsString());
                if (id.isEmpty()) {
                    continue;
                }
                String kind = o.get("kind").getAsString().toLowerCase(Locale.ROOT);
                KINDS.put(id, kind);
                Map<String, Double> nums = new HashMap<>();
                Map<String, String> texts = new HashMap<>();
                for (Map.Entry<String, JsonElement> a : o.getAsJsonObject("attrs").entrySet()) {
                    JsonElement v = a.getValue();
                    if (v == null || !v.isJsonPrimitive()) {
                        continue;
                    }
                    if (v.getAsJsonPrimitive().isNumber()) {
                        nums.put(a.getKey(), v.getAsDouble());
                    } else if (v.getAsJsonPrimitive().isString()) {
                        texts.put(a.getKey(), v.getAsString());
                    }
                }
                NUMERIC.put(id, nums);
                TEXT.put(id, texts);
                String set = texts.get("set");
                if (set != null && !set.isEmpty()) {
                    SETS.computeIfAbsent(set, k -> new HashSet<>()).add(id);
                }
            }
        }
    }

    private static <T> T read(Gson gson, String path, Class<T> type) {
        try (var in = ConsolidatedBehaviour.class.getResourceAsStream(BASE + path)) {
            return in == null ? null : gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), type);
        } catch (Exception e) {
            LOG.warn("bad bundle {}: {}", path, e.toString());
            return null;
        }
    }

    // ------------------------------------------------------------------ helpers

    /** Returns the registered path of the item in the stack when it belongs to our namespace, else null. */
    private static String idOf(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        try {
            Item item = stack.getItem();
            Identifier key = BuiltInRegistries.ITEM.getKey(item);
            if (key == null || !DiaithirClasses.ID.equals(key.getNamespace())) {
                return null;
            }
            return key.getPath();
        } catch (Exception e) {
            return null;
        }
    }

    private static double num(String id, String attr, double def) {
        if (id == null) {
            return def;
        }
        Map<String, Double> m = NUMERIC.get(id);
        if (m == null) {
            return def;
        }
        Double v = m.get(attr);
        return v == null ? def : v;
    }

    private static String text(String id, String attr) {
        if (id == null) {
            return null;
        }
        Map<String, String> m = TEXT.get(id);
        return m == null ? null : m.get(attr);
    }

    /** Spell power of an item considering the generic and school-specific attrs. */
    private static double spellPower(String id, String school) {
        if (id == null) {
            return 0.0;
        }
        double sp = num(id, "spell_power", 0.0);
        sp += num(id, "spell_power_all", 0.0);
        if (school != null) {
            sp += num(id, "spell_power_" + school, 0.0);
        }
        return sp;
    }

    private static boolean isUndeadLike(LivingEntity target) {
        if (target == null) {
            return false;
        }
        try {
            return target.is(net.minecraft.tags.EntityTypeTags.UNDEAD);
        } catch (Throwable t) {
            return false;
        }
    }

    private static int setPieces(Player player, String setName) {
        if (player == null || setName == null) {
            return 0;
        }
        Set<String> members = SETS.get(setName);
        if (members == null || members.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (EquipmentSlot slot : new EquipmentSlot[] {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        }) {
            try {
                String id = idOf(player.getItemBySlot(slot));
                if (id != null && members.contains(id)) {
                    count++;
                }
            } catch (Exception ignored) {
                // tolerate odd slot states
            }
        }
        return count;
    }

    /** Sum of spell power across worn armour plus the held weapon for the school. */
    private static double totalSpellPower(Player player, String school) {
        double total = 0.0;
        try {
            total += spellPower(idOf(player.getMainHandItem()), school);
            for (EquipmentSlot slot : new EquipmentSlot[] {
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
            }) {
                String id = idOf(player.getItemBySlot(slot));
                if (id == null) {
                    continue;
                }
                String armourSchool = text(id, "school");
                if (armourSchool == null || school == null || armourSchool.equals(school)) {
                    total += spellPower(id, school);
                }
            }
        } catch (Exception ignored) {
            // tolerate
        }
        return total;
    }

    /** Set bonus spell power (2pc / 4pc) for the known set naming conventions. */
    private static double setBonusSpellPower(Player player) {
        double bonus = 0.0;
        for (String set : SETS.keySet()) {
            int pieces = setPieces(player, set);
            if (pieces < 2) {
                continue;
            }
            if (set.contains("robes")) {
                bonus += 0.5;
                if (pieces >= 4) {
                    bonus += 1.0;
                }
            } else if (set.equals("ligichi_radiant")) {
                bonus += 1.5;
                if (pieces >= 4) {
                    bonus += 1.5;
                }
            } else if (set.equals("ligichi_robed")) {
                bonus += 2.0;
            } else if (set.equals("ligichi_warded") && pieces >= 4) {
                bonus += 1.0;
            }
        }
        return bonus;
    }

    // ------------------------------------------------------------------ damage handling

    private static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event == null) {
            return;
        }
        try {
            LivingEntity victim = event.getEntity();
            if (victim == null || victim.level() == null || victim.level().isClientSide()) {
                return;
            }
            if (event.getSource() == null) {
                return;
            }
            if (!(event.getSource().getEntity() instanceof Player attacker)) {
                return;
            }
            String held = idOf(attacker.getMainHandItem());
            if (held == null) {
                return;
            }
            String kind = KINDS.get(held);
            if (kind == null) {
                return;
            }
            float amount = event.getAmount();
            if (amount <= 0.0f) {
                return;
            }

            String school = text(held, "school");
            double holy = num(held, "spell_power_holy", 0.0);
            boolean castFocus = num(held, "cast_focus", 0.0) > 0.0
                    || "true".equalsIgnoreCase(text(held, "cast_focus"))
                    || holy > 0.0
                    || school != null;

            float multiplier = 1.0f;

            if (castFocus) {
                String effectiveSchool = school != null ? school : (holy > 0.0 ? "holy" : null);
                double sp = totalSpellPower(attacker, effectiveSchool) + setBonusSpellPower(attacker);
                // Each point of spell power adds 5 % damage to focus-wielded hits.
                multiplier += (float) (sp * 0.05);
            }

            if (holy > 0.0 && isUndeadLike(victim)) {
                multiplier *= 1.5f;
            }

            double haste = num(held, "haste_pct", 0.0);
            if (haste > 0.0 && castFocus) {
                multiplier += (float) (haste / 100.0 * 0.25);
            }

            if (multiplier != 1.0f) {
                event.setAmount(amount * multiplier);
            }
        } catch (Exception e) {
            LOG.debug("damage handler skipped: {}", e.toString());
        }
    }

    // ------------------------------------------------------------------ tick handling

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event == null) {
            return;
        }
        try {
            Player player = event.getEntity();
            if (!(player instanceof ServerPlayer sp) || sp.level() == null || sp.level().isClientSide()) {
                return;
            }
            long time = sp.level().getGameTime();
            if (time % 10L != 0L) {
                return;
            }
            handleOverheal(sp);
            if (time % 40L == 0L) {
                handleRuneFuelWarning(sp, time);
            }
        } catch (Exception e) {
            LOG.debug("tick handler skipped: {}", e.toString());
        }
    }

    /**
     * Converts healing beyond max health into absorption for players wearing any ligichi gear. Observed as health
     * sitting at the maximum after a healing event; the converted amount is capped at 1200 J (mapped to 2 points).
     */
    private static void handleOverheal(ServerPlayer player) {
        if (player == null) {
            return;
        }
        UUID uuid = player.getUUID();
        float current = player.getHealth();
        Float prev = LAST_HEALTH.put(uuid, current);
        if (prev == null) {
            return;
        }
        if (!wearsLigichi(player)) {
            return;
        }
        float max = player.getMaxHealth();
        if (current >= max - 0.01f && prev >= max - 0.01f) {
            return;
        }
        if (current >= max - 0.01f && prev < max - 0.01f) {
            float gained = max - prev;
            if (gained > 0.0f) {
                float absorb = Math.min(2.0f, gained * 0.25f);
                float existing = player.getAbsorptionAmount();
                if (existing < 2.0f) {
                    player.setAbsorptionAmount(Math.min(2.0f, existing + absorb));
                }
            }
        }
    }

    private static boolean wearsLigichi(Player player) {
        try {
            for (EquipmentSlot slot : new EquipmentSlot[] {
                EquipmentSlot.MAINHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
            }) {
                String id = idOf(player.getItemBySlot(slot));
                if (id != null && id.startsWith("ligichi_")) {
                    return true;
                }
            }
        } catch (Exception ignored) {
            // tolerate
        }
        return false;
    }

    /** Warns the player on the actionbar when a rune staff is held with 3 or fewer matching fuel runes. */
    private static void handleRuneFuelWarning(ServerPlayer player, long time) {
        if (player == null) {
            return;
        }
        String held = idOf(player.getMainHandItem());
        if (held == null || !held.startsWith("staff_")) {
            return;
        }
        String school = text(held, "school");
        if (school == null) {
            return;
        }
        String elementToken = elementToken(school);
        if (elementToken == null) {
            return;
        }
        int count = 0;
        boolean anyFuelItemKnown = false;
        try {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack s = player.getInventory().getItem(i);
                String id = idOf(s);
                if (id == null || !id.startsWith("fuel_rune_" + elementToken + "_")) {
                    continue;
                }
                anyFuelItemKnown = true;
                count += s.getCount();
            }
        } catch (Exception ignored) {
            return;
        }
        if (!anyFuelItemKnown && !hasFuelDefinition(elementToken)) {
            return;
        }
        if (count > 3) {
            return;
        }
        Long last = LAST_WARN.get(player.getUUID());
        if (last != null && time - last < 200L) {
            return;
        }
        LAST_WARN.put(player.getUUID(), time);
        player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal(
                        "Low " + school + " runes: " + count + " remaining"),
                true);
    }

    private static boolean hasFuelDefinition(String elementToken) {
        for (String id : KINDS.keySet()) {
            if (id.startsWith("fuel_rune_" + elementToken + "_")) {
                return true;
            }
        }
        return false;
    }

    /** Maps a school name to the gaelic element token used by the fuel rune ids. */
    private static String elementToken(String school) {
        return switch (school) {
            case "arcane" -> "ainnir";
            case "fire" -> "tine";
            case "frost" -> "sioc";
            default -> null;
        };
    }

    /** Exposed for diagnostics: ids of every item this class knows behaviour for. */
    static List<String> knownItems() {
        return new ArrayList<>(KINDS.keySet());
    }
}
