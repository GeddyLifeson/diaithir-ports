package com.otectus.immersivetanning.item;

import com.otectus.immersivetanning.leather.FieldRepair;
import com.otectus.immersivetanning.util.Feedback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Stitches at the Leatherworker's Bench, and patches worn leather in the field: keep using it to spend one Leather
 * Scrap on the most worn piece you are wearing.
 */
public class AwlItem extends Item {
    private static final int PATCH_TICKS = 24;

    public AwlItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (FieldRepair.mostWornEquipped(player) == null) {
            if (!level.isClientSide()) Feedback.info(player, "nothing_to_patch");
            return InteractionResult.PASS;
        }
        if (FieldRepair.findScraps(player) == null) {
            if (!level.isClientSide()) Feedback.fail(player, "need_scraps");
            return InteractionResult.FAIL;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack awl, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof Player player) {
            ItemStack target = FieldRepair.mostWornEquipped(player);
            ItemStack scraps = FieldRepair.findScraps(player);
            if (target != null && scraps != null && FieldRepair.repair(player, target, scraps, awl)) {
                Feedback.info(player, "patched", target.getHoverName());
            }
        }
        return awl;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return PATCH_TICKS;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.immersive_tanning.awl.hint").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.immersive_tanning.awl.hint_repair").withStyle(ChatFormatting.DARK_GRAY));
    }
}
