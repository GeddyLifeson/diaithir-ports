package net.diaithir.logistics.foundry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/** Loaded depots and ghosts of each level, so a depot run never scans the world. */
public final class Sites {
    private Sites() {}

    private static final Map<Level, Set<BlockPos>> GHOSTS = new WeakHashMap<>();

    static void addGhost(Level level, BlockPos pos) {
        GHOSTS.computeIfAbsent(level, l -> new LinkedHashSet<>()).add(pos.immutable());
    }

    static void removeGhost(Level level, BlockPos pos) {
        Set<BlockPos> s = GHOSTS.get(level);
        if (s != null) s.remove(pos);
    }

    static Set<BlockPos> ghosts(Level level) {
        return GHOSTS.getOrDefault(level, Set.of());
    }

    static void clear() {
        GHOSTS.clear();
    }
}
