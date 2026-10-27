package cn.leolezury.eternalstarlight.common.entity.projectile;

import net.minecraft.world.level.storage.ValueOutput;

import net.minecraft.world.level.storage.ValueInput;

import cn.leolezury.eternalstarlight.common.registry.ESEntities;
import cn.leolezury.eternalstarlight.common.registry.ESItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class GlaciteArrow extends AbstractArrow {
	private static final String TAG_DURATION = "duration";
	private int duration = 200;

	public GlaciteArrow(EntityType<? extends GlaciteArrow> entityType, Level level) {
		super(entityType, level);
	}

	public GlaciteArrow(Level level, LivingEntity livingEntity, ItemStack itemStack, @Nullable ItemStack itemStack2) {
		super(ESEntities.GLACITE_ARROW.get(), livingEntity, level, itemStack, itemStack2);
	}

	public GlaciteArrow(Level level, double d, double e, double f, ItemStack itemStack, @Nullable ItemStack itemStack2) {
		super(ESEntities.GLACITE_ARROW.get(), d, e, f, level, itemStack, itemStack2);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide() && !this.inGround) {
			this.level().addParticle(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
		}
	}

	@Override
	protected void doPostHurtEffects(LivingEntity livingEntity) {
		super.doPostHurtEffects(livingEntity);
		if (livingEntity.canFreeze()) {
			livingEntity.setTicksFrozen(Math.min(livingEntity.getTicksFrozen() + duration, 300));
		}
	}

	@Override
	public void readAdditionalSaveData(ValueInput compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.contains(TAG_DURATION)) {
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
		return ESItems.GLACITE_ARROW.get().getDefaultInstance();
	}
}
