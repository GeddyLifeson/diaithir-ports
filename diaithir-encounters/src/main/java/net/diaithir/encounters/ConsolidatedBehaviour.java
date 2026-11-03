package net.diaithir.encounters;

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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server-side behaviour for consolidated items. Numeric attrs are read from the same bundle JSON
 * that ConsolidatedItems uses, and handlers are keyed by the registered item ids.
 */
public final class ConsolidatedBehaviour {
    private static final Logger LOG = LoggerFactory.getLogger("diaithirencounters/behaviour");
    private static final String BASE = "/data/diaithirencounters/consolidated/";
    private static final String NS = DiaithirEncounters.ID;

    /** item path -> attrs object, loaded from bundles. */
    private static final Map<String, JsonObject> ATTRS = new HashMap<>();
    /** per-player cooldown end (game time) keyed by player uuid + ":" + item path. */
    private static final Map<String, Long> COOLDOWNS = new HashMap<>();

    private ConsolidatedBehaviour() {}

    public static void register() {
        load();
        NeoForge.EVENT_BUS.addListener(ConsolidatedBehaviour::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(ConsolidatedBehaviour::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(ConsolidatedBehaviour::onUseItem);
        LOG.info("consolidated behaviour registered, {} attr sets", ATTRS.size());
    }

    // ------------------------------------------------------------------ data

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
                if (!o.has("id") || o.get("id").isJsonNull() || !o.has("attrs") || !o.get("attrs").isJsonObject()) {
                    continue;
                }
                ATTRS.put(sanitize(o.get("id").getAsString()), o.getAsJsonObject("attrs"));
            }
        }
    }

    private static double num(String path, String key, double def) {
        JsonObject a = ATTRS.get(path);
        if (a == null || !a.has(key) || a.get(key).isJsonNull()) {
            return def;
        }
        try {
            return a.get(key).getAsDouble();
        } catch (Exception e) {
            return def;
        }
    }

    private static boolean isItem(ItemStack stack, String path) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && NS.equals(id.getNamespace()) && path.equals(id.getPath());
    }

    private static boolean has(String path) {
        Item it = lookup(path);
        return it != null;
    }

    private static Item lookup(String path) {
        try {
            Identifier id = Identifier.fromNamespaceAndPath(NS, path);
            return BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        } catch (Throwable t) {
            return null;
        }
    }

    // ---------------------------------------------------------------- damage

    private static void onIncomingDamage(LivingIncomingDamageEvent e) {
        if (e == null) {
            return;
        }
        LivingEntity victim = e.getEntity();
        if (victim == null || victim.level().isClientSide()) {
            return;
        }
        float amount = e.getAmount();

        // attacker-side bonuses
        Entity src = e.getSource() == null ? null : e.getSource().getEntity();
        if (src instanceof LivingEntity attacker) {
            ItemStack main = attacker.getMainHandItem();
            if (isItem(main, "caravan_tithe_blade") && isIllager(victim)) {
                amount *= 1.0f + (float) (num("caravan_tithe_blade", "bonus_vs_illager_pct", 25) / 100.0);
            }
            if (isItem(main, "siege_breaker_axe") && victim instanceof Monster) {
                amount *= 1.0f + (float) num("siege_breaker_axe", "bonus_vs_siege_mobs", 0.2);
            }
            if (isItem(main, "caravan_warden_axe") && victim.isBlocking()) {
                // the axe disables shields for a duration
                victim.getUseItem(); // touch to keep behaviour defensive
                if (victim instanceof Player p) {
                    int ticks = (int) Math.round(num("caravan_warden_axe", "shield_disable_s", 3.0) * 20.0);
                    p.getCooldowns().addCooldown(p.getUseItem(), ticks);
                    p.stopUsingItem();
                }
            }
        }

        // victim-side reductions
        if (victim instanceof Player player) {
            if (src instanceof LivingEntity attacker && isIllager(attacker) && hasAnywhere(player, "caravan_ward_charm")) {
                amount *= 1.0f - (float) (num("caravan_ward_charm", "damage_reduction_vs_illagers_pct", 15) / 100.0);
            }
            if (src instanceof Monster && hasAnywhere(player, "ember_ward_rune")) {
                double r = num("ember_ward_rune", "radius_m", 6);
                if (src.distanceToSqr(player) <= r * r) {
                    amount *= 1.0f - (float) num("ember_ward_rune", "damage_reduction", 0.15);
                }
            }
        }

        if (amount != e.getAmount()) {
            e.setAmount(Math.max(0.0f, amount));
        }
    }

    private static boolean isIllager(LivingEntity le) {
        if (le == null) {
            return false;
        }
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(le.getType());
        if (id == null) {
            return false;
        }
        String p = id.getPath();
        return p.equals("pillager") || p.equals("vindicator") || p.equals("evoker")
                || p.equals("illusioner") || p.equals("ravager");
    }

    private static boolean hasAnywhere(Player p, String path) {
        if (p == null) {
            return false;
        }
        if (isItem(p.getMainHandItem(), path) || isItem(p.getOffhandItem(), path)) {
            return true;
        }
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            if (isItem(p.getInventory().getItem(i), path)) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ tick

    private static void onPlayerTick(PlayerTickEvent.Post e) {
        if (e == null) {
            return;
        }
        Player player = e.getEntity();
        if (!(player instanceof ServerPlayer sp) || sp.level().isClientSide()) {
            return;
        }
        if (!(sp.level() instanceof ServerLevel level)) {
            return;
        }
        // run once per second
        if (sp.tickCount % 20 != 0) {
            return;
        }

        ItemStack main = sp.getMainHandItem();
        ItemStack off = sp.getOffhandItem();

        // caravan_ravager_hide: knockback resistance while carried
        if (hasAnywhere(sp, "caravan_ravager_hide")) {
            sp.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 40, 0, true, false, false));
        }

        // lasair_siubhail held: subtle night vision as a light stand-in
        if (isItem(main, "lasair_siubhail") || isItem(off, "lasair_siubhail")) {
            sp.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false, false));
        }

        // lantern_of_dawn in offhand: ignite undead nearby a little sooner in daylight
        if (isItem(off, "lantern_of_dawn") && level.isBrightOutside()) {
            double r = num("lantern_of_dawn", "radius_m", 10);
            AABB box = sp.getBoundingBox().inflate(r);
            for (LivingEntity le : level.getEntitiesOfClass(LivingEntity.class, box)) {
                if (le != null && le.isInvertedHealAndHarm() && le.isAlive()) {
                    le.igniteForSeconds(2.0f);
                }
            }
        }

        // ward banner / brazier style auras are block-entity driven elsewhere; nothing here.
    }

    // ------------------------------------------------------------------- use

    private static void onUseItem(PlayerInteractEvent.RightClickItem e) {
        if (e == null) {
            return;
        }
        Player player = e.getEntity();
        if (!(player instanceof ServerPlayer sp) || sp.level().isClientSide()) {
            return;
        }
        if (!(sp.level() instanceof ServerLevel level)) {
            return;
        }
        ItemStack stack = e.getItemStack();
        if (stack == null || stack.isEmpty()) {
            return;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null || !NS.equals(id.getNamespace())) {
            return;
        }
        String path = id.getPath();
        long now = level.getGameTime();

        switch (path) {
            case "caravan_horn" -> {
                if (!ready(sp, path, now)) {
                    return;
                }
                double r = num(path, "reveals_caravan_radius_m", 128);
                int ticks = (int) Math.round(num(path, "reveal_duration_s", 30) * 20.0);
                for (LivingEntity le : level.getEntitiesOfClass(LivingEntity.class, sp.getBoundingBox().inflate(r))) {
                    if (le != null && le != sp && isIllager(le)) {
                        le.addEffect(new MobEffectInstance(MobEffects.GLOWING, ticks, 0, false, false, false));
                    }
                }
                setCooldown(sp, path, now, num(path, "cooldown_s", 60));
                e.setCancellationResult(InteractionResult.SUCCESS);
                e.setCanceled(true);
            }
            case "dusk_toll", "diaithirnights_dusk_toll" -> {
                if (!ready(sp, path, now)) {
                    return;
                }
                double r = num(path, "range_m", 48);
                int ticks = (int) Math.round(num(path, "duration_s", 10) * 20.0);
                for (LivingEntity le : level.getEntitiesOfClass(Monster.class, sp.getBoundingBox().inflate(r))) {
                    if (le != null) {
                        le.addEffect(new MobEffectInstance(MobEffects.GLOWING, ticks, 0, false, false, false));
                    }
                }
                setCooldown(sp, path, now, num(path, "cooldown_s", 60));
                e.setCancellationResult(InteractionResult.SUCCESS);
                e.setCanceled(true);
            }
            case "tithe_of_dawn" -> {
                // consumed charm grants brief regeneration and absorption as the bonus-roll stand-in
                sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
                sp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 0));
                if (!sp.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                e.setCancellationResult(InteractionResult.SUCCESS);
                e.setCanceled(true);
            }
            case "seol_taistil" -> {
                if (!ready(sp, path, now)) {
                    return;
                }
                double r = num(path, "radius_m", 32);
                for (LivingEntity le : level.getEntitiesOfClass(Monster.class, sp.getBoundingBox().inflate(r))) {
                    if (le instanceof net.minecraft.world.entity.Mob mob && mob.getTarget() == sp) {
                        mob.setTarget(null);
                    }
                }
                setCooldown(sp, path, now, 30.0);
                e.setCancellationResult(InteractionResult.SUCCESS);
                e.setCanceled(true);
            }
            default -> {
                // no behaviour for this id
            }
        }
    }

    private static boolean ready(ServerPlayer sp, String path, long now) {
        Long end = COOLDOWNS.get(sp.getUUID() + ":" + path);
        return end == null || now >= end;
    }

    private static void setCooldown(ServerPlayer sp, String path, long now, double seconds) {
        COOLDOWNS.put(sp.getUUID() + ":" + path, now + Math.round(seconds * 20.0));
    }

    // ----------------------------------------------------------------- utils

    private static String sanitize(String raw) {
        String s = raw == null ? "" : raw;
        int colon = s.indexOf(':');
        if (colon >= 0) {
            s = s.substring(colon + 1);
        }
        return s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "_");
    }

    private static <T> T read(Gson gson, String path, Class<T> type) {
        try (var in = ConsolidatedBehaviour.class.getResourceAsStream(BASE + path)) {
            return in == null ? null : gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), type);
        } catch (Exception ex) {
            LOG.warn("bad bundle {}: {}", path, ex.toString());
            return null;
        }
    }
}
