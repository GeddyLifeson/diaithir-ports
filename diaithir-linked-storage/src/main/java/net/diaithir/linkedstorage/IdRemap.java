package net.diaithir.linkedstorage;

import java.util.Map;
import java.util.Optional;

/** Old registry names from the mods this one replaces, mapped to ours. Applied by a world data-fixer pass (the loader's missing-mapping hook) when one is available; otherwise this is the documented table to consult when migrating a world by hand. */
public final class IdRemap {
    private static final Map<String, String> BLOCKS = Map.ofEntries(
            Map.entry("dimstorage:dimensional_chest", "diaithirlinkedstorage:linked_chest"),
            Map.entry("dimstorage:dimensional_tank", "diaithirlinkedstorage:linked_tank"),
            Map.entry("dimstorage:dimensional_cell", "diaithirlinkedstorage:linked_cell"),
            Map.entry("dimstorage:dimensional_chest_item", "diaithirlinkedstorage:linked_chest"),
            Map.entry("dimstorage:dimensional_tank_item", "diaithirlinkedstorage:linked_tank"),
            Map.entry("linkedchests:linked_chest", "diaithirlinkedstorage:linked_chest"),
            Map.entry("linkedchests:linked_tank", "diaithirlinkedstorage:linked_tank"),
            Map.entry("entangled:block", "diaithirlinkedstorage:linked_chest"),
            Map.entry("entangled:entangled_block", "diaithirlinkedstorage:linked_chest"),
            Map.entry("entangled:binder", "diaithirlinkedstorage:linked_pair"));

    private IdRemap() {}

    public static Optional<String> migrate(String oldId) {
        return Optional.ofNullable(BLOCKS.get(oldId));
    }

    public static Map<String, String> table() {
        return BLOCKS;
    }
}
