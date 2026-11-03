package net.diaithir.carts;

import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
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

/** /diaithircarts desttest: a destination rail on channel 5 (yellow). An unlabelled cart and a lime cart leave it off; a yellow cart turns it on and it goes off again. */
final class DestinationTest {
    private DestinationTest() {}

    private static final Logger LOG = LogUtils.getLogger();
    private static CommandSourceStack source;
    private static ServerLevel level;
    private static BlockPos rail;
    private static AbstractMinecart cart;
    private static int t = -1;
    private static final List<String> fails = new ArrayList<>();

    static String start(CommandSourceStack s) {
        if (t >= 0) return "already running";
        source = s;
        level = s.getLevel();
        BlockPos at = new BlockPos(0, 236, 0);
        for (int cx = -1; cx <= 1; cx++) for (int cz = -1; cz <= 0; cz++) { level.getChunk(cx, cz); level.setChunkForced(cx, cz, true); }
        fails.clear();
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) {
            for (int y = 0; y <= 3; y++) level.setBlock(at.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(at.offset(x, -1, z), Blocks.STONE.defaultBlockState(), 3);
        }
        rail = at;
        level.setBlock(rail, DiaithirCarts.DESTINATION_RAIL.get().defaultBlockState().setValue(DestinationRail.SHAPE, RailShape.EAST_WEST)
                .setValue(DestinationRail.CHANNEL, DyeColorId.YELLOW + 1), 3);
        t = 0;
        return null;
    }

    private static final class DyeColorId { static final int YELLOW = net.minecraft.world.item.DyeColor.YELLOW.getId(); static final int LIME = net.minecraft.world.item.DyeColor.LIME.getId(); }

    private static boolean on() {
        return level.getBlockState(rail).getValue(DestinationRail.POWERED);
    }

    private static void spawn(int label) {
        cart = EntityType.MINECART.create(level, EntitySpawnReason.COMMAND);
        cart.setPos(rail.getX() + 0.5, rail.getY() + 0.1, rail.getZ() + 0.5);
        Destination.setLabel(cart, label);
        level.addFreshEntity(cart);
    }

    static void tick(MinecraftServer server) {
        if (t < 0) return;
        t++;
        if (t == 5) spawn(0);
        if (t == 25) { if (on()) fails.add("on for an unlabelled cart"); cart.discard(); }
        if (t == 30) spawn(DyeColorId.LIME + 1);
        if (t == 50) { if (on()) fails.add("on for a lime cart"); cart.discard(); }
        if (t == 55) spawn(DyeColorId.YELLOW + 1);
        if (t == 75) {
            if (!on()) fails.add("off for a yellow cart");
            if (level.getBestNeighborSignal(rail.east()) < 1 && level.getSignal(rail.above(), net.minecraft.core.Direction.UP) < 1 && level.getDirectSignal(rail.below(), net.minecraft.core.Direction.DOWN) < 15) fails.add("no signal to neighbours while on");
            if (Destination.labelOf(cart) != DyeColorId.YELLOW + 1) fails.add("label lost");
            cart.discard();
        }
        if (t == 110) {
            if (on()) fails.add("still on after the cart left");
            level.setBlock(rail, level.getBlockState(rail).setValue(DestinationRail.CHANNEL, 0), 3);
            spawn(0);
        }
        if (t == 130) {
            if (!on()) fails.add("channel 0 should accept an unlabelled cart");
            cart.discard();
            String line = fails.isEmpty() ? "Destination self test PASS" : "Destination self test FAIL: " + String.join("; ", fails);
            LOG.info(line);
            source.sendSuccess(() -> Component.literal(line), false);
            for (int cx = -1; cx <= 1; cx++) for (int cz = -1; cz <= 0; cz++) level.setChunkForced(cx, cz, false);
            t = -1;
        }
    }
}
