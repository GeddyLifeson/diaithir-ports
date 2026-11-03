package net.diaithir.materials;

import net.diaithir.stations.StationRecipe;
import net.diaithir.stations.StationRecipes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** The property table is sane, every built stage is registered with the table's strength, and the concrete chain has recipes at the right stations. */
final class MaterialsSelfTest {
    private MaterialsSelfTest() {}

    private static void check(List<String> fails, boolean ok, String what) {
        if (!ok) fails.add(what);
    }

    static List<String> run() {
        List<String> fails = new ArrayList<>();
        Set<String> classes = new HashSet<>();
        for (Materials.Row r : Materials.all()) {
            classes.add(r.cls());
            check(fails, Materials.CLASSES.contains(r.cls()), r.id() + " has unknown class " + r.cls());
            check(fails, Materials.AGES.contains(r.age()), r.id() + " has unknown age " + r.age());
            check(fails, r.density() > 0 && r.conductivity() > 0 && r.fireMin() >= 0 && r.hardness() >= 0 && r.resistance() >= 0, r.id() + " has a bad number");
        }
        check(fails, classes.containsAll(Materials.CLASSES), "a class has no rows: " + classes);
        for (String cls : Materials.CLASSES) {
            int last = -1;
            for (Materials.Row r : Materials.all()) {
                if (!r.cls().equals(cls)) continue;
                int a = Materials.AGES.indexOf(r.age());
                check(fails, a >= last, cls + " is not in Age order at " + r.id());
                last = a;
            }
        }
        for (var e : DiaithirMaterials.BLOCK_BY_ID.entrySet()) {
            Materials.Row r = Materials.get(e.getKey());
            check(fails, r.built(), e.getKey() + " is registered but not marked built");
            check(fails, e.getValue().get().defaultDestroyTime() == r.hardness(), e.getKey() + " hardness differs from the table");
            check(fails, e.getValue().get().getExplosionResistance() == r.resistance(), e.getKey() + " resistance differs from the table");
        }
        long built = Materials.all().stream().filter(Materials.Row::built).count();
        check(fails, built == DiaithirMaterials.BLOCK_BY_ID.size() + 1, "built rows (" + built + ") do not match registered stages");

        // concrete chain: pozzolan at the hearth bench, cement at the smeltery, Portland at the steam forge, reinforced and precast at the assembler
        String[][] chain = {{"station.hearth_bench", "concrete_pozzolan"}, {"station.smeltery", "cement_powder"}, {"station.steam_forge", "concrete_portland"},
                {"machine.assembler", "concrete_reinforced"}, {"machine.assembler", "concrete_precast"}};
        for (String[] c : chain) {
            boolean ok = StationRecipes.forStation(c[0]).stream().anyMatch((StationRecipe r) -> r.result().item().getRegisteredName().endsWith(":" + c[1]));
            check(fails, ok, c[1] + " has no recipe at " + c[0]);
        }
        return fails;
    }
}
