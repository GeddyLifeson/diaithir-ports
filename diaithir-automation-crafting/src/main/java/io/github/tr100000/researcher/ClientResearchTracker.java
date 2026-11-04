package io.github.tr100000.researcher;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.github.tr100000.researcher.api.PlayerResearchHolder;
import io.github.tr100000.researcher.api.ResearchHolder;
import io.github.tr100000.researcher.api.ResearcherClientEvents;
import io.github.tr100000.researcher.config.ResearcherConfigs;
import io.github.tr100000.researcher.criterion.ResearchItemsTrigger;
import io.github.tr100000.researcher.graph.ResearchGraph;
import io.github.tr100000.researcher.networking.ResearchUpdateS2CPacket;
import io.github.tr100000.researcher.networking.StartResearchC2SPacket;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@NullMarked
public class ClientResearchTracker implements ResearchHolder, PlayerResearchHolder {
    private static final Minecraft client = Minecraft.getInstance();

    private final Map<Research, ResearchProgress> progressMap = new Object2ObjectOpenHashMap<>();
    private @Nullable Identifier currentResearching;
    private List<Identifier> pinnedResearches = List.of();

    private final BiMap<Identifier, Research> researchMap = HashBiMap.create();
    private final Set<Identifier> unlockableRecipes = new ObjectOpenHashSet<>();
    private final Map<Identifier, Set<Identifier>> recipeUnlockedByMap = new Object2ObjectOpenHashMap<>();
    private final Map<Research, ResearchItemsTrigger.TriggerInstance> researchConditions = new Object2ObjectOpenHashMap<>();
    private @Nullable ResearchGraph graph;

    public ClientResearchTracker() {
        if (ResearcherConfigs.client.pinAvailableResearches.get()) {
            pinAvailableResearches();
        }
    }

    @ApiStatus.Internal
    public void handleUpdate(ResearchUpdateS2CPacket packet) {
        if (packet.clearCurrent()) {
            Researcher.LOGGER.info("Loaded {} researches on client", packet.loadedResearch().size());
            progressMap.clear();
            researchMap.clear();
            unlockableRecipes.clear();
            recipeUnlockedByMap.clear();
            researchConditions.clear();
            graph = null;
        }

        putAllResearch(packet.loadedResearch());
        AtomicBoolean shouldPinAvailable = new AtomicBoolean(false);
        packet.progressMap().forEach((id, progress) -> {
            Research research = get(id);
            progressMap.put(research, progress);
            if (!packet.clearCurrent() && progress.isFinished()) {
                client.getToastManager().addToast(new ResearchToast(research));
                ResearcherClientEvents.RESEARCH_FINISHED.invoker().onResearchFinished(this, research);
                shouldPinAvailable.set(true);
            }
            if (!progress.hasProgress()) {
                shouldPinAvailable.set(true);
            }
        });
        currentResearching = packet.currentResearching().orElse(null);
        pinnedResearches = packet.pinnedResearches();

        if (shouldPinAvailable.get() || packet.clearCurrent()) {
            pinAvailableResearches();
        }
    }

    private void putAllResearch(Map<Identifier, Research> prepared) {
        if (prepared.isEmpty()) return;
        prepared.forEach((id, entry) -> {
            researchMap.put(id, entry);
            entry.recipeUnlocks().forEach(unlockedRecipe -> {
                unlockableRecipes.add(unlockedRecipe);
                recipeUnlockedByMap.computeIfAbsent(unlockedRecipe, _ -> new ObjectAVLTreeSet<>()).add(id);
            });
            unlockableRecipes.addAll(entry.recipeUnlocks());
            if (entry.getConditions() instanceof ResearchItemsTrigger.TriggerInstance triggerInstance) {
                researchConditions.put(entry, triggerInstance);
            }
        });
        graph = ResearchGraph.build(researchMap);
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

    public boolean isRecipeUnlockable(@Nullable Identifier recipeId) {
        return unlockableRecipes.contains(recipeId);
    }

    @Override
    public Collection<Identifier> recipeUnlockedBy(Identifier recipeId) {
        return recipeUnlockedByMap.getOrDefault(recipeId, Collections.emptySet());
    }

    public ResearchItemsTrigger.@Nullable TriggerInstance getResearchConditions(Research research) {
        return researchConditions.get(research);
    }

    public @Nullable Identifier getCurrentResearchingId() {
        return currentResearching;
    }

    public @Nullable Research getCurrentResearching() {
        return get(currentResearching);
    }

    public List<Identifier> getPinnedResearches() {
        return pinnedResearches;
    }

    public boolean isCurrentOrPinned(Research research) {
        return getCurrentResearching() == research || pinnedResearches.contains(getId(research));
    }

    public boolean canResearch(@Nullable Research research) {
        if (research == null) return false;
        if (hasFinished(research)) return false;
        return research.prerequisites(this).stream().allMatch(this::hasFinished);
    }

    public boolean isAvailableOrFinished(Research research) {
        return hasFinished(research) || canResearch(research);
    }

    public Set<Research> getResearchableSuccessors(Research research) {
        return graph.successors(research).stream().filter(this::isAvailableOrFinished).collect(Collectors.toCollection(ObjectOpenHashSet::new));
    }

    public boolean canCraftRecipe(@Nullable Identifier recipeId) {
        return !isRecipeUnlockable(recipeId) || hasUnlockedRecipe(recipeId);
    }

    public boolean hasUnlockedRecipe(Identifier recipeId) {
        for (Map.Entry<Research, ResearchProgress> entry : progressMap.entrySet()) {
            if (entry.getValue().isFinished() && entry.getKey().recipeUnlocks().contains(recipeId)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasFinished(@Nullable Research research) {
        if (research == null) return false;
        return getProgress(research).isFinished();
    }

    public ResearchProgress getProgress(Research research) {
        return progressMap.computeIfAbsent(research, r -> new ResearchProgress());
    }

    public void pinAvailableResearches() {
        if (!ResearcherConfigs.client.pinAvailableResearches.get()) return;
        researchMap.values().forEach(research -> {
            if (getResearchConditions(research) == null && canResearch(research) && !pinnedResearches.contains(getId(research))) {
                ClientPacketDistributor.sendToServer(new StartResearchC2SPacket(StartResearchC2SPacket.Mode.PIN, Optional.ofNullable(getId(research))));
                Researcher.LOGGER.info("pinned research {}", getId(research));
            }
        });
    }

    public Component getTitleWithStatus(Research research) {
        ResearchProgress progress = getProgress(research);
        String titleKey;
        float progressPercentage = 0;
        if (progress.isFinished()) {
            titleKey = "finished";
            progressPercentage = 1;
        }
        else if (canResearch(research)) {
            titleKey = "available";
            if (progress.hasProgress()) {
                titleKey = "progress";
                progressPercentage = progress.getProgress01(research.trigger().count());
            }
        }
        else {
            titleKey = "locked";
        }
        return ModUtils.getScreenTranslated(titleKey, research.getTitle(this).getString(), (int)Math.floor(progressPercentage * 100));
    }
}
