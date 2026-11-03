package net.diaithir.carts;

import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * /diaithircarts scheduletest: two lanes with stations at x=10, 20 and 30 (the middle one holds 2 s, the others wait for a signal).
 * Lane 0: a crewed cart (villager aboard) with the schedule [20, 30]. Lane 1: the same schedule, no crew. The uncrewed cart is then released by fitting an autonomy module.
 */
final class ScheduleTest {
    private ScheduleTest() {}

    private static final Logger LOG = LogUtils.getLogger();
    private static final int[] STATIONS = {10, 20, 30};
    private static CommandSourceStack source;
    private static ServerLevel level;
    private static BlockPos at;
    private static final AbstractMinecart[] carts = new AbstractMinecart[2];
    private static Entity rider;
    private static int t = -1;
    private static final List<String> fails = new ArrayList<>();

    static String start(CommandSourceStack s) {
        if (t >= 0) return "already running";
        source = s;
        level = s.getLevel();
        at = new BlockPos(0, 232, 80);
        for (int cx = 0; cx <= 2; cx++) for (int cz = 4; cz <= 6; cz++) { level.getChunk(cx, cz); level.setChunkForced(cx, cz, true); }
        fails.clear();
        for (int x = -2; x <= 40; x++) for (int z = -3; z <= 12; z++) {
            for (int y = 0; y <= 4; y++) level.setBlock(at.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(at.offset(x, -1, z), Blocks.STONE.defaultBlockState(), 3);
        }
        for (int lane = 0; lane < 2; lane++) {
            int z = lane * 4;
            for (int x = 0; x <= 38; x++) {
                BlockPos p = at.offset(x, 0, z);
                int st = stationIndex(x);
                if (st >= 0) level.setBlock(p, DiaithirCarts.STATION_RAIL.get().defaultBlockState().setValue(PoweredRailBlock.SHAPE, RailShape.EAST_WEST)
                        .setValue(StationRail.DELAY, st == 1 ? 1 : 0), 3);
                else level.setBlock(p, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.EAST_WEST), 3);
            }
            AbstractMinecart c = EntityType.MINECART.create(level, EntitySpawnReason.COMMAND);
            c.setPos(at.getX() + 2.5, at.getY() + 0.1, at.getZ() + z + 0.5);
            level.addFreshEntity(c);
            Schedule.set(c, new Schedule(List.of(at.offset(20, 0, z), at.offset(30, 0, z)), 0));
            carts[lane] = c;
        }
        rider = EntityType.VILLAGER.create(level, EntitySpawnReason.COMMAND);
        rider.setPos(carts[0].getX(), carts[0].getY(), carts[0].getZ());
        level.addFreshEntity(rider);
        rider.startRiding(carts[0], true, false);
        t = 0;
        return null;
    }

    private static int stationIndex(int x) {
        for (int i = 0; i < STATIONS.length; i++) if (STATIONS[i] == x) return i;
        return -1;
    }

    private static boolean nearStation(double x) {
        for (int s : STATIONS) if (x > s - 4 && x < s + 3) return true;
        return false;
    }

    private static void expect(String what, boolean ok) {
        if (!ok) fails.add(what);
    }

    static void tick(MinecraftServer server) {
        if (t < 0) return;
        t++;
        for (int lane = 0; lane < 2; lane++) {
            AbstractMinecart c = carts[lane];
            if (c.isAlive() && t < 260 && !nearStation(c.getX() - at.getX()) && c.getDeltaMovement().horizontalDistance() < 0.2) c.setDeltaMovement(0.3, 0, 0);
        }
        // lane 2 runs under Age VII, the others under Age 0: switch the supplier by whose turn it is is not possible, so lane 2 gets its own check below
        if (t == 125) {
            double x0 = carts[0].getX() - at.getX(), x1 = carts[1].getX() - at.getX();
            expect("crewed cart should wait at its first stop x=20, got x=" + x0, x0 >= 20 && x0 < 21);
            expect("uncrewed cart should wait at the first station x=10, got x=" + x1, x1 >= 10 && x1 < 11);
            expect("schedule moved early: " + Schedule.get(carts[0]).next(), Schedule.get(carts[0]).next() == 0);
            expect("status of crewed cart", Autonomy.status(carts[0]) == Autonomy.Status.CREWED);
            expect("status of uncrewed cart", Autonomy.status(carts[1]) == Autonomy.Status.UNCREWED);
        }
        if (t == 260) {
            double x0 = carts[0].getX() - at.getX(), x1 = carts[1].getX() - at.getX();
            expect("crewed cart should wait at its second stop x=30, got x=" + x0, x0 >= 30 && x0 < 31);
            expect("crewed cart schedule should point at the second stop: " + Schedule.get(carts[0]).next(), Schedule.get(carts[0]).next() == 1);
            expect("uncrewed cart left its station x=" + x1, x1 >= 10 && x1 < 11);
            // now Age VII: the uncrewed cart is released and runs on
            expect("a module must not be fitted by default", !Autonomy.autonomous(carts[1]));
            expect("autonomousCrew must be false before a module", !Autonomy.autonomousCrew("driver", carts[1]));
            expect("the module fits a capable cart", Autonomy.fit(carts[1]));
            expect("status with a module", Autonomy.status(carts[1]) == Autonomy.Status.AUTONOMOUS);
            expect("autonomousCrew with a module", Autonomy.autonomousCrew("driver", carts[1]) && !Autonomy.autonomousCrew("farmer", carts[1]));
            carts[1].setDeltaMovement(0.01, 0, 0);
        }
        if (t == 262) Schedule.set(carts[1], new Schedule(Schedule.get(carts[1]).stops(), 0));
        if (t > 262 && t < 520 && !nearStation(carts[1].getX() - at.getX()) && carts[1].getDeltaMovement().horizontalDistance() < 0.2 && carts[1].isAlive()) carts[1].setDeltaMovement(0.3, 0, 0);
        if (t == 520) {
            double x1 = carts[1].getX() - at.getX();
            expect("autonomous cart should have reached its first stop x=20+, got x=" + x1, x1 >= 20);
            String line = fails.isEmpty() ? "Schedule self test PASS" : "Schedule self test FAIL: " + String.join("; ", fails);
            LOG.info(line);
            source.sendSuccess(() -> Component.literal(line), false);
            rider.discard();
            for (AbstractMinecart c : carts) c.discard();
            for (int cx = 0; cx <= 2; cx++) for (int cz = 4; cz <= 6; cz++) level.setChunkForced(cx, cz, false);
            t = -1;
        }
    }
}
