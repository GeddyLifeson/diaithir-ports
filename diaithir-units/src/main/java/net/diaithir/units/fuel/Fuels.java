package net.diaithir.units.fuel;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Shared fuel registry and maths for every vehicle and engine in the pack (cars, trucks, tractors, trains, ships, planes).
 * Data: {@code data/diaithirunits/fuels.csv}. Real SI throughout: J, W, L, kg, s, m. Pure Java, no Minecraft types.
 */
public final class Fuels {
    private static final Map<String, Fuel> FUELS = new LinkedHashMap<>();
    private static final Map<String, Engine> ENGINES = new LinkedHashMap<>();

    /** Mod-registered fuels (other modules call this; the CSV covers the built-in set). */
    public static synchronized Fuel register(Fuel f) { FUELS.put(f.id(), f); return f; }
    public static synchronized Engine register(Engine e) { ENGINES.put(e.id(), e); return e; }

    public static synchronized Fuel get(String id) { return FUELS.get(id); }
    public static synchronized Engine engine(String id) { return ENGINES.get(id); }
    public static synchronized Collection<Fuel> all() { return List.copyOf(FUELS.values()); }

    /** Fuel flow in litres per second (kg per second for solids) to deliver {@code powerW} at the shaft with {@code efficiency}. */
    public static double flowPerSecond(Fuel f, double powerW, double efficiency) {
        if (efficiency <= 0) throw new IllegalArgumentException("efficiency");
        return powerW / efficiency / f.joulesPerUnit();
    }

    /** Range in metres of a full tank at steady {@code powerW} and {@code speedMs}. */
    public static double rangeM(Fuel f, double quantity, double powerW, double efficiency, double speedMs) {
        return f.energyJ(quantity) * efficiency / powerW * speedMs;
    }

    /** Immersive Vehicles engine {@code fuelConsumption}: millibuckets per tick at full throttle (1 mB = 1 L / 1000, 20 ticks per second). */
    public static double ivConsumption(Fuel f, double maxPowerW, double efficiency) {
        return flowPerSecond(f, maxPowerW, efficiency) * 1000.0 / 20.0;
    }

    /**
     * Efficiency after upgrade modules: each efficiency level removes the same fraction of the remaining loss, so it never passes 1.
     * {@code lossCut} is the summed fractional cut from {@code Resolver} (for example 0.2 for 20 %).
     */
    public static double upgradedEfficiency(double base, double lossCut) {
        double cut = Math.max(0, Math.min(0.95, lossCut));
        return 1 - (1 - base) * (1 - cut);
    }

    static {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(
                Fuels.class.getResourceAsStream("/data/diaithirunits/fuels.csv"), StandardCharsets.UTF_8))) {
            r.readLine();
            for (String line; (line = r.readLine()) != null; ) {
                if (line.isBlank()) continue;
                String[] c = line.split(",", -1);
                Fuel.Kind k = Fuel.Kind.valueOf(c[2].toUpperCase(Locale.ROOT));
                register(new Fuel(c[0], c[1], k, c[3], Double.parseDouble(c[4]), c[5],
                        c[6].isEmpty() ? 0 : Double.parseDouble(c[6]), c[7].isEmpty() ? null : c[7]));
            }
        } catch (IOException | RuntimeException e) {
            throw new ExceptionInInitializerError(e);
        }
        // Typical efficiencies (fuel energy to shaft work): Wikipedia Thermal efficiency and Engine efficiency.
        register(new Engine("steam_piston", 0.08, List.of("wood", "charcoal", "coal", "heavy_fuel_oil")));
        register(new Engine("petrol_engine", 0.25, List.of("petrol", "ethanol", "lpg")));
        register(new Engine("diesel_engine", 0.35, List.of("diesel", "biodiesel", "heavy_fuel_oil")));
        register(new Engine("avgas_piston", 0.28, List.of("avgas")));
        register(new Engine("jet_turbine", 0.30, List.of("jet_a1", "kerosene")));
        register(new Engine("electric_motor", 0.85, List.of("lead_battery", "li_ion_battery")));
        register(new Engine("fuel_cell", 0.50, List.of("hydrogen")));
    }

    private Fuels() {}
}
