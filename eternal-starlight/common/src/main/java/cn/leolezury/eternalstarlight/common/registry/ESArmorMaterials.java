package cn.leolezury.eternalstarlight.common.registry;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.util.ESConventionalTags;
import cn.leolezury.eternalstarlight.common.util.ESTags;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.Map;

/** PORT: ArmorMaterial is no longer a registry in 26.1; materials are plain constants and the textures come from equipment asset json files. */
public class ESArmorMaterials {
	public static final ArmorMaterial AETHERSENT = new ArmorMaterial(45, Map.of(ArmorType.BOOTS, 3, ArmorType.LEGGINGS, 6, ArmorType.CHESTPLATE, 7, ArmorType.HELMET, 3, ArmorType.BODY, 11), 35, ESSoundEvents.ARMOR_EQUIP_AETHERSENT.asHolder(), 0F, 0F, ESConventionalTags.Items.INGOTS_AETHERSENT,
		ResourceKey.create(EquipmentAssets.ROOT_ID, EternalStarlight.id("aethersent")));
	public static final ArmorMaterial THERMAL_SPRINGSTONE = new ArmorMaterial(15, Map.of(ArmorType.BOOTS, 2, ArmorType.LEGGINGS, 5, ArmorType.CHESTPLATE, 6, ArmorType.HELMET, 3, ArmorType.BODY, 7), 15, ESSoundEvents.ARMOR_EQUIP_THERMAL_SPRINGSTONE.asHolder(), 0.5F, 0F, ESConventionalTags.Items.INGOTS_THERMAL_SPRINGSTONE,
		ResourceKey.create(EquipmentAssets.ROOT_ID, EternalStarlight.id("thermal_springstone")));
	public static final ArmorMaterial GLACITE = new ArmorMaterial(15, Map.of(ArmorType.BOOTS, 3, ArmorType.LEGGINGS, 5, ArmorType.CHESTPLATE, 6, ArmorType.HELMET, 3, ArmorType.BODY, 7), 9, ESSoundEvents.ARMOR_EQUIP_GLACITE.asHolder(), 0.5F, 0F, ESConventionalTags.Items.GEMS_GLACITE,
		ResourceKey.create(EquipmentAssets.ROOT_ID, EternalStarlight.id("glacite")));
	public static final ArmorMaterial STARLIT_DIAMOND = new ArmorMaterial(45, Map.of(ArmorType.BOOTS, 3, ArmorType.LEGGINGS, 6, ArmorType.CHESTPLATE, 8, ArmorType.HELMET, 3, ArmorType.BODY, 11), 22, ESSoundEvents.ARMOR_EQUIP_STARLIT_DIAMOND.asHolder(), 3F, 0.1F, ESConventionalTags.Items.GEMS_STARLIT_DIAMOND,
		ResourceKey.create(EquipmentAssets.ROOT_ID, EternalStarlight.id("starlit_diamond")));
	public static final ArmorMaterial DEEPSILVER = new ArmorMaterial(30, Map.of(ArmorType.BOOTS, 2, ArmorType.LEGGINGS, 5, ArmorType.CHESTPLATE, 6, ArmorType.HELMET, 2, ArmorType.BODY, 5), 25, ESSoundEvents.ARMOR_EQUIP_DEEPSILVER.asHolder(), 0.5F, 0.2F, ESConventionalTags.Items.INGOTS_DEEPSILVER,
		ResourceKey.create(EquipmentAssets.ROOT_ID, EternalStarlight.id("deepsilver")));
	public static final ArmorMaterial UNREALIUM = new ArmorMaterial(50, Map.of(ArmorType.BOOTS, 3, ArmorType.LEGGINGS, 6, ArmorType.CHESTPLATE, 8, ArmorType.HELMET, 3, ArmorType.BODY, 11), 25, ESSoundEvents.ARMOR_EQUIP_UNREALIUM.asHolder(), 0.5F, 0F, ESConventionalTags.Items.INGOTS_UNREALIUM,
		ResourceKey.create(EquipmentAssets.ROOT_ID, EternalStarlight.id("unrealium")));
	public static final ArmorMaterial AMARAMBER = new ArmorMaterial(15, Map.of(ArmorType.BOOTS, 2, ArmorType.LEGGINGS, 5, ArmorType.CHESTPLATE, 5, ArmorType.HELMET, 2, ArmorType.BODY, 5), 9, ESSoundEvents.ARMOR_EQUIP_AMARAMBER.asHolder(), 0F, 0F, ESConventionalTags.Items.INGOTS_AMARAMBER,
		ResourceKey.create(EquipmentAssets.ROOT_ID, EternalStarlight.id("amaramber")));
	public static final ArmorMaterial ALCHEMIST = new ArmorMaterial(10, Map.of(ArmorType.BOOTS, 2, ArmorType.LEGGINGS, 5, ArmorType.CHESTPLATE, 6, ArmorType.HELMET, 2, ArmorType.BODY, 5), 15, ESSoundEvents.ARMOR_EQUIP_ALCHEMIST.asHolder(), 0F, 0F, ESConventionalTags.Items.GEMS_THIOQUARTZ,
		ResourceKey.create(EquipmentAssets.ROOT_ID, EternalStarlight.id("alchemist")));
	public static final ArmorMaterial AIR_SAC = new ArmorMaterial(15, Map.of(ArmorType.BOOTS, 1, ArmorType.LEGGINGS, 4, ArmorType.CHESTPLATE, 4, ArmorType.HELMET, 1, ArmorType.BODY, 4), 9, SoundEvents.ARMOR_EQUIP_LEATHER, 0F, 0F, ESTags.Items.REPAIR_AIR_SAC,
		ResourceKey.create(EquipmentAssets.ROOT_ID, EternalStarlight.id("air_sac")));

	public static void loadClass() {
	}
}
