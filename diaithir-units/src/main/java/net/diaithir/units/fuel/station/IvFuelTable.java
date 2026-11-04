package net.diaithir.units.fuel.station;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Immersive Vehicles keeps its engine-fuel table (fuel type to fluids and potency) in mtsconfig.json, written the first time the game runs. If that run had
 * no vehicle packs loaded the table is saved empty and engines then burn nothing (fuelFlow 0) for ever. This fills any missing fuel type from IV's own
 * defaults for the packs that are loaded now, in memory only, so a config written too early does not leave engines fuel-free.
 */
final class IvFuelTable {
    private IvFuelTable() {}

    /** Returns how many fuel types were added, or -1 when IV is absent. */
    @SuppressWarnings("unchecked")
    static int repair() {
        try {
            Class<?> cs = Class.forName("minecrafttransportsimulator.systems.ConfigSystem");
            Object settings = cs.getField("settings").get(null);
            Object fuel = settings.getClass().getField("fuel").get(settings);
            Field f = fuel.getClass().getField("fuels");
            Map<String, Map<String, Double>> table = (Map<String, Map<String, Double>>) f.get(fuel);
            Map<String, Map<String, Double>> defaults = (Map<String, Map<String, Double>>) fuel.getClass().getMethod("getDefaultFuels").invoke(null);
            if (table == null) {
                f.set(fuel, defaults);
                return defaults.size();
            }
            int added = 0;
            for (Map.Entry<String, Map<String, Double>> e : defaults.entrySet()) {
                if (!table.containsKey(e.getKey()) || table.get(e.getKey()).isEmpty()) {
                    table.put(e.getKey(), e.getValue());
                    added++;
                }
            }
            return added;
        } catch (ClassNotFoundException ex) {
            return -1;
        } catch (ReflectiveOperationException | RuntimeException ex) {
            return -2;
        }
    }
}
