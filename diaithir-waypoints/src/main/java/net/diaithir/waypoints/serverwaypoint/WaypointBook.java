package net.diaithir.waypoints.serverwaypoint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/** Shared named lists of named waypoints, saved with the world. */
public final class WaypointBook extends SavedData {
    public record Wp(String dim, int x, int y, int z) {
        public static final Codec<Wp> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("dim").forGetter(Wp::dim), Codec.INT.fieldOf("x").forGetter(Wp::x),
                Codec.INT.fieldOf("y").forGetter(Wp::y), Codec.INT.fieldOf("z").forGetter(Wp::z)).apply(i, Wp::new));
    }

    private static final Codec<Map<String, Map<String, Wp>>> LISTS = Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Codec.STRING, Wp.CODEC));

    public static final SavedDataType<WaypointBook> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("diaithirwaypoints", "server_waypoints"), WaypointBook::new,
            RecordCodecBuilder.create(i -> i.group(LISTS.fieldOf("lists").forGetter(b -> b.lists)).apply(i, WaypointBook::new)),
            DataFixTypes.LEVEL);

    private final Map<String, Map<String, Wp>> lists = new TreeMap<>();

    public WaypointBook() {}

    private WaypointBook(Map<String, Map<String, Wp>> in) {
        in.forEach((k, v) -> lists.put(k, new LinkedHashMap<>(v)));
    }

    public static WaypointBook get(MinecraftServer server) {
        return server.getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(TYPE);
    }

    public Map<String, Map<String, Wp>> lists() { return lists; }

    public boolean addList(String list) {
        if (lists.containsKey(list)) return false;
        lists.put(list, new LinkedHashMap<>());
        setDirty();
        return true;
    }

    public boolean removeList(String list) {
        boolean r = lists.remove(list) != null;
        if (r) setDirty();
        return r;
    }

    /** Adds or replaces. False when the list does not exist. */
    public boolean put(String list, String name, Wp wp) {
        Map<String, Wp> l = lists.get(list);
        if (l == null) return false;
        l.put(name, wp);
        setDirty();
        return true;
    }

    public boolean remove(String list, String name) {
        Map<String, Wp> l = lists.get(list);
        boolean r = l != null && l.remove(name) != null;
        if (r) setDirty();
        return r;
    }

    public Wp find(String list, String name) {
        Map<String, Wp> l = lists.get(list);
        return l == null ? null : l.get(name);
    }
}
