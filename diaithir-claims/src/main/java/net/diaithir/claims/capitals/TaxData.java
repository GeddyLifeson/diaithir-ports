package net.diaithir.claims.capitals;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Tax rates, treasuries and who paid when, per capital (keyed by lower-case capital name). Times are real epoch seconds. */
public final class TaxData extends SavedData {
    public record Rate(String item, int amount) {
        static final Codec<Rate> CODEC = RecordCodecBuilder.create(i -> i.group(Codec.STRING.fieldOf("item").forGetter(Rate::item), Codec.INT.fieldOf("amount").forGetter(Rate::amount)).apply(i, Rate::new));
    }

    private static final Codec<Map<String, Rate>> RATES = Codec.unboundedMap(Codec.STRING, Rate.CODEC);
    private static final Codec<Map<String, Map<String, Integer>>> TREASURY = Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Codec.STRING, Codec.INT));
    private static final Codec<Map<String, Long>> PAID = Codec.unboundedMap(Codec.STRING, Codec.LONG);

    public static final SavedDataType<TaxData> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath("diaithirclaims", "taxes"), TaxData::new,
        RecordCodecBuilder.create(i -> i.group(RATES.optionalFieldOf("rates", Map.of()).forGetter(d -> d.rates),
            TREASURY.optionalFieldOf("treasury", Map.of()).forGetter(d -> d.treasury),
            PAID.optionalFieldOf("paid", Map.of()).forGetter(d -> d.paid)).apply(i, TaxData::new)), DataFixTypes.LEVEL);

    /** One payment per real day. */
    public static final long PERIOD_SECONDS = 24L * 3600;

    final Map<String, Rate> rates = new HashMap<>();
    final Map<String, Map<String, Integer>> treasury = new HashMap<>();
    final Map<String, Long> paid = new HashMap<>();

    public TaxData() {}

    private TaxData(Map<String, Rate> r, Map<String, Map<String, Integer>> t, Map<String, Long> p) {
        rates.putAll(r);
        t.forEach((k, v) -> treasury.put(k, new HashMap<>(v)));
        paid.putAll(p);
    }

    public static TaxData get(MinecraftServer s) {
        return s.getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(TYPE);
    }

    public Rate rate(String capital) {
        return rates.get(capital.toLowerCase());
    }

    public void setRate(String capital, Rate r) {
        rates.put(capital.toLowerCase(), r);
        setDirty();
    }

    public long lastPaid(String capital, java.util.UUID p) {
        return paid.getOrDefault(capital.toLowerCase() + "|" + p, 0L);
    }

    public void paid(String capital, java.util.UUID p, long now, String item, int n) {
        paid.put(capital.toLowerCase() + "|" + p, now);
        treasury.computeIfAbsent(capital.toLowerCase(), k -> new HashMap<>()).merge(item, n, Integer::sum);
        setDirty();
    }

    public Map<String, Integer> take(String capital) {
        Map<String, Integer> m = treasury.remove(capital.toLowerCase());
        setDirty();
        return m == null ? Map.of() : m;
    }

    public Map<String, Integer> view(String capital) {
        return Map.copyOf(treasury.getOrDefault(capital.toLowerCase(), Map.of()));
    }
}
