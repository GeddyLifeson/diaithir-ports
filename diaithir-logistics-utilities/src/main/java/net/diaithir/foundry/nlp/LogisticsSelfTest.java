// SPDX-License-Identifier: LGPL-3.0-only
// Design derived from Not Logistics Pipes (c) Contributors to Not Logistics Pipes <https://nlp.rs485.network>, reimplemented in Java for NeoForge 26.1.2 (Diaithir).
package net.diaithir.foundry.nlp;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.ArrayList;
import java.util.List;

/** Builds a pipe run high above spawn and drives the pipe ticks by hand: extractor, priority between sinks, item filter, route and delivery. */
final class LogisticsSelfTest {
    private LogisticsSelfTest() {}

    private static int total(ChestBlockEntity c, net.minecraft.world.item.Item item) {
        int n = 0;
        for (int i = 0; i < c.getContainerSize(); i++) if (c.getItem(i).is(item)) n += c.getItem(i).getCount();
        return n;
    }

    static List<String> run(MinecraftServer server) {
        List<String> fails = new ArrayList<>();
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        if (level == null) return List.of("no overworld");
        BlockPos at = new BlockPos(level.getRespawnData().pos().getX(), 240, level.getRespawnData().pos().getZ());
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-2, -2, -2), at.offset(12, 3, 2))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
        // x0 chest | x1 extractor | x2,x3 pipe | x4 sink A (iron, priority 80) | x5 pipe | x6 sink B (all, priority 50) | x7 pipe stub
        BlockPos chestSrc = at, extractor = at.offset(1, 0, 0), sinkA = at.offset(4, 0, 0), sinkB = at.offset(6, 0, 0);
        BlockPos chestA = sinkA.above(), chestB = sinkB.above();
        level.setBlock(chestSrc, Blocks.CHEST.defaultBlockState(), 3);
        level.setBlock(extractor, LogisticsMod.EXTRACTOR_PIPE.get().defaultBlockState(), 3);
        level.setBlock(at.offset(2, 0, 0), LogisticsMod.PIPE.get().defaultBlockState(), 3);
        level.setBlock(at.offset(3, 0, 0), LogisticsMod.SPEED_PIPE.get().defaultBlockState(), 3);
        level.setBlock(sinkA, LogisticsMod.SINK_PIPE.get().defaultBlockState(), 3);
        level.setBlock(at.offset(5, 0, 0), LogisticsMod.PIPE.get().defaultBlockState(), 3);
        level.setBlock(sinkB, LogisticsMod.SINK_PIPE.get().defaultBlockState(), 3);
        level.setBlock(at.offset(7, 0, 0), LogisticsMod.PIPE.get().defaultBlockState(), 3);
        level.setBlock(chestA, Blocks.CHEST.defaultBlockState(), 3);
        level.setBlock(chestB, Blocks.CHEST.defaultBlockState(), 3);
        ChestBlockEntity src = (ChestBlockEntity) level.getBlockEntity(chestSrc);
        ChestBlockEntity ca = (ChestBlockEntity) level.getBlockEntity(chestA);
        ChestBlockEntity cb = (ChestBlockEntity) level.getBlockEntity(chestB);
        if (src == null || ca == null || cb == null) return List.of("chests have no block entity");
        PipeBlockEntity a = (PipeBlockEntity) level.getBlockEntity(sinkA);
        PipeBlockEntity b = (PipeBlockEntity) level.getBlockEntity(sinkB);
        if (a == null || b == null) return List.of("pipes have no block entity");
        a.sink().priority = 80;
        a.sink().items.add(BuiltInRegistries.ITEM.getKey(Items.IRON_INGOT));
        b.sink().all = true;
        src.setItem(0, new ItemStack(Items.COBBLESTONE, 64));
        src.setItem(1, new ItemStack(Items.IRON_INGOT, 10));

        if (!PipeNet.discover(level, extractor).reaches(at.offset(7, 0, 0).asLong())) fails.add("route does not reach the last pipe");
        if (PipeNet.discover(level, extractor).distance(sinkB.asLong()) != 5) fails.add("distance to sink B is " + PipeNet.discover(level, extractor).distance(sinkB.asLong()));

        List<PipeBlockEntity> pipes = new ArrayList<>();
        for (int x = 1; x <= 7; x++) pipes.add((PipeBlockEntity) level.getBlockEntity(at.offset(x, 0, 0)));
        for (long t = 1; t <= 900; t++) for (PipeBlockEntity p : pipes) p.serverTick(level, t);

        if (total(ca, Items.IRON_INGOT) != 10) fails.add("sink A (priority 80) got " + total(ca, Items.IRON_INGOT) + " of 10 iron");
        if (total(cb, Items.IRON_INGOT) != 0) fails.add("lower priority sink took iron");
        if (total(ca, Items.COBBLESTONE) != 0) fails.add("sink A took cobble it does not list");
        if (total(cb, Items.COBBLESTONE) != 64) fails.add("sink B got " + total(cb, Items.COBBLESTONE) + " of 64 cobble");
        if (total(src, Items.COBBLESTONE) + total(src, Items.IRON_INGOT) != 0) fails.add("source chest not emptied");
        int left = 0;
        for (PipeBlockEntity p : pipes) left += p.travelling();
        if (left != 0) fails.add(left + " items still travelling");

        for (BlockPos p : BlockPos.betweenClosed(at.offset(-2, -2, -2), at.offset(12, 3, 2))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
        return fails;
    }
}
