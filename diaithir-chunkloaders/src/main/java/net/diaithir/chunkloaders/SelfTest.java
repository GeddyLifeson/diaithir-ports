package net.diaithir.chunkloaders;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.world.chunk.ForcedChunkManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Places a loader of each tier far from anyone, checks its chunks are force-loaded while it burns and released when it stops or is broken, then the same for a sealed cart. */
final class SelfTest {
    private SelfTest() {}

    static List<String> run(MinecraftServer server) {
        List<String> fails = new ArrayList<>();
        ServerLevel level = server.overworld();
        String dim = level.dimension().identifier().toString();
        LoaderData data = LoaderData.get(server);
        String who = UUID.randomUUID().toString();
        for (int t = 1; t <= 3; t++) {
            int r = Config.RADIUS[t].get(), cx = 400 * t, cz = 400 * t;
            BlockPos pos = new BlockPos(cx * 16 + 8, 80, cz * 16 + 8);
            level.getChunk(cx, cz);
            level.setBlock(pos, Registry.LOADERS.get(t - 1).get().defaultBlockState(), 3);
            if (!(level.getBlockEntity(pos) instanceof LoaderBlockEntity be)) { fails.add("tier " + t + " has no block entity"); continue; }
            be.claim(who, "selftest");
            if (data.count(who) != 1) fails.add("tier " + t + " not counted for its owner");
            be.addFuel(100);
            if (be.fuel != 100L * Config.FUEL_MULTIPLIER.get()) fails.add("tier " + t + " fuel not added");
            be.serverTick(level, level.getBlockState(pos));
            int side = 2 * r + 1;
            if (!be.holding() || be.heldChunks() != side * side) fails.add("tier " + t + " not holding " + side * side + " chunks");
            if (!ForcedChunkManager.hasForcedChunks(level)) fails.add("tier " + t + " no forced chunks recorded");
            for (ChunkPos p : Tickets.around(new ChunkPos(cx, cz), r)) {
                if (Tickets.BLOCKS.forceChunk(level, pos, p.x(), p.z(), true, true)) fails.add("tier " + t + " chunk " + p.x() + "," + p.z() + " was not force-loaded");
                if (level.getChunkSource().getChunkNow(p.x(), p.z()) == null) fails.add("tier " + t + " chunk " + p.x() + "," + p.z() + " is not loaded");
            }
            if (!level.getBlockState(pos).getValue(LoaderBlock.LIT)) fails.add("tier " + t + " not lit while burning");
            be.fuel = be.cost();   // one tick of fuel left: burns out, releases
            be.serverTick(level, level.getBlockState(pos));
            if (be.holding() || be.fuel != 0) fails.add("tier " + t + " did not release when out of fuel");
            for (ChunkPos p : Tickets.around(new ChunkPos(cx, cz), r)) {
                if (Tickets.BLOCKS.forceChunk(level, pos, p.x(), p.z(), false, true) || Tickets.BLOCKS.forceChunk(level, pos, p.x(), p.z(), false, false)) fails.add("tier " + t + " chunk " + p.x() + "," + p.z() + " still forced after burnout");
            }
            be.addFuel(100);
            be.serverTick(level, level.getBlockState(pos));
            if (!be.holding()) fails.add("tier " + t + " did not restart with fuel");
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);   // breaking it releases everything and unregisters it
            for (ChunkPos p : Tickets.around(new ChunkPos(cx, cz), r)) {
                if (Tickets.BLOCKS.forceChunk(level, pos, p.x(), p.z(), false, true) || Tickets.BLOCKS.forceChunk(level, pos, p.x(), p.z(), false, false)) fails.add("tier " + t + " chunk " + p.x() + "," + p.z() + " still forced after break");
            }
            if (data.has(dim, pos) || data.count(who) != 0) fails.add("tier " + t + " still registered after break");
        }
        cart(server, level, fails);
        return fails;
    }

    /** The cart's chunk square: held where the cart is, moved with it (old chunks dropped), released at the end. A freshly added entity is not findable until the next tick, so this drives hold and release directly. */
    private static void cart(MinecraftServer server, ServerLevel level, List<String> fails) {
        int r = Config.CART_RADIUS.get();
        UUID id = UUID.randomUUID();
        Carts.Active a = new Carts.Active(level.dimension().identifier().toString(), "");
        ChunkPos first = new ChunkPos(1600, 1600), second = new ChunkPos(1601, 1600);
        Carts.hold(level, id, first, a);
        for (ChunkPos p : Tickets.around(first, r)) {
            if (Tickets.CARTS.forceChunk(level, id, p.x(), p.z(), true, true)) fails.add("cart chunk " + p.x() + "," + p.z() + " was not force-loaded");
            if (level.getChunkSource().getChunkNow(p.x(), p.z()) == null) fails.add("cart chunk " + p.x() + "," + p.z() + " is not loaded");
        }
        Carts.hold(level, id, second, a);   // the cart moved one chunk east: the far west column is let go
        ChunkPos gone = new ChunkPos(first.x() - r, first.z());
        if (Tickets.CARTS.forceChunk(level, id, gone.x(), gone.z(), false, true) || Tickets.CARTS.forceChunk(level, id, gone.x(), gone.z(), false, false)) fails.add("cart kept a chunk it had left");
        if (Tickets.CARTS.forceChunk(level, id, second.x() + r, second.z(), true, true)) fails.add("cart did not take its new chunks");
        Carts.release(level, id, a);
        for (ChunkPos p : Tickets.around(second, r)) {
            if (Tickets.CARTS.forceChunk(level, id, p.x(), p.z(), false, true) || Tickets.CARTS.forceChunk(level, id, p.x(), p.z(), false, false)) fails.add("cart chunk " + p.x() + "," + p.z() + " still forced after release");
        }
    }
}
