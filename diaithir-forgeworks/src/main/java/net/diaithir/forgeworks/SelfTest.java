package net.diaithir.forgeworks;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;

/** Places machines high above spawn, powers them through the energy capability, feeds them through the item capability and checks results. */
final class SelfTest {
    private SelfTest() {}

    private static Block block(Kind k, int tier) {
        return Machines.ALL_BLOCKS.get(k.ordinal() * 3 + tier).get();
    }

    private static MachineBlockEntity place(ServerLevel level, BlockPos pos, Kind k, int tier, List<String> fails) {
        level.setBlock(pos, block(k, tier).defaultBlockState(), 3);
        if (level.getBlockEntity(pos) instanceof MachineBlockEntity be) {
            int in = fe(level, pos, 1_000_000);
            if (in <= 0) fails.add(k.id + " age" + (4 + tier) + ": energy capability accepted no FE");
            return be;
        }
        fails.add(k.id + ": no block entity");
        return null;
    }

    private static int fe(ServerLevel level, BlockPos pos, int amount) {
        var cap = level.getCapability(Capabilities.Energy.BLOCK, pos, Direction.UP);
        if (cap == null) return -1;
        try (Transaction tx = Transaction.openRoot()) {
            int n = cap.insert(amount, tx);
            tx.commit();
            return n;
        }
    }

    private static int insert(ServerLevel level, BlockPos pos, Item item, int count) {
        ResourceHandler<ItemResource> h = level.getCapability(Capabilities.Item.BLOCK, pos, Direction.UP);
        if (h == null) return -1;
        try (Transaction tx = Transaction.openRoot()) {
            int n = h.insert(ItemResource.of(item), count, tx);
            tx.commit();
            return n;
        }
    }

    private static int take(ServerLevel level, BlockPos pos, Item item, int count) {
        ResourceHandler<ItemResource> h = level.getCapability(Capabilities.Item.BLOCK, pos, Direction.DOWN);
        if (h == null) return -1;
        try (Transaction tx = Transaction.openRoot()) {
            int n = h.extract(ItemResource.of(item), count, tx);
            tx.commit();
            return n;
        }
    }

    private static int ticksUntil(ServerLevel level, MachineBlockEntity be, int max, BooleanSupplier done) {
        for (int i = 1; i <= max; i++) {
            fe(level, be.getBlockPos(), 100000); // a cable would top it up every tick, limited by the machine's intake
            be.serverTick(level);
            if (done.getAsBoolean()) return i;
        }
        return -1;
    }

    private static boolean has(MachineBlockEntity be, int slot, Item item, int count) {
        ItemStack s = be.getItem(slot);
        return s.is(item) && s.getCount() >= count;
    }

    static List<String> run(MinecraftServer server) {
        List<String> fails = new ArrayList<>();
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        if (level == null) return List.of("no overworld");
        BlockPos base = new BlockPos(level.getRespawnData().pos().getX(), 240, level.getRespawnData().pos().getZ());
        for (BlockPos p : BlockPos.betweenClosed(base.offset(-2, -2, -2), base.offset(40, 2, 2))) level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
        List<BlockPos> placed = new ArrayList<>();
        FakePlayer player = FakePlayerFactory.get(level, new GameProfile(UUID.nameUUIDFromBytes("fw-test".getBytes()), "fw_test"));
        try {
            // crusher: ore to dust, tier speed, side config, redstone, augment
            BlockPos p4 = base, p6 = base.offset(4, 0, 0), pa = base.offset(8, 0, 0), pr = base.offset(12, 0, 0);
            placed.addAll(List.of(p4, p6, pa, pr));
            MachineBlockEntity c4 = place(level, p4, Kind.CRUSHER, 0, fails);
            MachineBlockEntity c6 = place(level, p6, Kind.CRUSHER, 2, fails);
            MachineBlockEntity ca = place(level, pa, Kind.CRUSHER, 0, fails);
            MachineBlockEntity cr = place(level, pr, Kind.CRUSHER, 0, fails);
            if (c4 == null || c6 == null || ca == null || cr == null) return fails;

            if (!c4.acceptsInput(new ItemStack(Items.IRON_ORE))) fails.add("crusher does not accept iron ore");
            if (c4.acceptsInput(new ItemStack(Items.STICK))) fails.add("crusher accepts a stick");
            if (insert(level, p4, Items.STICK, 1) != 0) fails.add("stick went into the crusher");
            if (insert(level, p4, Items.IRON_ORE, 1) != 1) fails.add("could not insert iron ore through the capability");
            if (insert(level, p6, Items.IRON_ORE, 1) != 1) fails.add("could not insert iron ore (age6)");
            int t4 = ticksUntil(level, c4, 3000, () -> has(c4, Kind.OUT, Machines.MATERIALS.get(0).get(), 1));
            int t6 = ticksUntil(level, c6, 3000, () -> has(c6, Kind.OUT, Machines.MATERIALS.get(0).get(), 1));
            if (t4 < 0) fails.add("age4 crusher made no iron dust: " + c4.debug());
            if (t6 < 0) fails.add("age6 crusher made no iron dust");
            if (t4 > 0 && t6 > 0 && !(t6 * 3 < t4)) fails.add("age6 not about 4x faster: " + t4 + " vs " + t6);
            // an unpowered machine must not progress
            BlockPos pz = base.offset(28, 0, 0);
            placed.add(pz);
            level.setBlock(pz, block(Kind.CRUSHER, 0).defaultBlockState(), 3);
            if (level.getBlockEntity(pz) instanceof MachineBlockEntity cz) {
                insert(level, pz, Items.IRON_ORE, 1);
                for (int i = 0; i < 60; i++) cz.serverTick(level);
                if (cz.progress() != 0) fails.add("unpowered crusher progressed");
            }
            if (t4 > 0 && take(level, p4, Machines.MATERIALS.get(0).get(), 64) != 2) fails.add("expected 2 iron dust from the output side");

            // side config
            c4.setItemMode(Direction.UP, MachineBlockEntity.NONE);
            if (level.getCapability(Capabilities.Item.BLOCK, p4, Direction.UP) != null) fails.add("closed face still exposes items");
            c4.setEnergyOn(Direction.WEST, false);
            if (level.getCapability(Capabilities.Energy.BLOCK, p4, Direction.WEST) != null) fails.add("closed face still exposes energy");
            if (level.getCapability(Capabilities.Energy.BLOCK, p4, Direction.EAST) == null) fails.add("open face lost energy");

            // redstone: HIGH with no signal means it must not run
            cr.cycleRedstone();
            insert(level, pr, Items.IRON_ORE, 1);
            ticksUntil(level, cr, 60, () -> false);
            if (cr.progress() != 0) fails.add("redstone HIGH without a signal still progressed");
            cr.cycleRedstone();
            cr.cycleRedstone(); // LOW: runs without a signal
            if (ticksUntil(level, cr, 3000, () -> has(cr, Kind.OUT, Machines.MATERIALS.get(0).get(), 1)) < 0) fails.add("redstone LOW did not run unpowered");

            // speed augment
            ca.setItem(Kind.UPGRADE, new ItemStack(Machines.AUGMENTS.get(Augment.SPEED.ordinal()).get(), 2));
            insert(level, pa, Items.IRON_ORE, 1);
            int ta = ticksUntil(level, ca, 3000, () -> has(ca, Kind.OUT, Machines.MATERIALS.get(0).get(), 1));
            if (ta < 0 || t4 < 0 || !(ta * 3 < t4 * 2)) fails.add("speed augment did not speed up: " + ta + " vs " + t4);

            // menu
            AbstractContainerMenu menu = c4.createMenu(7, player.getInventory(), player);
            if (menu == null || menu.slots.size() != 5 + 36 - 1) fails.add("menu slot count " + (menu == null ? -1 : menu.slots.size()));
            else if (!menu.clickMenuButton(player, 0)) fails.add("menu button refused");

            // alloy furnace: two inputs in either order
            BlockPos pf = base.offset(16, 0, 0);
            placed.add(pf);
            MachineBlockEntity af = place(level, pf, Kind.ALLOY_FURNACE, 2, fails);
            if (af != null) {
                insert(level, pf, Items.COPPER_INGOT, 3);
                insert(level, pf, Items.IRON_INGOT, 1);
                if (ticksUntil(level, af, 3000, () -> has(af, Kind.OUT, Machines.MATERIALS.get(4).get(), 4)) < 0) fails.add("alloy furnace made no bronze");
            }

            // sawmill
            BlockPos ps = base.offset(20, 0, 0);
            placed.add(ps);
            MachineBlockEntity sm = place(level, ps, Kind.SAWMILL, 1, fails);
            if (sm != null) {
                insert(level, ps, Items.OAK_LOG, 1);
                if (ticksUntil(level, sm, 3000, () -> has(sm, Kind.OUT, Items.OAK_PLANKS, 6)) < 0) fails.add("sawmill made no planks");
            }

            // crucible
            BlockPos pc = base.offset(24, 0, 0);
            placed.add(pc);
            MachineBlockEntity cc = place(level, pc, Kind.CRUCIBLE, 1, fails);
            if (cc != null) {
                insert(level, pc, Items.COBBLESTONE, 4);
                if (ticksUntil(level, cc, 3000, () -> cc.tank.getAmountAsInt(0) >= 250) < 0) fails.add("crucible made no lava");
                ResourceHandler<FluidResource> fh = level.getCapability(Capabilities.Fluid.BLOCK, pc, Direction.NORTH);
                if (fh == null) fails.add("no fluid capability");
                else try (Transaction tx = Transaction.openRoot()) {
                    if (fh.insert(FluidResource.of(net.minecraft.world.level.material.Fluids.WATER), 1000, tx) != 0) fails.add("crucible took water");
                    int got = fh.extract(FluidResource.of(net.minecraft.world.level.material.Fluids.LAVA), 250, tx);
                    if (got != 250) fails.add("extracted " + got + " mB lava, wanted 250");
                    tx.commit();
                }
            }
        } finally {
            for (BlockPos p : placed) {
                if (level.getBlockEntity(p) instanceof MachineBlockEntity be) be.clearContent();
                level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16);
            }
        }
        return fails;
    }
}
