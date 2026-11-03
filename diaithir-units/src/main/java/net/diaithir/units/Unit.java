package net.diaithir.units;

/** A unit: canonical = value * factor + offset. Only temperatures use an offset. */
public record Unit(String id, Dim dim, String symbol, double factor, double offset) {
    public Unit(String id, Dim dim, String symbol, double factor) {
        this(id, dim, symbol, factor, 0);
    }

    public double toCanonical(double value) {
        return value * factor + offset;
    }

    public double fromCanonical(double canonical) {
        return (canonical - offset) / factor;
    }
}
