package net.diaithir.carts;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Builds a yard high above spawn and runs the cart rules in real ticks: the speed cap of vanilla, bronze, steel and high-speed rails, a powered and an unpowered
 * powered rail, a coupled pair (chain item, follow, spacing, cutters), and two stations (released by redstone, released by timer).
 */
final class CartSelfTest {
    private CartSelfTest() {}

    private static final Logger LOG = LogUtils.getLogger();
    private static final int LANE_LEN = 60;
    private static final int[] LANE_Z = {0, 3, 6, 9, 12, 15, 18, 21, 24, 27, 30};

    private static ServerLevel level;
    private static CommandSourceStack source;
    private static BlockPos at;
    private static int tick = -1;
    private static int warm = 0;
    private static final List<String> fails = new ArrayList<>();
    private static final AbstractMinecart[] carts = new AbstractMinecart[12];
    private static final double[] lastX = new double[12];
    private static final double[] maxStep = new double[12];
    private static final double[] startX = new double[12];
    private static final List<ChunkPos> forced = new ArrayList<>();
    private static FakePlayer player;
    private static ItemStack chains;

    static String start(CommandSourceStack src) {
        if (tick >= 0) return "already running";
        level = src.getServer().getLevel(Level.OVERWORLD);
        if (level == null) return "no overworld";
        source = src;
        fails.clear();
        at = new BlockPos(level.getRespawnData().pos().getX(), 240, level.getRespawnData().pos().getZ());
        for (int cx = (at.getX() - 4) >> 4; cx <= (at.getX() + LANE_LEN + 6) >> 4; cx++) {
            for (int cz = (at.getZ() - 4) >> 4; cz <= (at.getZ() + 36) >> 4; cz++) {
                level.getChunk(cx, cz);
                level.setChunkForced(cx, cz, true);
                forced.add(new ChunkPos(cx, cz));
            }
        }
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-2, -1, -2), at.offset(LANE_LEN + 4, 4, 34))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-1, -1, -1), at.offset(LANE_LEN + 2, -1, 32))) level.setBlock(p, Blocks.STONE_BRICKS.defaultBlockState(), 2 | 16);
        player = FakePlayerFactory.get(level, new GameProfile(UUID.nameUUIDFromBytes("carts-tester".getBytes()), "carts_tester"));
        chains = new ItemStack(DiaithirCarts.COUPLING_CHAIN.get(), 2);
        tick = 0;
        warm = 0;
        return null;
    }

    private static BlockState ew(Block b) {
        BlockState s = b.defaultBlockState();
        if (b instanceof PoweredRailBlock) return s.setValue(PoweredRailBlock.SHAPE, RailShape.EAST_WEST);
        return s.setValue(RailBlock.SHAPE, RailShape.EAST_WEST);
    }

    private static void lane(int idx, Block b) {
        for (int x = 0; x < LANE_LEN; x++) level.setBlock(at.offset(x, 0, LANE_Z[idx]), ew(b), 2 | 16);
    }

    private static AbstractMinecart spawn(int slot, double x, int laneIdx, double dx) {
        AbstractMinecart c = (AbstractMinecart) EntityType.MINECART.create(level, EntitySpawnReason.COMMAND);
        c.setPos(at.getX() + x, at.getY() + 0.0625, at.getZ() + LANE_Z[laneIdx] + 0.5);
        c.setDeltaMovement(dx, 0, 0);
        level.addFreshEntity(c);
        carts[slot] = c;
        lastX[slot] = c.getX();
        startX[slot] = c.getX();
        return c;
    }

    private static void check(boolean ok, String what) {
        if (!ok) fails.add(what);
    }

    private static double speed(int slot) {
        return carts[slot].getDeltaMovement().horizontalDistance();
    }

    static void tick(MinecraftServer server) {
        if (tick < 0) return;
        if (warm < 10) { // let the forced chunks become fully accessible before anything is spawned
            warm++;
            return;
        }
        try {
            step();
        } catch (RuntimeException e) {
            LOG.error("carts self test crashed", e);
            fails.add("exception " + e);
            tick = 1000;
        }
        if (tick >= 0) tick++;
        if (tick > 110) finish();
    }

    private static void step() {
        if (tick == 0) {
            lane(0, Blocks.RAIL);
            lane(1, DiaithirCarts.BRONZE_RAIL.get());
            lane(2, DiaithirCarts.STEEL_RAIL.get());
            lane(3, DiaithirCarts.FAST_RAIL.get());
            lane(4, DiaithirCarts.STEEL_POWERED_RAIL.get());
            lane(5, DiaithirCarts.STEEL_POWERED_RAIL.get());
            lane(6, DiaithirCarts.STEEL_RAIL.get());
            lane(7, DiaithirCarts.STEEL_RAIL.get());
            lane(8, DiaithirCarts.STEEL_RAIL.get());
            level.setBlock(at.offset(20, 0, LANE_Z[7]), ew(DiaithirCarts.STATION_RAIL.get()), 2 | 16);
            level.setBlock(at.offset(20, 0, LANE_Z[8]), ew(DiaithirCarts.STATION_RAIL.get()).setValue(StationRail.DELAY, 1), 2 | 16);
            for (int x = 0; x < LANE_LEN; x += 5) level.setBlock(at.offset(x, 0, LANE_Z[4] + 1), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
            level.setBlock(at.offset(LANE_LEN - 1, 0, LANE_Z[4] + 1), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
            // range: one redstone block at the start of a lane, vanilla powered rails against ours
            lane(9, DiaithirCarts.STEEL_POWERED_RAIL.get());
            lane(10, Blocks.POWERED_RAIL);
            level.setBlock(at.offset(0, 0, LANE_Z[9] + 1), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
            level.setBlock(at.offset(0, 0, LANE_Z[10] + 1), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
            for (int i = 0; i < 4; i++) spawn(i, 1.5, i, 2.0);
            spawn(4, 2.5, 4, 0.1);
            spawn(5, 2.5, 5, 0.3);
            AbstractMinecart leader = spawn(6, 12.5, 6, 0);
            AbstractMinecart follower = spawn(7, 10.5, 6, 0);
            spawn(8, 15.5, 7, 0.5);
            spawn(9, 15.5, 8, 0.5);
            // coupling through the item logic, first the leader then the follower
            Coupling.useChain(level, player, chains, leader);
            String msg = Coupling.useChain(level, player, chains, follower);
            check(msg.equals("Carts coupled."), "chain use did not couple: " + msg);
            check(chains.getCount() == 1, "chain was not used up, count " + chains.getCount());
            check(Coupling.hasLeader(follower) && Coupling.hasFollower(leader), "coupling data missing");
            // a loop and a second leader are refused
            check(!Coupling.canLink(level, follower, leader), "loop was allowed");
            leader.setDeltaMovement(0.8, 0, 0);
            return;
        }
        for (int i = 0; i < 10; i++) {
            if (carts[i] == null) continue;
            maxStep[i] = Math.max(maxStep[i], Math.abs(carts[i].getX() - lastX[i]));
            lastX[i] = carts[i].getX();
        }
        if (tick == 20) {
            for (int l : new int[] {9, 10}) {
                int n = 0;
                while (n < LANE_LEN && level.getBlockState(at.offset(n, 0, LANE_Z[l])).getValue(PoweredRailBlock.POWERED)) n++;
                LOG.info("lane {} powered rails from one source: {}", l, n);
                if (l == 9) check(n >= 9, "power from one source reached only " + n + " rails (vanilla reaches 9)");
            }
            double[] want = {0.4, Tier.BRONZE.maxSpeed, Tier.STEEL.maxSpeed, Tier.FAST.maxSpeed};
            String[] name = {"vanilla", "bronze", "steel", "fast"};
            for (int i = 0; i < 4; i++) {
                check(maxStep[i] <= want[i] + 0.002, name[i] + " rail exceeded its cap: " + maxStep[i] + " > " + want[i]);
                check(maxStep[i] >= want[i] - 0.03, name[i] + " rail did not reach its cap: " + maxStep[i] + " < " + want[i]);
            }
            check(maxStep[1] > maxStep[0] && maxStep[2] > maxStep[1] && maxStep[3] > maxStep[2], "tiers are not in speed order");
            // powered rail: all powered, the cart accelerated
            StringBuilder off = new StringBuilder();
            for (int x = 0; x < LANE_LEN; x++) if (!level.getBlockState(at.offset(x, 0, LANE_Z[4])).getValue(PoweredRailBlock.POWERED)) off.append(x).append(' ');
            check(off.length() == 0, "powered rails not powered by the redstone blocks at x= " + off);
            check(maxStep[4] > 0.6, "powered rail did not accelerate the cart, peak " + maxStep[4]);
            check(maxStep[4] <= Tier.STEEL.maxSpeed + 0.002, "powered steel rail went over its cap, peak " + maxStep[4]);
            check(level.getBlockState(at.offset(5, 0, LANE_Z[5])).getValue(PoweredRailBlock.POWERED) == false, "unpowered rail reads powered");
            check(carts[5].getX() - startX[5] < 2.5 && speed(5) < 0.03, "unpowered powered rail did not stop the cart");
        }
        if (tick == 40) {
            AbstractMinecart s1 = carts[8];
            check(s1.blockPosition().getX() == at.getX() + 20 && speed(8) < 0.02, "station 1 did not hold the cart, x=" + s1.getX() + " speed " + speed(8));
            check(CartLogic.isHeld(s1), "station 1 hold not registered");
            AbstractMinecart s2 = carts[9];
            check(s2.blockPosition().getX() == at.getX() + 20 && speed(9) < 0.02 && CartLogic.isHeld(s2), "station 2 did not hold the cart before its timer, x=" + s2.getX());
            level.setBlock(at.offset(20, 0, LANE_Z[7] + 1), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
            check(level.getBlockState(at.offset(20, 0, LANE_Z[7])).getValue(PoweredRailBlock.POWERED), "station not powered by redstone");
        }
        if (tick == 70) {
            check(carts[8].getX() > at.getX() + 22, "station 1 did not release on the redstone signal, x=" + (carts[8].getX() - at.getX()));
        }
        if (tick == 70) {
            // coupled pair: the follower must have followed and kept behind the leader
            double gap = carts[6].getX() - carts[7].getX();
            check(carts[7].getX() - startX[7] > 5, "follower barely moved: " + (carts[7].getX() - startX[7]));
            check(gap > 1.0 && gap < 2.2, "gap out of range: " + gap);
            check(Coupling.hasLeader(carts[7]), "link broke during the run");
            // cutters: follower cut loose, chain dropped
            int before = level.getEntitiesOfClass(ItemEntity.class, new AABB(at).inflate(80)).size();
            check(Coupling.cut(level, carts[6]), "cutters found no link on the leader");
            check(!Coupling.hasLeader(carts[7]) && !Coupling.hasFollower(carts[6]), "link still there after cutting");
            check(level.getEntitiesOfClass(ItemEntity.class, new AABB(at).inflate(80)).size() == before + 1, "cutting dropped no chain");
            check(!Coupling.cut(level, carts[6]), "cutting twice reported a cut");
        }
        if (tick == 100) {
            check(carts[9].getX() > at.getX() + 21.1, "station 2 did not release on its timer, x=" + (carts[9].getX() - at.getX()));
            check(!CartLogic.isHeld(carts[9]), "station 2 still holds");
        }
    }

    private static void finish() {
        for (AbstractMinecart c : carts) if (c != null) c.discard();
        for (Entity e : level.getEntitiesOfClass(ItemEntity.class, new AABB(at).inflate(80))) e.discard();
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-2, -1, -2), at.offset(LANE_LEN + 4, 4, 34))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
        for (ChunkPos c : forced) level.setChunkForced(c.x(), c.z(), false);
        forced.clear();
        java.util.Arrays.fill(carts, null);
        java.util.Arrays.fill(maxStep, 0);
        String line = fails.isEmpty() ? "Carts self test PASS" : "Carts self test FAIL: " + String.join("; ", fails);
        LOG.info(line);
        source.sendSuccess(() -> Component.literal(line), false);
        tick = -1;
    }
}
