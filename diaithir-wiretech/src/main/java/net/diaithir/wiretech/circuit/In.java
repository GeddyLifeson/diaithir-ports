package net.diaithir.wiretech.circuit;

/** What a node sees on its input terminal: the red and green networks apart. */
public record In(Signals red, Signals green) {
    public static final In NONE = new In(Signals.EMPTY, Signals.EMPTY);

    public Signals pick(int mask) {
        if (mask == Operand.RED) return red;
        if (mask == Operand.GREEN) return green;
        return red.copy().addAll(green);
    }
}
