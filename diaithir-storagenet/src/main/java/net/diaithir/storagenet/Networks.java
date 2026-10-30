package net.diaithir.storagenet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Tracks the loaded controllers of each level. A change next to a network block only sets a dirty flag on them; the walk happens
 * the next time something asks for the network, never per tick.
 */
public final class Networks {
    private Networks() {}

    private static final Map<Level, Set<ControllerBlockEntity>> CONTROLLERS = new WeakHashMap<>();

    static void register(ControllerBlockEntity c) {
        if (c.getLevel() != null) CONTROLLERS.computeIfAbsent(c.getLevel(), l -> new LinkedHashSet<>()).add(c);
    }

    static void unregister(ControllerBlockEntity c) {
        if (c.getLevel() == null) return;
        Set<ControllerBlockEntity> s = CONTROLLERS.get(c.getLevel());
        if (s != null) s.remove(c);
    }

    /** Something next to the network changed: every controller in the level re-walks on its next use. */
    public static void markDirty(Level level) {
        Set<ControllerBlockEntity> s = CONTROLLERS.get(level);
        if (s != null) for (ControllerBlockEntity c : s) c.markDirty();
    }

    /** The network that includes this position, or null if no controller reaches it. */
    public static @Nullable Network find(Level level, BlockPos pos) {
        Set<ControllerBlockEntity> s = CONTROLLERS.get(level);
        if (s == null) return null;
        long key = pos.asLong();
        for (ControllerBlockEntity c : s) {
            if (c.isRemoved()) continue;
            Network n = c.network();
            if (n != null && n.members().contains(key)) return n;
        }
        return null;
    }
}
