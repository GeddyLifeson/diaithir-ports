package net.diaithir.worldsys.economy;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The Codex price table: &lt;world&gt;/diaithir/prices.json, built from the live market. Port of econTable and econPublishTable. */
public final class EconTable {
    private EconTable() {}

    private static double r2(double v) { return Math.round(v * 100) / 100.0; }
    private static double r4(double v) { return Math.round(v * 10000) / 10000.0; }

    public static JsonObject build(EconState st, EconData data, List<EconClaims.Claim> claims, Map<String, double[]> regions,
                                   java.util.function.BiFunction<String, String, EconMarket.Ctx> ctxOf, String date, long day) {
        Map<String, List<String>> traders = new HashMap<>();
        for (var r : st.market.stock.entrySet()) for (String id : r.getValue().keySet()) traders.computeIfAbsent(id, k -> new java.util.ArrayList<>()).add(r.getKey());
        JsonObject base = new JsonObject(), prices = new JsonObject(), markets = new JsonObject(), rates = new JsonObject();
        for (String id : data.prices.keySet()) {
            double b = data.good(id).base();
            base.addProperty(id, b);
            List<String> rs = traders.get(id);
            double mean = b;
            if (rs != null) { double t = 0; for (String r : rs) t += EconModel.mid(st.market, regions, data::good, r, id, null, 0); mean = t / rs.size(); }
            prices.addProperty(id, r2(mean));
        }
        Map<String, EconMarket.Ctx> ctxs = new HashMap<>();
        for (EconClaims.Claim c : claims) {
            EconMarket.Ctx ctx = ctxs.computeIfAbsent(c.culture(), k -> ctxOf.apply(c.town(), c.culture()));
            rates.addProperty(ctx.unit(), r4(ctx.rate()));
            JsonObject mk = new JsonObject();
            mk.addProperty("name", c.name());
            mk.addProperty("culture", EconWorld.cultureName(c.culture()));
            mk.addProperty("currency", ctx.unit());
            mk.addProperty("perTw", r4(ctx.scale()));
            JsonArray terr = new JsonArray();
            JsonObject t = new JsonObject();
            t.addProperty("x", c.x()); t.addProperty("z", c.z()); t.addProperty("r", c.r());
            terr.add(t);
            mk.add("territory", terr);
            JsonObject pr = new JsonObject();
            for (String id : st.market.stock.getOrDefault(c.town(), Map.of()).keySet())
                if (data.good(id) != null) pr.addProperty(id, r2(EconModel.mid(st.market, regions, data::good, c.town(), id, null, 0) * ctx.scale()));
            mk.add("prices", pr);
            markets.add(c.town(), mk);
        }
        JsonObject out = new JsonObject();
        out.addProperty("updated", date);
        out.addProperty("updatedDay", day);
        out.addProperty("currency", "tw");
        out.add("prices", prices);
        out.add("base", base);
        out.add("rates", rates);
        out.add("markets", markets);
        return out;
    }

    public static void write(Path worldRoot, JsonObject table) throws IOException {
        Path f = worldRoot.resolve("diaithir").resolve("prices.json");
        Files.createDirectories(f.getParent());
        Files.writeString(f, new GsonBuilder().create().toJson(table), StandardCharsets.UTF_8);
    }
}
