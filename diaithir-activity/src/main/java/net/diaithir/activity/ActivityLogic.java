package net.diaithir.activity;

import java.util.Map;
import java.util.Set;

/** Decides which single activity a player shows, from what the server has seen. No game classes, so it is unit tested. */
public final class ActivityLogic {
    public static final String SLEEPING = "sleeping", RIDING = "riding", FISHING = "fishing", AFK = "afk";

    /**
     * @param last     activity id to the game tick it last happened (a block broken, a hit landed, a book opened)
     * @param menu     the activity of the open container, or null when none is open
     * @param idle     ticks since the player last moved, turned or acted
     */
    public record Snapshot(long now, boolean sleeping, boolean riding, boolean fishing, String menu, Map<String, Long> last, long idle) {}

    private ActivityLogic() {}

    /** The id to show, or empty string for nothing. Highest priority wins, ties go to the lower id so the answer is stable. */
    public static String decide(Snapshot s, Map<String, ActivityData.Def> defs, Set<String> disabled, long afkTicks) {
        String best = "";
        int bestPriority = Integer.MIN_VALUE;
        if (s.sleeping()) { best = pick(best, bestPriority, SLEEPING, defs, disabled); bestPriority = prio(best, defs); }
        if (s.fishing()) { best = pick(best, bestPriority, FISHING, defs, disabled); bestPriority = prio(best, defs); }
        if (s.riding()) { best = pick(best, bestPriority, RIDING, defs, disabled); bestPriority = prio(best, defs); }
        if (s.menu() != null) { best = pick(best, bestPriority, s.menu(), defs, disabled); bestPriority = prio(best, defs); }
        if (s.idle() >= afkTicks) { best = pick(best, bestPriority, AFK, defs, disabled); bestPriority = prio(best, defs); }
        for (Map.Entry<String, Long> e : s.last().entrySet()) {
            ActivityData.Def d = defs.get(e.getKey());
            if (d == null || s.now() - e.getValue() > d.hold()) continue;
            best = pick(best, bestPriority, e.getKey(), defs, disabled);
            bestPriority = prio(best, defs);
        }
        return best;
    }

    private static int prio(String id, Map<String, ActivityData.Def> defs) {
        ActivityData.Def d = defs.get(id);
        return d == null ? Integer.MIN_VALUE : d.priority();
    }

    private static String pick(String best, int bestPriority, String cand, Map<String, ActivityData.Def> defs, Set<String> disabled) {
        ActivityData.Def d = defs.get(cand);
        if (d == null || disabled.contains(cand)) return best;
        if (d.priority() > bestPriority || (d.priority() == bestPriority && cand.compareTo(best) < 0)) return cand;
        return best;
    }
}
