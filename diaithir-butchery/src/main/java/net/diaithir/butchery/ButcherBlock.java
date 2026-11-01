package net.diaithir.butchery;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Set;

/** A chopping block for small carcasses (birds, fish, rabbits, goats): hold the carcass and use it on the block with a knife in the other hand to take every stage at once. Larger animals still need the meat hook. */
public class ButcherBlock extends Block {
    private static final Set<String> SMALL = Set.of("land_tiny", "land_small", "bird_small", "bird_large", "fish_tiny", "fish_small", "fish_medium", "reptile");

    public ButcherBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!held.is(Butchery.CARCASS.get())) return InteractionResult.TRY_WITH_EMPTY_HAND;
        ItemStack tool = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        String body = held.getOrDefault(Butchery.BODY.get(), "");
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        if (!SMALL.contains(body) || !ButcheryData.known(body)) {
            player.sendOverlayMessage(Component.translatable("message.diaithirbutchery.too_big"));
            return InteractionResult.SUCCESS;
        }
        if (!tool.is(Butchery.TOOLS)) {
            player.sendOverlayMessage(Component.translatable("message.diaithirbutchery.need_knife"));
            return InteractionResult.SUCCESS;
        }
        for (int stage = 0; stage < 3; stage++) MeatHookEntity.dropStage(server, pos, held, body, stage, tool, 400);
        held.shrink(1);
        tool.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        level.playSound(null, pos, SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 1.0F, 1.1F);
        return InteractionResult.SUCCESS;
    }
}
