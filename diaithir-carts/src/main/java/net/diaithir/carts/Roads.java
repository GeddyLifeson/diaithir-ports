package net.diaithir.carts;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.List;

/**
 * Road surfaces, Dirt track (Age I) to concrete motorway (Age VII). The speed factor is vanilla's own block speed factor, so players, mounts, NPCs and
 * carts walk and ride faster on better roads; other vehicle mods read {@link #factorAt}. Table source: vehicles/roads.csv.
 */
public enum Roads {
    DIRT_TRACK("dirt_track", 1.0f), GRAVEL_ROAD("gravel_road", 1.15f), COBBLED_ROAD("cobbled_road", 1.3f),
    MACADAM_ROAD("macadam_road", 1.5f), ASPHALT_ROAD("asphalt_road", 1.8f), CONCRETE_MOTORWAY("concrete_motorway", 2.0f);

    public static final List<Roads> ALL = Arrays.asList(values());
    public final String id;
    public final float speedFactor;

    Roads(String id, float speedFactor) {
        this.id = id;
        this.speedFactor = speedFactor;
    }

    /** Speed factor of the road block at pos, or 1 when it is not one of ours. */
    public static float factorAt(Level level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        for (Roads r : ALL) if (DiaithirCarts.ROAD_BLOCKS.get(r).get() == s.getBlock()) return r.speedFactor;
        return 1.0f;
    }
}
