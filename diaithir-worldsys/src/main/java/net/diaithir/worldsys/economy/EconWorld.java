package net.diaithir.worldsys.economy;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.server.MinecraftServer;

/** The running economy on a server: its data, saved state, territory and the market context for a place. */
public final class EconWorld {
    private EconWorld() {}

    private static EconData data;
    private static MinecraftServer server;
    private static List<EconClaims.Claim> claims = new ArrayList<>();
    private static long claimsAt = Long.MIN_VALUE;
    public static final Map<String, double[]> REGIONS = new java.util.HashMap<>();

    public static synchronized EconData data() {
        if (data == null) {
            try { data = EconData.load(); } catch (IOException e) { throw new IllegalStateException("economy data missing from the jar", e); }
        }
        return data;
    }

    public static void server(MinecraftServer s) { server = s; }
    public static MinecraftServer server() { return server; }

    public static EconSave save() { return EconSave.get(server.overworld()); }
    public static EconState state() { return save().state; }
    public static void dirty() { save().setDirty(); }

    /** Territory, refreshed about once a game month: the engine's world simulation if it is loaded, else the historical towns as one culture. */
    public static List<EconClaims.Claim> claims() {
        long now = server.overworld().getGameTime();
        if (claims.isEmpty() || now - claimsAt > 24000L * 30) {
            claimsAt = now;
            List<EconClaims.Claim> fresh = fromEngine();
            if (fresh == null) fresh = fromCities();
            claims = fresh;
            for (EconClaims.Claim c : claims) REGIONS.put(c.town(), new double[] {c.x(), c.z()});
        }
        return claims;
    }

    /** The ages mod keeps the world simulation; asked by name so the economy loads without it. */
    private static List<EconClaims.Claim> fromEngine() {
        try {
            Class<?> saved = Class.forName("net.diaithir.ages.mc.AgesSavedData");
            Object inst = saved.getMethod("get", MinecraftServer.class).invoke(null, server);
            Object sim = inst.getClass().getField("sim").get(inst);
            String json = String.valueOf(sim.getClass().getMethod("toJson").invoke(sim));
            List<EconClaims.Claim> out = EconClaims.fromSim(JsonParser.parseString(json).getAsJsonObject());
            return out.isEmpty() ? null : out;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private static List<EconClaims.Claim> fromCities() {
        List<EconClaims.Claim> out = new ArrayList<>();
        try {
            JsonObject o = EconData.read("cities.json").getAsJsonObject();
            for (var e : o.entrySet()) {
                var a = e.getValue().getAsJsonArray();
                out.add(new EconClaims.Claim(a.get(1).getAsDouble(), a.get(2).getAsDouble(), 600, "diaithir", "diaithir", e.getKey(), a.get(0).getAsString()));
            }
        } catch (IOException ignored) { }
        return out;
    }

    public static EconClaims.Claim claimAt(double x, double z) { return EconClaims.at(claims(), x, z); }

    public static String ageOf(String culture) {
        String a = culture == null ? null : EconHooks.ageOf.apply(culture);
        return a != null && EconPolity.AGES.contains(a) ? a : "VII";
    }

    public static String cultureName(String culture) {
        String n = data().cultureNames.get(culture);
        return n != null ? n : culture;
    }

    /** The money context for a culture, priced at one of its towns (the free market when culture is null). */
    public static EconMarket.Ctx ctx(String town, String culture) {
        EconData d = data();
        EconState st = state();
        String age = culture == null ? "VII" : ageOf(culture);
        return EconMarket.ctx(st, d, culture, age, List.of(Map.of(), d.cultureMoney),
            id -> d.good(id) == null ? 1.0 : EconModel.mid(st.market, REGIONS, d::good, town, id, null, 0), EconWorld::cultureName);
    }

    /** The vendor kinds open to a culture: each Age's kinds up to its own, those whose tech it knows; a kind that recurs gains the later Age's goods. */
    public static List<EconOffers.Vendor> vendors(String culture) {
        int at = Math.max(1, EconPolity.AGES.indexOf(ageOf(culture)));
        java.util.LinkedHashMap<String, List<String>> out = new java.util.LinkedHashMap<>();
        for (EconData.VendorKind v : data().vendors) {
            if (EconPolity.AGES.indexOf(v.age()) > at) continue;
            if (v.needs() != null && !EconHooks.techKnown.test(culture, v.needs())) continue;
            List<String> items = out.computeIfAbsent(v.id(), k -> new ArrayList<>());
            for (String id : v.items().keySet()) if (!items.contains(id)) items.add(id);
        }
        List<EconOffers.Vendor> list = new ArrayList<>();
        out.forEach((k, v) -> list.add(new EconOffers.Vendor(k, v)));
        return list;
    }
}
