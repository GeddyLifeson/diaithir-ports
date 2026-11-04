package com.diaithir.overworldbiomes.naturesspirit.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;

public class CoconutHalfItem extends Item {

  private final Item LeftOverItem;

  public CoconutHalfItem(Item.Properties properties, Item item) {
    super(properties);
    LeftOverItem = item;
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
    super.finishUsingItem(stack, level, user);
    if (user instanceof ServerPlayer serverPlayer) {
      CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
      serverPlayer.awardStat(Stats.ITEM_USED.get(this));
    }

    if (!level.isClientSide()) {
      user.removeAllEffects();
    }

    if (stack.isEmpty()) {
      return new ItemStack(LeftOverItem);
    } else {
      if (user instanceof Player player && !player.getAbilities().instabuild) {
        ItemStack itemStack = new ItemStack(LeftOverItem);
        if (!player.getInventory().add(itemStack)) {
          player.drop(itemStack, false);
        }
      }

      return stack;
    }
  }

  @Override
  public InteractionResult use(Level level, Player user, InteractionHand hand) {
    return ItemUtils.startUsingInstantly(level, user, hand);
  }
}
