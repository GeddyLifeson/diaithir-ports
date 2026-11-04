package net.diaithir.worldvisuals.ctm;

import net.minecraft.core.Direction;

/** Which tile of a 4 by 4 sheet a face uses. Bits: 1 up, 2 right, 4 down, 8 left, set when a same block touches on that edge. Tile = mask, row major. */
public final class Mask {
    private Mask() {}

    /** Direction of "up" and "right" on the face as seen from outside. */
    public static Direction up(Direction face) {
        return face.getAxis().isHorizontal() ? Direction.UP : Direction.NORTH;
    }

    public static Direction right(Direction face) {
        return switch (face) {
            case NORTH -> Direction.WEST;
            case SOUTH -> Direction.EAST;
            case EAST -> Direction.NORTH;
            case WEST -> Direction.SOUTH;
            default -> Direction.EAST;
        };
    }

    public static int of(Direction face, java.util.function.Predicate<Direction> sameAt) {
        Direction up = up(face), right = right(face);
        int m = 0;
        if (sameAt.test(up)) m |= 1;
        if (sameAt.test(right)) m |= 2;
        if (sameAt.test(up.getOpposite())) m |= 4;
        if (sameAt.test(right.getOpposite())) m |= 8;
        return m;
    }

    /** Maps a position inside the sheet sprite (0..1) to the same position inside the tile for mask. */
    public static float tile(float rel, int index) {
        return (index + rel) / 4f;
    }
}
