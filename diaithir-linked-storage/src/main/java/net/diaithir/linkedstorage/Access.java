package net.diaithir.linkedstorage;

import java.util.Set;
import java.util.UUID;

/** Pure access rule. A team is the set of player ids the owner trusts. */
public final class Access {
    private Access() {}

    public static boolean allowed(Lock lock, UUID owner, Set<UUID> team, UUID player) {
        if (owner.equals(player)) return true;
        return switch (lock) {
            case OWNER -> false;
            case TEAM -> team.contains(player);
            case PUBLIC -> true;
        };
    }
}
