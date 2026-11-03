package net.diaithir.firearms;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

/** A firearm. Holding use fires (auto guns keep firing); the server does all the work in Engine. */
public final class GunItem extends Item {
    public final Defs.Gun gun;

    public GunItem(Properties p, Defs.Gun gun) { super(p); this.gun = gun; }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
            if (level instanceof ServerLevel && player instanceof ServerPlayer sp) Engine.attach(sp, stack);
            return InteractionResult.SUCCESS;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override public ItemUseAnimation getUseAnimation(ItemStack s) { return ItemUseAnimation.NONE; }
    @Override public int getUseDuration(ItemStack s, LivingEntity user) { return 72000; }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
        if (level instanceof ServerLevel sl && user instanceof ServerPlayer sp) Engine.trigger(sp, stack, gun, 72000 - remaining);
    }

    @Override
    public boolean releaseUsing(ItemStack s, Level l, LivingEntity e, int remaining) { return false; }

    @Override
    public Component getName(ItemStack stack) { return super.getName(stack); }
}
