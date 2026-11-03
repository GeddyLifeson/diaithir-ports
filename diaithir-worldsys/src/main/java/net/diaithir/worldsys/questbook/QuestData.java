package net.diaithir.worldsys.questbook;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.resources.Identifier;

/** Persistent team membership and per-owner quest progress. Owner key is a team name or a player uuid string. */
public final class QuestData extends SavedData {
    private static final Codec<Map<String, Map<String, Integer>>> PROGRESS = Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Codec.STRING, Codec.INT));
    private static final Codec<Map<String, String>> TEAMS = Codec.unboundedMap(Codec.STRING, Codec.STRING);

    public static final SavedDataType<QuestData> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath("diaithirworldsys", "questbook"), QuestData::new,
            RecordCodecBuilder.create(i -> i.group(
                    PROGRESS.fieldOf("progress").forGetter(d -> d.progress),
                    TEAMS.fieldOf("teams").forGetter(d -> d.teams)).apply(i, QuestData::new)),
            DataFixTypes.LEVEL);

    private final Map<String, Map<String, Integer>> progress = new HashMap<>();
    private final Map<String, String> teams = new HashMap<>();

    public QuestData() {}

    private QuestData(Map<String, Map<String, Integer>> p, Map<String, String> t) {
        p.forEach((k, v) -> progress.put(k, new HashMap<>(v)));
        teams.putAll(t);
    }

    public static QuestData get(MinecraftServer server) {
        return server.getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(TYPE);
    }

    public String ownerOf(UUID player) {
        String team = teams.get(player.toString());
        return team == null ? player.toString() : "team:" + team;
    }

    public String teamOf(UUID player) {
        return teams.get(player.toString());
    }

    public boolean teamExists(String team) {
        return teams.containsValue(team);
    }

    public void setTeam(UUID player, String team) {
        if (team == null) teams.remove(player.toString());
        else teams.put(player.toString(), team);
        setDirty();
    }

    public Set<UUID> members(String team) {
        Set<UUID> out = new HashSet<>();
        teams.forEach((p, t) -> {
            if (t.equals(team)) out.add(UUID.fromString(p));
        });
        return out;
    }

    public int get(String owner, String quest) {
        return progress.getOrDefault(owner, Map.of()).getOrDefault(quest, 0);
    }

    public void set(String owner, String quest, int value) {
        progress.computeIfAbsent(owner, k -> new HashMap<>()).put(quest, value);
        setDirty();
    }
}
