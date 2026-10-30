package net.diaithir.conduits;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The channel registry. A channel is an owner (player UUID) plus a number, so two players can both have a "channel 1" that never meet.
 * Holds every node on every channel (dimension and position) and each owner's trusted players. Saved with the world.
 */
public final class Channels extends SavedData {
    public record Entry(String owner, int number, String dim, BlockPos pos) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("owner").forGetter(Entry::owner),
                Codec.INT.fieldOf("number").forGetter(Entry::number),
                Codec.STRING.fieldOf("dim").forGetter(Entry::dim),
                BlockPos.CODEC.fieldOf("pos").forGetter(Entry::pos)).apply(i, Entry::new));
    }

    public record Pair(String a, String b) {
        static final Codec<Pair> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("a").forGetter(Pair::a), Codec.STRING.fieldOf("b").forGetter(Pair::b)).apply(i, Pair::new));
    }

    private record Raw(List<Entry> nodes, List<Pair> trust, List<Pair> names) {
        static final Codec<Raw> CODEC = RecordCodecBuilder.create(i -> i.group(
                Entry.CODEC.listOf().optionalFieldOf("nodes", List.of()).forGetter(Raw::nodes),
                Pair.CODEC.listOf().optionalFieldOf("trust", List.of()).forGetter(Raw::trust),
                Pair.CODEC.listOf().optionalFieldOf("names", List.of()).forGetter(Raw::names)).apply(i, Raw::new));
    }

    static final Codec<Channels> CODEC = Raw.CODEC.xmap(Channels::new, c -> {
        List<Pair> t = new ArrayList<>(), n = new ArrayList<>();
        c.trust.forEach((o, set) -> set.forEach(w -> t.add(new Pair(o, w))));
        c.names.forEach((u, name) -> n.add(new Pair(u, name)));
        return new Raw(new ArrayList<>(c.nodes), t, n);
    });
    static final SavedDataType<Channels> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath(DiaithirConduits.ID, "channels"), Channels::new, CODEC);

    private final List<Entry> nodes = new ArrayList<>();
    private final Map<String, Set<String>> trust = new HashMap<>();
    private final Map<String, String> names = new HashMap<>();

    private Channels() {}

    private Channels(Raw raw) {
        nodes.addAll(raw.nodes);
        raw.trust.forEach(p -> trust.computeIfAbsent(p.a, k -> new LinkedHashSet<>()).add(p.b));
        raw.names.forEach(p -> names.put(p.a, p.b));
    }

    public static Channels get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    // ---- names and trust ---------------------------------------------------------------------------------------------------------

    public void rememberName(String uuid, String name) {
        if (!name.equals(names.put(uuid, name))) setDirty();
    }

    public String nameOf(String uuid) {
        return names.getOrDefault(uuid, uuid.isEmpty() ? "nobody" : uuid.substring(0, Math.min(8, uuid.length())));
    }

    public String uuidOfName(String name) {
        for (Map.Entry<String, String> e : names.entrySet()) if (e.getValue().equalsIgnoreCase(name)) return e.getKey();
        return null;
    }

    public boolean trust(String owner, String who) {
        boolean added = trust.computeIfAbsent(owner, k -> new LinkedHashSet<>()).add(who);
        if (added) setDirty();
        return added;
    }

    public boolean untrust(String owner, String who) {
        Set<String> s = trust.get(owner);
        boolean removed = s != null && s.remove(who);
        if (removed) setDirty();
        return removed;
    }

    public Set<String> trusted(String owner) {
        return trust.getOrDefault(owner, Set.of());
    }

    // ---- channels ----------------------------------------------------------------------------------------------------------------

    /** Whether this player may put a node on that owner's channel: the owner, someone the owner trusts, or an operator. */
    public boolean mayUse(String owner, String player, boolean operator) {
        return owner.equals(player) || trusted(owner).contains(player) || (operator && Config.OPS_MANAGE_ALL.get());
    }

    public int channelsOwnedBy(String owner) {
        Set<Integer> s = new HashSet<>();
        for (Entry e : nodes) if (e.owner.equals(owner)) s.add(e.number);
        return s.size();
    }

    public List<Entry> on(String owner, int number) {
        List<Entry> out = new ArrayList<>();
        for (Entry e : nodes) if (e.number == number && e.owner.equals(owner)) out.add(e);
        return out;
    }

    public Entry find(String dim, BlockPos pos) {
        for (Entry e : nodes) if (e.dim.equals(dim) && e.pos.equals(pos)) return e;
        return null;
    }

    /**
     * Puts a node on a channel (leaving any channel it was on). Returns null on success or the reason it was refused:
     * a bad number, a player who is neither the owner nor trusted, too many nodes on the channel, or the owner already has too many channels.
     */
    public String join(String owner, int number, String dim, BlockPos pos, String player, boolean operator) {
        if (number < 1 || number > Config.MAX_CHANNEL_NUMBER.get()) return "Channels are numbered 1 to " + Config.MAX_CHANNEL_NUMBER.get() + ".";
        if (!mayUse(owner, player, operator)) return "Channel " + number + " belongs to " + nameOf(owner) + ", who has not trusted you.";
        Entry old = find(dim, pos);
        if (old != null && old.owner.equals(owner) && old.number == number) return null;
        if (old != null) nodes.remove(old);
        List<Entry> there = on(owner, number);
        if (there.size() >= Config.MAX_NODES_PER_CHANNEL.get()) {
            if (old != null) nodes.add(old);
            return "Channel " + number + " already has " + there.size() + " nodes (server limit).";
        }
        if (there.isEmpty() && channelsOwnedBy(owner) >= Config.MAX_CHANNELS_PER_PLAYER.get()) {
            if (old != null) nodes.add(old);
            return nameOf(owner) + " already has " + Config.MAX_CHANNELS_PER_PLAYER.get() + " channels (server limit).";
        }
        nodes.add(new Entry(owner, number, dim, pos));
        setDirty();
        return null;
    }

    public void leave(String dim, BlockPos pos) {
        if (nodes.removeIf(e -> e.dim.equals(dim) && e.pos.equals(pos))) setDirty();
    }

    public int nodeCount() {
        return nodes.size();
    }
}
