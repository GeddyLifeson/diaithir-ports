package com.gregtechceu.gtceu.common.item.armor;

import net.minecraft.world.item.equipment.ArmorType;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.util.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;

import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

public class GTArmorMaterials {

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister
            .create(BuiltInRegistries.ARMOR_MATERIAL, GTCEu.MOD_ID);/* ported: removed */

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> GOGGLES = ARMOR_MATERIALS.register("goggles",
            () -> new ArmorMaterial(Util.make(new EnumMap<>(ArmorType.class), map -> {
                map.put(ArmorType.BOOTS, 0);
                map.put(ArmorType.LEGGINGS, 0);
                map.put(ArmorType.CHESTPLATE, 0);
                map.put(ArmorType.HELMET, 0);
            }), 0, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY, BLANK_LAYERS, 0.0F, 0.0F));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> JETPACK = ARMOR_MATERIALS.register("jetpack",
            () -> new ArmorMaterial(Util.make(new EnumMap<>(ArmorType.class), map -> {
                map.put(ArmorType.BOOTS, 0);
                map.put(ArmorType.LEGGINGS, 0);
                map.put(ArmorType.CHESTPLATE, 0);
                map.put(ArmorType.HELMET, 0);
            }), 0, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY, BLANK_LAYERS, 0.0F, 0.0F));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ARMOR = ARMOR_MATERIALS.register("armor",
            () -> new ArmorMaterial(Util.make(new EnumMap<>(ArmorType.class), map -> {
                map.put(ArmorType.BOOTS, 0);
                map.put(ArmorType.LEGGINGS, 0);
                map.put(ArmorType.CHESTPLATE, 0);
                map.put(ArmorType.HELMET, 0);
            }), 0, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY, BLANK_LAYERS, 5.0F, 0.0F));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> BAD_PPE_EQUIPMENT = ARMOR_MATERIALS
            .register("bad_ppe_equipment",
                    () -> new ArmorMaterial(Util.make(new EnumMap<>(ArmorType.class), map -> {
                        map.put(ArmorType.BOOTS, 1);
                        map.put(ArmorType.LEGGINGS, 2);
                        map.put(ArmorType.CHESTPLATE, 3);
                        map.put(ArmorType.HELMET, 1);
                    }), 10, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY, BLANK_LAYERS, 0.0F, 0.0F));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> GOOD_PPE_EQUIPMENT = ARMOR_MATERIALS
            .register("good_ppe_equipment",
                    () -> new ArmorMaterial(Util.make(new EnumMap<>(ArmorType.class), map -> {
                        map.put(ArmorType.BOOTS, 2);
                        map.put(ArmorType.LEGGINGS, 5);
                        map.put(ArmorType.CHESTPLATE, 6);
                        map.put(ArmorType.HELMET, 2);
                    }), 10, SoundEvents.ARMOR_EQUIP_GENERIC, () -> Ingredient.EMPTY, BLANK_LAYERS, 0.0F, 0.0F));
}
