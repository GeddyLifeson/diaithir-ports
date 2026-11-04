package com.diaithir.overworld_biomes.realms;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Tools and armour of a realm: one tool and armour material per prefix, tier picked from the vanilla ladder. */
final class RealmGear {
    private RealmGear() {}

    /** item registry key -> material prefix, filled while gear registers. */
    private static final Map<String, String> PREFIX_OF = new HashMap<>();
    private static boolean hooked;

    private static boolean has(String p, String rx) { return java.util.regex.Pattern.compile(rx).matcher(p).find(); }

    /** Weapon strike: the struck creature gets an effect picked from the material's name. */
    private static void strike(String prefix, LivingEntity target, LivingEntity wielder) {
        if (has(prefix, "(frost|frozen|ice|shiver|cold|snow)")) target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1));
        else if (has(prefix, "(ember|blood|fire|rogdor|magma|blaze)")) target.igniteForSeconds(4);
        else if (has(prefix, "(rot|toxic|virulent|infected|sick|decay|poison)")) target.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
        else if (has(prefix, "(honey|amber|wax|royal|jelly|sugar|bee)")) wielder.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0));
        else if (has(prefix, "(cloud|sky|zanite|holy|aether|gravit)")) target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 30, 0));
        else target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
    }

    /** Full set of one material: the wearer gets a short Resistance when hit and the attacker is slowed. */
    private static void setBonus(LivingEntity wearer, LivingEntity attacker) {
        String first = null;
        for (EquipmentSlot sl : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            String p = PREFIX_OF.get(String.valueOf(BuiltInRegistries.ITEM.getKey(wearer.getItemBySlot(sl).getItem())));
            if (p == null || (first != null && !first.equals(p))) return;
            first = p;
        }
        wearer.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 60, 0));
        if (attacker != null && attacker != wearer) attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0));
    }

    private static void hook() {
        if (hooked) return;
        hooked = true;
        NeoForge.EVENT_BUS.addListener(LivingDamageEvent.Post.class, e -> {
            if (e.getEntity().level().isClientSide()) return;
            if (e.getSource().getEntity() instanceof LivingEntity a) {
                String p = PREFIX_OF.get(String.valueOf(BuiltInRegistries.ITEM.getKey(a.getMainHandItem().getItem())));
                if (p != null && e.getEntity() != a) strike(p, e.getEntity(), a);
            }
            setBonus(e.getEntity(), e.getSource().getEntity() instanceof LivingEntity a ? a : null);
        });
    }

    private static TagKey<Block> incorrect(String tier) {
        return switch (tier) {
            case "wood" -> BlockTags.INCORRECT_FOR_WOODEN_TOOL;
            case "stone" -> BlockTags.INCORRECT_FOR_STONE_TOOL;
            case "diamond" -> BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
            default -> BlockTags.INCORRECT_FOR_IRON_TOOL;
        };
    }

    private static ToolMaterial tool(String modId, String prefix, String tier) {
        int dur; float speed, bonus; int ench;
        switch (tier) {
            case "wood" -> { dur = 90; speed = 2.5F; bonus = 0.5F; ench = 15; }
            case "stone" -> { dur = 190; speed = 4.5F; bonus = 1.0F; ench = 8; }
            case "diamond" -> { dur = 1800; speed = 8.5F; bonus = 3.0F; ench = 12; }
            default -> { dur = 420; speed = 6.0F; bonus = 2.0F; ench = 14; }
        }
        TagKey<Item> repair = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(modId, "repair/" + prefix));
        return new ToolMaterial(incorrect(tier), dur, speed, bonus, ench, repair);
    }

    private static ArmorMaterial armor(String modId, String prefix, String tier) {
        int dur; int[] d; float tough; float kb; int ench;
        switch (tier) {
            case "wood" -> { dur = 8; d = new int[] {1, 2, 3, 1, 3}; tough = 0; kb = 0; ench = 12; }
            case "stone" -> { dur = 14; d = new int[] {1, 4, 5, 2, 4}; tough = 0; kb = 0; ench = 8; }
            case "diamond" -> { dur = 36; d = new int[] {3, 6, 8, 3, 11}; tough = 2.0F; kb = 0.05F; ench = 12; }
            default -> { dur = 20; d = new int[] {2, 5, 6, 2, 5}; tough = 0.5F; kb = 0; ench = 10; }
        }
        Map<ArmorType, Integer> def = new EnumMap<>(ArmorType.class);
        def.put(ArmorType.BOOTS, d[0]); def.put(ArmorType.LEGGINGS, d[1]); def.put(ArmorType.CHESTPLATE, d[2]); def.put(ArmorType.HELMET, d[3]); def.put(ArmorType.BODY, d[4]);
        ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(modId, prefix));
        TagKey<Item> repair = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(modId, "repair/" + prefix));
        return new ArmorMaterial(dur, def, ench, SoundEvents.ARMOR_EQUIP_IRON, tough, kb, repair, asset);
    }

    static void register(String modId, JsonObject manifest, DeferredRegister.Items items) {
        Map<String, ToolMaterial> tm = new HashMap<>();
        Map<String, ArmorMaterial> am = new HashMap<>();
        hook();
        for (JsonElement e : manifest.getAsJsonArray("gear")) {
            JsonObject g = e.getAsJsonObject();
            String id = g.get("id").getAsString(), prefix = g.get("prefix").getAsString(), tier = g.get("tier").getAsString(), kind = g.get("kind").getAsString();
            PREFIX_OF.put(modId + ":" + id, prefix);
            switch (kind) {
                case "helmet", "chestplate", "leggings", "boots" -> {
                    ArmorMaterial m = am.computeIfAbsent(prefix, p -> armor(modId, p, tier));
                    ArmorType t = switch (kind) { case "helmet" -> ArmorType.HELMET; case "chestplate" -> ArmorType.CHESTPLATE; case "leggings" -> ArmorType.LEGGINGS; default -> ArmorType.BOOTS; };
                    items.registerItem(id, p -> new Item(p.humanoidArmor(m, t)));
                }
                default -> {
                    ToolMaterial m = tm.computeIfAbsent(prefix, p -> tool(modId, p, tier));
                    items.registerItem(id, p -> new Item(switch (kind) {
                        case "pickaxe" -> p.pickaxe(m, 1.0F, -2.8F);
                        case "axe" -> p.axe(m, 6.0F, -3.1F);
                        case "battleaxe" -> p.axe(m, 7.0F, -3.3F);
                        case "shovel" -> p.shovel(m, 1.5F, -3.0F);
                        case "hoe" -> p.hoe(m, -1.0F, -1.0F);
                        case "spear" -> p.sword(m, 3.5F, -2.6F);
                        default -> p.sword(m, 3.0F, -2.4F);
                    }));
                }
            }
        }
    }
}
