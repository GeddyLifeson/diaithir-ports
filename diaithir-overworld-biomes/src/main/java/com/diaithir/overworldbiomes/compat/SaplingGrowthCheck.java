package com.diaithir.overworldbiomes.compat;

import com.mojang.brigadier.Command;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Op-only check: /diaithir_sapling_check grows every compat sapling with the real SaplingBlock code path and reports. */
public final class SaplingGrowthCheck {
    private SaplingGrowthCheck() {}

    public static void onRegister(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("diaithir_sapling_check").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(ctx -> {
            ServerLevel level = ctx.getSource().getLevel();
            int i = 0, ok = 0, bad = 0;
            StringBuilder failed = new StringBuilder();
            for (Block b : BuiltInRegistries.BLOCK) {
                var key = BuiltInRegistries.BLOCK.getKey(b);
                if (!(b instanceof SaplingBlock sap) || (!key.getNamespace().equals("biomesoplenty") && !key.getNamespace().equals("natures_spirit"))) continue;
                BlockPos base = new BlockPos(400 + (i % 10) * 24, 100, 400 + (i / 10) * 24);
                level.getChunk(base);
                for (int dx = -10; dx <= 10; dx++) for (int dz = -10; dz <= 10; dz++) {
                    level.setBlock(base.offset(dx, -1, dz), Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                    for (int dy = 0; dy < 24; dy++) level.setBlock(base.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), 2);
                }
                BlockState st = b.defaultBlockState();
                level.setBlock(base, st, 2);
                for (int k = 0; k < 2; k++) sap.advanceTree(level, base, level.getBlockState(base), level.getRandom());
                int logs = 0, leaves = 0;
                for (int dx = -8; dx <= 8; dx++) for (int dz = -8; dz <= 8; dz++) for (int dy = 0; dy < 24; dy++) {
                    String p = BuiltInRegistries.BLOCK.getKey(level.getBlockState(base.offset(dx, dy, dz)).getBlock()).getPath();
                    if (p.endsWith("_log") || p.endsWith("_wood")) logs++;
                    if (p.endsWith("_leaves")) leaves++;
                }
                if (logs > 0 && leaves > 0) ok++; else { bad++; failed.append(key).append(' '); }
                i++;
            }
            final String msg = "saplings ok=" + ok + " failed=" + bad + " " + failed;
            ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
            return Command.SINGLE_SUCCESS;
        }));
    }
}
