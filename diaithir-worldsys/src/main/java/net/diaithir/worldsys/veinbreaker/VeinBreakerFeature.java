package net.diaithir.worldsys.veinbreaker;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/** Sneak-mining an ore also breaks the connected vein of the same block. */
public final class VeinBreakerFeature {
    private static boolean active;

    public static void register(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(BreakBlockEvent.class, VeinBreakerFeature::onBreak);
    }

    private static void onBreak(BreakBlockEvent e) {
        if (active || !(e.getPlayer() instanceof ServerPlayer player) || !(e.getLevel() instanceof ServerLevel level)) return;
        if (player.isCreative() || !player.isShiftKeyDown()) return;
        BlockState origin = e.getState();
        if (!origin.is(VeinConfig.ORES)) return;
        ItemStack tool = player.getMainHandItem();
        if (tool.isEmpty() || !tool.isDamageableItem() || !tool.isCorrectToolForDrops(origin)) return;
        BlockPos start = e.getPos();
        Block type = origin.getBlock();
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        seen.add(start);
        queue.add(start);
        active = true;
        try {
            int broken = 0;
            while (!queue.isEmpty() && broken < VeinConfig.MAX_BLOCKS) {
                BlockPos cur = queue.poll();
                for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) for (int dz = -1; dz <= 1; dz++) {
                    BlockPos next = cur.offset(dx, dy, dz);
                    if (broken >= VeinConfig.MAX_BLOCKS || !seen.add(next)) continue;
                    if (Math.abs(next.getX() - start.getX()) > VeinConfig.RADIUS || Math.abs(next.getY() - start.getY()) > VeinConfig.RADIUS
                            || Math.abs(next.getZ() - start.getZ()) > VeinConfig.RADIUS) continue;
                    if (!level.getBlockState(next).is(type)) continue;
                    queue.add(next);
                    if (tool.getDamageValue() >= tool.getMaxDamage() - 1) return;
                    if (player.gameMode.destroyBlock(next)) {
                        tool.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                        player.causeFoodExhaustion(0.05F);
                        broken++;
                    }
                }
            }
        } finally {
            active = false;
        }
    }
}
