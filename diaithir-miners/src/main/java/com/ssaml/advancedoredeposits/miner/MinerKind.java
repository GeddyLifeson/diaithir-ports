package com.ssaml.advancedoredeposits.miner;

/** Miner tiers, ported from FactoriOres (MIT, Seraphaestus): burner range 1, electrical range 2, 200 ticks per cycle. */
public enum MinerKind {
    BURNER("burner_miner", 1, 200),
    ELECTRIC("electric_miner", 2, 200);

    public final String id;
    public final int range;
    public final int cycleTicks;

    MinerKind(String id, int range, int cycleTicks) {
        this.id = id;
        this.range = range;
        this.cycleTicks = cycleTicks;
    }
}
