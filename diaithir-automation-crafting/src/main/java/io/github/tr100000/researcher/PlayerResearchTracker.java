package io.github.tr100000.researcher;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tr100000.researcher.api.PlayerResearchHolder;
import io.github.tr100000.researcher.api.ResearcherEvents;
import io.github.tr100000.researcher.networking.ResearchUpdateS2CPacket;
import io.github.tr100000.researcher.networking.StartResearchC2SPacket;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.StrictJsonParser;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PlayerResearchTracker implements PlayerResearchHolder {
    private static final Gson GSON = Researcher.GSON;

    private ResearchManager researchManager;
    private final PlayerList playerManager;
    private final Path filePath;

    private PlayerAdvancements playerAdvancements;
    private ServerPlayer owner;
    private boolean dirty = true;
    private boolean forceNextUpdate = false;
    private boolean hasStartedTracking;

    private final Map<Research, ResearchProgress> progressMap = new Object2ObjectOpenHashMap<>();
    private final Set<Research> progressUpdates = new ObjectOpenHashSet<>();
    private @Nullable Identifier currentResearching;
    private List<Identifier> pinnedResearches = new ObjectArrayList<>();
    private final Set<PlayerResearchTracker> syncedTrackers = new ObjectOpenHashSet<>();

    public PlayerResearchTracker(ServerPlayer player, ResearchManager researchManager, PlayerList playerManager, Path filePath) {
        this.researchManager = researchManager;
        this.playerManager = playerManager;
        this.filePath = filePath;
        this.owner = player;
        this.playerAdvancements = player.getAdvancements();
        load();
        beginTrackingAll();
        Researcher.LOGGER.info("Initialized research tracker for {}", player.getName().getString());
    }

    public void setOwner(ServerPlayer owner) {
        this.owner = owner;
        this.playerAdvancements = owner.getAdvancements();
    }

    public ServerPlayer getOwner() {
        return owner;
    }

    public ResearchManager parent() {
        return researchManager;
    }

    private void load() {
        if (Files.isRegularFile(filePath)) {
            try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
                JsonElement json = StrictJsonParser.parse(reader);
                Data result = Data.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(JsonParseException::new);
                loadData(result);
            }
            catch (Exception e) {
                Researcher.LOGGER.error("Couldn't load player research data, creating empty!", e);
                loadData(Data.createEmpty());
            }
        }
        else {
            Researcher.LOGGER.error("Path {} was not a regular file, creating empty!", filePath);
            loadData(Data.createEmpty());
        }
    }

    public void reload(ResearchManager researchManager) {
        endTrackingAll();
        this.researchManager = researchManager;
        progressMap.clear();
        currentResearching = null;
        dirty = true;
        load();
        beginTrackingAll();
    }

    public void save(ResearchManager researchManager) {
        this.researchManager = researchManager;
        try {
            JsonElement data = Data.CODEC.encodeStart(JsonOps.INSTANCE, getData()).getOrThrow();
            Files.createDirectories(filePath.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                GSON.toJson(data, GSON.newJsonWriter(writer));
            }
        }
        catch (Exception e) {
            Researcher.LOGGER.error("Couldn't save research data to '{}'!", filePath, e);
        }
    }

    private void loadData(Data data) {
        data.progressMap.forEach((id, progress) -> {
            Research research = researchManager.get(id);
            if (research != null) {
                this.progressMap.put(research, progress);
                progressUpdates.add(research);
            }
            else {
                Researcher.LOGGER.warn("Ignored research '{}' in data file '{}' because it doesn't exist", id, filePath);
            }
        });
        this.currentResearching = data.currentResearching.orElse(null);
        this.pinnedResearches = new ObjectArrayList<>(data.pinnedResearches);
        removeUnresearchablePinned();
    }

    private Data getData() {
        Map<Identifier, ResearchProgress> progressMapById = new Object2ObjectOpenHashMap<>();
        progressMap.forEach((research, progress) -> {
            if (progress.hasProgress()) {
                progressMapById.put(researchManager.getIdOrThrow(research), progress);
            }
        });
        return new Data(progressMapById, Optional.ofNullable(currentResearching), pinnedResearches);
    }

    private void beginTrackingAll() {
        if (!hasStartedTracking) {
            researchManager.listAll().forEach(research -> {
                if (!getProgress(research).isFinished()) {
                    beginTracking(research);
                }
            });
            hasStartedTracking = true;
        }
    }

    @SuppressWarnings("unchecked")
    private void beginTracking(Research research) {
        Researcher.LOGGER.debug("Begin tracking research {}", researchManager.getId(research));
        research.getTrigger().addPlayerListener(playerAdvancements, getConditionsContainer(research));
    }

    private void endTrackingAll() {
        researchManager.listAll().forEach(this::endTracking);
    }

    @SuppressWarnings("unchecked")
    private void endTracking(Research research) {
        Researcher.LOGGER.debug("End tracking research {}", researchManager.getId(research));
        research.getTrigger().removePlayerListener(playerAdvancements, getConditionsContainer(research));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private CriterionTrigger.Listener getConditionsContainer(Research research) {
        return new CriterionTrigger.Listener(research.getConditions(), null, researchManager.getId(research).toString());
    }

    public void syncWith(PlayerResearchTracker other) {
        if (other == this) return;
        this.progressMap.forEach(other::combineProgress);
        this.syncedTrackers.add(other);
        this.dirty = true;

        other.progressMap.forEach(this::combineProgress);
        other.syncedTrackers.add(this);
        other.dirty = true;
    }

    private void combineProgress(Research research, ResearchProgress otherProgress) {
        ResearchProgress ourProgress = progressMap.get(research);
        progressMap.put(research, ResearchProgress.combine(ourProgress, otherProgress));

        if (!ourProgress.isFinished() && otherProgress.isFinished()) {
            // This prevents things like advancements not triggering when syncing settings are changed
            ResearcherCriteriaTriggers.HAS_RESEARCH.trigger(owner, researchManager.getIdOrThrow(research));
        }
    }

    public void stopSyncWith(PlayerResearchTracker other) {
        syncedTrackers.remove(other);
        other.syncedTrackers.remove(this);
    }

    public ResearchProgress getProgress(Research research) {
        return progressMap.computeIfAbsent(research, _ -> new ResearchProgress());
    }

    private void updateProgressWithoutSync(Research research, ResearchProgress progress) {
        progressMap.put(research, progress);
        progressUpdates.add(research);
    }

    public void syncProgress(Research research, ResearchProgress progress) {
        updateProgressWithoutSync(research, progress);
        syncedTrackers.forEach(tracker -> tracker.updateProgressWithoutSync(research, progress));
    }

    public boolean grantCriterion(Research research) {
        return grantCriterion(research, true);
    }

    private boolean grantCriterion(Research research, boolean sync) {
        ResearchProgress progress = getProgress(research);
        if (canResearch(research)) {
            progress.finish();
            onResearchFinished(research);
            if (sync) {
                syncedTrackers.forEach(tracker -> tracker.grantCriterion(research, false));
            }
            return true;
        }
        else {
            return false;
        }
    }

    private void onResearchFinished(Research research) {
        Identifier researchId = researchManager.getIdOrThrow(research);

        progressUpdates.add(research);
        endTracking(research);
        playerManager.broadcastSystemMessage(research.getChatAnnouncementText(researchManager, owner), false);
        if (researchId.equals(currentResearching)) {
            currentResearching = null;
        }
        pinnedResearches.remove(researchId);

        research.rewards().forEach(r -> r.grant(owner, research));

        ResearcherCriteriaTriggers.HAS_RESEARCH.trigger(owner, researchId);
        ResearcherEvents.RESEARCH_FINISHED.invoker().onResearchFinished(this, research);
    }

    public boolean incrementCriterion(Research research, int count) {
        return incrementCriterion(research, count, true);
    }

    private boolean incrementCriterion(Research research, int count, boolean sync) {
        ResearchProgress progress = getProgress(research);
        if (canResearch(research)) {
            progress.increment(research.trigger().count(), count);
            if (progress.isFinished()) {
                onResearchFinished(research);
                if (sync) {
                    syncedTrackers.forEach(tracker -> tracker.incrementCriterion(research, count, false));
                }
            }
            else if (sync) {
                syncProgress(research, progress);
            }
            return true;
        }
        else {
            return false;
        }
    }

    public boolean incrementCriterion(String id) {
        return incrementCriterion(researchManager.get(Identifier.parse(id)), 1);
    }

    public boolean revokeCriterion(Research research) {
        return revokeCriterion(research, true);
    }

    private boolean revokeCriterion(Research research, boolean sync) {
        ResearchProgress progress = getProgress(research);
        boolean wasFinished = progress.isFinished();
        if (progress.hasProgress()) {
            progress.reset();
            progressUpdates.add(research);
            pinnedResearches.remove(researchManager.getId(research));
            if (wasFinished) {
                beginTracking(research);
            }
            if (sync) {
                syncedTrackers.forEach(tracker -> tracker.revokeCriterion(research, false));
            }
            return true;
        }
        else {
            return false;
        }
    }

    public void sendUpdate() {
        if (dirty || !progressUpdates.isEmpty() || forceNextUpdate) {
            Map<Identifier, ResearchProgress> updatedProgressMap = new HashMap<>();
            progressUpdates.forEach(research -> updatedProgressMap.put(researchManager.getIdOrThrow(research), getProgress(research)));
            removeUnresearchablePinned();

            progressUpdates.clear();
            if (dirty || !updatedProgressMap.isEmpty() || forceNextUpdate) {
                Researcher.LOGGER.info("Sending research update to {}", owner.getName().getString());
                PacketDistributor.sendToPlayer(owner, new ResearchUpdateS2CPacket(
                        dirty,
                        updatedProgressMap,
                        dirty ? researchManager.getAll() : Collections.emptyMap(),
                        Optional.ofNullable(currentResearching),
                        pinnedResearches)
                );
            }

            dirty = false;
            forceNextUpdate = false;
        }
    }

    private void removeUnresearchablePinned() {
        pinnedResearches.removeIf(r -> !canResearch(parent().get(r)));
    }

    @ApiStatus.Internal
    public void handleUpdate(StartResearchC2SPacket payload) {
        switch (payload.mode()) {
            case SET_CURRENT -> setCurrentResearching(payload.researchId().orElse(null));
            case PIN -> payload.researchId().ifPresent(this::pinResearch);
            case UNPIN -> payload.researchId().ifPresent(this::unpinResearch);
            case GRANT -> {
                if (owner.hasInfiniteMaterials()) {
                    payload.researchId().ifPresent(id -> grantCriterion(researchManager.get(id)));
                }
            }
            case REVOKE -> {
                if (owner.hasInfiniteMaterials()) {
                    payload.researchId().ifPresent(id -> revokeCriterion(researchManager.get(id)));
                }
            }
        }
    }

    public @Nullable Identifier getCurrentResearchingId() {
        return currentResearching;
    }

    public @Nullable Research getCurrentResearching() {
        return researchManager.get(currentResearching);
    }

    public void setCurrentResearching(@Nullable Identifier researchId) {
        setCurrentResearching(researchId, true);
    }

    private void setCurrentResearching(@Nullable Identifier researchId, boolean sync) {
        if (researchId != null && !canResearch(researchManager.get(researchId))) return;
        this.currentResearching = researchId;
        forceNextUpdate = true;
        if (sync) {
            syncedTrackers.forEach(tracker -> tracker.setCurrentResearching(researchId, false));
        }
    }

    public List<Identifier> getPinnedResearches() {
        return pinnedResearches;
    }

    public void pinResearch(Identifier id) {
        if (!canResearch(researchManager.get(id)) || pinnedResearches.contains(id)) return;
        pinnedResearches.add(id);
        forceNextUpdate = true;
    }

    public void unpinResearch(Identifier id) {
        if (pinnedResearches.remove(id)) {
            forceNextUpdate = true;
        }
    }

    public boolean canResearch(@Nullable Research research) {
        if (research == null || hasFinished(research)) return false;
        return research.prerequisites(researchManager).stream().allMatch(this::hasFinished);
    }

    public boolean hasFinished(@Nullable Research research) {
        if (research == null) return false;
        return getProgress(research).isFinished();
    }

    public boolean canCraftRecipe(@Nullable Identifier recipeId) {
        if (!researchManager.isRecipeUnlockable(recipeId)) return true;
        for (Map.Entry<Research, ResearchProgress> entry : progressMap.entrySet()) {
            if (entry.getValue().isFinished() && entry.getKey().recipeUnlocks().contains(recipeId)) {
                return true;
            }
        }
        return false;
    }

    public record Data(Map<Identifier, ResearchProgress> progressMap, Optional<Identifier> currentResearching, List<Identifier> pinnedResearches) {
        public static final Codec<Map<Identifier, ResearchProgress>> PROGRESS_MAP_CODEC = Codec.unboundedMap(Identifier.CODEC, ResearchProgress.CODEC);
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        PROGRESS_MAP_CODEC.fieldOf("progress").forGetter(Data::progressMap),
                        Identifier.CODEC.optionalFieldOf("researching").forGetter(Data::currentResearching),
                        Identifier.CODEC.listOf().fieldOf("pinned").forGetter(Data::pinnedResearches)
                ).apply(instance, Data::new)
        );

        public static Data createEmpty() {
            return new Data(new HashMap<>(), Optional.empty(), new ArrayList<>());
        }
    }
}
