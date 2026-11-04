package net.diaithir.worldsys.economy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Government bonds (Age V on). Port of 05_bonds.js: the buyer pays face now, the treasury pays a weekly coupon and the face at maturity. */
public final class EconBonds {
    private EconBonds() {}

    public static final int WEEKS = 26, DEFAULT_LATE = 8;
    public static final double MIN_RATE = 0.005;

    public static double rate(EconState st, String culture, double policy) {
        EconState.Cur s = st.cur(culture);
        EconState.Bonds b = st.bonds(culture);
        double premium = Math.min(0.25, 4 * s.arrears / Math.max(1, s.ref)) + (b.blot > 0 ? 0.05 : 0);
        return Math.max(MIN_RATE, policy) + 0.01 + premium;
    }

    /** The caller takes the money from the purse; returns the bond, or null when the offer is too small. */
    public static EconState.Bond issue(EconState st, String culture, String who, double face, double policy) {
        if (face < 1) return null;
        EconState.Cur s = st.cur(culture);
        EconState.Bonds b = st.bonds(culture);
        EconPolity.mint(st, culture, -face);
        s.treasury += face;
        EconState.Bond bond = new EconState.Bond();
        bond.who = who; bond.face = face; bond.rate = rate(st, culture, policy); bond.left = WEEKS;
        b.list.add(bond);
        return bond;
    }

    public static List<String> week(EconState st, String culture) {
        EconState.Cur s = st.cur(culture);
        EconState.Bonds b = st.bonds(culture);
        List<String> notes = new ArrayList<>();
        if (b.blot > 0) b.blot--;
        for (Iterator<EconState.Bond> it = b.list.iterator(); it.hasNext();) {
            EconState.Bond bond = it.next();
            double coupon = bond.face * bond.rate / 52;
            if (s.treasury >= coupon) { s.treasury -= coupon; b.due.merge(bond.who, coupon, Double::sum); bond.late = 0; }
            else { s.arrears += coupon; bond.late++; }
            if (bond.late >= DEFAULT_LATE) { b.blot = 52; notes.add("default: " + bond.who); it.remove(); continue; }
            if (--bond.left > 0) continue;
            if (s.treasury >= bond.face) { s.treasury -= bond.face; b.due.merge(bond.who, bond.face, Double::sum); it.remove(); continue; }
            s.arrears += bond.face;
            bond.left = 1;
            bond.late++;
        }
        return notes;
    }

    /** Coupons and redemptions waiting for a holder; the caller adds them to the purse. */
    public static double collect(EconState st, String culture, String who) {
        EconState.Bonds b = st.bonds(culture);
        double n = b.due.getOrDefault(who, 0.0);
        b.due.put(who, 0.0);
        EconPolity.mint(st, culture, n);
        return n;
    }
}
