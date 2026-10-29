package com.gregtechceu.gtceu.data.loader;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTOreVeins;


import net.minecraft.core.Registry;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;

import org.jetbrains.annotations.NotNullByDefault;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@NotNullByDefault
public class PostRegistryListener extends ContextAwareReloadListener implements PreparableReloadListener {

    public static final PostRegistryListener INSTANCE = new PostRegistryListener();

    private PostRegistryListener() {}

    protected void apply() {
        var server = com.gregtechceu.gtceu.GTCEu.getMinecraftServer();
        if (server == null) return;
        var registry = server.registryAccess().lookupOrThrow(GTRegistries.ORE_VEIN_REGISTRY);

        buildVeinGenerators(registry);
        com.gregtechceu.gtceu.common.data.GTOreVeins.updateLargestVeinSize(registry);
        com.gregtechceu.gtceu.api.data.worldgen.WorldGeneratorUtils.invalidateOreVeinCache();
    }

    public static void buildVeinGenerators(net.minecraft.core.HolderLookup<GTOreDefinition> registry) {
        var iterator = registry.listElements().iterator();
        while (iterator.hasNext()) {
            var definition = iterator.next();
            var veinGen = definition.value().veinGenerator();
            if (veinGen != null && definition.value().canGenerate()) {
                veinGen.build();
            }
        }
    }

    @Override
    public CompletableFuture<Void> reload(SharedState sharedState, Executor backgroundExecutor,
                                          PreparationBarrier stage, Executor gameExecutor) {
        return stage.wait(null).thenRunAsync(this::apply, gameExecutor);
    }
}
