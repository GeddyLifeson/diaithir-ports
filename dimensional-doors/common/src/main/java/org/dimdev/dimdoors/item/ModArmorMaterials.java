package org.dimdev.dimdoors.item;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;
import org.dimdev.dimdoors.DimensionalDoors;

import java.util.HashMap;
import java.util.Map;

public class ModArmorMaterials {
    public static final ArmorMaterial GARMENT_OF_REALITY = register(
        "garment_of_reality",
        15,
        SoundEvents.ARMOR_EQUIP_LEATHER,
        new int[]{1, 2, 3, 1},
        0.0F,
        0.0F); //TODO: DEFINE TRAITS

    public static final ArmorMaterial WORLD_THREAD = register(
        "world_thread",
        15,
        SoundEvents.ARMOR_EQUIP_LEATHER,
        new int[]{1, 2, 3, 1},
        0.0F,
        0.0F);

    /** Repair items are now an item tag (data/dimdoors/tags/item/repairs_&lt;name&gt;.json). */
    public static ArmorMaterial register(String name, int enchantability, Holder<SoundEvent> equipSound, int[] protectionAmounts, float toughness, float knockbackResistance) {
        var id = DimensionalDoors.id(name);

        Map<ArmorType, Integer> map = Util.make(new HashMap<>(), typeMap -> {
            for (int i = 0; i < protectionAmounts.length; i++) {
                var type = ArmorType.values()[i];
                typeMap.put(type, protectionAmounts[i]);
            }
        });

        TagKey<Item> repairs = TagKey.create(Registries.ITEM, DimensionalDoors.id("repairs_" + name));
        ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, id);
        return new ArmorMaterial(15 * 5, map, enchantability, equipSound, toughness, knockbackResistance, repairs, asset);
    }

    public static void init() {
    }
}
