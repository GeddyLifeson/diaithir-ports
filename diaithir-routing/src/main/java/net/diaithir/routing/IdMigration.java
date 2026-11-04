package net.diaithir.routing;

import java.util.Map;

/** Documented static map from the registry names of the mods this one replaces to ours. No DataFixer hook is used: world conversion tools or a /datapack remap can consume this table. */
public final class IdMigration {
    public static final Map<String, String> MAP = Map.ofEntries(
            Map.entry("modularrouters:modular_router", "diaithirrouting:router"),
            Map.entry("modularrouters:sender_module_1", "diaithirrouting:sender_module"),
            Map.entry("modularrouters:sender_module_2", "diaithirrouting:sender_module"),
            Map.entry("modularrouters:puller_module_1", "diaithirrouting:puller_module"),
            Map.entry("modularrouters:puller_module_2", "diaithirrouting:puller_module"),
            Map.entry("modularrouters:vacuum_module", "diaithirrouting:vacuum_module"),
            Map.entry("modularrouters:void_module", "diaithirrouting:void_module"),
            Map.entry("modularrouters:sorter_module", "diaithirrouting:sorter_module"),
            Map.entry("modularrouters:fluid_module", "diaithirrouting:fluid_module"),
            Map.entry("modularrouters:flinger_module", "diaithirrouting:extractor_module"),
            Map.entry("modularrouters:bulk_item_filter", "diaithirrouting:filter"),
            Map.entry("modularrouters:range_upgrade", "diaithirrouting:range_card"),
            Map.entry("bblrouters:router", "diaithirrouting:router"),
            Map.entry("bblrouters:router_link", "diaithirrouting:router_link"),
            Map.entry("laserio:laser_node", "diaithirrouting:laser_node"),
            Map.entry("laserio:laser_connector", "diaithirrouting:laser_node"),
            Map.entry("laserio:filter_basic", "diaithirrouting:filter"),
            Map.entry("laserio:filter_count", "diaithirrouting:filter"),
            Map.entry("laserio:filter_tag", "diaithirrouting:filter"),
            Map.entry("laserio:card_item", "diaithirrouting:extractor_module"),
            Map.entry("laserio:card_fluid", "diaithirrouting:fluid_module"),
            Map.entry("laserio:card_energy", "diaithirrouting:energy_module"),
            Map.entry("logisticsnetwork:router", "diaithirrouting:router"),
            Map.entry("logisticsnetwork:filter", "diaithirrouting:filter"),
            Map.entry("logisticsnetwork:fast_hopper", "diaithirrouting:fast_hopper"),
            Map.entry("logisticsnetwork:filter_hopper", "diaithirrouting:filter_hopper"),
            Map.entry("logisticsnetwork:item_collector", "diaithirrouting:item_collector"),
            Map.entry("logisticsnetwork:copper_hopper", "diaithirrouting:copper_filter_hopper"));

    private IdMigration() {
    }

    /** Returns the new id for an old one, or the input unchanged if it is not in the table. */
    public static String migrate(String oldId) {
        return MAP.getOrDefault(oldId, oldId);
    }
}
