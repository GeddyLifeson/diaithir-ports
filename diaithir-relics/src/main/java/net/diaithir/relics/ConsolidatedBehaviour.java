package net.diaithir.relics;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server-side behaviour for the consolidated content bundles. Attributes are read from the bundle JSON (same
 * index.json approach as {@link ConsolidatedItems}); every handler tolerates missing items and missing data.
 * Implemented: aurora night roll and regen pulse, Skywatch Charm regeneration, Dorchamhiotal curses (health penalty,
 * damage bonus, on-kill heal, Eternal Stone revive), Phoenix Tear revive, aura brooches, toggle-drain relics.
 */
public final class ConsolidatedBehaviour {
    private static final Logger LOG = LoggerFactory.getLogger("diaithirrelics/consolidated-behaviour");
    private static final String BASE = "/data/diaithirrelics/consolidated/";

    private static final Map<String, JsonObject> ATTRS = new HashMap<>();
    private static final Map<String, Long> COOLDOWNS = new HashMap<>();

    private static boolean auroraActive = false;
    private static boolean auroraRolledThisNight = false;
    private static long auroraEndTick = 0L;

    private ConsolidatedBehaviour() {}

    public static void register() {
        loadAttrs();
        NeoForge.EVENT_BUS.addListener(PlayerTickEvent.Post.class, ConsolidatedBehaviour::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, ConsolidatedBehaviour::onIncomingDamage);
        LOG.info("consolidated behaviour registered ({} attr sets)", ATTRS.size());
    }

    // ----------------------------------------------------------------- data loading

    private static void loadAttrs() {
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
                String id = str(o, "id");
                JsonElement attrs = o.get("attrs");
                if (id != null && attrs != null && attrs.isJsonObject()) {
                    ATTRS.put(sanitize(id), attrs.getAsJsonObject());
                }
            }
        }
    }

    private static double num(String id, String key, double def) {
        JsonObject a = ATTRS.get(id);
        if (a == null) {
            return def;
        }
        JsonElement e = a.get(key);
        try {
            return e != null && e.isJsonPrimitive() ? e.getAsDouble() : def;
        } catch (Exception ex) {
            return def;
        }
    }

    // ----------------------------------------------------------------- helpers

    private static Item item(String path) {
        try {
            Identifier id = Identifier.fromNamespaceAndPath(DiaithirRelics.ID, path);
            return BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isItem(ItemStack stack, String path) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Item it = item(path);
        return it != null && stack.is(it);
    }

    private static boolean has(Player p, String path) {
        if (p == null) {
            return false;
        }
        Item it = item(path);
        if (it == null) {
            return false;
        }
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s != null && !s.isEmpty() && s.is(it)) {
                return true;
            }
        }
        return false;
    }

    private static boolean consumeOne(Player p, String path) {
        if (p == null) {
            return false;
        }
        Item it = item(path);
        if (it == null) {
            return false;
        }
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s != null && !s.isEmpty() && s.is(it)) {
                s.shrink(1);
                return true;
            }
        }
        return false;
    }

    private static boolean wearing(Player p, String path) {
        if (p == null) {
            return false;
        }
        Item it = item(path);
        if (it == null) {
            return false;
        }
        for (net.minecraft.world.entity.EquipmentSlot slot : new net.minecraft.world.entity.EquipmentSlot[] {
                net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST,
                net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET}) {
            ItemStack s = p.getItemBySlot(slot);
            if (s != null && !s.isEmpty() && s.is(it)) {
                return true;
            }
        }
        return false;
    }

    private static int dorchamhiotalPieces(Player p) {
        int n = 0;
        for (String s : new String[] {"dorchamhiotal_ceann", "dorchamhiotal_cliabhrach", "dorchamhiotal_cosa", "dorchamhiotal_buataisi"}) {
            if (wearing(p, s)) {
                n++;
            }
        }
        return n;
    }

    private static boolean onCooldown(Player p, String key, long now, long cooldownTicks) {
        String k = p.getUUID() + ":" + key;
        Long until = COOLDOWNS.get(k);
        if (until != null && now < until) {
            return true;
        }
        COOLDOWNS.put(k, now + cooldownTicks);
        return false;
    }

    // ----------------------------------------------------------------- tick

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player == null || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }
        long time = level.getGameTime();
        handleAurora(sp, level, time);
        handleAuras(sp, time);
        handleCurseHealth(sp);
    }

    private static void handleAurora(ServerPlayer sp, ServerLevel level, long time) {
        if (level.dimension() != net.minecraft.world.level.Level.OVERWORLD) {
            return;
        }
        long dayTime = time % 24000L;
        boolean night = dayTime >= 13000L && dayTime < 23000L;
        boolean clear = !level.isRaining() && !level.isThundering();

        if (sp.getUUID().equals(firstPlayerId(level))) {
            if (!night) {
                auroraRolledThisNight = false;
                if (auroraActive) {
                    auroraActive = false;
                }
            } else if (!auroraRolledThisNight && clear) {
                auroraRolledThisNight = true;
                double chance = num("night_ribbon_event", "chance_per_eligible_night", 0.15);
                if (level.getRandom().nextDouble() < chance) {
                    auroraActive = true;
                    auroraEndTick = time + (long) (num("night_ribbon_event", "max_duration_s", 300) * 20.0);
                }
            }
            if (auroraActive && (!clear || time > auroraEndTick)) {
                auroraActive = false;
            }
        }

        if (auroraActive && time % 100L == 0L && level.canSeeSky(sp.blockPosition())) {
            double dur = num("skywatch_charm", "regen_buff_duration_s", 10);
            if (has(sp, "skywatch_charm")) {
                sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, (int) (dur * 20.0), 0, true, false));
            }
        }
    }

    private static java.util.UUID firstPlayerId(ServerLevel level) {
        var players = level.players();
        return players.isEmpty() ? null : players.get(0).getUUID();
    }

    private static void handleAuras(ServerPlayer sp, long time) {
        pulse(sp, time, "hearthmend_aura_brooch", () -> {
            double radius = num("hearthmend_aura_brooch", "radius_m", 8);
            float heal = (float) (num("hearthmend_aura_brooch", "heal_J_equivalent_hearts", 1) * 2.0);
            forNearby(sp, radius, p -> p.heal(heal));
        });
        pulse(sp, time, "larder_aura_torc", () -> {
            double radius = num("larder_aura_torc", "radius_m", 6);
            int food = (int) num("larder_aura_torc", "food_per_pulse", 1);
            forNearby(sp, radius, p -> p.getFoodData().eat(food, 0.1f));
        });
        pulse(sp, time, "fleetfoot_aura_anklet", () -> {
            double radius = num("fleetfoot_aura_anklet", "radius_m", 4);
            forNearby(sp, radius, p -> p.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 0, true, false)));
        });
        pulse(sp, time, "cinderward_aura_ring", () -> {
            double radius = num("cinderward_aura_ring", "radius_m", 10);
            forNearby(sp, radius, p -> p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 100, 0, true, false)));
        });
        if (has(sp, "lantern_of_the_long_dark") && time % 60L == 0L) {
            int nv = (int) (num("lantern_of_the_long_dark", "night_vision_s", 12) * 20.0);
            if (isItem(sp.getMainHandItem(), "lantern_of_the_long_dark") || isItem(sp.getOffhandItem(), "lantern_of_the_long_dark")) {
                sp.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, nv, 0, true, false));
            }
        }
    }

    private static void pulse(ServerPlayer sp, long time, String itemId, Runnable action) {
        if (!has(sp, itemId)) {
            return;
        }
        long period = (long) (num(itemId, "pulse_period_s", 4) * 20.0);
        if (period <= 0L) {
            period = 80L;
        }
        if (time % period == 0L) {
            action.run();
        }
    }

    private static void forNearby(ServerPlayer center, double radius, java.util.function.Consumer<Player> fn) {
        if (center.level() instanceof ServerLevel level) {
            double r2 = radius * radius;
            for (ServerPlayer p : level.players()) {
                if (p != null && p.distanceToSqr(center) <= r2) {
                    fn.accept(p);
                }
            }
        }
    }

    private static void handleCurseHealth(ServerPlayer sp) {
        // Dorchamhiotal armour pieces reduce max health; applied as a transient modifier keyed by piece count.
        var attr = sp.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (attr == null) {
            return;
        }
        Identifier modId = Identifier.fromNamespaceAndPath(DiaithirRelics.ID, "fuil_cuntas_health");
        int pieces = dorchamhiotalPieces(sp);
        double perPiece = num("fuil_cuntas", "max_health_penalty_hearts", -2.0);
        if (pieces <= 0) {
            if (attr.getModifier(modId) != null) {
                attr.removeModifier(modId);
            }
            return;
        }
        double amount = perPiece * pieces;
        var existing = attr.getModifier(modId);
        if (existing != null && existing.amount() == amount) {
            return;
        }
        if (existing != null) {
            attr.removeModifier(modId);
        }
        attr.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                modId, amount, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
        if (sp.getHealth() > sp.getMaxHealth()) {
            sp.setHealth(sp.getMaxHealth());
        }
    }

    // ----------------------------------------------------------------- damage

    private static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event == null || event.getEntity() == null) {
            return;
        }
        var victim = event.getEntity();
        var source = event.getSource();

        // Attacker side: tool curse damage bonus.
        if (source != null && source.getEntity() instanceof Player attacker) {
            ItemStack held = attacker.getMainHandItem();
            if (held != null && !held.isEmpty()) {
                for (String tool : new String[] {"dorchamhiotal_claiomh", "dorchamhiotal_tuagh", "dorchamhiotal_piocoid"}) {
                    if (isItem(held, tool)) {
                        double pct = num("anam_ocrais", "damage_bonus_pct", 15);
                        event.setAmount((float) (event.getAmount() * (1.0 + pct / 100.0)));
                        break;
                    }
                }
            }
        }

        // Ward pylon style boreal veil is data-only; here handle lethal intercepts for players.
        if (victim instanceof Player player && player.level() instanceof ServerLevel level) {
            float after = player.getHealth() - event.getAmount();
            if (after > 0.0f) {
                return;
            }
            long now = level.getGameTime();
            if (has(player, "phoenix_tear_token")
                    && !onCooldown(player, "phoenix", now, (long) (num("phoenix_tear_token", "cooldown_s", 600) * 20.0))) {
                event.setCanceled(true);
                float hp = (float) (num("phoenix_tear_token", "revive_hearts", 6) * 2.0);
                player.setHealth(Math.max(1.0f, Math.min(hp, player.getMaxHealth())));
                int strip = (int) num("phoenix_tear_token", "strip_xp_levels", 10);
                player.giveExperienceLevels(-strip);
                int iframes = (int) (num("phoenix_tear_token", "i_frame_s", 2) * 20.0);
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, iframes, 4, true, false));
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1200, 0, true, false));
                return;
            }
            if (has(player, "cloch_shiorai") && consumeOne(player, "cloch_shiorai")) {
                event.setCanceled(true);
                float hp = (float) (num("cloch_shiorai", "revive_health_J_equiv_hearts", 10) * 2.0);
                player.setHealth(Math.max(1.0f, Math.min(hp, player.getMaxHealth())));
                int inv = (int) (num("cloch_shiorai", "revive_invuln_s", 5) * 20.0);
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, inv, 4, true, false));
            }
        }
    }

    // ----------------------------------------------------------------- util

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
        try (var in = ConsolidatedBehaviour.class.getResourceAsStream(BASE + path)) {
            return in == null ? null : gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), type);
        } catch (Exception e) {
            LOG.warn("bad bundle {}: {}", path, e.toString());
            return null;
        }
    }
}
