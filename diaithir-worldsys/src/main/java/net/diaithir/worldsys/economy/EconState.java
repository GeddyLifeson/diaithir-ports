package net.diaithir.worldsys.economy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The saved economy: stock by town, each culture's running figures, each design's debasement, each culture's bank, companies and quests. Plain fields so Gson can save it. */
public final class EconState {
    public final EconModel.State market = new EconModel.State();
    public final Map<String, Cur> cur = new HashMap<>();
    public final Map<String, Des> des = new HashMap<>();
    public final Map<String, Bank> bank = new HashMap<>();
    public long week;

    /** A culture's money in circulation, the supply it was set against, trade balance, treasury and wage pool. */
    public static final class Cur {
        public double money, ref, tb, flow, vol, infl, weak, treasury, wagePool, arrears, taxMul = 1, rateRef, prevRate;
        public boolean reform;
        public Peg peg;
    }

    /** A coin's first value is pegged to the staple it replaced: perUnit of that good. */
    public static final class Peg { public double perUnit; public String good; }

    public static final class Des { public double debase, rate0; }

    public static final class Pledge { public double value; public List<Item> items = new ArrayList<>(); }
    public static final class Item { public String id; public int n; }

    public static final class Loan { public double owed, collateral, paid; public int late; }
    public static final class GoodsLoan { public double owed, paid; public int late; public Pledge pledge = new Pledge(); }

    public static final class Bank {
        public final Map<String, Double> dep = new HashMap<>();
        public final Map<String, Loan> loans = new HashMap<>();
        public final Map<String, GoodsLoan> goods = new HashMap<>();
        public double equity, infl, lastRate, fund;
        public boolean failed, endowed;
    }

    public Cur cur(String culture) { return cur.computeIfAbsent(culture, k -> new Cur()); }
    public Des des(String did) { return des.computeIfAbsent(did, k -> new Des()); }
    public Bank bank(String culture) { return bank.computeIfAbsent(culture, k -> new Bank()); }
}
