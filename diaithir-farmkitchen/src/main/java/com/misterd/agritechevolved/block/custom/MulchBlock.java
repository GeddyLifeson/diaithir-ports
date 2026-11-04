package com.misterd.agritechevolved.block.custom;

import com.misterd.agritechevolved.block.ATEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class MulchBlock extends Block {

    public MulchBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return stack.getItem() instanceof HoeItem
                ? convertToInfusedFarmland(level, pos, player, stack)
                : InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        ItemStack mainHand = player.getMainHandItem();
        return mainHand.getItem() instanceof HoeItem
                ? convertToInfusedFarmland(level, pos, player, mainHand)
                : InteractionResult.PASS;
    }

    private InteractionResult convertToInfusedFarmland(Level level, BlockPos pos, Player player, ItemStack hoe) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        level.setBlockAndUpdate(pos, ATEBlocks.INFUSED_FARMLAND.get().defaultBlockState());
        level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (!player.getAbilities().instabuild) {
            hoe.hurtAndBreak(1, player, player.getEquipmentSlotForItem(hoe));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @Nullable
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility toolAction, boolean simulate) {
        return toolAction == ItemAbilities.HOE_TILL
                ? ATEBlocks.INFUSED_FARMLAND.get().defaultBlockState()
                : super.getToolModifiedState(state, context, toolAction, simulate);
    }
}