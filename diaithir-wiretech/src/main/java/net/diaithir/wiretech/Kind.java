package net.diaithir.wiretech;

/** The machine kinds. Slot layout of every machine: 0 and 1 inputs, 2 main output, 3 bonus output, 4 augment. */
public enum Kind {
    CRUSHER("crusher", "Brakkulon", 1, true, false),
    ARC_FURNACE("arc_furnace", "Vorthazek", 2, true, false);

    public static final int IN0 = 0, IN1 = 1, OUT = 2, BONUS = 3, UPGRADE = 4, SIZE = 5;

    public final String id;
    public final String display;
    public final int inputs;
    public final boolean bonus;
    public final boolean fluid;

    Kind(String id, String display, int inputs, boolean bonus, boolean fluid) {
        this.id = id;
        this.display = display;
        this.inputs = inputs;
        this.bonus = bonus;
        this.fluid = fluid;
    }
}
