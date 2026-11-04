package ca.spottedleaf.starlight.common.thread;

import ca.spottedleaf.starlight.interndep.flowsched.executor.LockToken;

public record LockTokenImpl(int ownerTag, long pos) implements LockToken {
}
