package net.diaithir.worldsys.questbook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private static final Codec<Map<String, List<String>>> ACCEPTED = Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf());
    private static final Codec<Map<String, Long>> DONE = Codec.unboundedMap(Codec.STRING, Codec.LONG);
    private static final Codec<Map<String, Integer>> POINTS = Codec.unboundedMap(Codec.STRING, Codec.INT);

    public static final SavedDataType<QuestData> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath("diaithirworldsys", "questbook"), QuestData::new,
            RecordCodecBuilder.create(i -> i.group(
                    PROGRESS.fieldOf("progress").forGetter(d -> d.progress),
                    TEAMS.fieldOf("teams").forGetter(d -> d.teams),
                    ACCEPTED.optionalFieldOf("accepted", Map.of()).forGetter(d -> d.accepted),
                    DONE.optionalFieldOf("done_at", Map.of()).forGetter(d -> d.doneAt),
                    POINTS.optionalFieldOf("points", Map.of()).forGetter(d -> d.points)).apply(i, QuestData::new)),
            DataFixTypes.LEVEL);

    private final Map<String, Map<String, Integer>> progress = new HashMap<>();
    private final Map<String, String> teams = new HashMap<>();
    private final Map<String, List<String>> accepted = new HashMap<>();
    private final Map<String, Long> doneAt = new HashMap<>();
    private final Map<String, Integer> points = new HashMap<>();

    public QuestData() {}

    private QuestData(Map<String, Map<String, Integer>> p, Map<String, String> t, Map<String, List<String>> a, Map<String, Long> d, Map<String, Integer> pt) {
        p.forEach((k, v) -> progress.put(k, new HashMap<>(v)));
        teams.putAll(t);
        a.forEach((k, v) -> accepted.put(k, new ArrayList<>(v)));
        doneAt.putAll(d);
        points.putAll(pt);
    }

    public boolean isAccepted(String owner, String quest) {
        return accepted.getOrDefault(owner, List.of()).contains(quest);
    }

    public List<String> acceptedList(String owner) {
        return List.copyOf(accepted.getOrDefault(owner, List.of()));
    }

    public void setAccepted(String owner, String quest, boolean on) {
        List<String> l = accepted.computeIfAbsent(owner, k -> new ArrayList<>());
        if (on && !l.contains(quest)) l.add(quest);
        if (!on) l.remove(quest);
        setDirty();
    }

    /** Epoch seconds of the last completion, 0 if never. */
    public long doneAt(String owner, String quest) {
        return doneAt.getOrDefault(owner + "|" + quest, 0L);
    }

    public void markDone(String owner, String quest, long epochSeconds) {
        doneAt.put(owner + "|" + quest, epochSeconds);
        setDirty();
    }

    public int points(String owner) {
        return points.getOrDefault(owner, 0);
    }

    public void addPoints(String owner, int n) {
        points.merge(owner, n, Integer::sum);
        setDirty();
    }

    /** Forgets the progress and acceptance of one quest (abort, or a repeat after the cooldown). */
    public void reset(String owner, String quest, int objectives) {
        Map<String, Integer> m = progress.get(owner);
        if (m != null) {
            m.remove(quest);
            for (int i = 1; i < objectives; i++) m.remove(quest + "#" + i);
        }
        setAccepted(owner, quest, false);
        setDirty();
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
