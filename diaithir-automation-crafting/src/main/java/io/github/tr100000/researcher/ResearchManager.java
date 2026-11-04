package io.github.tr100000.researcher;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.JsonOps;
import io.github.tr100000.researcher.api.ResearchHolder;
import io.github.tr100000.researcher.config.ResearcherConfigs;
import io.github.tr100000.researcher.criterion.ResearchItemsTrigger;
import io.github.tr100000.researcher.graph.ResearchGraph;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.LevelResource;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class ResearchManager extends SimpleJsonResourceReloadListener<Research> implements ResearchHolder {
    public static final String PATH = "research";
    public static final Identifier ID = ModUtils.id(PATH);
    public static final StateKey<ResearchManager> STATE_KEY = new StateKey<>();
    private final ReloadableServerResources parent;
    private final BiMap<Identifier, Research> researchMap = HashBiMap.create();
    private final Set<Identifier> unlockableRecipes = new ObjectOpenHashSet<>();
    private final Map<Identifier, Set<Identifier>> recipeUnlockedByMap = new Object2ObjectOpenHashMap<>();
    private final Map<Research, ResearchItemsTrigger.TriggerInstance> researchConditions = new Object2ObjectOpenHashMap<>();
    private @Nullable ResearchGraph graph;

    public static final LevelResource WORLD_SAVE_PATH = new LevelResource(PATH);

    public ResearchManager(HolderLookup.Provider lookup, ReloadableServerResources parent) {
        super(lookup.createSerializationContext(JsonOps.INSTANCE), Research.CODEC, FileToIdConverter.json(PATH));
        this.parent = parent;
    }

    @Override
    public void prepareSharedState(SharedState currentReload) {
        currentReload.set(STATE_KEY, this);
    }

    @Override
    protected void apply(Map<Identifier, Research> prepared, ResourceManager manager, ProfilerFiller profiler) {
        researchMap.clear();
        unlockableRecipes.clear();
        recipeUnlockedByMap.clear();
        researchConditions.clear();
        graph = null;

        LockableRecipeTypesList.reload(manager);

        final float researchCostMultiplier = ResearcherConfigs.server.researchCostMultiplier.get().floatValue();
        prepared.forEach((id, entry) -> {
            if (researchCostMultiplier > 1) {
                int cost = Math.max(1, (int)Math.ceil(entry.trigger().count() * researchCostMultiplier));
                entry = new Research(entry.titleText(), entry.descriptionText(), new ResearchCriterion<>(entry.trigger().criterion(), cost), entry.prerequisiteIds(), entry.recipeUnlocks(), entry.rewards(), entry.display());
            }
            researchMap.put(id, entry);
            entry.recipeUnlocks().forEach(unlock -> parent.getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, unlock)).ifPresentOrElse(
                    recipe -> {
                        if (LockableRecipeTypesList.getTypes().contains(recipe.value().getSerializer())) {
                            unlockableRecipes.add(unlock);
                            recipeUnlockedByMap.computeIfAbsent(recipe.id().identifier(), _ -> new ObjectAVLTreeSet<>()).add(id);
                        }
                        else {
                            Researcher.LOGGER.warn("Recipe {} of type {} is not lockable!", unlock, BuiltInRegistries.RECIPE_TYPE.getKey(recipe.value().getType()));
                        }
                    },
                    () -> Researcher.LOGGER.warn("Recipe with id {} doesn't exist!", unlock)
            ));
            if (entry.getConditions() instanceof ResearchItemsTrigger.TriggerInstance triggerInstance) {
                researchConditions.put(entry, triggerInstance);
            }
        });

        graph = ResearchGraph.build(researchMap);
        Researcher.LOGGER.info("Loaded {} researches", researchMap.size());
    }

    @Override
    public boolean isInitialized() {
        return graph != null;
    }

    public Map<Identifier, Research> getAll() {
        return researchMap;
    }

    public ResearchGraph getGraph() {
        return graph != null ? graph : ResearchGraph.EMPTY;
    }

    public @Nullable Research get(@Nullable Identifier id) {
        if (id == null) return null;
        return researchMap.get(id);
    }

    public @Nullable Identifier getId(@Nullable Research research) {
        if (research == null) return null;
        return researchMap.inverse().get(research);
    }

    public ResearchItemsTrigger.@Nullable TriggerInstance getResearchConditions(Research research) {
        return researchConditions.get(research);
    }

    public boolean isRecipeUnlockable(@Nullable Identifier recipeId) {
        return unlockableRecipes.contains(recipeId);
    }

    @Override
    public Collection<Identifier> recipeUnlockedBy(Identifier recipeId) {
        return recipeUnlockedByMap.getOrDefault(recipeId, Collections.emptySet());
    }
}
