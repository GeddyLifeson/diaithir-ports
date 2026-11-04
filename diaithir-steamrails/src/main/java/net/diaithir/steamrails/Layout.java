package net.diaithir.steamrails;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * What a boiler is made of, read from blocks that touch each other. The same scan runs on the real world and on the blocks of a moving
 * train carriage, so a view is just a function from position to block state.
 */
public final class Layout {
    /** Most boiler blocks one boiler may have. */
    public static final int MAX_CLUSTER = 64;

    public final BlockPos master;
    /** Water volume units: a firebox counts 2, a shell 1. */
    public final int size;
    /** Highest pressure in bar the weakest block holds. */
    public final int rating;
    public final int cores;
    public final int cylinders;
    public final int whistles;
    public final List<BlockPos> tenderPos;
    public final List<BlockPos> stacks;
    public final List<BlockPos> whistlePos;
    public final List<BlockPos> cylinderPos;
    public final Set<BlockPos> cluster;

    private Layout(BlockPos master, int size, int rating, int cores, List<BlockPos> cyl, List<BlockPos> whistle, List<BlockPos> stacks, List<BlockPos> tenders,
            Set<BlockPos> cluster) {
        this.master = master;
        this.size = size;
        this.rating = rating;
        this.cores = cores;
        this.cylinders = cyl.size();
        this.cylinderPos = cyl;
        this.whistles = whistle.size();
        this.whistlePos = whistle;
        this.stacks = stacks;
        this.tenderPos = tenders;
        this.cluster = cluster;
    }

    public int waterCapacity() {
        return size * SteamRails.WATER_PER_SIZE;
    }

    public boolean hasStack() {
        return !stacks.isEmpty();
    }

    /** Scans from any block of a boiler. Returns null when the position is not part of one. */
    public static Layout scan(Function<BlockPos, BlockState> view, BlockPos start) {
        Set<BlockPos> cluster = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        BlockState first = view.apply(start);
        if (!isBoilerBlock(first)) return null;
        cluster.add(start);
        queue.add(start);
        while (!queue.isEmpty() && cluster.size() < MAX_CLUSTER) {
            BlockPos p = queue.poll();
            for (Direction d : Direction.values()) {
                BlockPos n = p.relative(d);
                if (cluster.contains(n) || cluster.size() >= MAX_CLUSTER) continue;
                if (isBoilerBlock(view.apply(n))) {
                    cluster.add(n);
                    queue.add(n);
                }
            }
        }
        BlockPos master = null;
        int size = 0, rating = Integer.MAX_VALUE, cores = 0;
        for (BlockPos p : cluster) {
            BlockState s = view.apply(p);
            if (s.getBlock() instanceof BoilerBlock) {
                cores++;
                size += 2;
                rating = Math.min(rating, SteamRails.CORE_RATING);
                if (master == null || order(p, master) < 0) master = p;
            } else if (s.getBlock() instanceof ShellBlock sh) {
                size += 1;
                rating = Math.min(rating, sh.rating);
            }
        }
        if (master == null) return null;
        List<BlockPos> cyl = new ArrayList<>(), whistle = new ArrayList<>(), stacks = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        List<BlockPos> tenders = new ArrayList<>();
        for (BlockPos p : cluster) {
            for (Direction d : Direction.values()) {
                BlockPos n = p.relative(d);
                if (cluster.contains(n) || !seen.add(n)) continue;
                BlockState s = view.apply(n);
                if (s.getBlock() instanceof CylinderBlock) cyl.add(n);
                else if (s.getBlock() instanceof WhistleBlock) whistle.add(n);
                else if (s.getBlock() instanceof SmokestackBlock) stacks.add(n);
                else if (s.getBlock() instanceof TenderBlock) tenders.add(n);
            }
        }
        return new Layout(master, size, rating, cores, cyl, whistle, stacks, tenders, cluster);
    }

    public static boolean isBoilerBlock(BlockState s) {
        return s.getBlock() instanceof BoilerBlock || s.getBlock() instanceof ShellBlock;
    }

    private static int order(BlockPos a, BlockPos b) {
        if (a.getY() != b.getY()) return Integer.compare(a.getY(), b.getY());
        if (a.getX() != b.getX()) return Integer.compare(a.getX(), b.getX());
        return Integer.compare(a.getZ(), b.getZ());
    }
}
