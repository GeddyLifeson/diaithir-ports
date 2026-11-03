package com.diaithir.overworldbiomes.naturesspirit.entity;

import com.diaithir.overworldbiomes.naturesspirit.registration.NSEntityTypes;
import com.diaithir.overworldbiomes.naturesspirit.registration.NSMiscBlocks;
import com.diaithir.overworldbiomes.naturesspirit.registration.NSParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class CheeseArrowEntity extends AbstractArrow {

  public CheeseArrowEntity(EntityType<? extends CheeseArrowEntity> entityType, Level level) {
    super(entityType, level);
  }

  public CheeseArrowEntity(Level level, LivingEntity owner, ItemStack stack, @Nullable ItemStack weapon) {
    super(NSEntityTypes.CHEESE_ARROW.get(), owner, level, stack, weapon);
  }

  public CheeseArrowEntity(double x, double y, double z, Level level, ItemStack stack, @Nullable ItemStack weapon) {
    super(NSEntityTypes.CHEESE_ARROW.get(), x, y, z, level, stack, weapon);
  }

  @Override
  public void tick() {
    super.tick();
    if (this.level().isClientSide() && !this.isInGround()) {
      this.level().addParticle(NSParticleTypes.MILK_PARTICLE.get(), this.getX(), this.getY(), this.getZ(), 0D, 0D, 0D);
    }
  }

  @Override
  protected ItemStack getDefaultPickupItem() {
    return new ItemStack(NSMiscBlocks.CHEESE_ARROW.get());
  }

  @Override
  protected void doPostHurtEffects(LivingEntity target) {
    super.doPostHurtEffects(target);
    target.removeAllEffects();
  }
}
