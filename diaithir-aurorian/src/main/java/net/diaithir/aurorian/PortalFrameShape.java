package net.diaithir.aurorian;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/** Finds an empty rectangle (2 to 21 wide, 3 to 21 tall) ringed by portal-frame blocks. */
public final class PortalFrameShape {
    public final Direction.Axis axis;
    public final List<BlockPos> interior = new ArrayList<>();

    private PortalFrameShape(Direction.Axis axis) {
        this.axis = axis;
    }

    private static boolean frame(LevelReader level, BlockPos p) {
        return level.getBlockState(p).is(DiaithirAurorian.PORTAL_FRAME.get());
    }

    private static boolean open(LevelReader level, BlockPos p) {
        BlockState s = level.getBlockState(p);
        return s.isAir() || s.is(net.minecraft.tags.BlockTags.FIRE);
    }

    /** start is an empty block next to a frame block. Returns null when no complete frame surrounds it. */
    public static PortalFrameShape find(LevelReader level, BlockPos start) {
        for (Direction.Axis axis : new Direction.Axis[] {Direction.Axis.X, Direction.Axis.Z}) {
            PortalFrameShape s = tryAxis(level, start, axis);
            if (s != null) {
                return s;
            }
        }
        return null;
    }

    private static PortalFrameShape tryAxis(LevelReader level, BlockPos start, Direction.Axis axis) {
        Direction right = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        BlockPos p = start;
        int guard = 0;
        while (open(level, p.below()) && guard++ < 22) {
            p = p.below();
        }
        if (!frame(level, p.below()) || !open(level, p)) {
            return null;
        }
        BlockPos left = p;
        guard = 0;
        while (open(level, left.relative(right.getOpposite())) && frame(level, left.relative(right.getOpposite()).below()) && guard++ < 22) {
            left = left.relative(right.getOpposite());
        }
        if (!frame(level, left.relative(right.getOpposite()))) {
            return null;
        }
        int width = 0;
        while (width < 22 && open(level, left.relative(right, width)) && frame(level, left.relative(right, width).below())) {
            width++;
        }
        if (width < 2 || width > 21 || !frame(level, left.relative(right, width))) {
            return null;
        }
        int height = 0;
        outer:
        for (; height < 22; height++) {
            for (int i = 0; i < width; i++) {
                if (!open(level, left.above(height).relative(right, i))) {
                    break outer;
                }
            }
            if (!frame(level, left.above(height).relative(right.getOpposite())) || !frame(level, left.above(height).relative(right, width))) {
                return null;
            }
        }
        if (height < 3 || height > 21) {
            return null;
        }
        for (int i = 0; i < width; i++) {
            if (!frame(level, left.above(height).relative(right, i))) {
                return null;
            }
        }
        PortalFrameShape s = new PortalFrameShape(axis);
        for (int y = 0; y < height; y++) {
            for (int i = 0; i < width; i++) {
                s.interior.add(left.above(y).relative(right, i));
            }
        }
        return s;
    }
}
