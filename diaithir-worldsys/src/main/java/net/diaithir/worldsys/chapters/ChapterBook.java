package net.diaithir.worldsys.chapters;

import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * All loaded stages. A subject (item, recipe, dimension) is locked when at least one stage defines it and the
 * player owns none of the stages that define it. Owning any one defining stage unlocks it.
 */
public final class ChapterBook {
    private Map<Identifier, ChapterStage> stages = new LinkedHashMap<>();

    public void replace(Collection<ChapterStage> next) {
        Map<Identifier, ChapterStage> map = new LinkedHashMap<>();
        for (ChapterStage s : next) map.put(s.id(), s);
        stages = map;
    }

    public Set<Identifier> stageIds() { return stages.keySet(); }

    public boolean known(Identifier id) { return stages.containsKey(id); }

    public static boolean isLocked(Set<Identifier> defining, Set<Identifier> owned) {
        if (defining.isEmpty()) return false;
        for (Identifier s : defining) if (owned.contains(s)) return false;
        return true;
    }

    /** Stages whose {@code items} list matches the item id, any of its tags, or its namespace. */
    public Set<Identifier> definingItem(Identifier item, Collection<Identifier> itemTags) {
        Set<Identifier> out = new LinkedHashSet<>();
        for (ChapterStage s : stages.values()) {
            for (String e : s.items()) {
                if (e.startsWith("@")) {
                    if (e.substring(1).equals(item.getNamespace())) out.add(s.id());
                } else if (e.startsWith("#")) {
                    for (Identifier t : itemTags) if (e.substring(1).equals(t.toString())) out.add(s.id());
                } else if (e.equals(item.toString())) out.add(s.id());
            }
        }
        return out;
    }

    public Set<Identifier> definingRecipe(Identifier recipe) { return defining(recipe, true); }

    public Set<Identifier> definingDimension(Identifier dim) { return defining(dim, false); }

    private Set<Identifier> defining(Identifier id, boolean recipe) {
        Set<Identifier> out = new LinkedHashSet<>();
        for (ChapterStage s : stages.values()) {
            for (String e : recipe ? s.recipes() : s.dimensions()) {
                if (e.startsWith("@") ? e.substring(1).equals(id.getNamespace()) : e.equals(id.toString())) out.add(s.id());
            }
        }
        return out;
    }
}
