package net.diaithir.pipes;

/** Throughput tiers. Each run a pipe moves at most (base per kind) times the multiplier. */
public enum PipeTier {
    BASIC("basic", 1), IMPROVED("improved", 4), ADVANCED("advanced", 16);

    private final String id;
    private final int multiplier;

    PipeTier(String id, int multiplier) {
        this.id = id;
        this.multiplier = multiplier;
    }

    public String id() {
        return id;
    }

    public int multiplier() {
        return multiplier;
    }

    public long perRun(PipeKind kind) {
        return switch (kind) {
            case ITEM -> (long) Config.ITEMS_PER_RUN.get() * multiplier;
            case FLUID -> (long) Config.FLUID_PER_RUN.get() * multiplier;
            case ENERGY -> (long) Config.ENERGY_PER_RUN.get() * multiplier;
        };
    }
}
