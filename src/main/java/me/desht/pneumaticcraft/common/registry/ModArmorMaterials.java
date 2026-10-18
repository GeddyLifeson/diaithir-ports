package me.desht.pneumaticcraft.common.registry;

import me.desht.pneumaticcraft.api.data.PneumaticCraftTags;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.Map;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

// 26.1: armor materials are plain records; the model layers (primary/secondary/eyepiece/translucent) live in
// assets/pneumaticcraft/equipment/<name>.json
public class ModArmorMaterials {
    public static final Identifier COMPRESSED_IRON_ARMOR = RL("compressed_iron");
    public static final Identifier PNEUMATIC_ARMOR = RL("pneumatic");

    private static Map<ArmorType,Integer> defense() {
        return Map.of(ArmorType.BOOTS, 2, ArmorType.LEGGINGS, 5, ArmorType.CHESTPLATE, 6, ArmorType.HELMET, 2, ArmorType.BODY, 5);
    }

    public static final ArmorMaterial COMPRESSED_IRON = new ArmorMaterial(
            24, defense(), 9, SoundEvents.ARMOR_EQUIP_IRON, 1.0f, 0.075f,
            PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
            ResourceKey.create(EquipmentAssets.ROOT_ID, COMPRESSED_IRON_ARMOR)
    );

    public static final ArmorMaterial PNEUMATIC = new ArmorMaterial(
            24, defense(), 9, SoundEvents.ARMOR_EQUIP_IRON, 1.0f, 0.2f,
            PneumaticCraftTags.Items.INGOTS_COMPRESSED_IRON,
            ResourceKey.create(EquipmentAssets.ROOT_ID, PNEUMATIC_ARMOR)
    );
}
