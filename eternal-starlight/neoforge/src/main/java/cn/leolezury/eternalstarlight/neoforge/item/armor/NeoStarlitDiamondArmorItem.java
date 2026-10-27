package cn.leolezury.eternalstarlight.neoforge.item.armor;

import net.minecraft.world.item.equipment.ArmorType;

import net.minecraft.world.item.Item;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class NeoStarlitDiamondArmorItem extends Item {
	public NeoStarlitDiamondArmorItem(ArmorMaterial material, ArmorType type, Properties properties) {
		super(properties.humanoidArmor(material, type));
	}

	@Override
	public @Nullable Identifier getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
		return EternalStarlight.id("textures/armor/starlit_diamond_layer_" + ((slot == EquipmentSlot.LEGS) ? "2.png" : "1.png"));
	}
}
