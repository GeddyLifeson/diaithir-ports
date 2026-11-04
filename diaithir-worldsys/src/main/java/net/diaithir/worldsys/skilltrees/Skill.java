package net.diaithir.worldsys.skilltrees;

/** The four trainable skills. */
public enum Skill {
    MINING, COMBAT, FARMING, EXPLORING;

    public static final int MAX_LEVEL = 20;

    public static int xpForLevel(int level) {
        return 20 * level;
    }

    public String key() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
