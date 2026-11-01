package com.otectus.immersivetanning.recipe;

import com.otectus.immersivetanning.ImmersiveTanning;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.resources.FileToIdConverter;

import java.util.List;
import java.util.Map;

/** Loads {@code data/<namespace>/immersive_tanning/hide_mounting/<name>.json}; see {@link HideMountingRecipe}. */
public class HideMountingLoader extends SimpleJsonResourceReloadListener<HideMountingRecipe> {
    private static volatile List<HideMountingRecipe> loaded = List.of();

    public HideMountingLoader() {
        super(HideMountingRecipe.CODEC, FileToIdConverter.json("immersive_tanning/hide_mounting"));
    }

    @Override
    protected void apply(Map<Identifier, HideMountingRecipe> files, ResourceManager manager, ProfilerFiller profiler) {
        loaded = List.copyOf(files.values());
        ImmersiveTanning.LOGGER.info("Loaded {} hide mounting entries", loaded.size());
    }

    public static List<HideMountingRecipe> all() {
        return loaded;
    }
}
