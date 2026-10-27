package net.diaithir.woodvariants;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

/** Boxes in sixteenths for a block that faces north, and the shape rotated to each side. */
public final class Box {
    private Box() {}

    /** Which table corner legs exist (NW, NE, SE, SW): a leg is there when neither side of its corner is joined to a table. */
    public static boolean[] legs(boolean n, boolean e, boolean s, boolean w) {
        return new boolean[] {!n && !w, !n && !e, !s && !e, !s && !w};
    }

    /** x1,y1,z1,x2,y2,z2 */
    public static int[] b(int x1, int y1, int z1, int x2, int y2, int z2) {
        return new int[] {x1, y1, z1, x2, y2, z2};
    }

    /** Quarter turns clockwise seen from above: 0 north, 1 east, 2 south, 3 west (Direction.get2DDataValue order). */
    public static int[] rotate(int[] b, int turns) {
        return switch (turns & 3) {
            case 1 -> new int[] {16 - b[5], b[1], b[0], 16 - b[2], b[4], b[3]};
            case 2 -> new int[] {16 - b[3], b[1], 16 - b[5], 16 - b[0], b[4], 16 - b[2]};
            case 3 -> new int[] {b[2], b[1], 16 - b[3], b[5], b[4], 16 - b[0]};
            default -> b;
        };
    }

    public static VoxelShape shape(int[][] boxes, Direction dir) {
        VoxelShape s = Shapes.empty();
        for (int[] b : boxes) {
            int[] r = rotate(b, dir.get2DDataValue());
            s = Shapes.or(s, Block.box(r[0], r[1], r[2], r[3], r[4], r[5]));
        }
        return s;
    }

    public static Map<Direction, VoxelShape> shapes(int[][] boxes) {
        Map<Direction, VoxelShape> m = new EnumMap<>(Direction.class);
        for (Direction d : Direction.Plane.HORIZONTAL) {
            m.put(d, shape(boxes, d));
        }
        return m;
    }
}
