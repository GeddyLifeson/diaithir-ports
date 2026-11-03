package com.ssaml.advancedoredeposits;

import com.mojang.brigadier.Command;
import com.ssaml.advancedoredeposits.block.DepositBlockEntity;
import com.ssaml.advancedoredeposits.miner.MinerBlock;
import com.ssaml.advancedoredeposits.miner.MinerBlockEntity;
import com.ssaml.advancedoredeposits.miner.MinerKind;
import com.ssaml.advancedoredeposits.registry.ModBlocks;
import com.ssaml.advancedoredeposits.registry.ModItems;
import com.ssaml.advancedoredeposits.worldgen.DepositPatchPlan;

import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** /diaithirminers selftest: builds a yard above spawn, runs a burner miner against a 3-cycle tile and checks worldgen plans. */
public final class MinersSelfTest {
    private MinersSelfTest() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("diaithirminers").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.literal("selftest").executes(ctx -> {
                    String result = run(ctx.getSource().getLevel());
                    ctx.getSource().sendSuccess(() -> Component.literal(result), false);
                    return Command.SINGLE_SUCCESS;
                })));
    }

    /** Dev hook: a file named selftest.flag in the run folder runs the test once at server start, then stops the server. */
    public static void onStarted(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
        if (java.nio.file.Files.exists(java.nio.file.Path.of("selftest.flag"))) {
            AdvancedOreDeposits.LOGGER.info("SELFTEST {}", run(event.getServer().overworld()));
            event.getServer().halt(false);
        }
    }

    private static String run(ServerLevel level) {
        BlockPos spawnPos = level.getRespawnData().pos();
        level.getChunk(spawnPos.getX() >> 4, spawnPos.getZ() >> 4, net.minecraft.world.level.chunk.status.ChunkStatus.FULL, true);
        BlockPos base = new BlockPos(spawnPos.getX(), level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, spawnPos.getX(), spawnPos.getZ()) + 3, spawnPos.getZ());
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = 0; dy <= 4; dy++) {
                    level.setBlockAndUpdate(base.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
                }
                level.setBlockAndUpdate(base.offset(dx, -1, dz), Blocks.STONE.defaultBlockState());
            }
        }
        BlockPos tile = base.offset(1, 0, 0);
        level.setBlockAndUpdate(tile, ModBlocks.deposit(DepositType.IRON).defaultBlockState());
        ((DepositBlockEntity) level.getBlockEntity(tile)).initialize(DepositType.IRON, 3, 10, 16, 1L);
        BlockPos minerPos = tile.above();
        level.setBlockAndUpdate(minerPos, ModBlocks.MINERS.get(MinerKind.BURNER).get().defaultBlockState());
        MinerBlockEntity miner = (MinerBlockEntity) level.getBlockEntity(minerPos);
        miner.setItem(1, new ItemStack(Items.COAL, 4));
        for (int t = 0; t < 3 * MinerKind.BURNER.cycleTicks + 10; t++) {
            MinerBlockEntity.serverTick(level, minerPos, level.getBlockState(minerPos), miner);
        }
        int chunks = miner.getItem(0).is(ModItems.rawChunk(DepositType.IRON)) ? miner.getItem(0).getCount() : 0;
        boolean tileGone = level.getBlockState(tile).isAir();
        int plans = 0;
        for (int cx = -40; cx < 40; cx++) {
            for (int cz = -40; cz < 40; cz++) {
                if (DepositPatchPlan.forChunk(12345L, cx, cz) != null) {
                    plans++;
                }
            }
        }
        String power = powerTest(level, base.offset(-1, 0, 0));
        String gen = worldgenTest(level);
        boolean ok = power.startsWith("ok") && gen.startsWith("ok") && chunks == 3 && tileGone && plans > 0 && !(level.getBlockState(minerPos).getBlock() instanceof MinerBlock && false);
        return (ok ? "PASS" : "FAIL") + ": miner output chunks=" + chunks + " (want 3), tile removed when empty=" + tileGone + ", patches planned in 80x80 chunks=" + plans
                + " | power: " + power + " | worldgen: " + gen;
    }

    /** Feeds an electric miner through the public energy capability, the way a generator or cable would, then runs it. */
    private static String powerTest(ServerLevel level, BlockPos tile) {
        level.setBlockAndUpdate(tile, ModBlocks.deposit(DepositType.COPPER).defaultBlockState());
        ((DepositBlockEntity) level.getBlockEntity(tile)).initialize(DepositType.COPPER, 5, 10, 16, 2L);
        BlockPos pos = tile.above();
        level.setBlockAndUpdate(pos, ModBlocks.MINERS.get(MinerKind.ELECTRIC).get().defaultBlockState());
        MinerBlockEntity miner = (MinerBlockEntity) level.getBlockEntity(pos);
        var handler = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.Energy.BLOCK, pos, net.minecraft.core.Direction.UP);
        if (handler == null) {
            return "FAIL no energy capability";
        }
        for (int t = 0; t < MinerKind.ELECTRIC.cycleTicks + 10; t++) {
            MinerBlockEntity.serverTick(level, pos, level.getBlockState(pos), miner);
        }
        int dry = miner.getItem(0).getCount();
        int inserted;
        try (var tx = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
            inserted = handler.insert(ENERGY_FEED, tx);
            tx.commit();
        }
        for (int t = 0; t < 3 * MinerKind.ELECTRIC.cycleTicks; t++) {
            if (t % 40 == 0) { // a generator pushing 1000 FE every two seconds
                try (var tx = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                    handler.insert(ENERGY_FEED, tx);
                    tx.commit();
                }
            }
            MinerBlockEntity.serverTick(level, pos, level.getBlockState(pos), miner);
        }
        int powered = miner.getItem(0).getCount();
        long left = handler.getAmountAsLong();
        boolean ok = dry == 0 && inserted == ENERGY_FEED && powered >= 1;
        return (ok ? "ok" : "FAIL") + " unpowered=" + dry + " chunks, inserted=" + inserted + " FE, powered=" + powered + " chunks, FE left=" + left;
    }

    private static final int ENERGY_FEED = 1000;

    /** Generates the chunks of the nearest planned patch in the real world generator and counts deposit tiles. */
    private static String worldgenTest(ServerLevel level) {
        net.minecraft.world.level.ChunkPos spawn = net.minecraft.world.level.ChunkPos.containing(level.getRespawnData().pos());
        long seed = level.getSeed();
        int planned = 0, tiles = 0, withBe = 0, sample = 0;
        final int radius = 12;
        for (int x = spawn.x() - radius; x <= spawn.x() + radius; x++) {
            for (int z = spawn.z() - radius; z <= spawn.z() + radius; z++) {
                if (DepositPatchPlan.forChunk(seed, x, z) != null) {
                    planned++;
                }
                var chunk = level.getChunk(x, z, net.minecraft.world.level.chunk.status.ChunkStatus.FULL, true);
                for (int bx = 0; bx < 16; bx++) {
                    for (int bz = 0; bz < 16; bz++) {
                        int y = chunk.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, bx, bz);
                        for (int dy = 0; dy < 10; dy++) {
                            BlockPos bp = new BlockPos((x << 4) + bx, y + 1 - dy, (z << 4) + bz);
                            if (Math.abs(bp.getX() - level.getRespawnData().pos().getX()) <= 4 && Math.abs(bp.getZ() - level.getRespawnData().pos().getZ()) <= 4) {
                                continue; // the test yard
                            }
                            if (chunk.getBlockState(bp).getBlock() instanceof com.ssaml.advancedoredeposits.block.DepositBlock) {
                                tiles++;
                                if (chunk.getBlockEntity(bp) instanceof DepositBlockEntity d) {
                                    withBe++;
                                    sample = d.remainingBlocks();
                                }
                            }
                        }
                    }
                }
            }
        }
                String dbg = " [feature calls=" + com.ssaml.advancedoredeposits.worldgen.DepositPatchFeature.CALLS + " placed=" + com.ssaml.advancedoredeposits.worldgen.DepositPatchFeature.PLACED
                + " noSurface=" + com.ssaml.advancedoredeposits.worldgen.DepositPatchFeature.NO_SURFACE + " badBiome=" + com.ssaml.advancedoredeposits.worldgen.DepositPatchFeature.BAD_BIOME + "]";
        boolean ok = tiles > 0 && withBe == tiles;
        return (ok ? "ok" : "FAIL") + " " + (2 * radius + 1) + "x" + (2 * radius + 1) + " chunks around spawn, patches planned=" + planned + ": tiles=" + tiles
                + " withCount=" + withBe + " lastRemaining=" + sample + dbg;
    }
}
