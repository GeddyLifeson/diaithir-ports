package net.diaithir.yards;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Yards, catalog parts and rollout hooks by id. Owners (rail, ships, space, farm, trucks) register at mod init. */
public final class YardRegistry {
    /** Called when a build completes. {@code world} and {@code pos} are the owner's own types (Level, BlockPos). */
    @FunctionalInterface
    public interface RolloutHook {
        Object rollOut(Object world, Object pos, YardDef def, Stats stats, BuildState build);
    }

    private static final Map<String, YardDef> YARDS = new ConcurrentHashMap<>();
    private static final Map<String, PartDef> PARTS = new ConcurrentHashMap<>();
    private static final Map<String, RolloutHook> HOOKS = new ConcurrentHashMap<>();

    private YardRegistry() {}

    public static void register(YardDef d) { YARDS.put(d.id(), d); }

    public static void register(PartDef p) { PARTS.put(p.item(), p); }

    public static void rollout(String yardId, RolloutHook h) { HOOKS.put(yardId, h); }

    public static java.util.Collection<YardDef> yards() { return java.util.List.copyOf(YARDS.values()); }

    public static java.util.Collection<PartDef> parts() { return java.util.List.copyOf(PARTS.values()); }

    public static Optional<YardDef> yard(String id) { return Optional.ofNullable(YARDS.get(id)); }

    public static Optional<PartDef> part(String item) { return Optional.ofNullable(PARTS.get(item)); }

    public static Object finish(String yardId, Object world, Object pos, BuildState b) {
        YardDef d = YARDS.get(yardId);
        RolloutHook h = HOOKS.get(yardId);
        if (d == null || h == null || !b.complete()) return null;
        return h.rollOut(world, pos, d, b.stats(), b);
    }
}
