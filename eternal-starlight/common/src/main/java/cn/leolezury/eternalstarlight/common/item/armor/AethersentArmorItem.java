package cn.leolezury.eternalstarlight.common.item.armor;

import cn.leolezury.eternalstarlight.common.util.ESPortCompat;
import net.minecraft.world.item.equipment.ArmorType;

import net.minecraft.world.item.Item;

import cn.leolezury.eternalstarlight.common.registry.ESAttributes;
import com.google.common.base.Suppliers;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;


public class AethersentArmorItem extends Item {
	private final ArmorType armorType;

	public AethersentArmorItem(ArmorMaterial holder, ArmorType type, Properties properties) {
		super(ESPortCompat.armor(properties, holder, type, extra(type)));
		this.armorType = type;
	}

	private static ItemAttributeModifiers extra(ArmorType type) {
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		EquipmentSlotGroup equipmentSlotGroup = EquipmentSlotGroup.bySlot(type.getSlot());
		Identifier resourceLocation = Identifier.withDefaultNamespace("armor." + type.getName());
		builder.add(ESAttributes.METEOR_COUNTERATTACK_CHANCE.asHolder(), new AttributeModifier(resourceLocation, 0.25, AttributeModifier.Operation.ADD_VALUE), equipmentSlotGroup);
		return builder.build();
	}

}
