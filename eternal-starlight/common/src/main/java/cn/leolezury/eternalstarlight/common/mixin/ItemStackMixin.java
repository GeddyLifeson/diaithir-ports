package cn.leolezury.eternalstarlight.common.mixin;

import cn.leolezury.eternalstarlight.common.handler.ESCommonHandler;
import cn.leolezury.eternalstarlight.common.item.component.Accessory;
import cn.leolezury.eternalstarlight.common.registry.ESDataComponents;
import cn.leolezury.eternalstarlight.common.registry.ESItems;
import cn.leolezury.eternalstarlight.common.util.ESAccessoryUtil;
import cn.leolezury.eternalstarlight.common.util.ESTags;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.BiConsumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@Shadow
	public abstract void setDamageValue(int i);

	@Shadow
	public abstract int getDamageValue();

	@Shadow
	public abstract boolean isDamaged();

	@Inject(method = "inventoryTick", at = @At("RETURN"))
	private void inventoryTick(Level level, Entity entity, net.minecraft.world.entity.EquipmentSlot slot, CallbackInfo ci) {
		if (!level.isClientSide() && entity.tickCount % 600 == 0 && isDamaged() && (((ItemStack) (Object) this).is(ESTags.Items.MENDS_NATURALLY) || (((ItemStack) (Object) this).is(ESTags.Items.REPAIRED_BY_CRESCENT_PENDANT) && entity instanceof LivingEntity living && ESAccessoryUtil.getActiveAccessoriesOnArmors(living).contains(ESItems.CRESCENT_PENDANT.get())))) {
			setDamageValue(Math.max(getDamageValue() - 1, 0));
		}
	}

	@Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", at = @At("RETURN"))
	private void forEachModifier(EquipmentSlot slot, BiConsumer<Holder<Attribute>, AttributeModifier> action, CallbackInfo ci) {
		ItemStack stack = (ItemStack) (Object) this;
		List<ItemStack> accessories = stack.getOrDefault(ESDataComponents.ACCESSORIES.get(), List.of());
		for (ItemStack accessory : accessories) {
			Accessory data = accessory.get(ESDataComponents.ACCESSORY.get());
			if (data != null) {
				ItemAttributeModifiers modifiers = data.attributeModifiers();
				if (!modifiers.modifiers().isEmpty()) {
					modifiers.forEach(slot, action);
				}
			}
		}
	}

	@Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V", at = @At("RETURN"))
	private void forEachModifier(EquipmentSlotGroup slotGroup, BiConsumer<Holder<Attribute>, AttributeModifier> action, CallbackInfo ci) {
		ItemStack stack = (ItemStack) (Object) this;
		List<ItemStack> accessories = stack.getOrDefault(ESDataComponents.ACCESSORIES.get(), List.of());
		for (ItemStack accessory : accessories) {
			Accessory data = accessory.get(ESDataComponents.ACCESSORY.get());
			if (data != null) {
				ItemAttributeModifiers modifiers = data.attributeModifiers();
				if (!modifiers.modifiers().isEmpty()) {
					modifiers.forEach(slotGroup, action);
				}
			}
		}
	}

	@ModifyReturnValue(method = "getTooltipLines", at = @At("RETURN"))
	private List<Component> getTooltipLines(List<Component> original, @Local(argsOnly = true) Item.TooltipContext context, @Local(argsOnly = true) Player player, @Local(argsOnly = true) TooltipFlag flag) {
		ESCommonHandler.onItemTooltip(player, flag, (ItemStack) (Object) this, original, context);
		return original;
	}

	@ModifyReturnValue(method = "overrideStackedOnOther", at = @At("RETURN"))
	private boolean overrideStackedOnOther(boolean original, @Local(argsOnly = true) Slot slot, @Local(argsOnly = true) ClickAction action, @Local(argsOnly = true) Player player) {
		return original || ESAccessoryUtil.overrideEquipmentOnAccessory((ItemStack) (Object) this, slot, action, player);
	}

	@ModifyReturnValue(method = "overrideOtherStackedOnMe", at = @At("RETURN"))
	private boolean overrideOtherStackedOnMe(boolean original, @Local(argsOnly = true) ItemStack other, @Local(argsOnly = true) Slot slot, @Local(argsOnly = true) ClickAction action, @Local(argsOnly = true) Player player, @Local(argsOnly = true) SlotAccess access) {
		return original || ESAccessoryUtil.overrideAccessoryOnEquipment((ItemStack) (Object) this, other, slot, action, player, access);
	}
}
