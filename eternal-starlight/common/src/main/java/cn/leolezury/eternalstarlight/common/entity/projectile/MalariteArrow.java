package cn.leolezury.eternalstarlight.common.entity.projectile;

import cn.leolezury.eternalstarlight.common.util.ESPortCompat;

import net.minecraft.world.level.storage.ValueOutput;

import net.minecraft.world.level.storage.ValueInput;

import cn.leolezury.eternalstarlight.common.registry.ESEntities;
import cn.leolezury.eternalstarlight.common.registry.ESItems;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class MalariteArrow extends AbstractArrow {
	private static final String TAG_DURATION = "duration";
	private int duration = 200;

	public MalariteArrow(EntityType<? extends MalariteArrow> entityType, Level level) {
		super(entityType, level);
	}

	public MalariteArrow(Level level, LivingEntity livingEntity, ItemStack itemStack, @Nullable ItemStack itemStack2) {
		super(ESEntities.MALARITE_ARROW.get(), livingEntity, level, itemStack, itemStack2);
	}

	public MalariteArrow(Level level, double d, double e, double f, ItemStack itemStack, @Nullable ItemStack itemStack2) {
		super(ESEntities.MALARITE_ARROW.get(), d, e, f, level, itemStack, itemStack2);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide() && !this.isInGround()) {
			this.level().addParticle(new DustColorTransitionOptions(net.minecraft.util.ARGB.colorFromFloat(1F, 170 / 255F, 81 / 255F, 158 / 255F), net.minecraft.util.ARGB.colorFromFloat(1F, 42 / 255F, 61 / 255F, 56 / 255F), 1f), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
		}
	}

	@Override
	protected void doPostHurtEffects(LivingEntity livingEntity) {
		livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, this.duration, 0), this.getEffectSource());
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
		return ESItems.MALARITE_ARROW.get().getDefaultInstance();
	}
}
