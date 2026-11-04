package org.diaithir.archimedes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Deck logic for walking inside a ship, after the way aleki's Nifty Ships (MIT) lets you stand and move on a boat's decks: the hull is not one
 * solid box, entities stand on the floor cells and are blocked by the walls that rise above them. This class is pure geometry over a
 * {@link ShipChunk} (local coordinates, no world access) so it can be tested without a level.
 */
public final class DeckColliders {
    /** Headroom an entity needs above a floor cell, in blocks (a standing player is 1.8 tall). */
    public static final int HEADROOM = 2;

    private DeckColliders() {}

    /** Collision top of a block in its own cell (0..1), or -1 if it has no collision. */
    static double topOf(BlockState s) {
        VoxelShape shape = s.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        return shape.isEmpty() ? -1 : shape.max(Direction.Axis.Y);
    }

    /** Collision shape bottom to top as local boxes of one block. */
    private static List<AABB> boxesOf(BlockState s, BlockPos p) {
        List<AABB> out = new ArrayList<>();
        s.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).forAllBoxes((x0, y0, z0, x1, y1, z1) ->
                out.add(new AABB(p.getX() + x0, p.getY() + y0, p.getZ() + z0, p.getX() + x1, p.getY() + y1, p.getZ() + z1)));
        return out;
    }

    /**
     * Local height an entity standing at (x, z) with its feet near feetY would stand on: the top of the highest colliding block in that column that is
     * at or below feetY + stepUp, with free space above it. Returns NaN if there is no floor under that point (open air, or the block is under a roof
     * with less than {@link #HEADROOM} free blocks).
     */
    public static double floorAt(ShipChunk chunk, double x, double z, double feetY, double stepUp) {
        int bx = (int) Math.floor(x), bz = (int) Math.floor(z);
        if (x < 0 || z < 0 || bx >= chunk.sizeX || bz >= chunk.sizeZ) return Double.NaN;
        int top = Math.min(chunk.sizeY - 1, (int) Math.floor(feetY + stepUp));
        for (int y = top; y >= 0; y--) {
            BlockState s = chunk.blocks.get(new BlockPos(bx, y, bz));
            if (s == null) continue;
            double t = topOf(s);
            if (t < 0) continue;
            double floor = y + t;
            if (floor > feetY + stepUp) continue;
            for (int h = 1; h <= HEADROOM; h++) {
                BlockState above = chunk.blocks.get(new BlockPos(bx, y + h, bz));
                if (above != null && topOf(above) >= 0 && y + h >= floor) return Double.NaN;
            }
            return floor;
        }
        return Double.NaN;
    }

    /**
     * The collision boxes of a ship in local coordinates: every solid block's boxes, merged along X and then Z where neighbouring blocks have identical
     * boxes, and with interior blocks (all six neighbours solid) dropped. Entities collide with these instead of one box around the whole hull, so
     * a hollow hull can be entered. Floors, walls and roofs all come out of this; a flat deck is a handful of long slabs, not one box per block.
     */
    public static List<AABB> colliders(ShipChunk chunk) {
        record Cell(BlockPos pos, List<AABB> boxes) {}
        List<Cell> cells = new ArrayList<>();
        for (Map.Entry<BlockPos, BlockState> e : chunk.blocks.entrySet()) {
            if (topOf(e.getValue()) < 0) continue;
            if (buried(chunk, e.getKey())) continue;
            cells.add(new Cell(e.getKey(), boxesOf(e.getValue(), e.getKey())));
        }
        List<AABB> raw = new ArrayList<>();
        for (Cell c : cells) raw.addAll(c.boxes());
        return merge(raw);
    }

    private static boolean buried(ShipChunk chunk, BlockPos p) {
        for (Direction d : Direction.values()) {
            BlockState n = chunk.blocks.get(p.relative(d));
            if (n == null || !n.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)) return false;
        }
        return true;
    }

    /** Greedy merge of boxes that touch along a face and share the other two extents, repeated per axis until nothing merges. */
    static List<AABB> merge(List<AABB> in) {
        List<AABB> boxes = new ArrayList<>(in);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < boxes.size() && !changed; i++) {
                for (int j = i + 1; j < boxes.size() && !changed; j++) {
                    AABB m = join(boxes.get(i), boxes.get(j));
                    if (m != null) {
                        boxes.set(i, m);
                        boxes.remove(j);
                        changed = true;
                    }
                }
            }
        }
        return boxes;
    }

    private static boolean eq(double a, double b) {
        return Math.abs(a - b) < 1e-6;
    }

    private static AABB join(AABB a, AABB b) {
        boolean sameY = eq(a.minY, b.minY) && eq(a.maxY, b.maxY), sameX = eq(a.minX, b.minX) && eq(a.maxX, b.maxX), sameZ = eq(a.minZ, b.minZ) && eq(a.maxZ, b.maxZ);
        if (sameY && sameZ && (eq(a.maxX, b.minX) || eq(b.maxX, a.minX))) return new AABB(Math.min(a.minX, b.minX), a.minY, a.minZ, Math.max(a.maxX, b.maxX), a.maxY, a.maxZ);
        if (sameX && sameY && (eq(a.maxZ, b.minZ) || eq(b.maxZ, a.minZ))) return new AABB(a.minX, a.minY, Math.min(a.minZ, b.minZ), a.maxX, a.maxY, Math.max(a.maxZ, b.maxZ));
        if (sameX && sameZ && (eq(a.maxY, b.minY) || eq(b.maxY, a.minY))) return new AABB(a.minX, Math.min(a.minY, b.minY), a.minZ, a.maxX, Math.max(a.maxY, b.maxY), a.maxZ);
        return null;
    }
}
