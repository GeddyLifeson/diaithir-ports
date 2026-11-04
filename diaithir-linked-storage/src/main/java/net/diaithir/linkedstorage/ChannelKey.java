package net.diaithir.linkedstorage;

import java.util.UUID;

/** Identifies one shared store: kind, scope owner (or the public sentinel), frequency. */
public record ChannelKey(LinkKind kind, UUID scope, int frequency) {
    /** Scope used by public blocks, shared by everyone. */
    public static final UUID PUBLIC_SCOPE = new UUID(0L, 0L);

    /** Builds the key a block uses. Owner and team blocks both scope to the owner id (the owner id of a team block is the team leader's, set at placement). */
    public static ChannelKey of(LinkKind kind, Lock lock, UUID owner, int frequency) {
        return new ChannelKey(kind, lock == Lock.PUBLIC ? PUBLIC_SCOPE : owner, frequency & (Frequency.CHANNELS - 1));
    }

    public String id() {
        return kind.name().charAt(0) + "-" + scope + "-" + frequency;
    }
}
