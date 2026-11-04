package net.diaithir.materials;

import net.diaithir.stations.DiaithirStations;
import net.diaithir.stations.StationRecipe;
import net.diaithir.stations.StationRecipes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** The property table is sane, every built stage is registered with the table's strength, and each has a station recipe. */
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

        // every built stage has a recipe at one of the Foundry stations
        for (Materials.Row row : Materials.all()) {
            if (!row.built()) continue;
            boolean ok = DiaithirStations.STATIONS.values().stream().anyMatch(st -> StationRecipes.forStation(st).stream()
                    .anyMatch((StationRecipe r) -> r.result().item().getRegisteredName().endsWith(":" + row.id())));
            check(fails, ok, row.id() + " has no station recipe");
        }
        // table wired in: fire odds on blocks, road speed by block, breaking length
        var timber = DiaithirMaterials.BLOCK_BY_ID.get("timber_sawn").get();
        var stone = DiaithirMaterials.BLOCK_BY_ID.get("masonry_cut_stone").get();
        check(fails, timber.getFlammability(timber.defaultBlockState(), null, null, null) == 20 && timber.getFireSpreadSpeed(timber.defaultBlockState(), null, null, null) == 5, "timber fire odds are not the table's");
        check(fails, stone.getFlammability(stone.defaultBlockState(), null, null, null) == 0, "cut stone burns");
        check(fails, Materials.of(DiaithirMaterials.BLOCK_BY_ID.get("road_asphalt").get()).roadSpeed() == 0.3 && Materials.of(net.minecraft.world.level.block.Blocks.STONE) == null, "road speed lookup is wrong");
        check(fails, Materials.breakingLengthM(Materials.get("steel_i_beam")) > Materials.breakingLengthM(Materials.get("timber_sawn")), "steel does not out-span timber");
        return fails;
    }
}
