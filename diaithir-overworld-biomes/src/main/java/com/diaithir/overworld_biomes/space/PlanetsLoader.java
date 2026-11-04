package com.diaithir.overworld_biomes.space;

import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import org.exodusstudio.stellaris.common.data.PlanetsData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Reads every data/&lt;namespace&gt;/planets/*.json into {@link PlanetsData#PLANETS}. */
public final class PlanetsLoader {
    private static final Logger LOG = LoggerFactory.getLogger("StellarisPlanets");

    private PlanetsLoader() {}

    public static void load(MinecraftServer server) {
        ResourceManager rm = server.getResourceManager();
        List<PlanetsData.Planet> found = new ArrayList<>();
        rm.listResources("planets", id -> id.getPath().endsWith(".json")).forEach((id, res) -> {
            try (Reader r = res.openAsReader()) {
                found.add(PlanetsData.Planet.fromJson(JsonParser.parseReader(r).getAsJsonObject()));
            } catch (Exception ex) {
                LOG.warn("bad planet file {}: {}", id, ex.toString());
            }
        });
        PlanetsData.setPlanets(found);
        LOG.info("{} planets loaded", found.size());
    }
}
