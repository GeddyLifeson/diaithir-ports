package net.diaithir.classes;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.Codec;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Diaithir Classes: Fian, Sleagh, Ceilg and Cleirech gear for Ages III, V and VII, plus the Ogham rune and socketed
 * ring/charm system. Everything is described by data/diaithir_classes/classes.json (written by tools/gen.py). Armour and
 * tool materials reuse the numbers, repair tags and worn-armour assets of diaithir-arms.
 */
@Mod(DiaithirClasses.ID)
public final class DiaithirClasses {
    public static final String ID = "diaithir_classes";
    private static final String ARMS = "diaithirarms";

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<String>>> SOCKETS = COMPONENTS.registerComponentType("sockets",
        b -> b.persistent(Codec.STRING.listOf()).networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list())));

    private static final Map<String, DeferredItem<Item>> ALL = new LinkedHashMap<>();

    public record ArmorMat(int durability, int boots, int legs, int chest, int helmet, int enchant, String sound,
                           float toughness, float knockback, String repair) {}
    public record ToolMat(String incorrect, int durability, float speed, float bonus, int enchant, String repair) {}
    public record Extra(String attribute, double amount, String op, String slot) {}
    public record Spec(String id, String kind, String mat, String slot, float damage, float speed, List<Float> spear,
                       int durability, List<Extra> extras) {}
    public record Rune(String id, String attribute, double amount, String op) {}
    public record Ring(String id, int sockets, String curio) {}
    public record Data(Map<String, ArmorMat> armor, Map<String, ToolMat> tools, List<Spec> items, List<String> parts,
                       List<Rune> runes, List<Ring> rings, List<String> spells, List<String> sets, List<String> containers) {}

    public static final Data DATA = load();
    public static final Map<String, Rune> RUNES = new LinkedHashMap<>();
    private static final Map<String, ArmorMaterial> ARMOR = new LinkedHashMap<>();
    private static final Map<String, ToolMaterial> TOOLS = new LinkedHashMap<>();

    private static Data load() {
        try (Reader r = new InputStreamReader(DiaithirClasses.class.getResourceAsStream("/data/" + ID + "/classes.json"))) {
            Type t = new TypeToken<Data>() {}.getType();
            return new Gson().fromJson(r, t);
        } catch (Exception e) {
            throw new IllegalStateException("classes.json missing", e);
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

    static ArmorMaterial armorMaterial(String mat) {
        return ARMOR.computeIfAbsent(mat, k -> {
            ArmorMat m = DATA.armor().get(k);
            Map<ArmorType, Integer> d = new EnumMap<>(ArmorType.class);
            d.put(ArmorType.BOOTS, m.boots());
            d.put(ArmorType.LEGGINGS, m.legs());
            d.put(ArmorType.CHESTPLATE, m.chest());
            d.put(ArmorType.HELMET, m.helmet());
            d.put(ArmorType.BODY, m.chest());
            // the worn-armour layers belong to diaithir-arms: same material, same look
            ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(ARMS, k));
            return new ArmorMaterial(m.durability(), d, m.enchant(), sound(m.sound()), m.toughness(), m.knockback(), itemTag(m.repair()), asset);
        });
    }

    static ToolMaterial toolMaterial(String mat) {
        return TOOLS.computeIfAbsent(mat, k -> {
            ToolMat m = DATA.tools().get(k);
            TagKey<net.minecraft.world.level.block.Block> inc = switch (m.incorrect()) {
                case "netherite" -> BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
                case "iron" -> BlockTags.INCORRECT_FOR_IRON_TOOL;
                default -> BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
            };
            return new ToolMaterial(inc, m.durability(), m.speed(), m.bonus(), m.enchant(), itemTag(m.repair()));
        });
    }

    static Item.Properties props(Spec s) {
        return switch (s.kind()) {
            case "armor" -> new Item.Properties().humanoidArmor(armorMaterial(s.mat()), ArmorType.valueOf(s.slot().toUpperCase()));
            case "sword" -> new Item.Properties().sword(toolMaterial(s.mat()), s.damage(), s.speed());
            case "spear" -> {
                List<Float> p = s.spear();
                yield new Item.Properties().spear(toolMaterial(s.mat()), p.get(0), p.get(1), p.get(2), p.get(3), p.get(4), p.get(5), p.get(6), p.get(7), p.get(8));
            }
            case "bow" -> new Item.Properties().durability(s.durability()).enchantable(toolMaterial(s.mat()).enchantmentValue());
            case "sling" -> new Item.Properties().stacksTo(1).durability(s.durability()).enchantable(toolMaterial(s.mat()).enchantmentValue());
            default -> throw new IllegalArgumentException(s.kind());
        };
    }

    public DiaithirClasses(IEventBus bus) {
        for (Spec s : DATA.items()) {
            if (s.kind().equals("bow")) {
                ALL.put(s.id(), ITEMS.registerItem(s.id(), BowItem::new, () -> props(s)));
            } else {
                ALL.put(s.id(), ITEMS.registerItem(s.id(), Item::new, () -> props(s)));
            }
        }
        for (String id : DATA.parts()) {
            ALL.put(id, ITEMS.registerSimpleItem(id));
        }
        for (Rune r : DATA.runes()) {
            RUNES.put(r.id(), r);
            ALL.put("rune_" + r.id(), ITEMS.registerItem("rune_" + r.id(), RuneItem::new, () -> new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
        }
        for (Ring r : DATA.rings()) {
            ALL.put(r.id(), ITEMS.registerItem(r.id(), p -> new SocketItem(p, r.sockets()), () -> new Item.Properties().stacksTo(1)));
        }
        ITEMS.register(bus);
        COMPONENTS.register(bus);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey().equals(CreativeModeTabs.COMBAT)) {
                ALL.values().forEach(i -> e.accept(i.get()));
            }
        });
        bus.addListener(ModifyDefaultComponentsEvent.class, DiaithirClasses::modifyDefaults);
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> SelfTest.register(e.getDispatcher()));
        if (ModList.get().isLoaded("curios")) {
            CuriosHook.register();
        }
    }

    /** Adds the class power attributes (spell power etc.) to weapons, on top of the vanilla sword and spear attributes. */
    private static void modifyDefaults(ModifyDefaultComponentsEvent event) {
        for (Spec s : DATA.items()) {
            if (s.extras() == null || s.extras().isEmpty()) {
                continue;
            }
            Item item = ALL.get(s.id()).get();
            event.modify(item, (components, context, it) -> {
                ItemAttributeModifiers.Builder b = ItemAttributeModifiers.builder();
                ItemAttributeModifiers old = components.get(DataComponents.ATTRIBUTE_MODIFIERS);
                if (old != null) {
                    old.modifiers().forEach(en -> b.add(en.attribute(), en.modifier(), en.slot()));
                }
                int n = 0;
                for (Extra x : s.extras()) {
                    Holder<Attribute> a = attribute(x.attribute());
                    if (a == null) {
                        continue;
                    }
                    b.add(a, new AttributeModifier(Identifier.fromNamespaceAndPath(ID, s.id() + "_x" + (n++)), x.amount(), operation(x.op())),
                        net.minecraft.world.entity.EquipmentSlotGroup.bySlot(net.minecraft.world.entity.EquipmentSlot.valueOf(x.slot().toUpperCase())));
                }
                components.set(DataComponents.ATTRIBUTE_MODIFIERS, b.build());
            });
        }
    }

    static AttributeModifier.Operation operation(String op) {
        return switch (op) {
            case "add_multiplied_base" -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
            case "add_multiplied_total" -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
            default -> AttributeModifier.Operation.ADD_VALUE;
        };
    }

    static Holder<Attribute> attribute(String id) {
        return BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(id)).map(h -> (Holder<Attribute>) h).orElse(null);
    }

    public static Map<String, DeferredItem<Item>> all() { return ALL; }

    public static List<String> socketsOf(net.minecraft.world.item.ItemStack st) {
        List<String> l = st.get(SOCKETS.get());
        return l == null ? new ArrayList<>() : l;
    }
}
