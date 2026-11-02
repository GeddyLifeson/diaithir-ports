package net.diaithir.logistics.foundry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.ArrayList;
import java.util.List;

/** Builds a depot, a stocked chest and ghosts high above spawn, then drives depot time directly and checks dispatch, building, return and cap. */
final class FoundrySelfTest {
    private FoundrySelfTest() {}

    private static int count(ChestBlockEntity c, net.minecraft.world.item.Item item) {
        int n = 0;
        for (int i = 0; i < c.getContainerSize(); i++) if (c.getItem(i).is(item)) n += c.getItem(i).getCount();
        return n;
    }

    private static void ghost(ServerLevel level, BlockPos p, net.minecraft.world.level.block.state.BlockState target) {
        level.setBlock(p, DiaithirLogistics.GHOST.get().defaultBlockState(), 3);
        ((GhostBlockEntity) level.getBlockEntity(p)).setTarget(target);
    }

    static List<String> run(MinecraftServer server) {
        List<String> fails = new ArrayList<>();
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        if (level == null) return List.of("no overworld");
        BlockPos at = new BlockPos(level.getRespawnData().pos().getX(), 240, level.getRespawnData().pos().getZ());
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-4, -3, -4), at.offset(14, 4, 6))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
        try {
            BlockPos chestPos = at.offset(0, 1, 0);
            level.setBlock(at, DiaithirLogistics.DEPOT.get().defaultBlockState(), 3);
            level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
            ChestBlockEntity chest = (ChestBlockEntity) level.getBlockEntity(chestPos);
            DepotBlockEntity depot = (DepotBlockEntity) level.getBlockEntity(at);
            if (chest == null || depot == null) return List.of("setup failed");
            chest.setItem(0, new ItemStack(Items.STONE, 3));
            long t = level.getGameTime() + 100000;

            // 1. one ghost: dispatch takes one item, ghost stays until arrival, then becomes the block
            BlockPos g1 = at.offset(8, 0, 0);
            ghost(level, g1, Blocks.STONE.defaultBlockState());
            depot.work(level, t);
            if (count(chest, Items.STONE) != 2) fails.add("dispatch did not take an item (" + count(chest, Items.STONE) + ")");
            if (depot.couriersOut() != 1) fails.add("expected 1 courier out, got " + depot.couriersOut());
            if (!(level.getBlockState(g1).getBlock() instanceof GhostBlock)) fails.add("ghost built before arrival");
            depot.work(level, t + 1);
            if (depot.couriersOut() != 1) fails.add("second run sent a second courier to a claimed ghost");
            depot.work(level, t + 200);
            if (!level.getBlockState(g1).is(Blocks.STONE)) fails.add("ghost not built after arrival");
            depot.work(level, t + 400);
            if (depot.couriersOut() != 0) fails.add("courier did not come home (" + depot.couriersOut() + ")");

            // 2. ghost cleared mid-flight: item goes back to stock
            BlockPos g2 = at.offset(0, 0, 5);
            ghost(level, g2, Blocks.STONE.defaultBlockState());
            long t2 = t + 1000;
            depot.work(level, t2);
            if (count(chest, Items.STONE) != 1) fails.add("second dispatch did not take an item");
            level.setBlock(g2, Blocks.AIR.defaultBlockState(), 3);
            depot.work(level, t2 + 200);
            if (count(chest, Items.STONE) != 2) fails.add("item not returned for a cleared ghost (" + count(chest, Items.STONE) + ")");
            depot.work(level, t2 + 400);

            // 3. no stock for the item: nothing is sent
            BlockPos g3 = at.offset(5, 0, 0);
            ghost(level, g3, Blocks.DIAMOND_BLOCK.defaultBlockState());
            long t3 = t + 3000;
            depot.work(level, t3);
            if (depot.couriersOut() != 0) fails.add("sent a courier with no stock");
            level.setBlock(g3, Blocks.AIR.defaultBlockState(), 3);

            // 4. courier cap
            chest.setItem(0, new ItemStack(Items.STONE, 20));
            int cap = Config.COURIERS_PER_DEPOT.get();
            for (int i = 0; i < cap + 3; i++) ghost(level, at.offset(2 + i, 0, 3), Blocks.STONE.defaultBlockState());
            long t4 = t + 5000;
            depot.work(level, t4);
            if (depot.couriersOut() != cap) fails.add("courier cap not held: " + depot.couriersOut() + " vs " + cap);
            depot.work(level, t4 + 300);
            depot.work(level, t4 + 600);
            depot.work(level, t4 + 900);
            depot.work(level, t4 + 1200);
        } finally {
            for (BlockPos p : BlockPos.betweenClosed(at.offset(-4, -3, -4), at.offset(14, 4, 6))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
        }
        return fails;
    }
}
