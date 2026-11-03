package com.diaithir.overworldbiomes.naturesspirit.items;

import com.diaithir.overworldbiomes.naturesspirit.entity.CheeseArrowEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class CheeseArrowItem extends ArrowItem {

  public CheeseArrowItem(Item.Properties properties) {
    super(properties);
  }

  @Override
  public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity shooter, @Nullable ItemStack shotFrom) {
    return new CheeseArrowEntity(level, shooter, stack, shotFrom);
  }
}
