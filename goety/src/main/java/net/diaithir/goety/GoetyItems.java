package net.diaithir.goety;

import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Items of Goety: crafting materials, dark metal tools, black iron armour (goety_spec/items.json). */
public final class GoetyItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Goety.MOD_ID);
    public static final Map<String, DeferredItem<Item>> PLAIN = new LinkedHashMap<>();
    public static final List<DeferredItem<? extends Item>> ALL = new ArrayList<>();

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Goety.MOD_ID, path));
    }

    /** Dark metal: netherite-class mining, short of its durability. Upstream default tools break after 166 uses. */
    public static final ToolMaterial DARK = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 1031, 8.0F, 3.0F, 20, itemTag("dark_metal_materials"));

    public static final ArmorMaterial BLACK_IRON = new ArmorMaterial(30,
        Map.of(ArmorType.BOOTS, 2, ArmorType.LEGGINGS, 5, ArmorType.CHESTPLATE, 6, ArmorType.HELMET, 2, ArmorType.BODY, 6),
        15, SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.0F, itemTag("cursed_metal_materials"),
        ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(Goety.MOD_ID, "black_iron")));

    record ItemsSpec(List<String> plain, List<List<String>> tools, List<List<String>> armor) {}

    public static void register(IEventBus bus) {
        ItemsSpec spec = Spec.read("items.json", new TypeToken<ItemsSpec>() {});
        for (String id : spec.plain()) {
            DeferredItem<Item> it = ITEMS.registerSimpleItem(id);
            PLAIN.put(id, it);
            ALL.add(it);
        }
        for (List<String> t : spec.tools()) {
            String id = t.get(0);
            DeferredItem<? extends Item> it = switch (t.get(1)) {
                case "sword" -> ITEMS.registerItem(id, p -> new Item(p.sword(DARK, 3.0F, -2.4F)));
                case "pickaxe" -> ITEMS.registerItem(id, p -> new Item(p.pickaxe(DARK, 1.0F, -2.8F)));
                case "shovel" -> ITEMS.registerItem(id, p -> new ShovelItem(DARK, 1.5F, -3.0F, p));
                case "axe" -> ITEMS.registerItem(id, p -> new AxeItem(DARK, 6.0F, -3.1F, p));
                default -> ITEMS.registerItem(id, p -> new HoeItem(DARK, -2.0F, -1.0F, p));
            };
            ALL.add(it);
        }
        for (List<String> a : spec.armor()) {
            ArmorType type = switch (a.get(1)) {
                case "helmet" -> ArmorType.HELMET;
                case "chestplate" -> ArmorType.CHESTPLATE;
                case "leggings" -> ArmorType.LEGGINGS;
                default -> ArmorType.BOOTS;
            };
            ALL.add(ITEMS.registerItem(a.get(0), p -> new Item(p.humanoidArmor(BLACK_IRON, type))));
        }
        ITEMS.register(bus);
        // block items first in the creative tab
        ALL.addAll(0, GoetyBlocks.ITEMS);
    }

    private GoetyItems() {}
}
