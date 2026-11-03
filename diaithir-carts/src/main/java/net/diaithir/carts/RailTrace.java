package net.diaithir.carts;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Walks the rails connected to a start rail, in both directions, up to a limit. Used by the block signal to know which stretch of track it guards. */
final class RailTrace {
    private RailTrace() {}

    static Direction[] ends(RailShape s) {
        return switch (s) {
            case NORTH_SOUTH -> new Direction[]{Direction.NORTH, Direction.SOUTH};
            case EAST_WEST -> new Direction[]{Direction.EAST, Direction.WEST};
            case ASCENDING_EAST -> new Direction[]{Direction.EAST, Direction.WEST};
            case ASCENDING_WEST -> new Direction[]{Direction.EAST, Direction.WEST};
            case ASCENDING_NORTH -> new Direction[]{Direction.NORTH, Direction.SOUTH};
            case ASCENDING_SOUTH -> new Direction[]{Direction.NORTH, Direction.SOUTH};
            case SOUTH_EAST -> new Direction[]{Direction.SOUTH, Direction.EAST};
            case SOUTH_WEST -> new Direction[]{Direction.SOUTH, Direction.WEST};
            case NORTH_WEST -> new Direction[]{Direction.NORTH, Direction.WEST};
            case NORTH_EAST -> new Direction[]{Direction.NORTH, Direction.EAST};
        };
    }

    static RailShape shape(BlockState st) {
        return st.getValue(((BaseRailBlock) st.getBlock()).getShapeProperty());
    }

    static boolean isRail(Level level, BlockPos p) {
        return level.getBlockState(p).getBlock() instanceof BaseRailBlock;
    }

    /** The rails reached from start by following their connections, at most max of them, start included. */
    static List<BlockPos> trace(Level level, BlockPos start, int max) {
        List<BlockPos> out = new ArrayList<>();
        if (!isRail(level, start)) return out;
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> todo = new ArrayDeque<>();
        todo.add(start);
        seen.add(start);
        while (!todo.isEmpty() && out.size() < max) {
            BlockPos p = todo.poll();
            out.add(p);
            for (Direction d : ends(shape(level.getBlockState(p)))) {
                // a rail ascending toward d has its end one block up
                boolean up = shape(level.getBlockState(p)).isSlope() && ascendsToward(shape(level.getBlockState(p)), d);
                BlockPos base = p.relative(d);
                for (BlockPos n : new BlockPos[]{up ? base.above() : base, base.below(), base}) {
                    if (seen.contains(n) || !isRail(level, n)) continue;
                    boolean back = false;
                    for (Direction e : ends(shape(level.getBlockState(n)))) if (e == d.getOpposite()) back = true;
                    if (!back) continue;
                    seen.add(n);
                    todo.add(n);
                    break;
                }
            }
        }
        return out;
    }

    private static boolean ascendsToward(RailShape s, Direction d) {
        return (s == RailShape.ASCENDING_EAST && d == Direction.EAST) || (s == RailShape.ASCENDING_WEST && d == Direction.WEST)
                || (s == RailShape.ASCENDING_NORTH && d == Direction.NORTH) || (s == RailShape.ASCENDING_SOUTH && d == Direction.SOUTH);
    }

    static boolean occupied(Level level, List<BlockPos> rails) {
        for (BlockPos p : rails) {
            if (!level.getEntitiesOfClass(AbstractMinecart.class, new AABB(p).expandTowards(0, 0.6, 0)).isEmpty()) return true;
        }
        return false;
    }
}
