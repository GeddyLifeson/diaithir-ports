package cn.leolezury.eternalstarlight.common.item.armor;

import cn.leolezury.eternalstarlight.common.util.ESPortCompat;
import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.world.item.equipment.ArmorType;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.item.interfaces.TickableArmor;
import cn.leolezury.eternalstarlight.common.util.ESTags;
import com.google.common.base.Suppliers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DeepsilverArmorItem extends Item implements TickableArmor {
	private final ArmorType armorType;

	public DeepsilverArmorItem(ArmorMaterial materialHolder, ArmorType type, Item.Properties properties) {
		super(ESPortCompat.armor(properties, materialHolder, type, extra(type)));
		this.armorType = type;
	}

	private static ItemAttributeModifiers extra(ArmorType type) {
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		EquipmentSlotGroup equipmentSlotGroup = EquipmentSlotGroup.bySlot(type.getSlot());
		Identifier resourceLocation = Identifier.withDefaultNamespace("armor." + type.getName());
		builder.add(Attributes.ATTACK_SPEED, new AttributeModifier(resourceLocation, 0.05, AttributeModifier.Operation.ADD_VALUE), equipmentSlotGroup);
		builder.add(Attributes.MOVEMENT_SPEED, new AttributeModifier(resourceLocation, 0.01, AttributeModifier.Operation.ADD_VALUE), equipmentSlotGroup);
		return builder.build();
	}

	@Override
	public void tick(Level level, LivingEntity livingEntity, ItemStack armor) {
		AtomicBoolean fullSet = new AtomicBoolean(true);
		ESPortCompat.armorItems(livingEntity).forEach(stack -> {
			if (!(stack.getItem() instanceof DeepsilverArmorItem)) {
				fullSet.set(false);
			}
		});
		if (fullSet.get()) {
			level.registryAccess().lookupOrThrow(Registries.MOB_EFFECT).getTagOrEmpty(ESTags.MobEffects.DEEPSILVER_ARMOR_CAN_REMOVE).forEach(effect -> {
				if (livingEntity.hasEffect(effect)) {
					livingEntity.removeEffect(effect);
				}
			});
		}
	}

	@Override
	public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> list, TooltipFlag tooltipFlag) {
		list.accept(CommonComponents.EMPTY);
		list.accept(Component.translatable("tooltip." + EternalStarlight.ID + ".full_set").withStyle(ChatFormatting.GRAY));
		list.accept(Component.literal(" ").append(Component.translatable("tooltip." + EternalStarlight.ID + ".deepsilver_armor")).withStyle(ChatFormatting.YELLOW));
		super.appendHoverText(itemStack, tooltipContext, tooltipDisplay, list, tooltipFlag);
	}
}
