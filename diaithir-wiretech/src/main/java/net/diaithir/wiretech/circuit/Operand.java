package net.diaithir.wiretech.circuit;

/** Either a constant or a signal read from the red network, the green network or both added. */
public record Operand(String signal, int constant, int mask) {
    public static final int BOTH = 0, RED = 1, GREEN = 2;
    public static final String EACH = "each", ANY = "anything", ALL = "everything";

    public static Operand of(int c) {
        return new Operand(null, c, BOTH);
    }

    public boolean isConstant() {
        return signal == null;
    }

    public boolean isWild() {
        return EACH.equals(signal) || ANY.equals(signal) || ALL.equals(signal);
    }

    public int value(In in) {
        return signal == null ? constant : in.pick(mask).get(signal);
    }

    /** Back to the text form the parser reads. */
    public String text() {
        if (signal == null) return Integer.toString(constant);
        String p = mask == RED ? "red:" : mask == GREEN ? "green:" : "";
        return p + Logic.Parse.short_(signal);
    }
}
