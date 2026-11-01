package com.github.ysbbbbbb.kaleidoscopetavern.item;

import com.github.ysbbbbbb.kaleidoscopetavern.entity.ThrownMolotovEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class MolotovBlockItem extends BottleBlockItem {
    public MolotovBlockItem(Identifier id, Block block) {
        super(id, block);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        int time = this.getUseDuration(stack, entity) - timeLeft;
        if (time < 10) {
            return false;
        }
        if (!level.isClientSide()) {
            ThrownMolotovEntity molotov = new ThrownMolotovEntity(level, entity);
            molotov.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), 0, 0.8f, 1);
            level.addFreshEntity(molotov);

            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.5F,
                    0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

            if (!(entity instanceof Player player) || !player.isCreative()) {
                stack.shrink(1);
            }
        }
        return true;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.TRIDENT;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }
}
