package net.diaithir.wiretech.circuit;

/** Arithmetic on signed 32-bit values. Division and modulo by zero give 0, a negative power gives 0, shifts use the low 5 bits, results saturate. */
public enum ArithOp {
    ADD("+"), SUB("-"), MUL("*"), DIV("/"), MOD("%"), POW("^"), SHL("<<"), SHR(">>"), AND("and"), OR("or"), XOR("xor");

    public final String sym;

    ArithOp(String sym) {
        this.sym = sym;
    }

    public long apply(int a, int b) {
        return switch (this) {
            case ADD -> (long) a + b;
            case SUB -> (long) a - b;
            case MUL -> (long) a * b;
            case DIV -> b == 0 ? 0 : (long) a / b;
            case MOD -> b == 0 ? 0 : a % b;
            case POW -> pow(a, b);
            case SHL -> (long) a << (b & 31);
            case SHR -> a >> (b & 31);
            case AND -> a & b;
            case OR -> a | b;
            case XOR -> a ^ b;
        };
    }

    private static long pow(int a, int b) {
        if (b < 0) return 0;
        if (a == 0) return b == 0 ? 1 : 0;
        if (a == 1) return 1;
        if (a == -1) return (b & 1) == 0 ? 1 : -1;
        long r = 1;
        for (int i = 0; i < b && Math.abs(r) <= Integer.MAX_VALUE; i++) r *= a;
        return r;
    }

    public static ArithOp of(String s) {
        for (ArithOp o : values()) if (o.sym.equals(s)) return o;
        return null;
    }
}
