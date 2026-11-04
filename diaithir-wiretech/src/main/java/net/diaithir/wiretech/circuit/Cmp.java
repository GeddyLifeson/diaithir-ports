package net.diaithir.wiretech.circuit;

public enum Cmp {
    LT("<"), GT(">"), EQ("="), GE(">="), LE("<="), NE("!=");

    public final String sym;

    Cmp(String sym) {
        this.sym = sym;
    }

    public boolean test(long a, long b) {
        return switch (this) {
            case LT -> a < b;
            case GT -> a > b;
            case EQ -> a == b;
            case GE -> a >= b;
            case LE -> a <= b;
            case NE -> a != b;
        };
    }

    public static Cmp of(String s) {
        for (Cmp c : values()) if (c.sym.equals(s) || (c == EQ && s.equals("=="))) return c;
        return null;
    }
}
