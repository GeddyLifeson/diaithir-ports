package net.diaithir.nemesis;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Tunables and per-Age names, ranks and traits from data/diaithirnemesis/nemesis.json. An Age with no entry uses Age I's. */
public final class Rules {
    public record Age(List<String> names, List<String> epithets, List<String> ranks, List<String> warbands, List<String> traits, String heir) {}

    public int safeRadius;
    public int ambushCheckTicks;
    public double ambushChance;
    public long ambushCooldownTicks;
    public double cheatDeathChance;
    public int maxPerPlayer;
    /** Grudge points (damage either way between a player and a monster) for each hostility tier; the last makes a nemesis. */
    public List<Integer> grudgeTiers;
    public List<String> tierNames;
    public int feudTicks;
    public int chronicleLength;
    /** A nemesis below this share of its health can be interrogated (sneak and use on it); it lies this often. */
    public double interrogateBelow;
    public double lieChance;
    public Map<String, Age> ages;

    /** The trait ids Traits gives an effect to; an Age listing any other is rejected at load. */
    static final Set<String> TRAITS = Set.of("hardy", "swift", "brutal", "thick_hide", "venomous", "frostbite", "fire_thief", "bloodfed", "caller");

    public static Rules load() {
        Rules rules;
        try (Reader in = new InputStreamReader(Rules.class.getResourceAsStream("/data/diaithirnemesis/nemesis.json"), StandardCharsets.UTF_8)) {
            rules = new Gson().fromJson(in, Rules.class);
        } catch (Exception e) {
            throw new IllegalStateException("data/diaithirnemesis/nemesis.json missing or broken", e);
        }
        rules.ages.forEach((id, a) -> {
            if (a.names().isEmpty() || a.epithets().isEmpty() || a.ranks().isEmpty() || a.warbands().isEmpty() || !TRAITS.containsAll(a.traits()))
                throw new IllegalStateException("nemesis.json Age " + id + " needs names, epithets, ranks and warbands, and only known traits");
        });
        return rules;
    }

    public Age age(String id) { return ages.getOrDefault(id, ages.get("I")); }

    public String title(Nemesis n) {
        List<String> ranks = age(n.age).ranks();
        return ranks.get(Math.min(n.rank, ranks.size()) - 1);
    }

    public Nemesis promote(int id, String type, String age, String player, String house, Random r) {
        Age a = age(age);
        Nemesis n = new Nemesis(id, pick(a.names(), r), pick(a.epithets(), r), type, age, 1, 1, 0,
                List.of(), List.of(player), house.isEmpty() ? List.of() : List.of(house), "", "", false, pick(a.warbands(), r), "");
        addTrait(n, r);
        return n;
    }

    /** Another kill: it rises, and the victim joins the grudge. */
    public void rankUp(Nemesis n, String player, String house, Random r) {
        n.kills++;
        rise(n, r);
        if (!n.players.contains(player)) n.players.add(player);
        if (!house.isEmpty() && !n.houses.contains(house)) n.houses.add(house);
    }

    /** Up one rank (to the Age's top) and one more trait. */
    public void rise(Nemesis n, Random r) {
        n.rank = Math.min(n.rank + 1, age(n.age).ranks().size());
        addTrait(n, r);
    }

    /** Two nemeses of feuding warbands clash: the higher rank is likelier to win. The winner rises; the loser drops a rank, to 0 if it falls. */
    public Nemesis feud(Nemesis a, Nemesis b, Random r) {
        Nemesis win = r.nextInt(a.rank + b.rank) < a.rank ? a : b, lose = win == a ? b : a;
        rise(win, r);
        lose.rank--;
        return win;
    }

    /** Index of the hostility tier a grudge score has reached, or -1. */
    public int tier(float score) {
        int t = -1;
        for (int i = 0; i < grudgeTiers.size(); i++) if (score >= grudgeTiers.get(i)) t = i;
        return t;
    }

    /** Adds a trait the nemesis lacks that its Age allows, if any is left. */
    public void addTrait(Nemesis n, Random r) {
        List<String> open = new ArrayList<>(age(n.age).traits());
        open.removeAll(n.traits);
        if (!open.isEmpty()) n.traits.add(pick(open, r));
    }

    /** The heir at an Age Turn: a new name under the same epithet, one rank lower, half the traits (rounded up), the same grudges. */
    public Nemesis heir(Nemesis sire, int id, String newAge, Random r) {
        List<String> names = new ArrayList<>(age(newAge).names());
        if (names.size() > 1) names.remove(sire.name);
        List<String> traits = new ArrayList<>(sire.traits);
        while (traits.size() > (sire.traits.size() + 1) / 2) traits.remove(r.nextInt(traits.size()));
        traits.retainAll(age(newAge).traits());
        return new Nemesis(id, pick(names, r), sire.epithet, sire.type, newAge, Math.clamp(sire.rank - 1, 1, age(newAge).ranks().size()), sire.generation + 1, 0,
                traits, sire.players, sire.houses, sire.fullName(), "", false, sire.warband, "");
    }

    private static String pick(List<String> list, Random r) { return list.get(r.nextInt(list.size())); }
}
