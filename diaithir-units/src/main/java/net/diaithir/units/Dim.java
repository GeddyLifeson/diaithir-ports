package net.diaithir.units;

/** The only quantities the pack measures, each with its one canonical SI unit. */
public enum Dim {
    ENERGY("J"), POWER("W"), VOLUME("L"), TEMPERATURE("K"), PRESSURE("bar"), TORQUE("N·m"),
    ROTATION("rpm"), MASS("kg"), LENGTH("m"), TIME("s"), CURRENCY("g Ag");

    public final String canonicalSymbol;

    Dim(String canonicalSymbol) {
        this.canonicalSymbol = canonicalSymbol;
    }
}
