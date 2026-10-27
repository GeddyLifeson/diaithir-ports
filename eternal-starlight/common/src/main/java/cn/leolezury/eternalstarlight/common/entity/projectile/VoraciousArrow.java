package cn.leolezury.eternalstarlight.common.entity.projectile;

import cn.leolezury.eternalstarlight.common.util.ESPortCompat;

import net.minecraft.world.level.storage.ValueOutput;

import net.minecraft.world.level.storage.ValueInput;

import cn.leolezury.eternalstarlight.common.registry.ESDataComponents;
import cn.leolezury.eternalstarlight.common.registry.ESEntities;
import cn.leolezury.eternalstarlight.common.registry.ESItems;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class VoraciousArrow extends AbstractArrow {
	private static final String TAG_DURATION = "duration";
	private int duration = 200;

	public VoraciousArrow(EntityType<? extends VoraciousArrow> entityType, Level level) {
		super(entityType, level);
	}

	public VoraciousArrow(Level level, LivingEntity livingEntity, ItemStack itemStack, @Nullable ItemStack itemStack2) {
		super(ESEntities.VORACIOUS_ARROW.get(), livingEntity, level, itemStack, itemStack2);
	}

	public VoraciousArrow(Level level, double d, double e, double f, ItemStack itemStack, @Nullable ItemStack itemStack2) {
		super(ESEntities.VORACIOUS_ARROW.get(), d, e, f, level, itemStack, itemStack2);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide() && !this.isInGround()) {
			this.level().addParticle(new DustColorTransitionOptions(net.minecraft.util.ARGB.colorFromFloat(1F, 127 / 255F, 99 / 255F, 129 / 255F), net.minecraft.util.ARGB.colorFromFloat(1F, 85 / 255F, 71 / 255F, 87 / 255F), 1f), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
		}
	}

	@Override
	protected void doPostHurtEffects(LivingEntity livingEntity) {
		super.doPostHurtEffects(livingEntity);
		MobEffectInstance mobEffectInstance = new MobEffectInstance(MobEffects.HUNGER, this.duration, 0);
		livingEntity.addEffect(mobEffectInstance, this.getEffectSource());
		if (getOwner() instanceof Player player) {
			player.getFoodData().eat(3, 0);
			for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
				ItemStack stack = player.getInventory().getItem(i);
				if (stack.is(ESItems.DAGGER_OF_HUNGER.get())) {
					float hungerLevel = stack.getOrDefault(ESDataComponents.HUNGER_LEVEL.get(), 0f);
					stack.applyComponentsAndValidate(DataComponentPatch.builder().set(ESDataComponents.HUNGER_LEVEL.get(), Math.min(1, hungerLevel + 0.05f)).build());
				}
			}
		}
	}

	@Override
	public void readAdditionalSaveData(ValueInput compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (ESPortCompat.has(compoundTag, TAG_DURATION)) {
			this.duration = compoundTag.getIntOr(TAG_DURATION, 0);
		}
	}

	@Override
	public void addAdditionalSaveData(ValueOutput compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt(TAG_DURATION, this.duration);
	}

	@Override
	protected ItemStack getDefaultPickupItem() {
		return ESItems.VORACIOUS_ARROW.get().getDefaultInstance();
	}
}
