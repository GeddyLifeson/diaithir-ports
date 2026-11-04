package net.diaithir.druidry.ars.setup.registry;

import net.diaithir.druidry.ars.ArsNouveau;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;
import java.util.Map;

/** In 26.1 ArmorMaterial is a plain record, no longer a registry entry. */
public class MaterialRegistry {
    public static final TagKey<Item> REPAIRS_MAGE_ARMOR = TagKey.create(Registries.ITEM, ArsNouveau.prefix("repairs_mage_armor"));

    public static final ArmorMaterial LIGHT = make("an_light", 1, 3, 5, 2, 4);
    public static final ArmorMaterial MEDIUM = make("an_medium", 2, 5, 6, 2, 4);
    public static final ArmorMaterial HEAVY = make("an_heavy", 3, 6, 8, 3, 4);

    private static ArmorMaterial make(String name, int boots, int legs, int chest, int helm, int body) {
        Map<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
        defense.put(ArmorType.BOOTS, boots);
        defense.put(ArmorType.LEGGINGS, legs);
        defense.put(ArmorType.CHESTPLATE, chest);
        defense.put(ArmorType.HELMET, helm);
        defense.put(ArmorType.BODY, body);
        ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, ArsNouveau.prefix(name));
        return new ArmorMaterial(30, defense, 30, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0f, 0.0f, REPAIRS_MAGE_ARMOR, asset);
    }
}
