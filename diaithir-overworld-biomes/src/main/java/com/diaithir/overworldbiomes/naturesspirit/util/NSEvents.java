package com.diaithir.overworldbiomes.naturesspirit.util;

import com.diaithir.overworldbiomes.naturesspirit.blocks.BranchingTrunkBlock;
import com.diaithir.overworldbiomes.naturesspirit.registration.NSMiscBlocks;
import com.diaithir.overworldbiomes.naturesspirit.registration.NSWoods;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class NSEvents {

  public static void registerEvents() {
    NeoForge.EVENT_BUS.register(NSEvents.class);
  }

  public static void init(IEventBus modBus) {
    registerEvents();
  }

  @SubscribeEvent
  public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
    Player player = event.getEntity();
    Level world = event.getLevel();
    InteractionHand hand = event.getHand();
    BlockPos blockPos = event.getPos();
    BlockState blockState = world.getBlockState(blockPos);
    ItemStack stackInHand = player.getItemInHand(hand);

    if ((blockState.is(NSWoods.JOSHUA.getLog()) || blockState.is(NSMiscBlocks.ALLUAUDIA)) && stackInHand.is(ItemTags.AXES)) {
      Block block = blockState.is(NSWoods.JOSHUA.getLog()) ? NSWoods.JOSHUA.getStrippedLog() : NSMiscBlocks.STRIPPED_ALLUAUDIA;
      BlockState blockState2 = block.defaultBlockState()
          .setValue(BranchingTrunkBlock.DOWN, blockState.getValue(BranchingTrunkBlock.DOWN))
          .setValue(BranchingTrunkBlock.UP, blockState.getValue(BranchingTrunkBlock.UP))
          .setValue(BranchingTrunkBlock.NORTH, blockState.getValue(BranchingTrunkBlock.NORTH))
          .setValue(BranchingTrunkBlock.SOUTH, blockState.getValue(BranchingTrunkBlock.SOUTH))
          .setValue(BranchingTrunkBlock.EAST, blockState.getValue(BranchingTrunkBlock.EAST))
          .setValue(BranchingTrunkBlock.WEST, blockState.getValue(BranchingTrunkBlock.WEST))
          .setValue(BranchingTrunkBlock.WATERLOGGED, blockState.getValue(BranchingTrunkBlock.WATERLOGGED))
          .setValue(BranchingTrunkBlock.SHEARED, blockState.getValue(BranchingTrunkBlock.SHEARED));
      world.playSound(player, blockPos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);

      if (player instanceof ServerPlayer serverPlayer) {
        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, blockPos, stackInHand);
      }

      world.setBlock(blockPos, blockState2, 11);
      world.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(player, blockState2));
      if (player != null) {
        stackInHand.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
      }

      event.setCancellationResult(InteractionResult.SUCCESS);
      event.setCanceled(true);
      return;
    }
    if (blockState.is(BlockTags.CAULDRONS) && stackInHand.is(Items.MILK_BUCKET) && !blockState.is(NSMiscBlocks.MILK_CAULDRON)) {
      world.playSound(player, blockPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
      if (player instanceof ServerPlayer serverPlayer) {
        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, blockPos, stackInHand);
      }

      world.setBlock(blockPos, NSMiscBlocks.MILK_CAULDRON.defaultBlockState(), 11);
      world.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(player, NSMiscBlocks.MILK_CAULDRON.defaultBlockState()));
      if (!player.isCreative() && !player.isSpectator()) {
        player.setItemInHand(hand, new ItemStack(Items.BUCKET));
      }

      event.setCancellationResult(InteractionResult.SUCCESS);
      event.setCanceled(true);
      return;
    }
    if (blockState.is(BlockTags.CAULDRONS) && stackInHand.is(NSMiscBlocks.CHEESE_BUCKET) && !blockState.is(NSMiscBlocks.CHEESE_CAULDRON)) {
      world.playSound(player, blockPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
      if (player instanceof ServerPlayer serverPlayer) {
        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, blockPos, stackInHand);
      }

      world.setBlock(blockPos, NSMiscBlocks.CHEESE_CAULDRON.defaultBlockState(), 11);
      world.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(player, NSMiscBlocks.CHEESE_CAULDRON.defaultBlockState()));
      if (!player.isCreative() && !player.isSpectator()) {
        player.setItemInHand(hand, new ItemStack(Items.BUCKET));
      }

      event.setCancellationResult(InteractionResult.SUCCESS);
      event.setCanceled(true);
    }
  }
}
