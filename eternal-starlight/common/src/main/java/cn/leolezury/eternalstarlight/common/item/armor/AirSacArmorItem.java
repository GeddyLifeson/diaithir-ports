package cn.leolezury.eternalstarlight.common.item.armor;

import net.minecraft.world.item.equipment.ArmorType;

import net.minecraft.world.item.Item;

import com.google.common.base.Suppliers;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.function.Supplier;

public class AirSacArmorItem extends Item {
	private final Supplier<ItemAttributeModifiers> extraModifiers;

	public AirSacArmorItem(ArmorMaterial materialHolder, ArmorType type, Properties properties) {
		super(properties.humanoidArmor(materialHolder, type));
		this.extraModifiers = Suppliers.memoize(() -> {
			ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
			EquipmentSlotGroup equipmentSlotGroup = EquipmentSlotGroup.bySlot(type.getSlot());
			Identifier resourceLocation = Identifier.withDefaultNamespace("armor." + type.getName());
			builder.add(Attributes.WATER_MOVEMENT_EFFICIENCY, new AttributeModifier(resourceLocation, 0.9, AttributeModifier.Operation.ADD_VALUE), equipmentSlotGroup);
			return builder.build();
		});
	}

	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers() {
		ItemAttributeModifiers modifiers = super.getDefaultAttributeModifiers();
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
			builder.add(entry.attribute(), entry.modifier(), entry.slot());
		}
		for (ItemAttributeModifiers.Entry entry : extraModifiers.get().modifiers()) {
			builder.add(entry.attribute(), entry.modifier(), entry.slot());
		}
		return builder.build();
	}
}
