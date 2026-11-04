package fr.tathan.sky_aesthetics.client.compat;

/** Iris is not part of the Diaithir pack (dropped); upstream Iris hooks are no-ops here. */
public final class IrisCompat {
    private IrisCompat() {}

    public static void init() {}

    public static Object clearPhase() { return null; }

    public static void restorePhase(Object phase) {}
}
