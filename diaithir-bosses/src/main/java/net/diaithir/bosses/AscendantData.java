package net.diaithir.bosses;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** World state of the Ascendant tier: difficulty mode (only ever raised), bosses beaten (with the game time of the last win), best rush time. */
public final class AscendantData extends SavedData {
    private record Raw(String mode, long best, Map<String, Long> defeated, Map<String, String> rituals) {}

    static final Codec<AscendantData> CODEC = RecordCodecBuilder.<Raw>create(i -> i.group(
        Codec.STRING.fieldOf("mode").forGetter(Raw::mode),
        Codec.LONG.fieldOf("best").forGetter(Raw::best),
        Codec.unboundedMap(Codec.STRING, Codec.LONG).fieldOf("defeated").forGetter(Raw::defeated),
        Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("rituals").forGetter(Raw::rituals)).apply(i, Raw::new))
        .xmap(r -> new AscendantData(r.mode(), r.best(), r.defeated(), r.rituals()), d -> new Raw(d.mode, d.best, d.defeated, d.rituals));
    static final SavedDataType<AscendantData> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath(DiaithirBosses.ID, "ascendant"), AscendantData::new, CODEC);

    private String mode = "standard";
    private long best = -1;
    private final Map<String, Long> defeated = new LinkedHashMap<>();
    /** Rites in progress, by altar position x,y,z; value boss|step|progress. */
    private final Map<String, String> rituals = new LinkedHashMap<>();

    public AscendantData() {}

    private AscendantData(String mode, long best, Map<String, Long> defeated, Map<String, String> rituals) {
        this.mode = mode;
        this.best = best;
        this.defeated.putAll(defeated);
        this.rituals.putAll(rituals);
    }

    public static AscendantData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public String mode() { return mode; }

    /** Raises the mode; returns false if the mode is unknown or not above the current one. */
    public boolean raise(String id) {
        if (Difficulty.rank(id) <= Difficulty.rank(mode)) return false;
        mode = id;
        setDirty();
        return true;
    }

    public void defeat(String boss, long time) {
        defeated.put(boss, time);
        setDirty();
    }

    /** Test hook: forget every win. */
    void clearDefeats() {
        defeated.clear();
        setDirty();
    }

    public boolean beaten(String boss) { return defeated.containsKey(boss); }
    public long lastDefeat(String boss) { return defeated.getOrDefault(boss, -1L); }
    public long best() { return best; }

    public boolean record(long ticks) {
        if (best >= 0 && ticks >= best) return false;
        best = ticks;
        setDirty();
        return true;
    }

    public Map<String, String> rituals() { return rituals; }

    public void putRitual(String key, String value) {
        rituals.put(key, value);
        setDirty();
    }

    public void removeRitual(String key) {
        if (rituals.remove(key) != null) setDirty();
    }
}
