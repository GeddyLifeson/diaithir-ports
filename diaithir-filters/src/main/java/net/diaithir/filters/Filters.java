package net.diaithir.filters;

import net.minecraft.world.item.ItemStack;

/** Public API for other modules. */
public final class Filters {
    private Filters() {
    }

    public static boolean isFilter(ItemStack s) {
        return s.getItem() instanceof FilterItem;
    }

    public static boolean isCard(ItemStack s) {
        return s.getItem() instanceof FilterItem f && f.card();
    }

    public static FilterData data(ItemStack filter) {
        return filter.getOrDefault(DiaithirFilters.FILTER_DATA.get(), FilterData.EMPTY);
    }

    /** True if the candidate passes the filter. Anything that is not a filter, and an empty filter, passes everything. */
    public static boolean matches(ItemStack filter, ItemStack candidate) {
        if (!isFilter(filter)) return true;
        return matches(data(filter), Rule.Probe.of(candidate));
    }

    public static boolean matches(FilterData data, Rule.Probe probe) {
        return data.rules().isEmpty() || data.root().test(probe);
    }

    /** How many candidates a caller should test per tick, from the server config. */
    public static int budget() {
        return Config.MATCHES_PER_TICK.get();
    }
}
