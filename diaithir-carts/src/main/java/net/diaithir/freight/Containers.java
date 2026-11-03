package net.diaithir.freight;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Putting a container into the world and taking it out again. A deployed container is real blocks: a wall ring, four door cells at the far end, and the
 * loads standing inside as ordinary load blocks, so players walk in, stack by hand and blocks keep their place. Lifting turns it back into one item that
 * carries its cargo (what cranes, forklifts, trucks, rolling stock and ships move); lowering rebuilds it.
 */
public final class Containers {
    public enum Result { OK, BLOCKED, NOT_FOUND, TOO_HEAVY, NOT_EMPTY, WRONG_ITEM }

    public record Lift(Result result, ItemStack stack) {}

    private Containers() {}

    /** World position of exterior cell (i,j,k); i runs to the right of {@code facing}, k along it. */
    public static BlockPos cell(BlockPos origin, Direction facing, int i, int j, int k) {
        return origin.relative(facing.getClockWise(), i).above(j).relative(facing, k);
    }

    private enum Role { WALL, DOOR, UNIT, INTERIOR, VESSEL }

    private static Role role(LoadType t, int i, int j, int k) {
        if (t.kind() == LoadType.Kind.TANK) return Role.VESSEL;
        boolean mid = i >= 1 && i <= t.w() - 2 && j >= 1 && j <= t.h() - 2;
        if (!mid) return Role.WALL;
        if (k == t.l() - 1) return Role.DOOR;
        if (k == 0) return Role.WALL;
        if (t.cold() && k == 1) return Role.UNIT;
        return Role.INTERIOR;
    }

    /** World position of interior cell (x,y,z). */
    public static BlockPos interior(BlockPos origin, Direction facing, LoadType t, int x, int y, int z) {
        return cell(origin, facing, x + 1, y + 1, z + 1 + t.unitRows());
    }

    public static boolean isShell(Block b) {
        return b instanceof ContainerWall || b instanceof ContainerDoor || b == Freight.REEFER_UNIT.get() || b == Freight.TANK_VESSEL.get();
    }

    /** Builds a container with its cargo. Every cell must be air or replaceable. */
    public static Result deploy(ServerLevel level, LoadType t, BlockPos origin, Direction facing, BayData cargo) {
        for (int i = 0; i < t.w(); i++)
            for (int j = 0; j < t.h(); j++)
                for (int k = 0; k < t.l(); k++)
                    if (!level.getBlockState(cell(origin, facing, i, j, k)).canBeReplaced()) return Result.BLOCKED;
        Paint paint = Paint.of(t);
        for (int i = 0; i < t.w(); i++)
            for (int j = 0; j < t.h(); j++)
                for (int k = 0; k < t.l(); k++) {
                    BlockPos p = cell(origin, facing, i, j, k);
                    BlockState s = switch (role(t, i, j, k)) {
                        case WALL -> (i | j | k) == 0 ? Freight.CORE.get().defaultBlockState().setValue(ContainerWall.PAINT, paint)
                                : Freight.WALL.get().defaultBlockState().setValue(ContainerWall.PAINT, paint);
                        case DOOR -> Freight.DOOR.get().defaultBlockState().setValue(ContainerWall.PAINT, paint);
                        case UNIT -> Freight.REEFER_UNIT.get().defaultBlockState();
                        case VESSEL -> (i | j | k) == 0 ? Freight.CORE.get().defaultBlockState().setValue(ContainerWall.PAINT, paint)
                                : Freight.TANK_VESSEL.get().defaultBlockState();
                        case INTERIOR -> Blocks.AIR.defaultBlockState();
                    };
                    if (!s.isAir()) level.setBlock(p, s, 3);
                }
        if (level.getBlockEntity(origin) instanceof ContainerCoreEntity core) core.init(t, facing);
        if (t.container()) {
            CargoBay bay = cargo.toBay(t);
            for (CargoBay.Placed p : bay.loads()) {
                BlockPos at = interior(origin, facing, t, p.x, p.y, p.z);
                level.setBlock(at, Freight.LOADS.get(p.type.id()).get().defaultBlockState(), 3);
                if (level.getBlockEntity(at) instanceof LoadBlockEntity be && p.data instanceof List<?> items) {
                    for (int s = 0; s < items.size() && s < be.getContainerSize(); s++) be.setItem(s, (ItemStack) items.get(s));
                }
            }
        }
        return Result.OK;
    }

    /** The container this block belongs to (any wall, door, unit or vessel cell), or null. */
    public static @Nullable ContainerCoreEntity find(Level level, BlockPos from) {
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> todo = new ArrayDeque<>();
        todo.add(from);
        seen.add(from);
        while (!todo.isEmpty() && seen.size() < 400) {
            BlockPos p = todo.poll();
            if (!level.isLoaded(p)) continue;
            BlockState s = level.getBlockState(p);
            if (!isShell(s.getBlock())) continue;
            if (s.getBlock() instanceof ContainerCore && level.getBlockEntity(p) instanceof ContainerCoreEntity c) return c;
            for (Direction d : Direction.values()) {
                BlockPos n = p.relative(d);
                if (seen.add(n)) todo.add(n);
            }
        }
        return null;
    }

    /** First container found by walking up to {@code reach} cells from {@code from} along {@code dir}: what a dock faces. */
    public static @Nullable ContainerCoreEntity findAlong(Level level, BlockPos from, Direction dir, int reach) {
        for (int n = 1; n <= reach; n++) {
            BlockPos p = from.relative(dir, n);
            if (!level.isLoaded(p)) return null;
            Block b = level.getBlockState(p).getBlock();
            if (isShell(b)) return find(level, p);
            if (!level.getBlockState(p).isAir() && !(b instanceof LoadBlock)) return null;
        }
        return null;
    }

    public static List<BlockPos> interiorCells(ContainerCoreEntity core) {
        LoadType t = core.type();
        List<BlockPos> out = new ArrayList<>();
        if (t == null || !t.container()) return out;
        for (int x = 0; x < t.inner()[0]; x++)
            for (int y = 0; y < t.inner()[1]; y++)
                for (int z = 0; z < t.inner()[2]; z++) out.add(interior(core.getBlockPos(), core.facing(), t, x, y, z));
        return out;
    }

    /** The loads standing in the container right now, read from the blocks. */
    public static BayData contents(Level level, ContainerCoreEntity core) {
        LoadType t = core.type();
        List<BayData.Entry> out = new ArrayList<>();
        if (t != null && t.container()) {
            for (int x = 0; x < t.inner()[0]; x++)
                for (int y = 0; y < t.inner()[1]; y++)
                    for (int z = 0; z < t.inner()[2]; z++) {
                        BlockPos p = interior(core.getBlockPos(), core.facing(), t, x, y, z);
                        if (level.getBlockEntity(p) instanceof LoadBlockEntity be) {
                            NonNullList<ItemStack> items = NonNullList.withSize(be.getContainerSize(), ItemStack.EMPTY);
                            for (int s = 0; s < items.size(); s++) items.set(s, be.getItem(s).copy());
                            out.add(new BayData.Entry(be.type.id(), x, y, z, false, ItemContainerContents.fromItems(items)));
                        }
                    }
        }
        return new BayData(out);
    }

    /** Every load's inventory as one Container, for docks. Slot numbers run load by load. */
    public static Container view(Level level, ContainerCoreEntity core) {
        List<LoadBlockEntity> loads = new ArrayList<>();
        for (BlockPos p : interiorCells(core)) if (level.getBlockEntity(p) instanceof LoadBlockEntity be) loads.add(be);
        return new ContainerView(loads);
    }

    /** Gross mass: tare, loads, and the fluid of a tank (1 kg per litre as a rule of thumb for water-like cargo). */
    public static double grossKg(Level level, ContainerCoreEntity core) {
        LoadType t = core.type();
        double kg = t.tareKg() + core.fluidMb() / 1000.0;
        for (BlockPos p : interiorCells(core)) if (level.getBlockEntity(p) instanceof LoadBlockEntity be) kg += be.type.tareKg() + be.payloadKg();
        return kg;
    }

    public static void setDoor(ServerLevel level, ContainerCoreEntity core, boolean open) {
        LoadType t = core.type();
        if (t == null || t.kind() == LoadType.Kind.TANK) return;
        for (int i = 1; i <= t.w() - 2; i++)
            for (int j = 1; j <= t.h() - 2; j++) {
                BlockPos p = cell(core.getBlockPos(), core.facing(), i, j, t.l() - 1);
                BlockState s = level.getBlockState(p);
                if (s.getBlock() instanceof ContainerDoor) level.setBlock(p, s.setValue(ContainerDoor.OPEN, open), 3);
            }
    }

    /** Turns a deployed container into one item carrying its cargo, if the lifter can bear it ({@code liftKg}). {@code simulate} only checks. */
    public static Lift lift(ServerLevel level, BlockPos anyShellBlock, double liftKg, boolean simulate) {
        ContainerCoreEntity core = find(level, anyShellBlock);
        if (core == null || core.type() == null) return new Lift(Result.NOT_FOUND, ItemStack.EMPTY);
        LoadType t = core.type();
        if (core.fluidMb() > 0) return new Lift(Result.NOT_EMPTY, ItemStack.EMPTY);
        if (grossKg(level, core) > liftKg) return new Lift(Result.TOO_HEAVY, ItemStack.EMPTY);
        ItemStack out = new ItemStack(Freight.CONTAINER_ITEMS.get(t.id()).get());
        out.set(Freight.BAY.get(), contents(level, core));
        if (!simulate) {
            BlockPos o = core.getBlockPos();
            Direction f = core.facing();
            for (int i = 0; i < t.w(); i++)
                for (int j = 0; j < t.h(); j++)
                    for (int k = 0; k < t.l(); k++) level.setBlock(cell(o, f, i, j, k), Blocks.AIR.defaultBlockState(), 2 | 16);
        }
        return new Lift(Result.OK, out);
    }

    /** Sets a container item down with its low corner at {@code origin}. */
    public static Result lower(ServerLevel level, ItemStack stack, BlockPos origin, Direction facing) {
        if (!(stack.getItem() instanceof ContainerItem ci)) return Result.WRONG_ITEM;
        return deploy(level, ci.type, origin, facing, stack.getOrDefault(Freight.BAY.get(), BayData.EMPTY));
    }

    /** Virtual form for a vehicle bay: the container's cargo as a {@link CargoBay}. */
    public static CargoBay bayOf(ItemStack stack) {
        ContainerItem ci = (ContainerItem) stack.getItem();
        return stack.getOrDefault(Freight.BAY.get(), BayData.EMPTY).toBay(ci.type);
    }
}
