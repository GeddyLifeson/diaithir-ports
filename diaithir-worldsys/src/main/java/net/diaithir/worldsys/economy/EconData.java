package net.diaithir.worldsys.economy;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The economy's data files, read from the jar (data/diaithirworldsys/economy): prices, currency designs, vendors by Age, drink values and culture tables. */
public final class EconData {
    public static final String ROOT = "/data/diaithirworldsys/economy/";
    public static final List<String> VENDOR_FILES = List.of("vendors.json", "vendors_age2.json", "vendors_age3.json", "vendors_age4.json", "vendors_age5.json",
        "vendors_age6.json", "vendors_age7.json", "vendors_age8.json", "vendors_age9.json");

    /** A currency design as the data names it: the staple (commodity) and the coin of metal, each with its denominations (item, units of the smallest). */
    public record DesignData(String name, String age, EconPolity.Design design, List<EconOffers.Denom> commodityDenoms, List<EconOffers.Denom> coinDenoms) {}

    /** A vendor kind with the Age it first opens, the tech it needs and its items with price and stock. */
    public record VendorKind(String id, String name, String age, String needs, Map<String, double[]> items) {}

    public final Map<String, double[]> prices = new HashMap<>();
    public final Map<String, DesignData> designs = new LinkedHashMap<>();
    /** The Age each good opens in (tech_ages.json: items beat namespaces); a good in neither is Age I. */
    /** The hand-priced staples (ECON_GOODS): id to [base price, usual stock]; their opening Age is in goodAges. */
    public final Map<String, double[]> goods = new HashMap<>();
    public final Map<String, String> goodAges = new HashMap<>();
    /** Item id to the Age the progression graph first has a node making it (gates.json, from plans/progression/graph.json). */
    public final Map<String, String> gates = new HashMap<>();
    public final Map<String, String> techItems = new HashMap<>(), techNamespaces = new HashMap<>();
    public final Map<String, String> cultureMoney = new HashMap<>();
    public final Map<String, String> cultureNames = new HashMap<>();
    public final Map<String, EconDrinks.Style> drinks = new HashMap<>();
    public final List<VendorKind> vendors = new ArrayList<>();

    public static JsonElement read(String file) throws IOException {
        try (var in = EconData.class.getResourceAsStream(ROOT + file)) {
            if (in == null) throw new IOException("missing " + file);
            try (Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) { return JsonParser.parseReader(r); }
        }
    }

    public static EconData load() throws IOException {
        EconData d = new EconData();
        for (var e : read("prices.json").getAsJsonObject().entrySet()) { JsonArray a = e.getValue().getAsJsonArray(); d.prices.put(e.getKey(), new double[] {a.get(0).getAsDouble(), a.get(1).getAsDouble()}); }
        for (var e : read("currencies.json").getAsJsonObject().entrySet()) {
            JsonObject o = e.getValue().getAsJsonObject();
            JsonObject com = o.has("commodity") ? o.getAsJsonObject("commodity") : null, coin = o.has("coin") ? o.getAsJsonObject("coin") : null;
            EconPolity.Design design = new EconPolity.Design(com != null ? com.get("good").getAsString() : null, com != null ? com.get("perUnit").getAsDouble() : 0,
                coin != null ? coin.get("metal").getAsString() : null, coin != null ? coin.get("content").getAsDouble() : 0);
            d.designs.put(e.getKey(), new DesignData(o.get("name").getAsString(), o.has("age") ? o.get("age").getAsString() : null, design,
                com != null ? denoms(com.getAsJsonArray("denoms")) : List.of(), coin != null ? denoms(coin.getAsJsonArray("denoms")) : List.of()));
        }
        for (var e : read("culture_money.json").getAsJsonObject().entrySet()) d.cultureMoney.put(e.getKey(), e.getValue().getAsString());
        for (var e : read("culture_names.json").getAsJsonObject().entrySet()) d.cultureNames.put(e.getKey(), e.getValue().getAsString());
        for (var e : read("drink_value.json").getAsJsonObject().entrySet()) {
            JsonObject o = e.getValue().getAsJsonObject();
            d.drinks.put(e.getKey(), new EconDrinks.Style(o.has("abv") ? o.get("abv").getAsDouble() : 0, o.has("aged") && o.get("aged").getAsBoolean(), o.has("peak") ? o.get("peak").getAsDouble() : 0));
        }
        for (var e : read("goods.json").getAsJsonObject().entrySet()) {
            JsonArray a = e.getValue().getAsJsonArray();
            d.goods.put(e.getKey(), new double[] {a.get(0).getAsDouble(), a.get(1).getAsDouble()});
            d.goodAges.put(e.getKey(), a.get(2).getAsString());
        }
        for (var e : read("derived_prices.json").getAsJsonObject().entrySet()) { // crafted items priced from their recipes (tools/gen_derived_prices.py)
            JsonArray a = e.getValue().getAsJsonArray();
            d.goods.put(e.getKey(), new double[] {a.get(0).getAsDouble(), a.get(1).getAsDouble()});
            d.goodAges.put(e.getKey(), a.get(2).getAsString());
        }
        for (var e : read("gates.json").getAsJsonObject().entrySet()) d.gates.put(e.getKey(), e.getValue().getAsJsonArray().get(0).getAsString());
        JsonObject ta = read("tech_ages.json").getAsJsonObject();
        for (var e : ta.getAsJsonObject("items").entrySet()) d.techItems.put(e.getKey(), e.getValue().getAsString());
        for (var e : ta.getAsJsonObject("namespaces").entrySet()) d.techNamespaces.put(e.getKey(), e.getValue().getAsString());
        for (String f : VENDOR_FILES) {
            JsonObject root = read(f).getAsJsonObject();
            String age = root.get("age").getAsString();
            for (JsonElement ve : root.getAsJsonArray("vendors")) {
                JsonObject v = ve.getAsJsonObject();
                Map<String, double[]> items = new LinkedHashMap<>();
                for (var it : v.getAsJsonObject("items").entrySet()) { JsonObject io = it.getValue().getAsJsonObject(); items.put(it.getKey(), new double[] {io.get("price").getAsDouble(), io.has("stock") ? io.get("stock").getAsDouble() : 0}); }
                d.vendors.add(new VendorKind(v.get("id").getAsString(), v.has("name") ? v.get("name").getAsString() : v.get("id").getAsString(), age, v.has("needs") ? v.get("needs").getAsString() : null, items));
            }
        }
        return d;
    }

    private static List<EconOffers.Denom> denoms(JsonArray a) {
        List<EconOffers.Denom> out = new ArrayList<>();
        for (JsonElement e : a) out.add(new EconOffers.Denom(e.getAsJsonArray().get(0).getAsString(), e.getAsJsonArray().get(1).getAsLong()));
        return out;
    }

    /** A good's [base price in tw, usual stock]; the usual stock follows the price (dearer goods are scarcer), as the KubeJS econGood. */
    public EconModel.Good good(String id) {
        double[] g = goods.get(id);
        if (g != null) return new EconModel.Good(g[0], g[1]);
        double[] p = prices.get(id);
        return p == null ? null : new EconModel.Good(p[0], Math.max(8, Math.round(256 / Math.sqrt(p[0]))));
    }

    /** Goods open to trade in an Age: those whose opening Age is not later. */
    public List<String> openIds(String age) {
        int a = EconPolity.AGES.indexOf(age);
        List<String> out = new ArrayList<>();
        for (String id : allIds()) {
            String g = ageOfGood(id);
            if (EconPolity.AGES.indexOf(g == null ? "I" : g) <= a) out.add(id);
        }
        return out;
    }

    /** The Age a good opens in: the hook if a tech module answers, else the shipped tech_ages table. */
    public String ageOfGood(String id) {
        String own = goodAges.get(id);
        if (own != null) return own;
        String gate = gates.get(id); // the graph's Age: the item's first node, gated as opens_age + tier - 1 with tier 1
        if (gate != null) return TechBridge.gateAge(gate, 1);
        String h = EconHooks.goodAge.apply(id);
        if (h != null && !"I".equals(h)) return h;
        String a = techItems.get(id);
        if (a == null) a = techNamespaces.get(id.substring(0, Math.max(0, id.indexOf(':'))));
        return a != null && EconPolity.AGES.contains(a) ? a : "I";
    }

    /** Every priced good: the table and the hand-priced staples. */
    public java.util.Set<String> allIds() {
        java.util.Set<String> s = new java.util.LinkedHashSet<>(prices.keySet());
        s.addAll(goods.keySet());
        return s;
    }
}
