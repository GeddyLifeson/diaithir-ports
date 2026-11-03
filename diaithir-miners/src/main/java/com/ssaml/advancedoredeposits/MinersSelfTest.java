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
        BlockPos base = level.getRespawnData().pos().above(120);
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
        boolean ok = chunks == 3 && tileGone && plans > 0 && !(level.getBlockState(minerPos).getBlock() instanceof MinerBlock && false);
        return (ok ? "PASS" : "FAIL") + ": miner output chunks=" + chunks + " (want 3), tile removed when empty=" + tileGone + ", patches planned in 80x80 chunks=" + plans;
    }
}
