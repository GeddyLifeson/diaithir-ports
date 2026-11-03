package net.diaithir.vehicles.fuel;

import java.util.LinkedHashMap;
import java.util.Map;

/** A kind of liquid fuel: the fluid id it comes from and its energy per litre in joules (lower heating value, rounded, public physics data). */
public record Fuel(String id, String name, double joulesPerLitre) {
    private static final Map<String, Fuel> ALL = new LinkedHashMap<>();

    public static final Fuel GASOLINE = reg("c:gasoline", "Gasoline", 34.2e6);
    public static final Fuel DIESEL = reg("c:diesel", "Diesel", 38.6e6);
    public static final Fuel BIODIESEL = reg("c:biodiesel", "Biodiesel", 33.0e6);
    public static final Fuel ETHANOL = reg("c:ethanol", "Ethanol", 21.1e6);
    public static final Fuel KEROSENE = reg("c:kerosene", "Kerosene", 35.0e6);
    public static final Fuel CREOSOTE = reg("c:creosote", "Creosote oil", 30.0e6);

    private static Fuel reg(String id, String name, double j) {
        Fuel f = new Fuel(id, name, j);
        ALL.put(id, f);
        return f;
    }

    /** Add a fuel from another module (e.g. diaithir-units Fuels). Replaces a same-id entry. */
    public static Fuel register(String id, String name, double joulesPerLitre) {
        return reg(id, name, joulesPerLitre);
    }

    public static Fuel get(String id) {
        return ALL.get(id);
    }

    public static Iterable<Fuel> all() {
        return ALL.values();
    }
}
