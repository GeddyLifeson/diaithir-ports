package com.diaithir.end.nullscape;

/**
 * Placeholder for Nullscape migration logic.
 *
 * This class intentionally has no @SubscribeEvent methods and must NOT be
 * registered on the event bus (doing so throws IllegalArgumentException at
 * mod construction). Call its static helpers directly where needed.
 */
public final class NullscapeMigration {
    private NullscapeMigration() {
    }

    public static void init() {
        // no-op: migration hooks are applied through data (upgrade_rules/nullscape.json)
    }
}
