package net.diaithir.classes.tree;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.diaithir.classes.DiaithirClasses;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Per-player class, spendable points and unlocked nodes. */
public final class TreeData extends SavedData {
    public static final class Entry {
        private String cls;
        private int points;
        private final Set<String> unlocked = new LinkedHashSet<>();
        public String cls() { return cls; }
        public int points() { return points; }
        public Set<String> unlocked() { return unlocked; }
    }

    private record Raw(Optional<String> cls, int points, List<String> unlocked) {
        static final Codec<Raw> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.optionalFieldOf("cls").forGetter(Raw::cls),
            Codec.INT.fieldOf("points").forGetter(Raw::points),
            Codec.STRING.listOf().fieldOf("unlocked").forGetter(Raw::unlocked)).apply(i, Raw::new));
    }

    static final Codec<TreeData> CODEC = Codec.unboundedMap(Codec.STRING, Raw.CODEC).fieldOf("players").codec().xmap(m -> {
        TreeData d = new TreeData();
        m.forEach((k, r) -> { Entry e = new Entry(); e.cls = r.cls().orElse(null); e.points = r.points(); e.unlocked.addAll(r.unlocked()); d.map.put(UUID.fromString(k), e); });
        return d;
    }, d -> {
        Map<String, Raw> out = new HashMap<>();
        d.map.forEach((k, e) -> out.put(k.toString(), new Raw(Optional.ofNullable(e.cls), e.points, new ArrayList<>(e.unlocked))));
        return out;
    });
    static final SavedDataType<TreeData> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath(DiaithirClasses.ID, "skill_trees"), TreeData::new, CODEC);

    private final Map<UUID, Entry> map = new HashMap<>();

    public TreeData() {}

    public static TreeData get(MinecraftServer s) { return s.overworld().getDataStorage().computeIfAbsent(TYPE); }

    public Entry entry(UUID id) { return map.computeIfAbsent(id, k -> new Entry()); }

    /** Pick or change class: unlocked nodes are refunded. */
    public void choose(UUID id, String cls) {
        Entry e = entry(id);
        refund(e);
        e.cls = cls;
        setDirty();
    }

    private void refund(Entry e) {
        for (String n : e.unlocked) { var node = e.cls == null ? null : Trees.node(e.cls, n); if (node != null) e.points += node.cost(); }
        e.unlocked.clear();
    }

    public void reset(UUID id) { refund(entry(id)); setDirty(); }
    public void addPoints(UUID id, int n) { entry(id).points += n; setDirty(); }

    public boolean unlock(UUID id, String node) {
        Entry e = entry(id);
        Trees.Node n = Trees.node(e.cls == null ? "" : e.cls, node);
        if (Trees.whyNot(e, n) != null) return false;
        e.points -= n.cost();
        e.unlocked.add(n.id());
        setDirty();
        return true;
    }
}
