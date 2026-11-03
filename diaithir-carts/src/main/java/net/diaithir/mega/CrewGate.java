package net.diaithir.mega;

/**
 * Whether a mega vehicle may run. Mirrors Autonomy in net.diaithir.carts (PR 220): crewless only when the type is capable AND a module is
 * fitted; otherwise a living crew member must be aboard, whatever the Age. Wire the booleans from {@code Autonomy.capable/hasModule}
 * once that PR is on the base branch.
 */
public final class CrewGate {
    private CrewGate() {}

    public enum Status { UNCREWED, CREWED, AUTONOMOUS }

    public static Status status(boolean capable, boolean moduleFitted, int crewAboard) {
        if (capable && moduleFitted) return Status.AUTONOMOUS;
        return crewAboard > 0 ? Status.CREWED : Status.UNCREWED;
    }

    public static boolean mayRun(boolean capable, boolean moduleFitted, int crewAboard) {
        return status(capable, moduleFitted, crewAboard) != Status.UNCREWED;
    }
}
