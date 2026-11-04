package net.diaithir.worldsys.rankperms;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.resources.Identifier;
import net.minecraft.core.UUIDUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Persistent ranks and player assignments. */
public final class RankData extends SavedData {
    private static final Codec<RankData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Rank.CODEC.listOf().fieldOf("ranks").forGetter(d -> new ArrayList<>(d.ranks.values())),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("players").forGetter(d -> {
                Map<String, String> m = new HashMap<>();
                d.players.forEach((k, v) -> m.put(k.toString(), v));
                return m;
            })
    ).apply(i, RankData::new));

    public static final SavedDataType<RankData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("diaithirworldsys", "ranks"), RankData::new, CODEC, null);

    private final Map<String, Rank> ranks = new LinkedHashMap<>();
    private final Map<UUID, String> players = new HashMap<>();

    public RankData() {
    }

    private RankData(List<Rank> rs, Map<String, String> ps) {
        rs.forEach(r -> ranks.put(r.name(), r));
        ps.forEach((k, v) -> players.put(UUID.fromString(k), v));
    }

    public static RankData get(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    public Map<String, Rank> ranks() {
        return ranks;
    }

    public Rank rankOf(UUID id) {
        String n = players.get(id);
        return n == null ? null : ranks.get(n);
    }

    public void put(Rank r) {
        ranks.put(r.name(), r);
        setDirty();
    }

    public void remove(String name) {
        ranks.remove(name);
        players.values().removeIf(name::equals);
        setDirty();
    }

    public void assign(UUID id, String name) {
        if (name == null) players.remove(id);
        else players.put(id, name);
        setDirty();
    }
}
