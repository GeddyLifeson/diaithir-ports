package net.diaithir.arms.immersive;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.diaithir.arms.DiaithirArms;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Immersive Armors fold, stage 1 (GPL-3.0; this package is the GPL part of the jar, see META-INF/licenses/LICENSE-ImmersiveArmors-GPL-3.0.txt).
 * Ten armour sets, stats and effect numbers from upstream Items.java, held in data/diaithirarms/immersive_sets.json. Worn and icon art is
 * recoloured from this module's own steel set (tools/gen_immersive.py); none of the upstream art is used. Dyeing and the cape-hiding
 * rule of upstream are not ported yet.
 */
public final class Sets {
    public record Set(String name, List<Integer> protection, int durability, float toughness, float knockback, float weight, float water, int enchant,
                      String repair, String sound, List<List<Object>> effects, boolean antiSkeleton) {}
    public record Data(List<Set> sets) {}

    public static final Data DATA = load();
    /** Item to its set, for the effect code. */
    private static final Map<Item, Set> BY_ITEM = new IdentityHashMap<>();
    private static final Map<String, DeferredItem<Item>> ALL = new LinkedHashMap<>();
    private static final ArmorType[] ORDER = {ArmorType.HELMET, ArmorType.CHESTPLATE, ArmorType.LEGGINGS, ArmorType.BOOTS};

    private static Data load() {
        try (Reader r = new InputStreamReader(Sets.class.getResourceAsStream("/data/" + DiaithirArms.ID + "/immersive_sets.json"))) {
            return new Gson().fromJson(r, Data.class);
        } catch (Exception e) {
            throw new IllegalStateException("immersive_sets.json missing", e);
        }
    }

    static SoundEvent sound(String s) {
        return switch (s) {
            case "skeleton" -> SoundEvents.SKELETON_AMBIENT;
            case "wither_skeleton" -> SoundEvents.WITHER_SKELETON_AMBIENT;
            case "wool" -> SoundEvents.WOOL_PLACE;
            case "slime" -> SoundEvents.SLIME_SQUISH;
            case "leather" -> SoundEvents.ARMOR_EQUIP_LEATHER.value();
            case "redstone" -> SoundEvents.REDSTONE_TORCH_BURNOUT;
            default -> SoundEvents.ARMOR_EQUIP_IRON.value();
        };
    }

    static ArmorMaterial material(Set s) {
        Map<ArmorType, Integer> d = new EnumMap<>(ArmorType.class);
        for (int i = 0; i < 4; i++) d.put(ORDER[i], s.protection().get(i));
        d.put(ArmorType.BODY, s.protection().get(1));
        ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(DiaithirArms.ID, "imm_" + s.name()));
        TagKey<Item> repair = TagKey.create(Registries.ITEM, Identifier.parse(s.repair()));
        return new ArmorMaterial(s.durability(), d, Math.max(1, s.enchant()), BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound(s.sound())), s.toughness(), s.knockback(), repair, asset);
    }

    static Item.Properties props(Set s, ArmorType type) {
        ArmorMaterial m = material(s);
        Item.Properties p = new Item.Properties().stacksTo(1).humanoidArmor(m, type);
        if (s.weight() == 0 && s.water() == 0) return p;
        var b = ItemAttributeModifiers.builder();
        for (var e : m.createAttributes(type).modifiers()) b.add(e.attribute(), e.modifier(), e.slot());
        EquipmentSlotGroup g = EquipmentSlotGroup.bySlot(type.getSlot());
        if (s.weight() != 0) b.add(Attributes.MOVEMENT_SPEED, new AttributeModifier(Identifier.fromNamespaceAndPath(DiaithirArms.ID, "imm_weight_" + s.name() + "_" + type.getName()), s.weight(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE), g);
        if (s.water() != 0) b.add(Attributes.WATER_MOVEMENT_EFFICIENCY, new AttributeModifier(Identifier.fromNamespaceAndPath(DiaithirArms.ID, "imm_water_" + s.name() + "_" + type.getName()), s.water(), AttributeModifier.Operation.ADD_VALUE), g);
        return p.attributes(b.build());
    }

    public static void register(DeferredRegister.Items items) {
        for (Set s : DATA.sets()) {
            for (int i = 0; i < 4; i++) {
                ArmorType t = ORDER[i];
                String id = s.name() + "_" + t.getName() + "_imm";
                ALL.put(id, items.registerItem(id, Item::new, () -> props(s, t)));
            }
        }
    }

    public static Map<String, DeferredItem<Item>> all() { return ALL; }

    /** Set of a stack, or null. Built lazily because items exist only after registration. */
    public static Set of(ItemStack st) {
        if (st.isEmpty()) return null;
        if (BY_ITEM.isEmpty()) {
            for (Set s : DATA.sets()) for (ArmorType t : ORDER) {
                var di = ALL.get(s.name() + "_" + t.getName() + "_imm");
                if (di != null && di.isBound()) BY_ITEM.put(di.get(), s);
            }
        }
        return BY_ITEM.get(st.getItem());
    }

    /** How many pieces of the set the entity wears. */
    public static int count(LivingEntity e, Set s) {
        int n = 0;
        for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) if (of(e.getItemBySlot(slot)) == s) n++;
        return n;
    }

    private Sets() {}
}
