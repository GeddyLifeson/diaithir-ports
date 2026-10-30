package net.diaithir.conduits;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Two endpoints on one channel at two positions (and a third in the Nether): items between chests, fluid between fluid cells, FE between energy cells,
 * the bandwidth limit of each tier, face modes, pipe input, and the owner rules (a stranger is refused until trusted, and the per-player channel cap).
 */
final class ConduitSelfTest {
    private ConduitSelfTest() {}

    private static int total(ChestBlockEntity c, ItemResource r) {
        int n = 0;
        for (int i = 0; i < c.getContainerSize(); i++) if (r.matches(c.getItem(i))) n += c.getItem(i).getCount();
        return n;
    }

    private static ChestBlockEntity chest(ServerLevel l, BlockPos p) {
        l.setBlock(p, Blocks.CHEST.defaultBlockState(), 3);
        return (ChestBlockEntity) l.getBlockEntity(p);
    }

    private static CellBlockEntity cell(ServerLevel l, BlockPos p, Block b) {
        l.setBlock(p, b.defaultBlockState(), 3);
        return (CellBlockEntity) l.getBlockEntity(p);
    }

    private static ConduitBlockEntity node(ServerLevel l, BlockPos p, Block b, FakePlayer owner) {
        l.setBlock(p, b.defaultBlockState(), 3);
        ConduitBlockEntity be = (ConduitBlockEntity) l.getBlockEntity(p);
        be.claim(owner);
        return be;
    }

    private static void face(ConduitBlockEntity n, int mode) {
        n.setMode(Direction.UP, mode);
        n.setMode(Direction.DOWN, mode);
        n.setMode(Direction.NORTH, mode);
    }

    private static void fillFluid(CellBlockEntity c, int mb) {
        try (Transaction tx = Transaction.openRoot()) {
            c.tank.insert(FluidResource.of(Fluids.WATER), mb, tx);
            tx.commit();
        }
    }

    private static void fillEnergy(CellBlockEntity c, int fe) {
        try (Transaction tx = Transaction.openRoot()) {
            c.battery.insert(fe, tx);
            tx.commit();
        }
    }

    static List<String> run(MinecraftServer server) {
        List<String> fails = new ArrayList<>();
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        ServerLevel nether = server.getLevel(Level.NETHER);
        if (level == null) return List.of("no overworld");
        Channels reg = Channels.get(server);
        int baseline = reg.nodeCount();
        BlockPos at = new BlockPos(level.getRespawnData().pos().getX(), 240, level.getRespawnData().pos().getZ());
        BlockPos at2 = at.offset(40, 0, 0); // "another position", well apart from the first
        BlockPos at3 = new BlockPos(at.getX() / 8, 100, at.getZ() / 8); // the Nether
        List<BlockPos> clearOver = List.of(at, at2);
        for (BlockPos c : clearOver) for (BlockPos p : BlockPos.betweenClosed(c.offset(-3, -3, -3), c.offset(3, 3, 3))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
        if (nether != null) {
            nether.getChunk(at3);
            for (BlockPos p : BlockPos.betweenClosed(at3.offset(-3, -3, -3), at3.offset(3, 3, 3))) nether.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
        }
        FakePlayer owner = FakePlayerFactory.get(level, new GameProfile(UUID.nameUUIDFromBytes("cn-owner".getBytes()), "cn_owner"));
        FakePlayer stranger = FakePlayerFactory.get(level, new GameProfile(UUID.nameUUIDFromBytes("cn-stranger".getBytes()), "cn_stranger"));
        String oid = owner.getStringUUID(), sid = stranger.getStringUUID();
        reg.rememberName(oid, "cn_owner");
        reg.rememberName(sid, "cn_stranger");
        reg.untrust(oid, sid);
        String dimTag = "conduits:selftest";
        try {
            // layout: node + chest above + fluid cell below + energy cell north, at each of two positions
            ConduitBlockEntity a = node(level, at, DiaithirConduits.CONDUIT_1.get(), owner);
            ConduitBlockEntity b = node(level, at2, DiaithirConduits.CONDUIT_1.get(), owner);
            ChestBlockEntity srcChest = chest(level, at.above()), dstChest = chest(level, at2.above());
            CellBlockEntity srcTank = cell(level, at.below(), DiaithirConduits.FLUID_CELL.get()), dstTank = cell(level, at2.below(), DiaithirConduits.FLUID_CELL.get());
            CellBlockEntity srcBat = cell(level, at.north(), DiaithirConduits.ENERGY_CELL.get()), dstBat = cell(level, at2.north(), DiaithirConduits.ENERGY_CELL.get());
            if (srcChest == null || dstChest == null || srcTank == null || dstTank == null || srcBat == null || dstBat == null) return List.of("setup failed");

            String err = a.join(level, oid, 1, owner);
            if (err != null) fails.add("owner could not join own channel: " + err);
            err = b.join(level, oid, 1, owner);
            if (err != null) fails.add("owner could not join second node: " + err);
            if (reg.on(oid, 1).size() != 2) fails.add("registry holds " + reg.on(oid, 1).size() + " nodes on channel 1");
            face(a, ConduitBlockEntity.SEND);
            face(b, ConduitBlockEntity.RECEIVE);

            srcChest.setItem(0, new ItemStack(Items.COBBLESTONE, 64));
            fillFluid(srcTank, 5000);
            fillEnergy(srcBat, 20000);
            ItemResource cobble = ItemResource.of(Items.COBBLESTONE);

            // one run at tier 1: bandwidth limits apply to each kind
            a.work(level);
            if (total(dstChest, cobble) != 16) fails.add("tier 1 run moved " + total(dstChest, cobble) + " items, wanted 16");
            if (dstTank.tank.getAmountAsInt(0) != 1000) fails.add("tier 1 run moved " + dstTank.tank.getAmountAsInt(0) + " mB, wanted 1000");
            if (dstBat.battery.getAmountAsInt() != 4000) fails.add("tier 1 run moved " + dstBat.battery.getAmountAsInt() + " FE, wanted 4000");
            if (total(srcChest, cobble) != 48 || srcTank.tank.getAmountAsInt(0) != 4000 || srcBat.battery.getAmountAsInt() != 16000) fails.add("source did not lose exactly what the target gained: chest " + total(srcChest, cobble) + ", tank " + srcTank.tank.getAmountAsInt(0) + ", FE " + srcBat.battery.getAmountAsInt());
            for (int i = 0; i < 4; i++) a.work(level);
            if (total(dstChest, cobble) != 64 || total(srcChest, cobble) != 0) fails.add("items after 5 runs: " + total(dstChest, cobble));
            if (dstTank.tank.getAmountAsInt(0) != 5000) fails.add("fluid after 5 runs: " + dstTank.tank.getAmountAsInt(0));
            if (dstBat.battery.getAmountAsInt() != 20000) fails.add("FE after 5 runs: " + dstBat.battery.getAmountAsInt());

            a.setMode(Direction.DOWN, ConduitBlockEntity.OFF);
            a.setMode(Direction.NORTH, ConduitBlockEntity.OFF);
            // face modes: an off face moves nothing, and nothing flows backwards from a receive face
            srcChest.setItem(0, new ItemStack(Items.COBBLESTONE, 10));
            a.setMode(Direction.UP, ConduitBlockEntity.OFF);
            a.work(level);
            if (total(srcChest, cobble) != 10) fails.add("an off face still sent items");
            a.setMode(Direction.UP, ConduitBlockEntity.SEND);
            b.work(level);
            if (total(srcChest, cobble) != 10) fails.add("a receive-only node sent items back");
            a.setKind(ConduitBlockEntity.ITEMS, false);
            a.work(level);
            if (total(srcChest, cobble) != 10) fails.add("switching items off still sent items");
            a.setKind(ConduitBlockEntity.ITEMS, true);

            // pipes feeding a send face share the same bandwidth window
            a.setMode(Direction.NORTH, ConduitBlockEntity.SEND);
            EnergyHandler pipe = level.getCapability(Capabilities.Energy.BLOCK, at, Direction.NORTH);
            if (pipe == null) {
                fails.add("send face exposes no energy input");
            } else {
                a.newWindow();
                int in1, in2;
                try (Transaction tx = Transaction.openRoot()) {
                    in1 = pipe.insert(10000, tx);
                    tx.commit();
                }
                try (Transaction tx = Transaction.openRoot()) {
                    in2 = pipe.insert(10000, tx);
                    tx.commit();
                }
                if (in1 != 4000 || in2 != 0) fails.add("pipe input took " + in1 + " then " + in2 + " FE, wanted 4000 then 0");
                if (dstBat.battery.getAmountAsInt() != 24000) fails.add("FE after pipe input: " + dstBat.battery.getAmountAsInt());
                if (level.getCapability(Capabilities.Energy.BLOCK, at, Direction.EAST) != null) fails.add("an off face exposes energy input");
            }

            // tier 2 has more bandwidth: 100 items in one run
            level.setBlock(at, DiaithirConduits.CONDUIT_2.get().defaultBlockState(), 3);
            ConduitBlockEntity a2 = (ConduitBlockEntity) level.getBlockEntity(at);
            a2.claim(owner);
            String e2 = a2.join(level, oid, 1, owner);
            if (e2 != null) fails.add("tier 2 join: " + e2);
            a2.setMode(Direction.UP, ConduitBlockEntity.SEND);
            ChestBlockEntity again = (ChestBlockEntity) level.getBlockEntity(at.above());
            again.clearContent();
            dstChest.clearContent();
            for (int i = 0; i < 3; i++) again.setItem(i, new ItemStack(Items.IRON_INGOT, 64));
            ItemResource iron = ItemResource.of(Items.IRON_INGOT);
            a2.work(level);
            if (total(dstChest, iron) != 128) fails.add("tier 2 run moved " + total(dstChest, iron) + " items, wanted 128");

            // across dimensions
            if (nether != null) {
                ConduitBlockEntity n = node(nether, at3, DiaithirConduits.CONDUIT_1.get(), owner);
                ChestBlockEntity netherChest = chest(nether, at3.above());
                String e3 = n.join(nether, oid, 1, owner);
                if (e3 != null) fails.add("nether join: " + e3);
                n.setMode(Direction.UP, ConduitBlockEntity.RECEIVE);
                b.setMode(Direction.UP, ConduitBlockEntity.OFF); // only the Nether chest receives now
                a2.work(level);
                if (total(netherChest, iron) != 64 || total(again, iron) != 0) {
                    // 192 iron went in, 128 went to the overworld chest earlier; 64 is left to arrive
                    fails.add("nether chest got " + total(netherChest, iron) + " iron across dimensions, wanted 64");
                }
            } else {
                fails.add("no nether to test across dimensions");
            }

            // owner rules
            ConduitBlockEntity theirs = node(level, at.offset(2, 2, 0), DiaithirConduits.CONDUIT_1.get(), stranger);
            String refused = theirs.join(level, oid, 1, stranger);
            if (refused == null) fails.add("a stranger joined the owner's channel");
            if (theirs.channel() != -1) fails.add("refused node still holds a channel");
            if (a2.canManage(stranger)) fails.add("a stranger may configure the owner's node");
            if (a2.join(level, sid, 5, stranger) == null) fails.add("a stranger re-channelled the owner's node");
            if (a2.channel() != 1) fails.add("the owner's node was moved by a stranger");
            reg.trust(oid, sid);
            if (theirs.join(level, oid, 1, stranger) != null) fails.add("a trusted player could not join");
            if (reg.on(oid, 1).size() != (nether != null ? 4 : 3)) fails.add("registry count " + reg.on(oid, 1).size());
            reg.untrust(oid, sid);
            if (theirs.join(level, oid, 2, stranger) == null) fails.add("trust did not end");
            theirs.leave(level, stranger);

            // channel cap and node cap
            int cap = Config.MAX_CHANNELS_PER_PLAYER.get();
            List<BlockPos> dummy = new ArrayList<>();
            int ownedBefore = reg.channelsOwnedBy(oid);
            String last = null;
            for (int i = 0; i < cap + 1; i++) {
                BlockPos dp = new BlockPos(i, 0, 0);
                dummy.add(dp);
                last = reg.join(oid, 100 + i, dimTag, dp, oid, false);
                if (last != null) break;
            }
            if (last == null) fails.add("channel cap of " + cap + " not enforced (owned " + ownedBefore + " first)");
            if (reg.channelsOwnedBy(oid) > cap) fails.add("owner holds " + reg.channelsOwnedBy(oid) + " channels, over the cap");
            if (reg.join(oid, 0, dimTag, new BlockPos(0, 1, 0), oid, false) == null) fails.add("channel 0 accepted");
        } finally {
            for (BlockPos c : clearOver) for (BlockPos p : BlockPos.betweenClosed(c.offset(-3, -3, -3), c.offset(3, 3, 3))) {
                if (level.getBlockEntity(p) instanceof ChestBlockEntity ch) ch.clearContent();
                level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
            }
            for (BlockPos p : BlockPos.betweenClosed(at.offset(2, 2, 0), at.offset(2, 2, 0))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
            if (nether != null) for (BlockPos p : BlockPos.betweenClosed(at3.offset(-3, -3, -3), at3.offset(3, 3, 3))) {
                if (nether.getBlockEntity(p) instanceof ChestBlockEntity ch) ch.clearContent();
                nether.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
            }
            for (int i = 0; i < 20; i++) reg.leave(dimTag, new BlockPos(i, 0, 0));
            for (Channels.Entry e : new ArrayList<>(reg.on(oid, 1))) reg.leave(e.dim(), e.pos());
            for (Channels.Entry e : new ArrayList<>(reg.on(sid, 1))) reg.leave(e.dim(), e.pos());
            reg.untrust(oid, sid);
        }
        if (reg.nodeCount() != baseline) fails.add("registry leaked " + (reg.nodeCount() - baseline) + " entries");
        return fails;
    }
}
