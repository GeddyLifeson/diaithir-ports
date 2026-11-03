package net.diaithir.combat;

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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data-driven server-side behaviour for the consolidated items: attribute modifiers read from the bundle attrs,
 * crit / backstab / lance-charge damage handling and a small tick handler. Everything tolerates missing ids.
 */
public final class ConsolidatedBehaviour {
    private static final Logger LOG = LoggerFactory.getLogger("diaithircombat/behaviour");
    private static final String BASE = "/data/diaithircombat/consolidated/";

    /** item registry id -> attrs object from the bundle. */
    private static final Map<Identifier, JsonObject> ATTRS = new HashMap<>();
    /** item registry id -> kind string. */
    private static final Map<Identifier, String> KINDS = new HashMap<>();

    private static boolean registered = false;

    private ConsolidatedBehaviour() {}

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        load();
        NeoForge.EVENT_BUS.addListener(ConsolidatedBehaviour::onItemAttributes);
        NeoForge.EVENT_BUS.addListener(ConsolidatedBehaviour::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(ConsolidatedBehaviour::onPlayerTick);
        LOG.info("behaviour registered for {} items", ATTRS.size());
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
                if (!o.has("id") || o.get("id").isJsonNull()) {
                    continue;
                }
                String kind = o.has("kind") && !o.get("kind").isJsonNull() ? o.get("kind").getAsString() : "";
                String path = sanitize(o.get("id").getAsString());
                if (path.isEmpty()) {
                    continue;
                }
                Identifier id = Identifier.fromNamespaceAndPath(DiaithirCombat.ID, path);
                if (o.has("attrs") && o.get("attrs").isJsonObject()) {
                    ATTRS.put(id, o.getAsJsonObject("attrs"));
                }
                KINDS.put(id, kind);
            }
        }
    }

    // ------------------------------------------------------------------ attribute modifiers

    private static void onItemAttributes(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack == null || stack.isEmpty()) {
            return;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) {
            return;
        }
        JsonObject attrs = ATTRS.get(id);
        if (attrs == null) {
            return;
        }
        String kind = KINDS.getOrDefault(id, "");
        EquipmentSlotGroup group = groupFor(kind, attrs);
        if (group == null) {
            return;
        }
        String tag = id.getPath();

        if ("weapon".equals(kind)) {
            if (num(attrs, "attack_damage") != null) {
                double dmg = num(attrs, "attack_damage");
                event.addModifier(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(mod("attack_damage_" + tag), dmg, AttributeModifier.Operation.ADD_VALUE),
                        group);
            }
            if (num(attrs, "attack_speed") != null) {
                double spd = num(attrs, "attack_speed");
                event.addModifier(Attributes.ATTACK_SPEED,
                        new AttributeModifier(mod("attack_speed_" + tag), spd, AttributeModifier.Operation.ADD_VALUE),
                        group);
            }
            if (num(attrs, "reach_m") != null) {
                double reach = num(attrs, "reach_m") - 3.0;
                if (Math.abs(reach) > 1.0e-6) {
                    event.addModifier(Attributes.ENTITY_INTERACTION_RANGE,
                            new AttributeModifier(mod("reach_" + tag), reach, AttributeModifier.Operation.ADD_VALUE),
                            group);
                }
            }
        }

        if ("armor".equals(kind)) {
            if (num(attrs, "armor_points") != null) {
                event.addModifier(Attributes.ARMOR,
                        new AttributeModifier(mod("armor_" + tag), num(attrs, "armor_points"),
                                AttributeModifier.Operation.ADD_VALUE),
                        group);
            }
            if (num(attrs, "toughness") != null) {
                event.addModifier(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(mod("toughness_" + tag), num(attrs, "toughness"),
                                AttributeModifier.Operation.ADD_VALUE),
                        group);
            }
        }

        // generic "minecraft:*" keyed attrs, applicable to any kind
        for (Map.Entry<String, JsonElement> en : attrs.entrySet()) {
            String key = en.getKey();
            if (!key.startsWith("minecraft:") || !en.getValue().isJsonPrimitive()
                    || !en.getValue().getAsJsonPrimitive().isNumber()) {
                continue;
            }
            double val = en.getValue().getAsDouble();
            String name = key.substring("minecraft:".length());
            if (name.equals("armor") || name.equals("armor_toughness")) {
                Attribute a = name.equals("armor") ? Attributes.ARMOR.value() : Attributes.ARMOR_TOUGHNESS.value();
                applyHolder(event, name, a, val, AttributeModifier.Operation.ADD_VALUE, group, tag);
            }
        }
    }

    private static void applyHolder(ItemAttributeModifierEvent event, String name, Attribute ignored, double val,
            AttributeModifier.Operation op, EquipmentSlotGroup group, String tag) {
        if (name.equals("armor")) {
            event.addModifier(Attributes.ARMOR, new AttributeModifier(mod("mc_armor_" + tag), val, op), group);
        } else if (name.equals("armor_toughness")) {
            event.addModifier(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(mod("mc_tough_" + tag), val, op),
                    group);
        }
    }

    private static EquipmentSlotGroup groupFor(String kind, JsonObject attrs) {
        if ("weapon".equals(kind)) {
            return EquipmentSlotGroup.MAINHAND;
        }
        if ("armor".equals(kind)) {
            String slot = str(attrs, "slot");
            if (slot == null) {
                return EquipmentSlotGroup.ARMOR;
            }
            return switch (slot) {
                case "head" -> EquipmentSlotGroup.HEAD;
                case "chest" -> EquipmentSlotGroup.CHEST;
                case "legs" -> EquipmentSlotGroup.LEGS;
                case "feet" -> EquipmentSlotGroup.FEET;
                case "offhand_bracer" -> EquipmentSlotGroup.OFFHAND;
                default -> EquipmentSlotGroup.ARMOR;
            };
        }
        return null;
    }

    // ------------------------------------------------------------------ damage handling

    private static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event == null) {
            return;
        }
        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();
        if (target == null || source == null) {
            return;
        }
        Entity direct = source.getDirectEntity();
        if (!(source.getEntity() instanceof LivingEntity attacker) || attacker.level().isClientSide()) {
            return;
        }
        ItemStack held = attacker.getMainHandItem();
        if (held == null || held.isEmpty()) {
            return;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(held.getItem());
        if (id == null) {
            return;
        }
        JsonObject attrs = ATTRS.get(id);
        if (attrs == null) {
            return;
        }
        if (direct != null && direct != attacker) {
            return; // projectiles etc. are not melee weapon hits
        }

        float amount = event.getAmount();
        String path = id.getPath();

        // critical strike data on weapons: first modifier entries with crit_chance / crit_damage
        double chance = 0.0;
        double bonus = 0.0;
        if (attrs.has("modifiers") && attrs.get("modifiers").isJsonArray()) {
            for (JsonElement me : attrs.getAsJsonArray("modifiers")) {
                if (!me.isJsonObject()) {
                    continue;
                }
                JsonObject m = me.getAsJsonObject();
                String attr = str(m, "attribute");
                Double amt = num(m, "amount");
                if (attr == null || amt == null) {
                    continue;
                }
                if (attr.endsWith("crit_chance")) {
                    chance += amt;
                } else if (attr.endsWith("crit_damage")) {
                    bonus += amt;
                }
            }
        }
        if (chance > 0.0) {
            chance = Math.min(chance + 0.05, 0.75);
            if (attacker.getRandom().nextDouble() < chance) {
                double mult = Math.max(1.0, Math.min(1.5 + bonus, 3.0));
                amount = (float) (amount * mult);
            }
        }

        // backstab: sneaking attacker on a weapon that defines sneak_damage_mult with backstab pattern
        Double sneakMult = num(attrs, "sneak_damage_mult");
        String sneakPattern = str(attrs, "sneak_pattern");
        if (sneakMult != null && attacker.isShiftKeyDown()) {
            if ("backstab".equals(sneakPattern)) {
                if (isBehind(attacker, target)) {
                    amount = (float) (amount * sneakMult);
                }
            }
        }

        // lance charge: speed-scaled damage while mounted
        Double minSpeed = num(attrs, "min_charge_speed_m_s");
        if (minSpeed != null && Boolean.TRUE.equals(bool(attrs, "mounted_only"))) {
            Entity vehicle = attacker.getVehicle();
            if (vehicle == null) {
                event.setCanceled(true);
                return;
            }
            double dx = vehicle.getDeltaMovement().x;
            double dz = vehicle.getDeltaMovement().z;
            double speed = Math.sqrt(dx * dx + dz * dz) * 20.0; // blocks per tick -> m/s
            if (speed >= minSpeed) {
                Double base = num(attrs, "base_damage_hp");
                double baseDmg = base != null ? base : amount;
                amount = (float) (baseDmg * (1.0 + 0.1 * speed));
                amount = Math.min(amount, 40.0f);
                Double unseat = num(attrs, "unseat_speed_m_s");
                if (unseat != null && speed >= unseat) {
                    Entity tv = target.getVehicle();
                    if (tv != null && !target.isBlocking()) {
                        target.stopRiding();
                        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 4));
                    }
                }
                Double shatter = num(attrs, "shatter_chance");
                if (shatter != null && attacker.getRandom().nextDouble() < shatter && speed >= 10.0) {
                    held.shrink(1);
                }
            } else {
                amount = amount * 0.5f;
            }
        }

        // dagger style bleed on sneak backstab events or charge-thrust knockback burst
        if ("charge_thrust".equals(sneakPattern) && attacker.getVehicle() != null) {
            Double bonusSpeed = num(attrs, "charge_speed_bonus_mps");
            if (bonusSpeed != null && sneakMult != null) {
                amount = (float) (amount * sneakMult);
            }
        }

        if (amount != event.getAmount() && path != null) {
            event.setAmount(amount);
        }
    }

    private static boolean isBehind(LivingEntity attacker, LivingEntity target) {
        double ax = attacker.getX() - target.getX();
        double az = attacker.getZ() - target.getZ();
        double len = Math.sqrt(ax * ax + az * az);
        if (len < 1.0e-6) {
            return false;
        }
        ax /= len;
        az /= len;
        float yaw = target.getYRot() * ((float) Math.PI / 180.0f);
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);
        return (ax * fx + az * fz) < -0.3;
    }

    // ------------------------------------------------------------------ tick handling

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player == null || player.level().isClientSide() || !(player instanceof ServerPlayer)) {
            return;
        }
        if (player.tickCount % 40 != 0) {
            return;
        }
        // standing guard with a heavy helm grants slight stability, data-driven via toughness
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (head == null || head.isEmpty()) {
            return;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(head.getItem());
        if (id == null) {
            return;
        }
        JsonObject attrs = ATTRS.get(id);
        if (attrs == null) {
            return;
        }
        Double tough = num(attrs, "toughness");
        if (tough != null && tough >= 1.0 && player.isShiftKeyDown()) {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 60, 0, true, false, false));
        }
    }

    // ------------------------------------------------------------------ helpers

    private static Identifier mod(String name) {
        return Identifier.fromNamespaceAndPath(DiaithirCombat.ID, sanitize(name));
    }

    private static Double num(JsonObject o, String key) {
        if (o == null || !o.has(key) || o.get(key).isJsonNull() || !o.get(key).isJsonPrimitive()) {
            return null;
        }
        try {
            if (o.get(key).getAsJsonPrimitive().isNumber()) {
                return o.get(key).getAsDouble();
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    private static String str(JsonObject o, String key) {
        if (o == null || !o.has(key) || o.get(key).isJsonNull() || !o.get(key).isJsonPrimitive()) {
            return null;
        }
        try {
            return o.get(key).getAsString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Boolean bool(JsonObject o, String key) {
        if (o == null || !o.has(key) || o.get(key).isJsonNull() || !o.get(key).isJsonPrimitive()) {
            return null;
        }
        try {
            if (o.get(key).getAsJsonPrimitive().isBoolean()) {
                return o.get(key).getAsBoolean();
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

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
        } catch (Exception e) {
            LOG.warn("bad bundle {}: {}", path, e.toString());
            return null;
        }
    }
}
