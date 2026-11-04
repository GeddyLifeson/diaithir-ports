package net.diaithir.worldsys.economy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;

/** A keeper's trade screen, planned from the live market: prices split into coin items, bundles sized, a stall chosen by profession and Age. Port of 03_offers.js. */
public final class EconOffers {
    private EconOffers() {}

    /** A coin item and how many units of the smallest unit it is worth. */
    public record Denom(String item, long units) {}
    public record Cost(String item, double count) {}
    public record Split(List<Cost> cost, long units) {}
    public record Payout(String item, long count, long units) {}
    public record Offer(String dir, String id, int n, List<Cost> cost, Cost result, long units) {}
    public record Vendor(String id, List<String> items) {}

    /** A price paid in at most two denominations (largest first), or null above 64 of the biggest. */
    public static Split split(double price, List<Denom> denoms) {
        long p = (long) Math.max(1, Math.ceil(price));
        for (int i = 0; i < denoms.size(); i++) {
            Denom hi = denoms.get(i), lo = i + 1 < denoms.size() ? denoms.get(i + 1) : null;
            long a = p / hi.units(), rem = p - a * hi.units(), b = 0;
            if (lo != null) { b = (long) Math.ceil((double) rem / lo.units()); if (b * lo.units() >= hi.units()) { a++; b = 0; } }
            else if (rem != 0) { a++; rem = 0; }
            if (a > 64 || b > 64) continue;
            if (a == 0 && lo != null) continue;
            List<Cost> cost = new ArrayList<>();
            cost.add(new Cost(hi.item(), a));
            if (b != 0) cost.add(new Cost(lo.item(), b));
            return new Split(cost, a * hi.units() + (lo != null ? b * lo.units() : 0));
        }
        Denom small = denoms.get(denoms.size() - 1);
        return p <= 64 * small.units() ? new Split(List.of(new Cost(small.item(), (double) p / small.units())), p) : null;
    }

    /** The money a keeper hands over for a sale, in one stack of the biggest denomination that fits in 64. */
    public static Payout payout(double units, List<Denom> denoms) {
        for (Denom d : denoms) {
            long n = (long) Math.floor(units / d.units());
            if (n >= 1) return new Payout(d.item(), Math.min(64, n), Math.min(64, n) * d.units());
        }
        return null;
    }

    /** goods: item ids the keeper deals in (buyN for sale, sellN wanted). quote(id, n, buying) is the money for n units. */
    public static List<Offer> offerPlan(List<String> goods, List<Denom> denoms, QuoteFn quote, Function<String, Double> held, int buyN, int sellN) {
        List<Offer> out = new ArrayList<>();
        for (String id : goods.subList(0, Math.min(buyN, goods.size()))) {
            if (held.apply(id) < 1) continue;
            int n = 1;
            while (quote.quote(id, n, true) < 3 && n < 16 && held.apply(id) >= n * 2) n *= 2;
            Split s = split(quote.quote(id, n, true), denoms);
            if (s != null) out.add(new Offer("buy", id, n, s.cost(), null, s.units()));
        }
        for (String id : goods.subList(Math.min(buyN, goods.size()), Math.min(buyN + sellN, goods.size()))) {
            int n = 1;
            while (quote.quote(id, n, false) < 3 && n < 16) n *= 2;
            Payout pay = payout(quote.quote(id, n, false), denoms);
            if (pay != null) out.add(new Offer("sell", id, n, null, new Cost(pay.item(), pay.count()), pay.units()));
        }
        return out;
    }

    public interface QuoteFn { double quote(String id, int n, boolean buying); }

    private static final Map<String, Pattern> STALLS = Map.ofEntries(
        Map.entry("farmer", Pattern.compile("wheat|bread|carrot|potato|beetroot|melon|pumpkin|hay_block|seeds$|apple|honey|cake|pie")),
        Map.entry("fisherman", Pattern.compile("cod|salmon|kelp|fishing_rod|bucket|tropical|puffer")),
        Map.entry("shepherd", Pattern.compile("wool|string|shears|carpet|dye$")),
        Map.entry("leatherworker", Pattern.compile("leather|saddle|rabbit_hide|scute")),
        Map.entry("butcher", Pattern.compile("beef|porkchop|mutton|chicken|rabbit|bone|feather")),
        Map.entry("toolsmith", Pattern.compile("(pickaxe|axe|shovel|hoe)$|flint|stick")),
        Map.entry("armorer", Pattern.compile("(helmet|chestplate|leggings|boots)$|iron_ingot|chain|shield")),
        Map.entry("weaponsmith", Pattern.compile("sword$|bow$|crossbow|arrow|iron_ingot|axe$")),
        Map.entry("mason", Pattern.compile("stone|brick|clay|cobble|andesite|diorite|granite|sandstone|terracotta")),
        Map.entry("fletcher", Pattern.compile("arrow|bow$|stick|feather|flint|string")),
        Map.entry("cartographer", Pattern.compile("paper|map|compass")),
        Map.entry("cleric", Pattern.compile("redstone|lapis|amethyst|glowstone|quartz|potion|bottle")),
        Map.entry("librarian", Pattern.compile("book|paper|ink|feather|lantern")),
        Map.entry("nitwit", Pattern.compile("honeycomb|oak_log|clay_ball|flint|bone|salt")),
        Map.entry("none", Pattern.compile("honeycomb|oak_log|leather|wheat|flint|salt|bone")));

    /** The vendor kind each profession keeps in each Age; a later Age's entries replace the earlier ones. */
    public static final List<Map<String, String>> PROF_VENDOR = List.of(
        Map.ofEntries(Map.entry("farmer", "forager"), Map.entry("fisherman", "fisher"), Map.entry("butcher", "hunter"), Map.entry("shepherd", "hide_worker"),
            Map.entry("leatherworker", "hide_worker"), Map.entry("toolsmith", "flint_smith"), Map.entry("mason", "flint_smith"), Map.entry("armorer", "flint_smith"),
            Map.entry("weaponsmith", "flint_smith"), Map.entry("fletcher", "timber_worker"), Map.entry("cleric", "herb_healer"), Map.entry("librarian", "herb_healer"),
            Map.entry("cartographer", "herb_healer"), Map.entry("nitwit", "salt_trader"), Map.entry("none", "salt_trader")),
        Map.of("farmer", "farmer", "mason", "potter", "nitwit", "baker", "shepherd", "brewer"),
        Map.ofEntries(Map.entry("toolsmith", "smith"), Map.entry("armorer", "smith"), Map.entry("weaponsmith", "smith"), Map.entry("mason", "smith"),
            Map.entry("fletcher", "sylvan_wright"), Map.entry("fisherman", "angler_hall"), Map.entry("cleric", "banker"), Map.entry("priest", "banker"),
            Map.entry("librarian", "rite_keeper"), Map.entry("cartographer", "jeweller"), Map.entry("nitwit", "trader"), Map.entry("butcher", "vintner"),
            Map.entry("leatherworker", "distiller")),
        Map.of("farmer", "miller", "librarian", "alchemist", "cartographer", "scribe", "fletcher", "joiner", "weaponsmith", "chest_maker"),
        Map.of("toolsmith", "engineer", "armorer", "engineer", "weaponsmith", "wireman", "mason", "pipefitter", "cartographer", "wireman"),
        Map.of("toolsmith", "machinist", "armorer", "fitter", "weaponsmith", "locksmith", "mason", "power_engineer", "cleric", "locksmith", "cartographer", "machinist"),
        Map.of("toolsmith", "fabricator", "armorer", "grid_engineer", "weaponsmith", "fabricator", "mason", "grid_engineer", "librarian", "network_architect",
            "cartographer", "network_architect", "cleric", "network_architect"),
        Map.of("toolsmith", "plasma_armourer", "armorer", "aegis_warden", "weaponsmith", "plasma_armourer", "mason", "fusion_wright", "librarian", "mind_wright",
            "cleric", "mind_wright", "cartographer", "orbital_broker", "farmer", "nano_artisan"),
        Map.of("toolsmith", "shardsmith", "armorer", "shardsmith", "weaponsmith", "shardsmith", "mason", "shardsmith", "cleric", "shard_listener",
            "librarian", "shard_listener", "cartographer", "shard_listener"));

    private static Vendor find(List<Vendor> vendors, String id) { for (Vendor v : vendors) if (v.id().equals(id)) return v; return null; }

    /** The vendor kind a profession keeps; a kind not open yet leaves the Age before's choice standing. */
    public static Vendor vendorFor(String profession, List<Vendor> vendors) {
        Vendor found = null;
        for (Map<String, String> m : PROF_VENDOR) { String k = m.get(profession); if (k != null && find(vendors, k) != null) found = find(vendors, k); }
        return found != null ? found : find(vendors, "salt_trader");
    }

    /** The goods a keeper shows, in an order fixed by the keeper (seed). */
    public static List<String> stall(String profession, List<String> openIds, double seed, List<Vendor> vendors) {
        Vendor vendor = vendors != null && !vendors.isEmpty() ? vendorFor(profession, vendors) : null;
        List<String> ids = new ArrayList<>();
        if (vendor != null) { Set<String> only = new HashSet<>(vendor.items()); for (String id : openIds) if (only.contains(id)) ids.add(id); }
        else { Pattern re = STALLS.getOrDefault(profession, STALLS.get("none")); for (String id : openIds) if (re.matcher(id.substring(id.indexOf(':') + 1)).find()) ids.add(id); }
        List<int[]> keyed = new ArrayList<>();
        List<String> byIndex = ids;
        for (int i = 0; i < ids.size(); i++) keyed.add(new int[] {hash(seed, i), i});
        keyed.sort(Comparator.comparingInt((int[] k) -> k[0]));
        List<String> out = new ArrayList<>();
        for (int[] k : keyed) out.add(byIndex.get(k[1]));
        return out;
    }

    /** JS: x = (seed * 2654435761 + n * 40503) >>> 0, then the murmur3 finalizer (xor-shift 16, imul 0x85ebca6b, xor-shift 13, imul 0xc2b2ae35, xor-shift 16), as a signed 32-bit int.
     *  The old x ^ (x >>> 13) left the high bits following n, so a long list only ever offered its first ~26 entries. */
    static int hash(double seed, int n) {
        double v = seed * 2654435761.0 + n * 40503.0;
        double m = v % 4294967296.0;
        if (m < 0) m += 4294967296.0;
        int x = (int) (long) m;
        x ^= x >>> 16;
        x *= 0x85ebca6b;
        x ^= x >>> 13;
        x *= 0xc2b2ae35;
        return x ^ (x >>> 16);
    }
}
