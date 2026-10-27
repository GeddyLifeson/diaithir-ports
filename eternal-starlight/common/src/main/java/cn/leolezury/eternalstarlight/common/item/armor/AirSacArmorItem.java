package cn.leolezury.eternalstarlight.common.item.armor;

import cn.leolezury.eternalstarlight.common.util.ESPortCompat;
import net.minecraft.world.item.equipment.ArmorType;

import net.minecraft.world.item.Item;

import com.google.common.base.Suppliers;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;


public class AirSacArmorItem extends Item {
	private final ArmorType armorType;

	public AirSacArmorItem(ArmorMaterial materialHolder, ArmorType type, Properties properties) {
		super(ESPortCompat.armor(properties, materialHolder, type, extra(type)));
		this.armorType = type;
	}

	private static ItemAttributeModifiers extra(ArmorType type) {
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		EquipmentSlotGroup equipmentSlotGroup = EquipmentSlotGroup.bySlot(type.getSlot());
		Identifier resourceLocation = Identifier.withDefaultNamespace("armor." + type.getName());
		builder.add(Attributes.WATER_MOVEMENT_EFFICIENCY, new AttributeModifier(resourceLocation, 0.9, AttributeModifier.Operation.ADD_VALUE), equipmentSlotGroup);
		return builder.build();
	}

}
