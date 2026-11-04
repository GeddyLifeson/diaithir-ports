package net.diaithir.travel.laser;

import com.mojang.brigadier.CommandDispatcher;
import net.diaithir.travel.laser.blocks.BridgeSourceBlock;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** /diaithirtravel selftest (owners): grows, shortens and removes a laser bridge in the loaded area at the console or player position, 200 blocks up. */
final class TravelCommands {
    private TravelCommands() {}

    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("diaithirtravel").then(Commands.literal("selftest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
            String r = run(c.getSource());
            c.getSource().sendSuccess(() -> Component.literal(r), false);
            return r.startsWith("PASS") ? 1 : 0;
        })));
    }

    private static int lasers(ServerLevel level, BlockPos src, int max) {
        int n = 0;
        for (int i = 1; i <= max; i++) if (level.getBlockState(src.above(i)).is(DiaithirTravel.LASER_BLOCK.get())) n++;
        return n;
    }

    static String run(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        BlockPos base = BlockPos.containing(source.getPosition().x, 200, source.getPosition().z);
        if (!level.isLoaded(base) || !level.isLoaded(base.above(20))) return "FAIL: test area not loaded";
        StringBuilder fails = new StringBuilder();
        BlockPos src = base.above();
        level.setBlock(base, Blocks.STONE.defaultBlockState(), 3);
        BlockState s = DiaithirTravel.BRIDGE_SOURCE_BLOCK.get().defaultBlockState().setValue(BridgeSourceBlock.FACE, AttachFace.FLOOR)
                .setValue(BlockStateProperties.POWERED, true);
        level.setBlock(src, s, 3);
        BridgeSourceBlock block = (BridgeSourceBlock) s.getBlock();
        int max = Config.maxLength();
        block.generateBridge(level, 15, src, s);
        int full = lasers(level, src, max + 2);
        if (full != max - 1) fails.append(" full power gave ").append(full).append(" lasers, expected ").append(max - 1).append(';');
        block.generateBridge(level, 7, src, s);
        int half = lasers(level, src, max + 2);
        int expectedHalf = (int) ((float) max / 15 * 7) - 1;
        if (half != expectedHalf) fails.append(" power 7 gave ").append(half).append(" lasers, expected ").append(expectedHalf).append(';');
        block.generateBridge(level, 0, src, s);
        level.setBlock(src.above(3), Blocks.STONE.defaultBlockState(), 3);
        block.generateBridge(level, 15, src, s);
        int blocked = lasers(level, src, max + 2);
        if (blocked != 2) fails.append(" blocked bridge gave ").append(blocked).append(" lasers, expected 2;");
        level.setBlock(src.above(3), Blocks.AIR.defaultBlockState(), 3);
        block.generateBridge(level, 0, src, s);
        int none = lasers(level, src, max + 2);
        if (none != 0) fails.append(" power 0 left ").append(none).append(" lasers;");
        for (int i = 0; i <= max + 2; i++) level.setBlock(base.above(i), Blocks.AIR.defaultBlockState(), 3);
        return fails.length() == 0 ? "PASS: laser bridge grows to " + (max - 1) + " m, shortens with power, stops at a block, clears at 0" : "FAIL:" + fails;
    }
}
