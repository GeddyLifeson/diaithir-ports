package net.diaithir.foundry;

import java.util.Map;

/** Documented static map of old registry names to ours. No Remap hook is wired; apply this map from a data fixer or a manual /replaceitem pass. */
public final class IdMigration {
    public static final Map<String, String> OLD_TO_NEW = Map.ofEntries(
            Map.entry("productivemetalworks:foundry_controller", "diaithirfoundry:foundry_controller"),
            Map.entry("productivemetalworks:foundry_drain", "diaithirfoundry:foundry_drain"),
            Map.entry("productivemetalworks:foundry_tank", "diaithirfoundry:foundry_tank"),
            Map.entry("productivemetalworks:foundry_bricks", "diaithirfoundry:foundry_bricks"),
            Map.entry("productivemetalworks:ingot_cast", "diaithirfoundry:cast_ingot"),
            Map.entry("productivemetalworks:nugget_cast", "diaithirfoundry:cast_nugget"),
            Map.entry("productivemetalworks:plate_cast", "diaithirfoundry:cast_plate"),
            Map.entry("productivemetalworks:rod_cast", "diaithirfoundry:cast_rod"),
            Map.entry("productivemetalworks:gear_cast", "diaithirfoundry:cast_gear"),
            Map.entry("productivemetalworks:block_cast", "diaithirfoundry:cast_block"),
            Map.entry("silentgems:gem_cutter", "diaithirfoundry:gem_cutter"),
            Map.entry("silentgems:raw_gem", "diaithirfoundry:raw_gem"),
            Map.entry("silentgems:cut_gem", "diaithirfoundry:cut_gem"),
            Map.entry("jaopca:plate", "diaithirfoundry:plate"),
            Map.entry("jaopca:rod", "diaithirfoundry:rod"),
            Map.entry("jaopca:gear", "diaithirfoundry:gear"));

    private IdMigration() {
    }

    public static String migrate(String old) {
        return OLD_TO_NEW.getOrDefault(old, old);
    }
}
