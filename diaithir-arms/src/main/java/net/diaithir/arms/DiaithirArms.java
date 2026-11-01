package net.diaithir.arms;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Diaithir Arms: armour and weapons for every Age. All content is described by data/diaithirarms/arms.json
 * (written by tools/gen.py): armour materials, tool materials, and the list of items.
 */
@Mod(DiaithirArms.ID)
public final class DiaithirArms {
    public static final String ID = "diaithirarms";
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    private static final Map<String, DeferredItem<Item>> ALL = new LinkedHashMap<>();

    public record ArmorMat(int durability, int boots, int legs, int chest, int helmet, int enchant, String sound,
                           float toughness, float knockback, String repair) {}
    public record ToolMat(String incorrect, int durability, float speed, float bonus, int enchant, String repair) {}
    public record Spec(String id, String kind, String mat, String slot, float damage, float speed,
                       float mult, float shield, List<Float> spear, int durability) {}
    public record Data(Map<String, ArmorMat> armor, Map<String, ToolMat> tools, List<Spec> items, List<String> parts) {}

    public static final Data DATA = load();
    private static final Map<String, ArmorMaterial> ARMOR = new LinkedHashMap<>();
    private static final Map<String, ToolMaterial> TOOLS = new LinkedHashMap<>();

    private static Data load() {
        try (Reader r = new InputStreamReader(DiaithirArms.class.getResourceAsStream("/data/" + ID + "/arms.json"))) {
            Type t = new TypeToken<Data>() {}.getType();
            return new Gson().fromJson(r, t);
        } catch (Exception e) {
            throw new IllegalStateException("arms.json missing", e);
        }
    }

    private static TagKey<Item> itemTag(String s) {
        return TagKey.create(Registries.ITEM, Identifier.parse(s));
    }

    private static Holder<SoundEvent> sound(String s) {
        return switch (s) {
            case "leather" -> SoundEvents.ARMOR_EQUIP_LEATHER;
            case "copper" -> SoundEvents.ARMOR_EQUIP_COPPER;
            case "chain" -> SoundEvents.ARMOR_EQUIP_CHAIN;
            case "diamond" -> SoundEvents.ARMOR_EQUIP_DIAMOND;
            case "netherite" -> SoundEvents.ARMOR_EQUIP_NETHERITE;
            default -> SoundEvents.ARMOR_EQUIP_IRON;
        };
    }

    private static TagKey<net.minecraft.world.level.block.Block> incorrect(String s) {
        return switch (s) {
            case "netherite" -> BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
            case "iron" -> BlockTags.INCORRECT_FOR_IRON_TOOL;
            default -> BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
        };
    }

    static ArmorMaterial armorMaterial(String mat) {
        return ARMOR.computeIfAbsent(mat, k -> {
            ArmorMat m = DATA.armor().get(k);
            Map<ArmorType, Integer> d = new EnumMap<>(ArmorType.class);
            d.put(ArmorType.BOOTS, m.boots());
            d.put(ArmorType.LEGGINGS, m.legs());
            d.put(ArmorType.CHESTPLATE, m.chest());
            d.put(ArmorType.HELMET, m.helmet());
            d.put(ArmorType.BODY, m.chest());
            ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(ID, k));
            return new ArmorMaterial(m.durability(), d, m.enchant(), sound(m.sound()), m.toughness(), m.knockback(), itemTag(m.repair()), asset);
        });
    }

    static ToolMaterial toolMaterial(String mat) {
        return TOOLS.computeIfAbsent(mat, k -> {
            ToolMat m = DATA.tools().get(k);
            return new ToolMaterial(incorrect(m.incorrect()), m.durability(), m.speed(), m.bonus(), m.enchant(), itemTag(m.repair()));
        });
    }

    static Item.Properties shieldProps(Spec s) {
        ToolMat m = DATA.tools().get(s.mat());
        float factor = s.shield();
        return new Item.Properties().durability(s.durability()).component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
            .repairable(itemTag(m.repair())).enchantable(m.enchant()).equippableUnswappable(EquipmentSlot.OFFHAND)
            .delayedComponent(DataComponents.BLOCKS_ATTACKS, context -> new BlocksAttacks(0.25F, 1.0F,
                List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, factor)),
                new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
                Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                Optional.of(SoundEvents.SHIELD_BLOCK), Optional.of(SoundEvents.SHIELD_BREAK)))
            .component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK);
    }

    static Item.Properties props(Spec s) {
        return switch (s.kind()) {
            case "armor" -> new Item.Properties().humanoidArmor(armorMaterial(s.mat()), ArmorType.valueOf(s.slot().toUpperCase()));
            case "sword" -> new Item.Properties().sword(toolMaterial(s.mat()), s.damage(), s.speed());
            case "axe" -> new Item.Properties().axe(toolMaterial(s.mat()), s.damage(), s.speed());
            case "spear" -> {
                List<Float> p = s.spear();
                yield new Item.Properties().spear(toolMaterial(s.mat()), p.get(0), p.get(1), p.get(2), p.get(3), p.get(4), p.get(5), p.get(6), p.get(7), p.get(8));
            }
            case "bow" -> new Item.Properties().durability(s.durability()).enchantable(toolMaterial(s.mat()).enchantmentValue());
            case "crossbow" -> new Item.Properties().stacksTo(1).durability(s.durability())
                .component(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY).enchantable(toolMaterial(s.mat()).enchantmentValue());
            case "shield" -> shieldProps(s);
            default -> throw new IllegalArgumentException(s.kind());
        };
    }

    static java.util.function.Function<Item.Properties, Item> factory(Spec s) {
        return switch (s.kind()) {
            case "bow" -> p -> new ArmsBow(p, s.mult());
            case "crossbow" -> p -> new ArmsCrossbow(p, s.mult());
            case "shield" -> ShieldItem::new;
            default -> Item::new;
        };
    }

    public DiaithirArms(IEventBus bus) {
        for (Spec s : DATA.items()) {
            if (s.kind().equals("axe")) {
                ALL.put(s.id(), ITEMS.registerItem(s.id(), p -> new AxeItem(toolMaterial(s.mat()), s.damage(), s.speed(), p), () -> new Item.Properties()));
            } else {
                ALL.put(s.id(), ITEMS.registerItem(s.id(), factory(s), () -> props(s)));
            }
        }
        for (String id : DATA.parts()) {
            ALL.put(id, ITEMS.registerSimpleItem(id));
        }
        ITEMS.register(bus);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey().equals(CreativeModeTabs.COMBAT)) {
                ALL.values().forEach(i -> e.accept(i.get()));
            }
        });
        NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.RegisterCommandsEvent.class, e -> SelfTest.register(e.getDispatcher()));
    }

    public static Map<String, DeferredItem<Item>> all() { return ALL; }
}
