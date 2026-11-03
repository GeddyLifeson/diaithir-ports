package net.diaithir.worldsys.economy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The saved economy: stock by town, each culture's running figures, each design's debasement, each culture's bank, companies and quests. Plain fields so Gson can save it. */
public final class EconState {
    public final EconModel.State market = new EconModel.State();
    public final Map<String, Cur> cur = new HashMap<>();
    /** A culture's own money design, set when it reforms. */
    public final Map<String, String> own = new HashMap<>();
    public final Map<String, Des> des = new HashMap<>();
    public final Map<String, Bank> bank = new HashMap<>();
    public final Map<String, Bonds> bonds = new HashMap<>();
    public final Map<String, Company> company = new HashMap<>();
    public final Map<String, Double> season = new HashMap<>();
    public final Map<String, ShareCompany> shares = new HashMap<>();
    public final Map<String, Map<String, Integer>> holdings = new HashMap<>();
    public final Map<String, Double> div = new HashMap<>();
    public final Map<String, PlayerQuest> quests = new HashMap<>();
    public long week;

    /** A culture's money in circulation, the supply it was set against, trade balance, treasury and wage pool. */
    public static final class Cur {
        public double money, ref, tb, flow, vol, infl, weak, treasury, wagePool, arrears, taxMul = 1, rateRef, prevRate;
        public boolean reform;
        public String did;
        public Peg peg;
    }

    /** A coin's first value is pegged to the staple it replaced: perUnit of that good. */
    public static final class Peg { public double perUnit; public String good; }

    public static final class Des { public double debase, rate0, worn; }

    /** A joint-stock company on the exchange. */
    public static final class ShareCompany { public String name, culture; public double float_ = 1000, fair = 100, z; }

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

    public static final class Bond { public String who; public double face, rate; public int left, late; }
    public static final class Bonds { public List<Bond> list = new ArrayList<>(); public final Map<String, Double> due = new HashMap<>(); public int blot; }

    public static final class Line { public String from, to, vehicle, cargo; public int n = 1, weeks, units; public double upkeep, rev, cost; }
    public static final class Company { public String name, home; public double money, loan; public List<Line> lines = new ArrayList<>(); public int broke; public long founded; public boolean closed; }

    /** A player's progress in the company quests: Safe passage (caravan) and Found a company. */
    public static final class PlayerQuest {
        public String caravan, company;
        public int talks;
        public CaravanJob job;
    }
    public static final class CaravanJob { public String id, from, fromName, to, name; public int n; public long week; }

    public PlayerQuest quest(String player) { return quests.computeIfAbsent(player, k -> new PlayerQuest()); }
    public Bonds bonds(String culture) { return bonds.computeIfAbsent(culture, k -> new Bonds()); }
    public Cur cur(String culture) { return cur.computeIfAbsent(culture, k -> new Cur()); }
    public Des des(String did) { return des.computeIfAbsent(did, k -> new Des()); }
    public Bank bank(String culture) { return bank.computeIfAbsent(culture, k -> new Bank()); }
}
