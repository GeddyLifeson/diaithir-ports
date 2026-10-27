package cn.leolezury.eternalstarlight.common.item.armor;

import cn.leolezury.eternalstarlight.common.util.ESPortCompat;
import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.world.item.equipment.ArmorType;

import net.minecraft.world.item.Item;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.registry.ESAttributes;
import com.google.common.base.Suppliers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.List;

public class UnrealiumArmorItem extends Item {
	private final ArmorType armorType;

	public UnrealiumArmorItem(ArmorMaterial materialHolder, ArmorType type, Properties properties) {
		super(ESPortCompat.armor(properties, materialHolder, type, extra(type)));
		this.armorType = type;
	}

	private static ItemAttributeModifiers extra(ArmorType type) {
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		EquipmentSlotGroup equipmentSlotGroup = EquipmentSlotGroup.bySlot(type.getSlot());
		Identifier resourceLocation = Identifier.withDefaultNamespace("armor." + type.getName());
		builder.add(Attributes.MOVEMENT_SPEED, new AttributeModifier(resourceLocation, 0.05, AttributeModifier.Operation.ADD_VALUE), equipmentSlotGroup);
		builder.add(Attributes.MOVEMENT_SPEED, new AttributeModifier(resourceLocation, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), equipmentSlotGroup);
		builder.add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(resourceLocation, -0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), equipmentSlotGroup);
		builder.add(ESAttributes.ENEMY_FOLLOW_RANGE_MULTIPLIER.asHolder(), new AttributeModifier(resourceLocation, -0.15, AttributeModifier.Operation.ADD_VALUE), equipmentSlotGroup);
		if (type == ArmorType.HELMET) {
			builder.add(ESAttributes.FOG_VISION.asHolder(), new AttributeModifier(resourceLocation, 50, AttributeModifier.Operation.ADD_VALUE), equipmentSlotGroup);
		}
		if (type == ArmorType.CHESTPLATE) {
			builder.add(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE, new AttributeModifier(resourceLocation, 1, AttributeModifier.Operation.ADD_VALUE), equipmentSlotGroup);
		}
		if (type == ArmorType.LEGGINGS) {
			builder.add(Attributes.GRAVITY, new AttributeModifier(resourceLocation, -0.02, AttributeModifier.Operation.ADD_VALUE), equipmentSlotGroup);
		}
		if (type == ArmorType.BOOTS) {
			builder.add(Attributes.MOVEMENT_EFFICIENCY, new AttributeModifier(resourceLocation, 1, AttributeModifier.Operation.ADD_VALUE), equipmentSlotGroup);
			builder.add(Attributes.STEP_HEIGHT, new AttributeModifier(resourceLocation, 0.5, AttributeModifier.Operation.ADD_VALUE), equipmentSlotGroup);
		}
		return builder.build();
	}

	@Override
	public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> list, TooltipFlag tooltipFlag) {
		if (armorType == ArmorType.HELMET) {
			list.accept(CommonComponents.EMPTY);
			list.accept(Component.translatable("item.modifiers.armor").withStyle(ChatFormatting.GRAY));
			list.accept(Component.literal(" ").append(Component.translatable("tooltip." + EternalStarlight.ID + ".unrealium_helmet")).withStyle(ChatFormatting.DARK_PURPLE));
			list.accept(Component.literal(" ").append(Component.translatable("tooltip." + EternalStarlight.ID + ".unrealium_helmet.mute")).withStyle(ChatFormatting.DARK_PURPLE));
		} else if (armorType == ArmorType.CHESTPLATE) {
			list.accept(CommonComponents.EMPTY);
			list.accept(Component.translatable("item.modifiers.armor").withStyle(ChatFormatting.GRAY));
			list.accept(Component.literal(" ").append(Component.translatable("tooltip." + EternalStarlight.ID + ".unrealium_chestplate")).withStyle(ChatFormatting.DARK_PURPLE));
			list.accept(Component.literal(" ").append(Component.translatable("tooltip." + EternalStarlight.ID + ".unrealium_chestplate.mute")).withStyle(ChatFormatting.DARK_PURPLE));
		} else if (armorType == ArmorType.LEGGINGS) {
			list.accept(CommonComponents.EMPTY);
			list.accept(Component.translatable("item.modifiers.armor").withStyle(ChatFormatting.GRAY));
			list.accept(Component.literal(" ").append(Component.translatable("tooltip." + EternalStarlight.ID + ".unrealium_leggings.mute")).withStyle(ChatFormatting.DARK_PURPLE));
		} else if (armorType == ArmorType.BOOTS) {
			list.accept(CommonComponents.EMPTY);
			list.accept(Component.translatable("item.modifiers.armor").withStyle(ChatFormatting.GRAY));
			list.accept(Component.literal(" ").append(Component.translatable("tooltip." + EternalStarlight.ID + ".unrealium_boots.mute")).withStyle(ChatFormatting.DARK_PURPLE));
		}
		super.appendHoverText(itemStack, tooltipContext, tooltipDisplay, list, tooltipFlag);
	}
}
