package cn.leolezury.eternalstarlight.common.entity.projectile;

import cn.leolezury.eternalstarlight.common.util.ESPortCompat;

import net.minecraft.world.level.storage.ValueOutput;

import net.minecraft.world.level.storage.ValueInput;

import cn.leolezury.eternalstarlight.common.registry.ESEntities;
import cn.leolezury.eternalstarlight.common.registry.ESItems;
import cn.leolezury.eternalstarlight.common.registry.ESMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class AmaramberArrow extends AbstractArrow {
	private static final String TAG_DURATION = "duration";
	private int duration = 400;

	public AmaramberArrow(EntityType<? extends AmaramberArrow> entityType, Level level) {
		super(entityType, level);
	}

	public AmaramberArrow(Level level, LivingEntity livingEntity, ItemStack itemStack, @Nullable ItemStack itemStack2) {
		super(ESEntities.AMARAMBER_ARROW.get(), livingEntity, level, itemStack, itemStack2);
	}

	public AmaramberArrow(Level level, double d, double e, double f, ItemStack itemStack, @Nullable ItemStack itemStack2) {
		super(ESEntities.AMARAMBER_ARROW.get(), d, e, f, level, itemStack, itemStack2);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide() && !this.isInGround()) {
			this.level().addParticle(ParticleTypes.CRIMSON_SPORE, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
		}
	}

	@Override
	protected void doPostHurtEffects(LivingEntity livingEntity) {
		super.doPostHurtEffects(livingEntity);
		MobEffectInstance mobEffectInstance = new MobEffectInstance(ESMobEffects.FLAMMABLE.asHolder(), this.duration, 0);
		livingEntity.addEffect(mobEffectInstance, this.getEffectSource());
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
		return ESItems.AMARAMBER_ARROW.get().getDefaultInstance();
	}
}
