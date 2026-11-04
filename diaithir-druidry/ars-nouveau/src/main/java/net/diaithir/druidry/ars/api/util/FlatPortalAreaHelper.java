package net.diaithir.druidry.ars.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

/**
 * Finds a rectangular, horizontal (X/Z plane) frame of foundation blocks around a free interior.
 * Port note: reimplemented because the NeoForge PortalFrameTester base class no longer exists.
 */
public class FlatPortalAreaHelper {
    public int xSize = -1, zSize = -1;
    public final int maxXSize = 21, maxZSize = 21;
    public BlockPos lowerCorner;
    public Level world;
    public Predicate<BlockState> VALID_FRAME;

    public FlatPortalAreaHelper() {
    }

    private boolean isFree(BlockPos pos) {
        return !VALID_FRAME.test(world.getBlockState(pos));
    }

    public FlatPortalAreaHelper init(Level world, BlockPos blockPos, Direction.Axis axis, Predicate<BlockState> foundations) {
        this.VALID_FRAME = foundations;
        this.world = world;
        BlockPos corner = blockPos;
        int i = 0;
        while (i++ < maxXSize && isFree(corner.west())) corner = corner.west();
        i = 0;
        while (i++ < maxZSize && isFree(corner.north())) corner = corner.north();
        if (!isFree(corner)) {
            lowerCorner = null;
            xSize = zSize = 0;
            return this;
        }
        int x = 0;
        while (x < maxXSize && isFree(corner.east(x))) x++;
        int z = 0;
        while (z < maxZSize && isFree(corner.south(z))) z++;
        boolean ok = x >= 1 && z >= 1 && x < maxXSize && z < maxZSize;
        for (int dx = 0; ok && dx < x; dx++) {
            ok = !isFree(corner.east(dx).north()) && !isFree(corner.east(dx).south(z));
        }
        for (int dz = 0; ok && dz < z; dz++) {
            ok = !isFree(corner.south(dz).west()) && !isFree(corner.south(dz).east(x));
        }
        if (ok) {
            lowerCorner = corner;
            xSize = x;
            zSize = z;
        } else {
            lowerCorner = null;
            xSize = zSize = 1;
        }
        return this;
    }

    public boolean isValidFrame() {
        return this.lowerCorner != null && xSize >= 1 && zSize >= 1 && xSize < maxXSize && zSize < maxZSize;
    }
}
