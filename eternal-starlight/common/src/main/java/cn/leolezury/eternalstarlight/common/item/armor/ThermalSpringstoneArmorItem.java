package cn.leolezury.eternalstarlight.common.item.armor;

import cn.leolezury.eternalstarlight.common.util.ESPortCompat;
import java.util.function.Consumer;

import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.world.item.equipment.ArmorType;

import net.minecraft.world.item.Item;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.item.interfaces.TickableArmor;
import cn.leolezury.eternalstarlight.common.registry.ESAttributes;
import com.google.common.base.Suppliers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThermalSpringstoneArmorItem extends Item implements TickableArmor {
	private final ArmorType armorType;

	public ThermalSpringstoneArmorItem(ArmorMaterial holder, ArmorType type, Properties properties) {
		super(ESPortCompat.armor(properties, holder, type, extra(type)));
		this.armorType = type;
	}

	private static ItemAttributeModifiers extra(ArmorType type) {
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		EquipmentSlotGroup equipmentSlotGroup = EquipmentSlotGroup.bySlot(type.getSlot());
		Identifier resourceLocation = Identifier.withDefaultNamespace("armor." + type.getName());
		builder.add(ESAttributes.FIRE_RESISTANCE.asHolder(), new AttributeModifier(resourceLocation, 0.25, AttributeModifier.Operation.ADD_VALUE), equipmentSlotGroup);
		builder.add(ESAttributes.COLD_RESISTANCE.asHolder(), new AttributeModifier(resourceLocation, 0.10, AttributeModifier.Operation.ADD_VALUE), equipmentSlotGroup);
		return builder.build();
	}

	public static Identifier getTexture(EquipmentSlot slot) {
		return EternalStarlight.id("textures/armor/thermal_springstone_layer_" + ((slot == EquipmentSlot.LEGS) ? "2.png" : "1.png"));
	}

	@Override
	public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> list, TooltipFlag tooltipFlag) {
		list.accept(CommonComponents.EMPTY);
		list.accept(Component.translatable("item.modifiers.armor").withStyle(ChatFormatting.GRAY));
		list.accept(Component.literal(" ").append(Component.translatable("tooltip." + EternalStarlight.ID + ".thermal_springstone_armor")).withStyle(ChatFormatting.GOLD));
		super.appendHoverText(itemStack, tooltipContext, tooltipDisplay, list, tooltipFlag);
	}

	@Override
	public void tick(Level level, LivingEntity livingEntity, ItemStack armor) {
		AtomicBoolean fullSet = new AtomicBoolean(true);
		ESPortCompat.armorItems(livingEntity).forEach(stack -> {
			if (!(stack.getItem() instanceof ThermalSpringstoneArmorItem)) {
				fullSet.set(false);
			}
		});
		if (fullSet.get()) {
			livingEntity.setRemainingFireTicks(Math.min(livingEntity.getRemainingFireTicks(), 5));
		}
	}
}
