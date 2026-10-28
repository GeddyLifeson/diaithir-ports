package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;

import java.util.Collections;
import java.util.Map;

/**
 * 26.1: armor materials are plain records. Like Mekanism, these cannot be used with
 * {@link Item.Properties#humanoidArmor}; use {@link #apply(Item.Properties, ArmorMaterial, ArmorType)}.
 */
public class GTArmorMaterials {

    private static final Map<ArmorType, Integer> NONE = Collections.emptyMap();

    public static final ArmorMaterial GOGGLES = create("goggles", NONE, 0, 0.0F);
    public static final ArmorMaterial JETPACK = create("jetpack", NONE, 0, 0.0F);
    public static final ArmorMaterial ARMOR = create("armor", NONE, 0, 5.0F);
    public static final ArmorMaterial BAD_PPE_EQUIPMENT = create("bad_ppe_equipment",
            Map.of(ArmorType.BOOTS, 1, ArmorType.LEGGINGS, 2, ArmorType.CHESTPLATE, 3, ArmorType.HELMET, 1), 10, 0.0F);
    public static final ArmorMaterial GOOD_PPE_EQUIPMENT = create("good_ppe_equipment",
            Map.of(ArmorType.BOOTS, 2, ArmorType.LEGGINGS, 5, ArmorType.CHESTPLATE, 6, ArmorType.HELMET, 2), 10, 0.0F);

    public static Item.Properties apply(Item.Properties properties, ArmorMaterial material, ArmorType type) {
        properties.attributes(material.createAttributes(type))
                .component(DataComponents.EQUIPPABLE, Equippable.builder(type.getSlot())
                        .setEquipSound(material.equipSound())
                        .setAsset(material.assetId())
                        .build());
        if (material.enchantmentValue() > 0) properties.enchantable(material.enchantmentValue());
        return properties;
    }

    private static ArmorMaterial create(String name, Map<ArmorType, Integer> defense, int enchantValue,
                                        float toughness) {
        ResourceKey<EquipmentAsset> key = ResourceKey.create(EquipmentAssets.ROOT_ID, GTCEu.id(name));
        return new ArmorMaterial(0, defense, enchantValue, SoundEvents.ARMOR_EQUIP_GENERIC, toughness, 0.0F, null, key);
    }

    public static void init() {}
}
