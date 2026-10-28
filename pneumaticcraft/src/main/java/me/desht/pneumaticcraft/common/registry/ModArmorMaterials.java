package me.desht.pneumaticcraft.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.Map;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Armor materials are plain records in 26.1; the equipment model is an asset JSON
 * ({@code assets/pneumaticcraft/equipment/*.json}) referenced by key.
 */
public class ModArmorMaterials {
    public static final Identifier COMPRESSED_IRON_ARMOR = RL("compressed_iron");
    public static final Identifier PNEUMATIC_ARMOR = RL("pneumatic");

    public static final TagKey<Item> REPAIRS_ARMOR = TagKey.create(Registries.ITEM, RL("repairs_armor"));

    private static final Map<ArmorType, Integer> DEFENSE = Map.of(
            ArmorType.BOOTS, 2,
            ArmorType.LEGGINGS, 5,
            ArmorType.CHESTPLATE, 6,
            ArmorType.HELMET, 2,
            ArmorType.BODY, 5
    );

    public static final ArmorMaterial COMPRESSED_IRON = new ArmorMaterial(
            24, DEFENSE, 9, SoundEvents.ARMOR_EQUIP_IRON,
            1.0f, 0.075f, REPAIRS_ARMOR,
            ResourceKey.create(EquipmentAssets.ROOT_ID, COMPRESSED_IRON_ARMOR)
    );

    public static final ArmorMaterial PNEUMATIC = new ArmorMaterial(
            24, DEFENSE, 9, SoundEvents.ARMOR_EQUIP_IRON,
            1.0f, 0.2f, REPAIRS_ARMOR,
            ResourceKey.create(EquipmentAssets.ROOT_ID, PNEUMATIC_ARMOR)
    );
}
