package net.diaithir.automationcrafting.packages;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.diaithir.automationcrafting.DiaithirAutomationCrafting;

/** Which packages have been researched. Shared by the whole server, like a Factorio team: the first Researcher to finish unlocks it for everyone. */
public final class ResearchState extends SavedData {
    static final Codec<ResearchState> CODEC = Codec.STRING.listOf().xmap(ResearchState::new, s -> new ArrayList<>(s.done));
    static final SavedDataType<ResearchState> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath(DiaithirAutomationCrafting.ID, "research"), ResearchState::new, CODEC);

    private final Set<String> done = new LinkedHashSet<>();

    private ResearchState() {}

    private ResearchState(List<String> saved) {
        done.addAll(saved);
    }

    public static ResearchState get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean has(String id) {
        return done.contains(id);
    }

    public boolean set(String id, boolean value) {
        boolean changed = value ? done.add(id) : done.remove(id);
        if (changed) setDirty();
        return changed;
    }

    /** True when the package exists and every prerequisite is researched. */
    public boolean available(PackageDef d) {
        for (String p : d.prereqs()) if (!done.contains(p)) return false;
        return true;
    }

    public Set<String> all() {
        return done;
    }
}
