package net.diaithir.worldsys.economy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** A culture's money in its market: which design it uses in an Age, and the context (rate, scale, denominations) the trades use. Port of econDesignOf and econCtx. */
public final class EconMarket {
    private EconMarket() {}

    public static final double MONEY_REF = 20000; // tw of money a culture starts with in circulation

    /** The design a culture uses: the first of the tradition maps naming a design of the right kind for the Age, else a stable hash pick among those the Age allows. */
    public static String designOf(Map<String, EconData.DesignData> designs, String culture, EconPolity.Kind kind, List<Map<String, String>> own, String age) {
        for (Map<String, String> m : own) {
            String id = m == null ? null : m.get(culture);
            EconData.DesignData d = id == null ? null : designs.get(id);
            if (d != null && ok(d, kind, age)) return id;
        }
        List<String> ids = new ArrayList<>();
        for (var e : designs.entrySet()) if (ok(e.getValue(), kind, age)) ids.add(e.getKey());
        if (ids.isEmpty()) return null;
        ids.sort(null);
        long h = 0;
        for (int i = 0; i < culture.length(); i++) h = (h * 31 + culture.charAt(i)) & 0xFFFFFFFFL;
        return ids.get((int) (h % ids.size()));
    }

    private static boolean ok(EconData.DesignData d, EconPolity.Kind kind, String age) {
        return kind == EconPolity.Kind.COMMODITY ? !d.commodityDenoms().isEmpty() && EconCompany.ageLe(d.age(), age) : !d.coinDenoms().isEmpty();
    }

    /** The money in play at a town: rate in tw per unit, scale in units per tw, and the coins that make it up. */
    public record Ctx(String culture, String age, EconPolity.Kind kind, String did, EconData.DesignData design, List<EconOffers.Denom> denoms, double rate, double scale, String unit) {}

    /** Builds the context for a culture at a town. A culture null is the free market, which deals in salt. mid gives a good's real price in tw at the town. */
    public static Ctx ctx(EconState st, EconData data, String culture, String age, List<Map<String, String>> own, Function<String, Double> mid, Function<String, String> cultureName) {
        EconPolity.Kind kind = culture != null ? EconPolity.kindAt(age) : EconPolity.Kind.COMMODITY;
        String cul = culture != null ? culture : "wilds";
        String did = culture != null ? designOf(data.designs, cul, kind, own, age) : data.designs.containsKey("salund") ? "salund"
            : data.designs.entrySet().stream().filter(e -> !e.getValue().commodityDenoms().isEmpty()).map(Map.Entry::getKey).sorted().findFirst().orElse(null);
        EconData.DesignData design = data.designs.get(did);
        EconState.Cur s = st.cur(cul);
        String tag = did + ":" + kind.name().toLowerCase();
        if (!tag.equals(s.did)) { // first sight, or the Age moved the culture to another money
            if (kind != EconPolity.Kind.COMMODITY && s.did != null && s.did.endsWith(":commodity")) {
                EconData.DesignData old = data.designs.get(s.did.split(":")[0]);
                if (old != null && old.design().commodityGood() != null) { s.peg = new EconState.Peg(); s.peg.good = old.design().commodityGood(); s.peg.perUnit = old.design().perUnit(); }
            }
            s.did = tag;
            s.ref = s.money = Math.round(MONEY_REF / Math.max(0.01, EconPolity.designValue(st, design.design(), did, kind, mid)));
            s.tb = 0;
        }
        double rate = EconPolity.rate(st, cul, design.design(), did, kind, mid);
        List<EconOffers.Denom> denoms = kind == EconPolity.Kind.COMMODITY ? design.commodityDenoms() : design.coinDenoms();
        return new Ctx(cul, age, kind, did, design, denoms, rate, 1 / rate, design.name() + (culture != null ? " " + cultureName.apply(cul) : ""));
    }
}
