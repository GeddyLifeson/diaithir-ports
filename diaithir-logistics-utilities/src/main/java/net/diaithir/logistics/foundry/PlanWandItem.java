package net.diaithir.logistics.foundry;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

/**
 * Plan wand. Hold it with a block in the other hand and use it on a face: a ghost of that block appears there, and nothing is used up.
 * Sneak and use it on a ghost to clear the ghost.
 */
public final class PlanWandItem extends Item {
    public PlanWandItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        BlockPos clicked = ctx.getClickedPos();
        if (player.isShiftKeyDown() && level.getBlockState(clicked).getBlock() instanceof GhostBlock) {
            level.setBlock(clicked, Blocks.AIR.defaultBlockState(), 3);
            player.sendOverlayMessage(Component.literal("Ghost cleared"));
            return InteractionResult.SUCCESS;
        }
        ItemStack other = player.getItemInHand(ctx.getHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (other.isEmpty() && !(level.getBlockState(clicked).getBlock() instanceof GhostBlock) && player.isShiftKeyDown()) {
            return mark(level, player, clicked);
        }
        if (!(other.getItem() instanceof BlockItem bi) || bi.getBlock() instanceof GhostBlock) {
            player.sendOverlayMessage(Component.literal("Hold the block to plan in your other hand, or sneak with an empty hand to mark a block for demolition"));
            return InteractionResult.FAIL;
        }
        BlockPlaceContext pc = new BlockPlaceContext(ctx);
        if (!pc.canPlace()) {
            player.sendOverlayMessage(Component.literal("No room for a ghost there"));
            return InteractionResult.FAIL;
        }
        Block block = bi.getBlock();
        BlockState target = block.getStateForPlacement(pc);
        if (target == null) target = block.defaultBlockState();
        BlockPos at = pc.getClickedPos();
        level.setBlock(at, DiaithirLogistics.GHOST.get().defaultBlockState(), 3);
        if (level.getBlockEntity(at) instanceof GhostBlockEntity g) g.setTarget(target);
        return InteractionResult.SUCCESS;
    }

    /** Sneak with an empty other hand: mark the block for the nearest depot in reach to demolish (deconstruction plan). */
    private static InteractionResult mark(Level level, Player player, BlockPos clicked) {
        DepotBlockEntity best = null;
        double bd = Double.MAX_VALUE;
        double r2 = Math.pow(Config.SERVICE_RADIUS.get(), 2);
        for (BlockPos dp : Sites.depots(level)) {
            double d = dp.distSqr(clicked);
            if (d <= r2 && d < bd && level.getBlockEntity(dp) instanceof DepotBlockEntity be) {
                best = be;
                bd = d;
            }
        }
        if (best == null) {
            player.sendOverlayMessage(Component.literal("No depot reaches that block"));
            return InteractionResult.FAIL;
        }
        player.sendOverlayMessage(Component.literal(best.mark(clicked) ? "Marked for demolition" : "Already marked"));
        return InteractionResult.SUCCESS;
    }
}
