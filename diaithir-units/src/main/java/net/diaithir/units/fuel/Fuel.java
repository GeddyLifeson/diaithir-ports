package net.diaithir.units.fuel;

/** One fuel. Energy is in MJ per stored unit: per litre for liquids, per kg for solids and batteries. */
public record Fuel(String id, String name, Kind kind, String age, double mjPerUnit, String unit, double kgPerLitre, String ivFuelType) {
    public enum Kind { SOLID, LIQUID, ELECTRIC }

    public double joulesPerUnit() { return mjPerUnit * 1e6; }

    /** Stored energy for a quantity (L for liquids, kg otherwise), in joules. */
    public double energyJ(double quantity) { return quantity * joulesPerUnit(); }

    /** Mass of a quantity in kg. */
    public double massKg(double quantity) { return kind == Kind.LIQUID ? quantity * kgPerLitre : quantity; }
}
