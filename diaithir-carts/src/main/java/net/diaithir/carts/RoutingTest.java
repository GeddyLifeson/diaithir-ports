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
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/** /diaithircarts routingtest: carts against and with a one-way rail, a destination rail with named and unnamed carts, and a cart loader loading and unloading a chest cart. */
final class RoutingTest {
    private RoutingTest() {}

    private static final Logger LOG = LogUtils.getLogger();
    private static CommandSourceStack source;
    private static ServerLevel level;
    private static BlockPos at;
    private static AbstractMinecart a, b, named, plain, latchCart, ilCart, blocker;
    private static MinecartChest chestCart;
    private static double minB;
    private static int t = -1;
    private static final List<String> fails = new ArrayList<>();

    private static void rail(BlockPos p, net.minecraft.world.level.block.Block block, RailShape shape) {
        level.setBlock(p, block.defaultBlockState().setValue(RailBlock.SHAPE, shape), 3);
    }

    private static AbstractMinecart spawn(BlockPos p, double vx) {
        AbstractMinecart c = EntityType.MINECART.create(level, EntitySpawnReason.COMMAND);
        c.setPos(p.getX() + 0.5, p.getY() + 0.1, p.getZ() + 0.5);
        c.setDeltaMovement(vx, 0, 0);
        level.addFreshEntity(c);
        return c;
    }

    static String start(CommandSourceStack s) {
        if (t >= 0) return "already running";
        source = s;
        level = s.getLevel();
        at = new BlockPos(0, 232, 40);
        for (int cx = 0; cx <= 2; cx++) for (int cz = 1; cz <= 4; cz++) { level.getChunk(cx, cz); level.setChunkForced(cx, cz, true); }
        fails.clear();
        for (int x = -2; x <= 40; x++) for (int z = -3; z <= 24; z++) {
            for (int y = 0; y <= 4; y++) level.setBlock(at.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(at.offset(x, -1, z), Blocks.STONE.defaultBlockState(), 3);
        }
        // one-way run: z=0, x 0..38, one-way rails at 10 and 20 letting carts east
        for (int z = 0; z <= 4; z += 4) for (int x = 0; x <= 38; x++) {
            boolean oneWay = x == 10 || x == 20;
            rail(at.offset(x, 0, z), oneWay ? DiaithirCarts.ONE_WAY_RAIL.get() : Blocks.RAIL, RailShape.EAST_WEST);
        }
        a = spawn(at.east(2), 0.4);
        b = spawn(at.offset(30, 0, 4), -0.4);
        minB = 99;
        // destination rail: z=8, channel 3 (C)
        BlockPos dest = at.offset(5, 0, 8);
        for (int x = 3; x <= 8; x++) rail(at.offset(x, 0, 8), x == 5 ? DiaithirCarts.DESTINATION_RAIL.get() : Blocks.RAIL, RailShape.EAST_WEST);
        level.setBlock(dest, level.getBlockState(dest).setValue(DestinationRail.CHANNEL, 3), 3);
        // loader: z=16, rail at x=5, loader above it, chest above that
        BlockPos railAt = at.offset(5, 0, 16);
        for (int x = 3; x <= 8; x++) rail(at.offset(x, 0, 16), Blocks.RAIL, RailShape.EAST_WEST);
        level.setBlock(railAt.above(), DiaithirCarts.CART_LOADER.get().defaultBlockState(), 3);
        level.setBlock(railAt.above(2), Blocks.CHEST.defaultBlockState(), 3);
        if (level.getBlockEntity(railAt.above(2)) instanceof ChestBlockEntity c) { c.setItem(0, new ItemStack(Items.COAL, 20)); c.setItem(3, new ItemStack(Items.IRON_INGOT, 5)); }
        chestCart = EntityType.CHEST_MINECART.create(level, EntitySpawnReason.COMMAND);
        chestCart.setPos(railAt.getX() + 0.5, railAt.getY() + 0.1, railAt.getZ() + 0.5);
        level.addFreshEntity(chestCart);
        // locking rails: z=12 latch, z=16 interlock with a blocker parked ahead
        for (int z : new int[]{12, 20}) for (int x = 0; x <= 30; x++) {
            if (x == 10) level.setBlock(at.offset(x, 0, z), DiaithirCarts.LOCKING_RAIL.get().defaultBlockState().setValue(net.minecraft.world.level.block.PoweredRailBlock.SHAPE, RailShape.EAST_WEST)
                    .setValue(LockingRail.MODE, z == 12 ? 0 : 1), 3);
            else rail(at.offset(x, 0, z), Blocks.RAIL, RailShape.EAST_WEST);
        }
        latchCart = spawn(at.offset(5, 0, 12), 0.3);
        ilCart = spawn(at.offset(5, 0, 20), 0.3);
        blocker = spawn(at.offset(22, 0, 20), 0);
        // names
        named = spawn(dest, 0);
        named.setCustomName(Component.literal("C"));
        plain = spawn(at.offset(8, 0, 8), 0);
        t = 0;
        return null;
    }

    private static int count(net.minecraft.world.Container c) {
        int n = 0;
        for (int i = 0; i < c.getContainerSize(); i++) n += c.getItem(i).getCount();
        return n;
    }

    static void tick(MinecraftServer server) {
        if (t < 0) return;
        t++;
        if (b != null && b.isAlive()) minB = Math.min(minB, b.getX());
        BlockPos dest = at.offset(5, 0, 8), railAt = at.offset(5, 0, 16);
        // carts slow quickly on plain rail, so keep pushing them until they have had their chance at the one-way rails
        if (t <= 130 && a.isAlive() && a.getX() < 23) a.setDeltaMovement(0.3, 0, 0);
        if (t <= 130 && b.isAlive() && b.getX() > 21.3) b.setDeltaMovement(-0.3, 0, 0);
        if (t == 140) {
            if (a.getX() < 21) fails.add("a cart going with the one-way rails stopped at x=" + a.getX());
            if (minB < 19.4) fails.add("a cart got past a one-way rail against it (min x " + minB + ")");
            if (b.getX() < 20.4) fails.add("the turned-back cart did not come back, x=" + b.getX());
        }
        if (t <= 60) { if (latchCart.getX() < 9.5) latchCart.setDeltaMovement(0.3, 0, 0); if (ilCart.getX() < 9.5) ilCart.setDeltaMovement(0.3, 0, 0); }
        if (t == 70) {
            if (latchCart.getX() < 10 || latchCart.getX() >= 11 || latchCart.getDeltaMovement().horizontalDistance() > 0.05) fails.add("latch did not hold the cart: x=" + latchCart.getX());
            if (ilCart.getX() < 10 || ilCart.getX() >= 11) fails.add("interlock did not hold the cart with a cart ahead: x=" + ilCart.getX());
            level.setBlock(at.offset(10, 0, 11), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);   // powers the latch rail: a pulse
            blocker.discard();
        }
        if (t == 71 && latchCart.getX() < 11) { /* give the pulse a tick to be seen */ }
        if (t == 125) {
            if (latchCart.getX() < 11.5) fails.add("latch did not release on a pulse: x=" + latchCart.getX());
            if (ilCart.getX() < 11.5) fails.add("interlock did not release when the track ahead cleared: x=" + ilCart.getX());
        }
        if (t == 60) {
            var st = level.getBlockState(dest);
            if (!st.getValue(DestinationRail.POWERED)) fails.add("destination rail not powered by the cart named C");
            if (level.getBestNeighborSignal(dest.below()) < 0) fails.add("no signal read");
            // the named cart leaves: the rail goes off. The unnamed one alone does not power channel 3.
            named.discard();
            plain.setPos(dest.getX() + 0.5, dest.getY() + 0.1, dest.getZ() + 0.5);
            plain.setDeltaMovement(0, 0, 0);
        }
        if (t == 90) {
            if (level.getBlockState(dest).getValue(DestinationRail.POWERED)) fails.add("destination rail still powered with only an unnamed cart on channel 3");
            level.setBlock(dest, level.getBlockState(dest).setValue(DestinationRail.CHANNEL, 0), 3);
        }
        if (t == 110 && !level.getBlockState(dest).getValue(DestinationRail.POWERED)) fails.add("channel 0 should power for any cart");
        if (t == 50) {
            // the loader has been loading since the cart arrived: container to cart
            ChestBlockEntity chest = (ChestBlockEntity) level.getBlockEntity(railAt.above(2));
            int inCart = count(chestCart), inChest = count(chest);
            if (inCart != 25 || inChest != 0) fails.add("loading: " + inCart + " in the cart, " + inChest + " in the chest (want 25 and 0)");
            level.setBlock(railAt.above(), DiaithirCarts.CART_LOADER.get().defaultBlockState().setValue(CartLoader.UNLOAD, true), 3);
        }
        if (t == 100) {
            ChestBlockEntity chest = (ChestBlockEntity) level.getBlockEntity(railAt.above(2));
            int inCart = count(chestCart), inChest = count(chest);
            if (inCart != 0 || inChest != 25) fails.add("unloading: " + inCart + " in the cart, " + inChest + " in the chest (want 0 and 25)");
            level.setBlock(railAt.above(2).east(), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
            level.setBlock(railAt.above().east(), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
            level.setBlock(railAt.above(), DiaithirCarts.CART_LOADER.get().defaultBlockState(), 3);   // loading again, but powered off
            level.setBlock(railAt.above().east(), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
        }
        if (t == 170) {
            if (count(chestCart) != 0) fails.add("a powered loader still loaded: " + count(chestCart));
            String line = fails.isEmpty() ? "Routing self test PASS" : "Routing self test FAIL: " + String.join("; ", fails);
            LOG.info(line);
            source.sendSuccess(() -> Component.literal(line), false);
            for (var e : List.of(a, b, plain, chestCart, latchCart, ilCart)) if (e != null) e.discard();
            for (int cx = 0; cx <= 2; cx++) for (int cz = 1; cz <= 4; cz++) level.setChunkForced(cx, cz, false);
            t = -1;
        }
    }
}
