package com.otectus.immersivetanning.minigame;

import com.google.gson.JsonElement;
import com.otectus.immersivetanning.ImmersiveTanning;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Loads {@code data/<namespace>/immersive_tanning/seam_patterns/<name>.json}; see {@link StitchPattern}. */
public class SeamPatterns extends SimpleJsonResourceReloadListener<JsonElement> {
    private static volatile Map<Identifier, StitchPattern> patterns = Map.of();

    public SeamPatterns() {
        super(net.minecraft.util.ExtraCodecs.JSON, net.minecraft.resources.FileToIdConverter.json("immersive_tanning/seam_patterns"));
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, StitchPattern> loaded = new LinkedHashMap<>();
        files.forEach((id, element) -> {
            try {
                loaded.put(id, StitchPattern.fromJson(id, GsonHelper.convertToJsonObject(element, "seam pattern")));
            } catch (RuntimeException e) {
                ImmersiveTanning.LOGGER.error("Skipping seam pattern {}: {}", id, e.getMessage());
            }
        });
        patterns = Map.copyOf(loaded);
        ImmersiveTanning.LOGGER.info("Loaded {} seam patterns", loaded.size());
    }

    public static Set<Identifier> ids() {
        return patterns.keySet();
    }

    public static boolean has(Identifier id) {
        return patterns.containsKey(id);
    }

    /** The named pattern, else the generic one, else the built-in default. */
    public static StitchPattern get(Identifier id) {
        StitchPattern pattern = patterns.get(id);
        if (pattern == null) pattern = patterns.get(StitchPattern.GENERIC);
        return pattern != null ? pattern : StitchPattern.DEFAULT;
    }
}
