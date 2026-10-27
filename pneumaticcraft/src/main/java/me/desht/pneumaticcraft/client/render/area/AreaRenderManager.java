package me.desht.pneumaticcraft.client.render.area;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Set;

/**
 * 26.1 port: area rendering is not yet ported (client side); calls are accepted and ignored.
 */
public enum AreaRenderManager {
    INSTANCE;

    public static AreaRenderManager getInstance() {
        return INSTANCE;
    }

    public Object showArea(BlockPos[] area, int color, BlockEntity areaShower) {
        return null;
    }

    public Object showArea(Set<BlockPos> area, int color, BlockEntity areaShower, boolean depth) {
        return null;
    }

    public Object showArea(Set<BlockPos> area, int color, BlockEntity areaShower) {
        return null;
    }

    public boolean isShowing(BlockEntity te) {
        return false;
    }

    public void removeHandlers(BlockEntity te) {
    }

    public void clearPosProviderCache() {
    }
}
