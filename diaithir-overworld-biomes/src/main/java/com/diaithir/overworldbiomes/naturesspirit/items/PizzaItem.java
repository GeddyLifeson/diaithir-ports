package com.diaithir.overworldbiomes.naturesspirit.items;

import com.diaithir.overworldbiomes.naturesspirit.NatureSpirit;
import com.diaithir.overworldbiomes.naturesspirit.blocks.PizzaBlock;
import com.diaithir.overworldbiomes.naturesspirit.blocks.block_entities.PizzaToppingVariant;
import com.diaithir.overworldbiomes.naturesspirit.registration.NSDataComponents;
import com.diaithir.overworldbiomes.naturesspirit.registration.NSMiscBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.BlockItem;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class PizzaItem extends BlockItem {

  public PizzaItem(Block block, Item.Properties settings) {
    super(block, settings);
  }

  public void addBitesToPizza(ItemStack pizza) {
    BlockItemStateProperties blockStateComponent = pizza.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
    Object blockEntityComponent = pizza.get(DataComponents.BLOCK_ENTITY_DATA);
    int pizzaSlice = this.asItem() == NSMiscBlocks.WHOLE_PIZZA ? 0 : this.asItem() == NSMiscBlocks.THREE_QUARTERS_PIZZA ? 1 : this.asItem() == NSMiscBlocks.HALF_PIZZA ? 2 : 3;
    pizza.set(DataComponents.BLOCK_STATE, blockStateComponent.with(PizzaBlock.BITES, pizzaSlice));
    if (blockEntityComponent == null) {
      CompoundTag nbtCompound = new CompoundTag();
      nbtCompound.putString("id", "natures_spirit:pizza_block_entity");
      pizza.set(DataComponents.BLOCK_ENTITY_DATA, net.minecraft.world.item.component.TypedEntityData.of(NSMiscBlocks.PIZZA_BLOCK_ENTITY_TYPE.get(), nbtCompound));
    }
  }

  @Override
  public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, net.minecraft.world.entity.EquipmentSlot slot) {
    addBitesToPizza(stack);
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag type) {
    super.appendHoverText(stack, context, display, tooltip, type);
    List<PizzaToppingVariant> list = stack.get(NSDataComponents.TOPPINGS);
    if (list != null) {
      for (PizzaToppingVariant pizzaToppingVariant : list) {
        tooltip.accept(Component.translatable("block.natures_spirit.pizza." + pizzaToppingVariant.translationKey().replace(":", ".")).withStyle(ChatFormatting.GRAY));
      }
    }
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
    ItemStack itemStack = super.finishUsingItem(stack, world, user);
    Item pizzaSlice = this.asItem() == NSMiscBlocks.WHOLE_PIZZA ? NSMiscBlocks.THREE_QUARTERS_PIZZA
        : this.asItem() == NSMiscBlocks.THREE_QUARTERS_PIZZA ? NSMiscBlocks.HALF_PIZZA : this.asItem() == NSMiscBlocks.HALF_PIZZA ? NSMiscBlocks.QUARTER_PIZZA : Items.AIR;

    Player holder = (Player) user;
    holder.awardStat(NatureSpirit.EAT_PIZZA_SLICE);
    List<PizzaToppingVariant> list = stack.get(NSDataComponents.TOPPINGS);
    if (list != null) {
      int foodAmount = 2;
      float saturationModifier = 0.2F;
      for (PizzaToppingVariant pizzaToppingVariant : list) {
        foodAmount += pizzaToppingVariant.hunger();
        saturationModifier += pizzaToppingVariant.saturation();
      }
      holder.getFoodData().eat(foodAmount, saturationModifier);
    }
    if (((Player) user).getAbilities().instabuild) {
      return itemStack;
    } else {
      ItemStack itemStack1 = new ItemStack(pizzaSlice, 1);
      itemStack1.applyComponents(stack.getComponents());
      return itemStack1;
    }
  }
}
