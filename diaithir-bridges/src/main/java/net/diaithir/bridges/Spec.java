package net.diaithir.bridges;

/** One tier of one machine: what it moves, how fast, and how much it loses. */
record Spec(Kind kind, int tier, String id, String age, int rate, int effPct, int outPerTick, int capacity, int inPerTick, int cost, int lossPct) {
    enum Kind { SOURCE, CHARGER, RELAY }

    /** Machine families; every tier of a family shares a name stem and a block entity type. */
    static final java.util.List<Spec> ALL = java.util.List.of(
            // Toburdeluin, magic reagent to FE (Age VI): burn rate FE/t, yield, push per tick, buffer
            new Spec(Kind.SOURCE, 1, "tobardealain", "VI", 80, 100, 2_000, 200_000, 0, 0, 0),
            new Spec(Kind.SOURCE, 2, "tobardealainumha", "VII", 320, 125, 8_000, 800_000, 0, 0, 0),
            new Spec(Kind.SOURCE, 3, "tobardealainoir", "RODOS", 1_280, 150, 32_000, 3_200_000, 0, 0, 0),
            // Lochrandraoidheachd, FE to a reagent (Age VII): intake per tick, FE per dust, buffer
            new Spec(Kind.CHARGER, 1, "lochrandraoidheachd", "VII", 0, 100, 0, 100_000, 4_000, 20_000, 0),
            new Spec(Kind.CHARGER, 2, "lochrandraoidheachdumha", "RODOS", 0, 100, 0, 400_000, 16_000, 14_000, 0),
            // Aibnedeluin, FE relay between tech mods (Age VI): throughput per tick, loss
            new Spec(Kind.RELAY, 1, "aibhnedealain", "VI", 0, 100, 4_000, 40_000, 4_000, 0, 8),
            new Spec(Kind.RELAY, 2, "aibhnedealainumha", "VII", 0, 100, 32_000, 320_000, 32_000, 0, 4),
            new Spec(Kind.RELAY, 3, "aibhnedealainoir", "RODOS", 0, 100, 256_000, 2_560_000, 256_000, 0, 0));
}
