package net.diaithir.hostile.celestials;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Which night holds which lunar event, kept per world and generated ahead a year at a time (same scheduling rules as Enhanced Celestials). */
public final class Forecast extends SavedData {
    public record Entry(long day, String event, boolean forced) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(b -> b.group(
            Codec.LONG.fieldOf("day").forGetter(Entry::day),
            Codec.STRING.fieldOf("event").forGetter(Entry::event),
            Codec.BOOL.optionalFieldOf("forced", false).forGetter(Entry::forced)).apply(b, Entry::new));
    }

    private static final Codec<Forecast> CODEC = RecordCodecBuilder.create(b -> b.group(
        Entry.CODEC.listOf().fieldOf("entries").forGetter(f -> new ArrayList<>(f.byDay.values())),
        Codec.LONG.fieldOf("generated_to").forGetter(f -> f.generatedTo)).apply(b, Forecast::new));

    public static final SavedDataType<Forecast> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath(DiaithirHostile.ID, "lunar_forecast"), Forecast::new, CODEC);

    private final Map<Long, Entry> byDay = new HashMap<>();
    private long generatedTo = -1L;

    public Forecast() {}

    private Forecast(List<Entry> entries, long generatedTo) {
        for (Entry e : entries) byDay.put(e.day(), e);
        this.generatedTo = generatedTo;
    }

    public static Forecast of(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public static long dayOf(ServerLevel level) { return level.getOverworldClockTime() / Lunar.DAY; }

    public static boolean night(ServerLevel level) {
        long t = level.getOverworldClockTime() % Lunar.DAY;
        return t >= 13000L && t < 23000L;
    }

    /** The event active right now: the default moon by day, in rain, or on an unscheduled night. */
    public Lunar.Event current(ServerLevel level) {
        if (!night(level) || level.isRaining()) return Lunar.none();
        Entry e = byDay.get(dayOf(level));
        return e == null ? Lunar.none() : Lunar.get(e.event());
    }

    public List<Entry> upcoming(long today, int limit) {
        List<Entry> out = new ArrayList<>();
        for (Entry e : byDay.values()) if (e.day() >= today) out.add(e);
        out.sort((a, b) -> Long.compare(a.day(), b.day()));
        return out.size() > limit ? out.subList(0, limit) : out;
    }

    /** Keeps the schedule {@link Lunar#YEAR_DAYS} days ahead and drops old nights. */
    public void update(ServerLevel level) {
        long today = dayOf(level);
        if (generatedTo >= today + Lunar.YEAR_DAYS - 1) return;
        if (byDay.values().removeIf(e -> e.day() < today - Lunar.YEAR_DAYS)) setDirty();
        Map<String, Lunar.Event> pool = new HashMap<>();
        Lunar.events().forEach((k, v) -> { if (v.schedulable()) pool.put(k, v); });
        long last = -1L;
        Map<String, Long> lastOf = new HashMap<>();
        for (Entry e : byDay.values()) {
            last = Math.max(last, e.day());
            lastOf.merge(e.event(), e.day(), Math::max);
        }
        for (long day = Math.max(generatedTo + 1, today); day < today + Lunar.YEAR_DAYS; day++) {
            if (pool.isEmpty() || byDay.containsKey(day)) continue;
            Random random = new Random(day + level.getSeed() * 31L);
            List<String> order = new ArrayList<>(pool.keySet());
            Collections.sort(order);
            Collections.shuffle(order, random);
            for (String id : order) {
                Lunar.Event ev = pool.get(id);
                boolean farFromSame = day - lastOf.getOrDefault(id, today) > ev.min_nights_between;
                boolean farFromAny = day - last > Lunar.MIN_BETWEEN;
                boolean phase = ev.valid_moon_phases.contains((int) (day % 8));
                boolean lucky = ev.chance >= random.nextDouble();
                boolean pass = farFromSame && farFromAny && phase && lucky;
                boolean overdue = !pass && last != -1 && day - last >= Lunar.MAX_BETWEEN;
                if (pass || overdue) {
                    byDay.put(day, new Entry(day, id, false));
                    lastOf.put(id, day);
                    last = day;
                    break;
                }
            }
        }
        generatedTo = today + Lunar.YEAR_DAYS - 1;
        setDirty();
    }

    public void recompute(ServerLevel level) {
        byDay.values().removeIf(e -> !e.forced());
        generatedTo = -1L;
        update(level);
    }

    /** Forces an event onto tonight; the time jumps to dusk when it is day. */
    public void force(ServerLevel level, String id) {
        long today = dayOf(level);
        if (!night(level)) level.registryAccess().get(net.minecraft.world.clock.WorldClocks.OVERWORLD).ifPresent(c -> level.clockManager().setTotalTicks(c, today * Lunar.DAY + 13000L));
        if (id.equals(Lunar.DEFAULT_ID)) byDay.remove(today); else byDay.put(today, new Entry(today, id, true));
        setDirty();
    }
}
