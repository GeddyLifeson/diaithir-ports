package net.diaithir.nemesis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Every living nemesis, the current Age and each player's House, as the diaithir-ages story events reported them. */
public final class NemesisData extends SavedData {
    // Kept here, not on Nemesis, so the rules stay testable without Minecraft on the classpath.
    static final Codec<Nemesis> NEMESIS = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("id").forGetter(n -> n.id),
            Codec.STRING.fieldOf("name").forGetter(n -> n.name),
            Codec.STRING.fieldOf("epithet").forGetter(n -> n.epithet),
            Codec.STRING.fieldOf("type").forGetter(n -> n.type),
            Codec.STRING.fieldOf("age").forGetter(n -> n.age),
            Codec.INT.fieldOf("rank").forGetter(n -> n.rank),
            Codec.INT.optionalFieldOf("generation", 1).forGetter(n -> n.generation),
            Codec.INT.optionalFieldOf("kills", 0).forGetter(n -> n.kills),
            Codec.STRING.listOf().fieldOf("traits").forGetter(n -> n.traits),
            Codec.STRING.listOf().fieldOf("players").forGetter(n -> n.players),
            Codec.STRING.listOf().optionalFieldOf("houses", List.of()).forGetter(n -> n.houses),
            Codec.STRING.optionalFieldOf("sire", "").forGetter(n -> n.sire),
            Codec.STRING.optionalFieldOf("entity", "").forGetter(n -> n.entity),
            Codec.BOOL.optionalFieldOf("cheated", false).forGetter(n -> n.cheated),
            Codec.LONG.optionalFieldOf("lastAmbush", 0L).forGetter(n -> n.lastAmbush),
            Codec.STRING.optionalFieldOf("seen", "").forGetter(n -> n.seen)
    ).apply(i, Nemesis::new));

    public static final Codec<NemesisData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("nextId", 1).forGetter(d -> d.nextId),
            Codec.STRING.optionalFieldOf("age", "I").forGetter(d -> d.age),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("houses", Map.of()).forGetter(d -> d.houses),
            NEMESIS.listOf().optionalFieldOf("nemeses", List.of()).forGetter(d -> d.nemeses),
            Codec.unboundedMap(Codec.STRING, ItemStack.CODEC).optionalFieldOf("trophies", Map.of()).forGetter(d -> d.trophies),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("shaken", Map.of()).forGetter(d -> d.shaken)
    ).apply(i, NemesisData::new));
    public static final SavedDataType<NemesisData> TYPE =
            new SavedDataType<>(Identifier.fromNamespaceAndPath(DiaithirNemesis.ID, "nemeses"), NemesisData::new, CODEC);

    public int nextId;
    public String age;
    public final Map<String, String> houses;
    public final List<Nemesis> nemeses;
    /** The item each nemesis took from a player it killed, by nemesis id; it is dropped when the nemesis falls. */
    public final Map<String, ItemStack> trophies;
    /** Players left Shaken (weakened) by a nemesis, by player UUID to nemesis id, until it falls. */
    public final Map<String, Integer> shaken;

    private NemesisData() { this(1, "I", Map.of(), List.of(), Map.of(), Map.of()); }

    private NemesisData(int nextId, String age, Map<String, String> houses, List<Nemesis> nemeses, Map<String, ItemStack> trophies, Map<String, Integer> shaken) {
        this.nextId = nextId;
        this.age = age;
        this.houses = new HashMap<>(houses);
        this.nemeses = new ArrayList<>(nemeses);
        this.trophies = new HashMap<>(trophies);
        this.shaken = new HashMap<>(shaken);
    }

    public static NemesisData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public ItemStack trophy(Nemesis n) { return trophies.getOrDefault(key(n), ItemStack.EMPTY); }

    public void setTrophy(Nemesis n, ItemStack stack) { trophies.put(key(n), stack); }

    /** Hands the trophy and the Shaken players on to an heir. */
    public void inherit(Nemesis from, Nemesis to) {
        ItemStack t = trophies.remove(key(from));
        if (t != null) trophies.put(key(to), t);
        shaken.replaceAll((p, id) -> id == from.id ? to.id : id);
    }

    /** Forgets a nemesis, its trophy and whom it shook. */
    public void remove(Nemesis n) {
        nemeses.remove(n);
        trophies.remove(key(n));
        shaken.values().removeIf(id -> id == n.id);
    }

    private static String key(Nemesis n) { return Integer.toString(n.id); }

    public String house(String player) { return houses.getOrDefault(player, ""); }

    public Nemesis byId(int id) {
        for (Nemesis n : nemeses) if (n.id == id) return n;
        return null;
    }

    /** Nemeses holding a grudge against this player or their House. */
    public List<Nemesis> against(String player) {
        String house = house(player);
        List<Nemesis> out = new ArrayList<>();
        for (Nemesis n : nemeses) if (n.players.contains(player) || (!house.isEmpty() && n.houses.contains(house))) out.add(n);
        return out;
    }
}
