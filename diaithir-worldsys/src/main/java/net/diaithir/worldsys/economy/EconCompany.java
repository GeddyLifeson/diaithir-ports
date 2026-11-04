package net.diaithir.worldsys.economy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Companies, the founding office and the caravan job. Port of 07_company.js. Lines haul the good with the widest price gap between two towns of one
 * culture; the destination's treasury pays, so nothing is minted. Founding needs a built office and a "Safe passage" delivery first.
 */
public final class EconCompany {
    private EconCompany() {}

    public record Vehicle(String id, String name, String age, String tech, double year, int cap, double speed, double cost, double upkeep) {}

    public static final List<Vehicle> VEHICLES = List.of(
        new Vehicle("pack_train", "Pack-animal train", "I", "herding", -9000, 16, 700, 120, 4),
        new Vehicle("ox_cart", "Ox cart", "II", "wheel", -3500, 40, 1100, 260, 8),
        new Vehicle("sailing_ship", "Sailing ship", "III", "sail", -3200, 160, 2600, 1100, 30),
        new Vehicle("steam_train", "Steam train", "V", "railway", 1825, 600, 9000, 6000, 160),
        new Vehicle("motor_truck", "Motor truck", "V", "factory", 1900, 120, 12000, 3000, 90),
        new Vehicle("cargo_aircraft", "Cargo aircraft", "VI", "aviation", 1910, 250, 60000, 30000, 700));
    public static final int BANKRUPT_WEEKS = 4;

    static boolean ageLe(String a, String age) {
        if (a == null || age == null) return true;
        return Math.max(1, EconPolity.AGES.indexOf(a)) <= Math.max(1, EconPolity.AGES.indexOf(age));
    }

    /** Vehicles a culture may buy: its Age is reached, its tech known, and the calendar has reached the first year (year null: not published). */
    public static List<Vehicle> vehicles(String age, Predicate<String> known, Double year) {
        List<Vehicle> out = new ArrayList<>();
        for (Vehicle v : VEHICLES) if (ageLe(v.age(), age) && (v.tech() == null || known.test(v.tech())) && (year == null || year >= v.year())) out.add(v);
        return out;
    }

    public static Vehicle vehicle(String id) { for (Vehicle v : VEHICLES) if (v.id().equals(id)) return v; return null; }

    public static EconState.Company found(EconState st, String key, String name, String culture) {
        if (st.company.containsKey(key)) return null;
        EconState.Company c = new EconState.Company();
        c.name = name; c.home = culture; c.founded = st.week;
        st.company.put(key, c);
        return c;
    }

    /** held less usual: negative means demand, positive supply. */
    public static double gap(EconState st, Function<String, EconModel.Good> good, String town, String id) {
        double usual = good.apply(id).usual();
        Double held = st.market.stock.getOrDefault(town, Map.of()).get(id);
        return (held == null ? usual : held) - usual;
    }

    public record Plan(String id, int units, double score) {}

    public static Plan plan(EconState st, Map<String, double[]> regions, Function<String, EconModel.Good> good, String from, String to, int capacity, List<String> ids) {
        Plan best = null;
        for (String id : ids) {
            int units = (int) Math.min(capacity, Math.min(Math.floor(gap(st, good, from, id)), Math.floor(-gap(st, good, to, id))));
            if (units < 1) continue;
            double margin = EconModel.mid(st.market, regions, good, to, id, null, 0) - EconModel.mid(st.market, regions, good, from, id, null, 0);
            if (margin <= 0) continue;
            if (best == null || margin * units > best.score()) best = new Plan(id, units, margin * units);
        }
        return best;
    }

    /** Freight bonus on the destination price: grows with distance (to 6000 blocks) and speed (to twice 2000 blocks a day). */
    public static double bonus(double dist, double speed) { return 0.5 * Math.min(1, dist / 6000) * Math.min(2, speed / 2000); }

    /** What the world gives one line each week. */
    public record Env(EconState state, Map<String, double[]> regions, Function<String, EconModel.Good> good, double scale, List<String> ids) {}

    public static List<String> lineWeek(Env env, EconState.Company co, EconState.Line line) {
        Vehicle v = vehicle(line.vehicle);
        List<String> notes = new ArrayList<>();
        if (v == null) { notes.add("unknown vehicle " + line.vehicle); return notes; }
        double dist = Math.max(100, EconModel.dist(env.regions(), line.from, line.to));
        int trips = (int) Math.max(1, Math.min(14, Math.floor(7 * v.speed() / (2 * dist))));
        double upkeep = Math.ceil(v.upkeep() * line.n * env.scale());
        co.money -= upkeep;
        env.state().cur(co.home).treasury += upkeep;
        line.upkeep += upkeep;
        line.weeks++;
        Plan plan = plan(env.state(), env.regions(), env.good(), line.from, line.to, trips * v.cap() * line.n, env.ids());
        if (plan == null) { notes.add("nothing to haul"); return notes; }
        int units = plan.units();
        while (units > 0 && EconModel.quote(env.state().market, env.regions(), env.good(), line.from, plan.id(), units, true, env.scale()) > co.money) units = units / 2;
        if (units < 1) { notes.add("not enough money to buy cargo"); return notes; }
        long cost = EconModel.trade(env.state().market, env.regions(), env.good(), line.from, plan.id(), units, true, env.scale());
        long sale = EconModel.trade(env.state().market, env.regions(), env.good(), line.to, plan.id(), units, false, env.scale());
        EconState.Cur dest = env.state().cur(co.home);
        long due = (long) Math.floor(sale * (1 + bonus(dist, v.speed())));
        long pay = Math.min(due, Math.max(0, (long) Math.floor(dest.treasury)));
        if (pay < due) notes.add("the treasury could not pay in full");
        co.money += pay - cost;
        dest.treasury += cost - pay;
        line.rev += pay;
        line.cost += cost;
        line.units += units;
        line.cargo = plan.id();
        return notes;
    }

    /** Once a week for every company; envOf gives a culture's scale and open goods. */
    public static List<String> week(EconState st, Map<String, double[]> regions, Function<String, EconModel.Good> good, Function<String, Double> scaleOf, Function<String, List<String>> idsOf) {
        List<String> notes = new ArrayList<>();
        for (EconState.Company co : st.company.values()) {
            if (co.closed) continue;
            double before = co.money;
            Env env = new Env(st, regions, good, scaleOf.apply(co.home), idsOf.apply(co.home));
            for (EconState.Line l : co.lines) for (String n : lineWeek(env, co, l)) notes.add(co.name + ": " + n);
            if (co.money < 0 && before <= 0) co.broke++;
            else if (co.money >= 0) co.broke = 0;
            if (co.broke >= BANKRUPT_WEEKS) { co.closed = true; co.lines = new ArrayList<>(); notes.add(co.name + " is bankrupt: its lines are closed"); }
        }
        return notes;
    }

    /** Used vehicles as a pledge: 70% of cost. */
    public static double assets(EconState.Company co) {
        double t = 0;
        for (EconState.Line l : co.lines) { Vehicle v = vehicle(l.vehicle); if (v != null) t += v.cost() * l.n * 0.7; }
        return t;
    }

    public static double profit(EconState.Line l) { return l.rev - l.cost - l.upkeep; }

    // ---- The founding office ----------------------------------------------------------------------------------------------------

    public record Block(String id, boolean passable) {}
    public record Office(boolean ok, List<String> missing, int fee) {}

    static final int[] AREA = {6, 10, 16}, STRONGBOX = {0, 1, 1}, SHELVES = {0, 1, 2}, DEPOT = {1, 2, 4}, FEE = {30, 120, 400};
    static final String[] TECH = {null, "writing", "railway"};
    static final int VOLUME = 400;

    public static int tier(String age) {
        if (!List.of("I", "II", "III", "IV").contains(age)) return 2;
        return List.of("I", "II").contains(age) ? 0 : 1;
    }

    private static final int[][] DIRS = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
    private static final Pattern DOOR = Pattern.compile("_door$"), DESK = Pattern.compile(":lectern$"), BOX = Pattern.compile(":(chest|barrel|trapped_chest)$"),
        SHELF = Pattern.compile("bookshelf$"), YARD = Pattern.compile(":(barrel|hay_block)$");

    public interface World { Block at(int x, int y, int z); }

    public static Office officeCheck(World at, int[] start, String age, Predicate<String> known) {
        int t = tier(age);
        List<String> miss = new ArrayList<>();
        if (TECH[t] != null && known != null && !known.test(TECH[t])) miss.add("the " + TECH[t] + " tech");
        Set<String> seen = new HashSet<>();
        List<int[]> cells = new ArrayList<>(), q = new ArrayList<>();
        q.add(start.clone());
        seen.add(key(start[0], start[1], start[2]));
        boolean open = false;
        while (!q.isEmpty() && !open) {
            int[] c = q.remove(q.size() - 1);
            cells.add(c);
            if (cells.size() > VOLUME) { open = true; break; }
            for (int[] d : DIRS) {
                int[] n = {c[0] + d[0], c[1] + d[1], c[2] + d[2]};
                String k = key(n[0], n[1], n[2]);
                if (!seen.add(k)) continue;
                if (at.at(n[0], n[1], n[2]).passable()) q.add(n);
            }
        }
        if (open) { miss.add("walls and a roof (the room is open)"); return new Office(false, miss, 0); }
        long floor = cells.stream().filter(c -> c[1] == start[1]).count();
        if (floor < AREA[t]) miss.add("a floor of " + AREA[t] + " blocks (it has " + floor + ")");
        Map<String, String> walls = new LinkedHashMap<>();
        for (int[] c : cells) for (int[] d : DIRS) {
            int x = c[0] + d[0], y = c[1] + d[1], z = c[2] + d[2];
            String k = key(x, y, z);
            if (seen.contains(k) && !walls.containsKey(k)) { Block b = at.at(x, y, z); if (!b.passable()) walls.put(k, b.id()); }
        }
        int door = 0, desk = 0, box = 0, shelf = 0;
        for (String id : walls.values()) {
            if (DOOR.matcher(id).find()) door++;
            else if (DESK.matcher(id).find()) desk++;
            else if (BOX.matcher(id).find()) box++;
            else if (SHELF.matcher(id).find()) shelf++;
        }
        if (door == 0) miss.add("a door");
        if (desk == 0) miss.add("a lectern as the charter desk");
        if (box < STRONGBOX[t]) miss.add("a chest or barrel as the strongbox");
        if (shelf < SHELVES[t]) miss.add(SHELVES[t] + " bookshelf" + (SHELVES[t] > 1 ? "s" : "") + " for the ledgers");
        int yard = 0, r = 24;
        for (int x = start[0] - r; x <= start[0] + r; x++) for (int z = start[2] - r; z <= start[2] + r; z++) for (int y = start[1] - 6; y <= start[1] + 6; y++) {
            String k = key(x, y, z);
            if (seen.contains(k) && !walls.containsKey(k)) continue;
            if (YARD.matcher(at.at(x, y, z).id()).find() && !walls.containsKey(k)) yard++;
        }
        if (yard < DEPOT[t]) miss.add("a depot yard of " + DEPOT[t] + " barrels or hay bales outside, within " + r + " blocks (" + yard + " found)");
        return new Office(miss.isEmpty(), miss, FEE[t]);
    }

    private static String key(int x, int y, int z) { return x + "," + y + "," + z; }

    // ---- The caravan job: any working person can ask for safe transport ---------------------------------------------------------------

    public record Job(String id, String line) {}
    public record Claim(String town, double x, double z, String culture) {}

    private static final String DEFAULT_LINE = "Moving things around is the hard part of every trade I know. I wish someone offered safe transport.";
    public static final Map<String, String[]> JOBS = new HashMap<>();
    static {
        JOBS.put("toolsmith", new String[] {"minecraft:flint", "minecraft:iron_ingot", "My ore and billets pile up in the yard. Nobody I trust will carry them to the next town."});
        JOBS.put("armorer", new String[] {"minecraft:leather", "minecraft:iron_ingot", "I can make a hundred plates a season and I cannot get the metal here or the plates out."});
        JOBS.put("weaponsmith", new String[] {"minecraft:flint", "minecraft:iron_ingot", "Every blade I sell is a blade that crossed a road, and every road eats a third of the load."});
        JOBS.put("mason", new String[] {"minecraft:cobblestone", "minecraft:stone_bricks", "Dressed stone is worth three times more one town over, if it arrives whole."});
        JOBS.put("farmer", new String[] {"minecraft:wheat", "minecraft:wheat", "The harvest rots in my barn while the next town goes short. I need it carried and I need it safe."});
        JOBS.put("shepherd", new String[] {"minecraft:white_wool", "minecraft:white_wool", "Wool keeps no better than I do. It needs to be at the weavers, not in my shed."});
        JOBS.put("butcher", new String[] {"minecraft:beef", "minecraft:beef", "Meat will not wait for a slow cart. I need it hauled quickly and not robbed on the way."});
        JOBS.put("fisherman", new String[] {"minecraft:cod", "minecraft:cod", "The catch is best on the day it is landed. Getting it inland is the whole trade."});
        JOBS.put("leatherworker", new String[] {"minecraft:leather", "minecraft:leather", "Hides pile up at my door and the tanner you need is a road away."});
        JOBS.put("fletcher", new String[] {"minecraft:feather", "minecraft:arrow", "Shafts and feathers come from three places and none of them is where I am."});
        JOBS.put("librarian", new String[] {"minecraft:paper", "minecraft:book", "Books are worth little here and a great deal elsewhere, if someone honest will carry them."});
        JOBS.put("cartographer", new String[] {"minecraft:paper", "minecraft:map", "My charts only pay when they reach the people who sail by them."});
        JOBS.put("cleric", new String[] {"minecraft:glass_bottle", "minecraft:glass_bottle", "The temple has stores for the other shrine, and nobody to take them safely."});
        JOBS.put("priest", new String[] {"minecraft:candle", "minecraft:candle", "The rites need offerings from the next town, and the road is not what it was."});
        JOBS.put("nitwit", new String[] {"minecraft:bread", "minecraft:bread", "I was told to move this somewhere. I do not remember where. Can you?"});
        JOBS.put("none", new String[] {"minecraft:bread", "minecraft:bread", "I have goods and no way to get them across the country in one piece."});
    }

    public static Job job(String profession, String age) {
        String[] j = JOBS.getOrDefault(profession, new String[] {"minecraft:bread", "minecraft:bread", DEFAULT_LINE});
        return new Job(tier(age) == 0 ? j[0] : j[1], j[2]);
    }

    /** Nearest other town at least 100 blocks away; the same culture's if there is one. */
    public static Claim dest(List<Claim> claims, String from) {
        Claim me = claims.stream().filter(c -> c.town().equals(from)).findFirst().orElse(null);
        if (me == null) return null;
        List<Claim> others = new ArrayList<>(claims.stream().filter(c -> !c.town().equals(from) && Math.hypot(c.x() - me.x(), c.z() - me.z()) >= 100).toList());
        others.sort((a, b) -> Double.compare(Math.hypot(a.x() - me.x(), a.z() - me.z()), Math.hypot(b.x() - me.x(), b.z() - me.z())));
        for (Claim c : others) if (c.culture().equals(me.culture())) return c;
        return others.isEmpty() ? null : others.get(0);
    }

    /** The carrying fee, in tw; late loads earn half. */
    public static long reward(double priceTw, int n, boolean late) { return Math.max(6, Math.round(priceTw * n * 0.6 * (late ? 0.5 : 1))); }
}
