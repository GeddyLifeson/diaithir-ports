package net.diaithir.stations;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Loads station recipes on every reload and answers lookups by station key. */
public final class StationRecipes extends SimpleJsonResourceReloadListener<StationRecipe> {
    public static final String PATH = "station_recipe";
    public static final Identifier ID = Identifier.fromNamespaceAndPath(DiaithirStations.ID, PATH);

    /** Recipes load from data/&lt;ns&gt;/diaithir_stations/station_recipe/*.json (the directory the registry key gives). */
    public static final ResourceKey<Registry<StationRecipe>> KEY = ResourceKey.createRegistryKey(ID);

    private static volatile Map<Identifier, StationRecipe> all = Map.of();

    public StationRecipes(HolderLookup.Provider lookup) {
        super(lookup, StationRecipe.CODEC, KEY);
    }

    @Override
    protected void apply(Map<Identifier, StationRecipe> prepared, ResourceManager manager, ProfilerFiller profiler) {
        all = Map.copyOf(prepared);
        DiaithirStations.LOGGER.info("Loaded {} station recipes", all.size());
    }

    public static List<StationRecipe> forStation(String station) {
        List<StationRecipe> out = new ArrayList<>();
        for (StationRecipe r : all.values()) if (r.station().equals(station)) out.add(r);
        return out;
    }

    public static int count() { return all.size(); }

    /** Test hook: replaces the loaded recipes. */
    static void setForTest(Map<Identifier, StationRecipe> recipes) { all = Map.copyOf(recipes); }
}
