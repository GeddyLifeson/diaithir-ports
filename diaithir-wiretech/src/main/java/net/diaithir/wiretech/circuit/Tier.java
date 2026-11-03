package net.diaithir.wiretech.circuit;

/**
 * The three eras of signal wiring. Each tier is the predecessor of the next: a drum-and-peg cord linkage, a relay panel, then electronics.
 * period is ticks between updates (20 ticks = 1 s), maxValue clamps every signal, maxTypes limits distinct signals on one output,
 * range is the longest wire in metres (one block = 1 m).
 */
public enum Tier {
    MECHANICAL("mechanical", 3, 20, 99, 4, 8),
    RELAY("relay", 4, 4, 9999, 16, 16),
    ELECTRONIC("electronic", 6, 1, Integer.MAX_VALUE, 256, 32);

    public final String id;
    public final int age;
    public final int period;
    public final int maxValue;
    public final int maxTypes;
    public final int range;

    Tier(String id, int age, int period, int maxValue, int maxTypes, int range) {
        this.id = id;
        this.age = age;
        this.period = period;
        this.maxValue = maxValue;
        this.maxTypes = maxTypes;
        this.range = range;
    }
}
