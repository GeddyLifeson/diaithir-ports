package net.diaithir.freight;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * /diaithircarts freighttest (owners). Builds containers high above spawn and checks: deploying with cargo, doors, a dock handing the cargo to a storage-style
 * handler (insert, extract, through the real block capability), lifting with a weight limit, lowering with cargo intact, a tank and its fluid rule, and a rift
 * dock pair serving a container from the other end. Returns null when everything passed, else what failed.
 */
public final class FreightTest {
    private static final List<String> fails = new ArrayList<>();

    private FreightTest() {}

    private static void check(boolean ok, String what) { if (!ok) fails.add(what); }

    private static BayData cargo() {
        NonNullList<ItemStack> ingots = NonNullList.withSize(18, ItemStack.EMPTY);
        ingots.set(0, new ItemStack(Items.IRON_INGOT, 64));
        ingots.set(1, new ItemStack(Items.GOLD_INGOT, 5));
        NonNullList<ItemStack> chest = NonNullList.withSize(27, ItemStack.EMPTY);
        chest.set(0, new ItemStack(Items.APPLE, 12));
        return new BayData(List.of(new BayData.Entry("pallet", 0, 0, 0, false, ItemContainerContents.fromItems(ingots)),
                new BayData.Entry("chest", 1, 0, 0, false, ItemContainerContents.fromItems(chest)),
                new BayData.Entry("barrel", 0, 1, 0, false, ItemContainerContents.EMPTY)));
    }

    private static long count(ResourceHandler<ItemResource> h, ItemResource r) {
        long n = 0;
        for (int i = 0; i < h.size(); i++) if (h.getResource(i).equals(r)) n += h.getAmountAsLong(i);
        return n;
    }

    public static String run(MinecraftServer server) {
        fails.clear();
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        if (level == null) return "no overworld";
        BlockPos at = new BlockPos(level.getRespawnData().pos().getX() + 10, 240, level.getRespawnData().pos().getZ() + 10);
        for (int cx = (at.getX() - 8) >> 4; cx <= (at.getX() + 40) >> 4; cx++)
            for (int cz = (at.getZ() - 8) >> 4; cz <= (at.getZ() + 20) >> 4; cz++) level.getChunk(cx, cz);
        BlockPos hi = at.offset(40, 6, 14), lo = at.offset(-6, -1, -6);
        BlockPos.betweenClosedStream(lo, hi).forEach(p -> level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16));

        Direction f = Direction.EAST;
        LoadType t20 = LoadType.byId("container_20");
        BlockPos origin = at;
        check(Containers.deploy(level, t20, origin, f, cargo()) == Containers.Result.OK, "deploy 20 ft");
        check(Containers.deploy(level, t20, origin, f, cargo()) == Containers.Result.BLOCKED, "deploy into an occupied space is refused");
        ContainerCoreEntity core = level.getBlockEntity(origin) instanceof ContainerCoreEntity c ? c : null;
        check(core != null && core.type() == t20 && core.facing() == f, "core entity holds type and facing");
        BlockPos door = Containers.cell(origin, f, 1, 1, t20.l() - 1);
        check(level.getBlockState(door).getBlock() instanceof ContainerDoor, "door cell at the far end");
        check(level.getBlockState(Containers.cell(origin, f, 0, 0, 3)).getBlock() instanceof ContainerWall, "wall cell");
        check(level.getBlockState(Containers.interior(origin, f, t20, 1, 1, 4)).isAir(), "interior is empty space you can walk in");
        check(level.getBlockState(Containers.interior(origin, f, t20, 0, 0, 0)).getBlock() instanceof LoadBlock, "pallet stands where it was placed");
        BayData seen = Containers.contents(level, core);
        check(seen.loads().size() == 3, "scan finds three loads, found " + seen.loads().size());
        CargoBay bay = seen.toBay(t20);
        check(bay.loads().size() == 3 && bay.freeCells() == 20 - 3, "bay model matches the blocks");

        // door
        Containers.setDoor(level, core, true);
        check(level.getBlockState(door).getValue(ContainerDoor.OPEN), "door opens");
        check(level.getBlockState(door).getCollisionShape(level, door).isEmpty(), "open door has no collision");

        // loading dock one block past the door, facing the container
        BlockPos dockPos = Containers.cell(origin, f, 1, 1, t20.l());
        level.setBlock(dockPos, Freight.LOADING_DOCK.get().defaultBlockState().setValue(LoadingDock.FACING, f.getOpposite()), 3);
        ResourceHandler<ItemResource> h = level.getCapability(Capabilities.Item.BLOCK, dockPos, null);
        check(h != null, "dock offers an item handler");
        if (h != null) {
            ItemResource iron = ItemResource.of(Items.IRON_INGOT);
            check(count(h, iron) == 64, "dock sees the pallet's iron");
            int in;
            try (Transaction tx = Transaction.openRoot()) { in = h.insert(iron, 100, tx); tx.commit(); }
            check(in == 100, "dock takes 100 more ingots, took " + in);
            int out;
            try (Transaction tx = Transaction.openRoot()) { out = h.extract(iron, 150, tx); tx.commit(); }
            check(out == 150, "dock gives 150 ingots, gave " + out);
            check(count(h, iron) == 14, "14 ingots left");
            check(count(h, ItemResource.of(Items.APPLE)) == 12, "dock reaches the chest's apples");
        }

        // lift: too heavy, then fine
        check(Containers.lift(level, origin, 1_000, false).result() == Containers.Result.TOO_HEAVY, "1 t crane refuses");
        check(level.getBlockState(origin).getBlock() instanceof ContainerCore, "refused lift leaves the container");
        Containers.Lift lift = Containers.lift(level, door, CargoHook.LIFT_KG, false);
        check(lift.result() == Containers.Result.OK && !lift.stack().isEmpty(), "hook lifts the box");
        check(level.getBlockState(origin).isAir() && level.getBlockState(door).isAir(), "lifted container leaves no blocks");
        BayData carried = lift.stack().getOrDefault(Freight.BAY.get(), BayData.EMPTY);
        check(carried.loads().size() == 3, "item carries three loads");
        CargoBay onItem = Containers.bayOf(lift.stack());
        check(Math.abs(onItem.payloadKg() - (3 * 0 + 25 + 30 + 20 + 0.25 * (14 + 5 + 12))) < 1e-6, "carried mass is tare of loads plus items, got " + onItem.payloadKg());

        // lower elsewhere with cargo intact
        BlockPos second = at.offset(0, 0, 8);
        check(Containers.lower(level, lift.stack(), second, Direction.SOUTH) == Containers.Result.OK, "lower at a new place and heading");
        ContainerCoreEntity core2 = level.getBlockEntity(second) instanceof ContainerCoreEntity c ? c : null;
        BayData again = core2 == null ? BayData.EMPTY : Containers.contents(level, core2);
        check(again.equals(carried), "cargo identical after lowering");
        check(Containers.find(level, Containers.cell(second, Direction.SOUTH, 3, 3, 3)) == core2, "any wall block finds its core");

        // rift pair: container behind dock A (plain dock removed), served from rift dock B on the same channel
        BlockPos riftA = Containers.cell(second, Direction.SOUTH, 1, 1, t20.l());
        BlockPos riftB = at.offset(30, 0, 0);
        level.setBlock(riftA, Freight.RIFT_DOCK.get().defaultBlockState().setValue(LoadingDock.FACING, Direction.SOUTH.getOpposite()), 3);
        level.setBlock(riftB, Freight.RIFT_DOCK.get().defaultBlockState(), 3);
        check(level.getCapability(Capabilities.Item.BLOCK, riftB, null) != null, "rift dock B serves the container at A");
        check(level.getBlockEntity(riftB) instanceof DockBlockEntity b && b.cycleChannel(1) == 2, "channel cycles");
        check(level.getCapability(Capabilities.Item.BLOCK, riftB, null) == null, "other channel has no partner");

        // tank
        LoadType tank = LoadType.byId("tank_20");
        BlockPos tpos = at.offset(12, 0, 0);
        check(Containers.deploy(level, tank, tpos, Direction.EAST, BayData.EMPTY) == Containers.Result.OK, "deploy tank");
        BlockPos tdock = Containers.cell(tpos, Direction.EAST, 1, 1, tank.l());
        level.setBlock(tdock, Freight.LOADING_DOCK.get().defaultBlockState().setValue(LoadingDock.FACING, Direction.WEST), 3);
        ResourceHandler<FluidResource> fh = level.getCapability(Capabilities.Fluid.BLOCK, tdock, null);
        check(fh != null, "dock offers the tank's fluid handler");
        if (fh != null) {
            int in;
            try (Transaction tx = Transaction.openRoot()) { in = fh.insert(FluidResource.of(Fluids.WATER), 5_000, tx); tx.commit(); }
            check(in == 5_000, "tank takes 5000 mB, took " + in);
            check(Containers.lift(level, tpos, 100_000, true).result() == Containers.Result.NOT_EMPTY, "full tank is not lifted");
            try (Transaction tx = Transaction.openRoot()) { fh.extract(FluidResource.of(Fluids.WATER), 5_000, tx); tx.commit(); }
            check(Containers.lift(level, tpos, 100_000, false).result() == Containers.Result.OK, "empty tank lifts");
        }

        BlockPos.betweenClosedStream(lo, hi).forEach(p -> level.setBlock(p, Blocks.AIR.defaultBlockState(), 2 | 16));
        return fails.isEmpty() ? null : String.join("; ", fails);
    }
}
