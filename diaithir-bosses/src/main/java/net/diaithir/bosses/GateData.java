package net.diaithir.bosses;

import com.mojang.serialization.Codec;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** World state of the Shardfall Gate: which requirements are met and whether the Gate has been opened. */
public final class GateData extends SavedData {
    private record Raw(java.util.List<String> met, long opened) {}

    static final Codec<GateData> CODEC = Codec.pair(Codec.STRING.listOf().fieldOf("met").codec(), Codec.LONG.fieldOf("opened").codec())
        .xmap(p -> new GateData(new LinkedHashSet<>(p.getFirst()), p.getSecond()), d -> com.mojang.datafixers.util.Pair.of(java.util.List.copyOf(d.met), d.opened));
    static final SavedDataType<GateData> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath(DiaithirBosses.ID, "gate"), GateData::new, CODEC);

    private final Set<String> met = new LinkedHashSet<>();
    private long opened = -1;

    public GateData() {}

    private GateData(Set<String> met, long opened) { this.met.addAll(met); this.opened = opened; }

    public static GateData get(MinecraftServer server) { return server.overworld().getDataStorage().computeIfAbsent(TYPE); }

    public boolean grant(String id) { boolean n = met.add(id); if (n) setDirty(); return n; }
    public boolean revoke(String id) { boolean n = met.remove(id); if (n) setDirty(); return n; }
    public boolean has(String id) { return met.contains(id); }
    public Set<String> all() { return Set.copyOf(met); }
    public boolean isOpen() { return opened >= 0; }
    public long openedAt() { return opened; }
    public void open(long time) { opened = time; setDirty(); }
    /** Test hook: back to a closed Gate with nothing met. */
    void reset() { met.clear(); opened = -1; setDirty(); }
}
