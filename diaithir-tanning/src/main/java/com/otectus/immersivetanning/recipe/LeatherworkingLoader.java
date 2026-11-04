package com.otectus.immersivetanning.recipe;

import com.otectus.immersivetanning.ImmersiveTanning;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

/** Loads {@code data/<namespace>/immersive_tanning/leatherworking/<name>.json}; see {@link LeatherworkingRecipe}. */
public class LeatherworkingLoader extends SimpleJsonResourceReloadListener<LeatherworkingRecipe.Data> {

    public LeatherworkingLoader() {
        super(LeatherworkingRecipe.Data.CODEC, FileToIdConverter.json("immersive_tanning/leatherworking"));
    }

    @Override
    protected void apply(Map<Identifier, LeatherworkingRecipe.Data> files, ResourceManager manager, ProfilerFiller profiler) {
        TanningData.setExplicit(files.entrySet().stream().map(e -> LeatherworkingRecipe.of(e.getKey(), e.getValue())).toList());
        ImmersiveTanning.LOGGER.info("Loaded {} leatherworking recipes", files.size());
    }
}
