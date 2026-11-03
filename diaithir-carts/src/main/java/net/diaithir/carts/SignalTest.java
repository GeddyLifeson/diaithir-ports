package net.diaithir.carts;

import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/** /diaithircarts signaltest: a straight run of rail with a slope and a corner, a signal beside it; green with no cart, red with one on the far end, green again when it is gone. */
final class SignalTest {
    private SignalTest() {}

    private static final Logger LOG = LogUtils.getLogger();
    private static CommandSourceStack source;
    private static ServerLevel level;
    private static BlockPos at, signal, far;
    private static AbstractMinecart cart;
    private static int t = -1;
    private static final List<String> fails = new ArrayList<>();

    static String start(CommandSourceStack s) {
        if (t >= 0) return "already running";
        source = s;
        level = s.getLevel();
        at = new BlockPos(0, 232, 0);
        for (int cx = -1; cx <= 1; cx++) for (int cz = -1; cz <= 0; cz++) { level.getChunk(cx, cz); level.setChunkForced(cx, cz, true); }
        fails.clear();
        for (int x = -2; x <= 30; x++) for (int z = -6; z <= 6; z++) {
            for (int y = 0; y <= 4; y++) level.setBlock(at.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(at.offset(x, -1, z), Blocks.STONE.defaultBlockState(), 3);
        }
        // east along z=0 for 20 rails, then a corner turning south for 4 more
        for (int x = 0; x < 20; x++) level.setBlock(at.east(x), Blocks.RAIL.defaultBlockState().setValue(net.minecraft.world.level.block.RailBlock.SHAPE, RailShape.EAST_WEST), 3);
        level.setBlock(at.east(20), Blocks.RAIL.defaultBlockState().setValue(net.minecraft.world.level.block.RailBlock.SHAPE, RailShape.SOUTH_WEST), 3);
        for (int z = 1; z <= 4; z++) level.setBlock(at.east(20).south(z), Blocks.RAIL.defaultBlockState().setValue(net.minecraft.world.level.block.RailBlock.SHAPE, RailShape.NORTH_SOUTH), 3);
        signal = at.east(2).north();
        level.setBlock(signal, DiaithirCarts.BLOCK_SIGNAL.get().defaultBlockState().setValue(BlockSignal.FACING, Direction.SOUTH), 3);
        far = at.east(20).south(4);
        List<BlockPos> stretch = BlockSignal.stretch(level, signal, Direction.SOUTH);
        if (stretch.size() != 25) fails.add("the stretch has " + stretch.size() + " rails, not 25");
        t = 0;
        return null;
    }

    static void tick(MinecraftServer server) {
        if (t < 0) return;
        t++;
        BlockPos sig = signal;
        if (t == 20 && level.getBlockState(sig).getValue(BlockSignal.RED)) fails.add("red with no cart");
        if (t == 20) {
            cart = EntityType.MINECART.create(level, EntitySpawnReason.COMMAND);
            cart.setPos(far.getX() + 0.5, far.getY() + 0.1, far.getZ() + 0.5);
            level.addFreshEntity(cart);
        }
        if (t == 50) {
            if (!level.getBlockState(sig).getValue(BlockSignal.RED)) fails.add("not red with a cart at the far end of the stretch");
            if (level.getSignal(sig.east(), Direction.EAST) != 15 && level.getBestNeighborSignal(sig.east()) != 15) fails.add("no redstone signal while red");
            cart.discard();
        }
        if (t == 80) {
            if (level.getBlockState(sig).getValue(BlockSignal.RED)) fails.add("still red after the cart left");
            // a second signal facing a track that is not connected sees only its own stretch
            BlockPos lone = at.offset(0, 0, 8);
            level.setBlock(lone, Blocks.RAIL.defaultBlockState(), 3);
            if (BlockSignal.stretch(level, lone.north(), Direction.SOUTH).size() != 1) fails.add("a lone rail's stretch is not 1");
            level.setBlock(lone, Blocks.AIR.defaultBlockState(), 3);
            String line = fails.isEmpty() ? "Signal self test PASS" : "Signal self test FAIL: " + String.join("; ", fails);
            LOG.info(line);
            source.sendSuccess(() -> Component.literal(line), false);
            for (int cx = -1; cx <= 1; cx++) for (int cz = -1; cz <= 0; cz++) level.setChunkForced(cx, cz, false);
            t = -1;
        }
    }
}
