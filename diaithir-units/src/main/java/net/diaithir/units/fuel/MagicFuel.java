package net.diaithir.units.fuel;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * What a magic reagent is worth as fuel, in joules, at the pack rate of 100 J per mana (Source is ten mana, 1000 J).
 * Data: {@code data/diaithirunits/magic_fuel.csv} (item id, mana). Pure Java. Missing mods cost nothing: an id with no row is not fuel.
 * <p>
 * Scale warning for vehicle code: coal is 24 MJ/kg, the best reagent here is 80 kJ, so one is about 0.3 % of a coal. Count burns in joules
 * (keep a bank in the vehicle) rather than "one item = one coal", or magic fuel is free. {@link #coalEquivalent} gives the fraction.
 * Stored mana is not an item: read it through the plain NeoForge energy capability (1 FE = 1 J), which is what the bridge blocks expose.
 */
public final class MagicFuel {
    public static final double J_PER_MANA = 100.0;
    private static final Map<String, Double> MANA = new LinkedHashMap<>();

    private MagicFuel() {}

    /** Joules in one item, or 0 when the id is not a magic fuel. */
    public static double joules(String itemId) {
        Double m = MANA.get(itemId);
        return m == null ? 0 : m * J_PER_MANA;
    }

    public static boolean isFuel(String itemId) { return MANA.containsKey(itemId); }

    /** Fraction of one coal (24 MJ) that an item is worth. */
    public static double coalEquivalent(String itemId) { return joules(itemId) / (Fuels.get("coal").mjPerUnit() * 1e6); }

    /** Seconds one item burns at {@code powerW} watts of shaft power and {@code efficiency}. */
    public static double burnSeconds(String itemId, double powerW, double efficiency) {
        if (powerW <= 0 || efficiency <= 0) throw new IllegalArgumentException("power and efficiency must be positive");
        return joules(itemId) * efficiency / powerW;
    }

    public static Set<String> ids() { return Collections.unmodifiableSet(MANA.keySet()); }

    static {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(
                MagicFuel.class.getResourceAsStream("/data/diaithirunits/magic_fuel.csv"), StandardCharsets.UTF_8))) {
            r.readLine();
            for (String line; (line = r.readLine()) != null; ) {
                if (line.isBlank()) continue;
                String[] c = line.split(",", 3);
                MANA.put(c[0], Double.parseDouble(c[1]));
            }
        } catch (IOException | RuntimeException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
