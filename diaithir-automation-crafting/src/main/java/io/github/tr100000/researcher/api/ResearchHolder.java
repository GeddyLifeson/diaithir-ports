package io.github.tr100000.researcher.api;

import com.google.common.collect.ImmutableSet;
import io.github.tr100000.researcher.ModUtils;
import io.github.tr100000.researcher.Research;
import io.github.tr100000.researcher.criterion.ResearchItemsTrigger;
import io.github.tr100000.researcher.graph.ResearchGraph;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public interface ResearchHolder {
    @Contract(pure = true)
    boolean isInitialized();

    @Contract(pure = true)
    Map<Identifier, Research> getAll();

    @Contract(pure = true)
    ResearchGraph getGraph();

    @Contract(value = "null -> null", pure = true)
    @Nullable Research get(@Nullable Identifier id);

    @Contract(value = "null -> null", pure = true)
    @Nullable Identifier getId(@Nullable Research research);

    @Contract(value = "null -> false", pure = true)
    boolean isRecipeUnlockable(@Nullable Identifier recipeId);

    @Contract(pure = true)
    Collection<Identifier> recipeUnlockedBy(Identifier recipeId);

    @Contract(pure = true)
    ResearchItemsTrigger.@Nullable TriggerInstance getResearchConditions(Research research);

    @Contract(pure = true)
    default Collection<Research> listAll() {
        return getAll().values();
    }

    @Contract(pure = true)
    default Set<Research> listAllAsSet() {
        return ImmutableSet.copyOf(getAll().values());
    }

    @Contract(pure = true)
    default Set<Identifier> listAllIds() {
        return getAll().keySet();
    }

    @Contract(pure = true)
    default Identifier getIdOrEmpty(Research research) {
        Identifier id = getId(research);
        if (id == null) id = ModUtils.id("");
        return id;
    }

    @Contract(pure = true)
    default Identifier getIdOrThrow(Research research) {
        Identifier id = getId(research);
        return Objects.requireNonNull(id, "id is null");
    }

    @Contract(pure = true)
    default List<Research> directPredecessorsOf(Research research) {
        return getGraph().predecessors(research);
    }

    @Contract(pure = true)
    default Set<Research> getDepthMapPredecessors(Research research) {
        return new ObjectOpenHashSet<>(directPredecessorsOf(research));
    }

    @Contract(pure = true)
    default Set<Research> allPredecessorsOf(@Nullable Research research) {
        Set<Research> predecessors = new ObjectOpenHashSet<>(getGraph().predecessors(research));
        if (!predecessors.isEmpty()) {
            predecessors.forEach(node -> predecessors.addAll(allPredecessorsOf(node)));
        }
        return predecessors;
    }

    private Map<Research, Integer> predecessorDepthMapImpl(Research research, int depth, Map<Research, Integer> depthMap, Function<Research, Set<Research>> researchToPredecessors) {
        Set<Research> predecessors = new ObjectOpenHashSet<>(researchToPredecessors.apply(research));
        if (!predecessors.isEmpty()) {
            predecessors.forEach(node -> {
                if (!depthMap.containsKey(node) || depthMap.get(node) > depth - 1) {
                    depthMap.put(node, depth - 1);
                }
                predecessorDepthMapImpl(node, depth - 1, depthMap, researchToPredecessors);
            });
        }
        return depthMap;
    }

    @Contract(pure = true)
    default Map<Research, Integer> predecessorDepthMap(Research research) {
        return predecessorDepthMapImpl(research, 0, new Object2IntLinkedOpenHashMap<>(), this::getDepthMapPredecessors);
    }

    @Contract(pure = true)
    default Map<Research, Integer> predecessorDepthMap(Research research, Function<Research, Set<Research>> researchToPredecessors) {
        return predecessorDepthMapImpl(research, 0, new Object2IntLinkedOpenHashMap<>(), researchToPredecessors);
    }

    @Contract(pure = true)
    default List<Research> directSuccessorsOf(Research research) {
        return getGraph().successors(research);
    }

    @Contract(pure = true)
    default Set<Research> getDepthMapSuccessors(Research research) {
        return new ObjectOpenHashSet<>(directSuccessorsOf(research));
    }

    @Contract(pure = true)
    default Set<Research> allSuccessorsOf(Research research) {
        Set<Research> successors = new ObjectOpenHashSet<>(getGraph().successors(research));
        if (!successors.isEmpty()) {
            successors.forEach(node -> successors.addAll(allSuccessorsOf(node)));
        }
        return successors;
    }

    private Map<Research, Integer> successorDepthMapImpl(Research research, int depth, Map<Research, Integer> depthMap, Function<Research, Set<Research>> researchToSuccessors) {
        Set<Research> successors = new ObjectOpenHashSet<>(researchToSuccessors.apply(research));
        if (!successors.isEmpty()) {
            successors.forEach(node -> {
                if (!depthMap.containsKey(node) || depthMap.get(node) < depth + 1) {
                    depthMap.put(node, depth + 1);
                }
                successorDepthMapImpl(node, depth + 1, depthMap, researchToSuccessors);
            });
        }
        return depthMap;
    }

    @Contract(pure = true)
    default Map<Research, Integer> successorDepthMap(Research research) {
        return successorDepthMapImpl(research, 0, new Object2IntLinkedOpenHashMap<>(), this::getDepthMapSuccessors);
    }

    @Contract(pure = true)
    default Map<Research, Integer> successorDepthMap(Research research, Function<Research, Set<Research>> researchToSuccessors) {
        return successorDepthMapImpl(research, 0, new Object2IntLinkedOpenHashMap<>(), researchToSuccessors);
    }

    @Contract(pure = true)
    default Map<Research, Integer> getRelatedMap(Research research) {
        return getRelatedMap(research, this::getDepthMapPredecessors, this::getDepthMapSuccessors);
    }

    @Contract(pure = true)
    default Map<Research, Integer> getRelatedMap(Research research, Function<Research, Set<Research>> researchToPredecessors, Function<Research, Set<Research>> researchToSuccessors) {
        Map<Research, Integer> relatedMap = new Object2IntOpenHashMap<>();

        relatedMap.putAll(predecessorDepthMap(research, researchToPredecessors));
        relatedMap.put(research, 0);
        relatedMap.putAll(successorDepthMap(research, researchToSuccessors));

        return relatedMap;
    }

    @Contract(pure = true)
    default Set<Research> getRootNodes() {
        return new ObjectOpenHashSet<>(getGraph().rootNodes());
    }

    @Contract(pure = true)
    default boolean isValid(Research research) {
        return getGraph().containsNode(research);
    }
}
