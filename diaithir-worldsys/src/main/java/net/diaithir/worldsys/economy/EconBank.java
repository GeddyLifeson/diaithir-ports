package net.diaithir.worldsys.economy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Banks, credit and interest. Port of 02_bank.js. Age III-IV: a temple vault that lends nothing from deposits (reserve ratio 1).
 * Age V on: a fractional-reserve bank whose loans create money and whose repayments destroy it.
 */
public final class EconBank {
    private EconBank() {}

    public static final Map<String, Double> RESERVE = Map.of("III", 1.0, "IV", 1.0, "V", 0.2, "VI", 0.1, "VII", 0.1, "VIII", 0.1, "RODOS", 0.1);
    public static final double FEE_WEEK = 0.0005, COLLATERAL = 0.5, SPREAD = 0.015, VAULT_RATE = 0.06, VAULT_COVER = 1.5, VAULT_SALE = 0.8, LOAN_FEE = 0.01;
    public static final int DEFAULT_WEEKS = 8;
    public static final double VAULT_ENDOWMENT = 200;

    public record Result(boolean ok, double amount, String why) {
        static Result no(String why) { return new Result(false, 0, why); }
    }
    public record Rates(double policy, double lend, double save) {}
    public record Repaid(double taken, List<EconState.Item> items) {}

    public static double reserve(String age) { return RESERVE.getOrDefault(age, 0.0); }
    public static double deposits(EconState.Bank b) { return b.dep.values().stream().mapToDouble(Double::doubleValue).sum(); }
    public static double loans(EconState.Bank b) { return b.loans.values().stream().mapToDouble(l -> l.owed).sum(); }
    public static double locked(EconState.Bank b, String who) { EconState.Loan l = b.loans.get(who); return l == null ? 0 : l.collateral; }

    /** How much more the bank may lend: deposits / reserve ratio is the most it can have out in all, less what it has. */
    public static double capacity(EconState.Bank b, String age) {
        double rr = reserve(age);
        if (rr == 0 || b.failed) return 0;
        return Math.max(0, deposits(b) * (1 / rr - 1) - loans(b));
    }

    public static Rates rates(EconState.Bank b, String age) {
        double rr = reserve(age);
        double policy = Math.max(0.005, b.infl + 0.02);
        double use = rr != 0 && deposits(b) != 0 ? Math.min(1, loans(b) / (deposits(b) * (1 / rr - 1))) : 0;
        double lend = policy + 0.01 + 0.08 * use * use;
        return new Rates(policy, lend, rr < 1 && loans(b) > 0 ? Math.max(0, policy - SPREAD) : 0);
    }

    public static boolean deposit(EconState.Bank b, String who, double amount) {
        if (b.failed || amount <= 0) return false;
        b.dep.merge(who, amount, Double::sum);
        return true;
    }

    /** Takes out what is not locked as collateral; returns the amount handed over. */
    public static double withdraw(EconState.Bank b, String who, double amount) {
        if (amount <= 0) return 0;
        double free = Math.max(0, b.dep.getOrDefault(who, 0.0) - locked(b, who));
        double n = Math.min(amount, free);
        b.dep.merge(who, -n, Double::sum);
        return n;
    }

    public static Result borrow(EconState.Bank b, String who, double amount, String age) {
        if (b.failed) return Result.no("The bank has failed.");
        if (reserve(age) >= 1 || reserve(age) == 0) return Result.no("There is no lender here.");
        if (b.loans.containsKey(who)) return Result.no("Pay what you owe first.");
        double need = amount * COLLATERAL;
        if (b.dep.getOrDefault(who, 0.0) < need) return Result.no("A loan of " + fmt(amount) + " needs " + fmt(need) + " kept in your deposit.");
        if (amount > capacity(b, age)) return Result.no("The bank has no more to lend.");
        EconState.Loan l = new EconState.Loan();
        l.owed = amount * (1 + LOAN_FEE);
        l.collateral = need;
        b.loans.put(who, l);
        return new Result(true, amount, null);
    }

    private static String fmt(double d) { return d == Math.rint(d) ? String.valueOf((long) d) : String.valueOf(d); }

    /** A payment on a loan from the purse; returns the amount taken. */
    public static double repay(EconState.Bank b, String who, double amount) {
        EconState.Loan l = b.loans.get(who);
        if (l == null) return 0;
        double n = Math.min(amount, l.owed);
        l.owed -= n;
        l.paid += n;
        l.late = 0;
        if (l.owed <= 1e-9) b.loans.remove(who);
        return n;
    }

    /** Once a week. Returns notes such as "default: who" or "The bank fails ...". */
    public static List<String> week(EconState.Bank b, String age, double rateNow) {
        List<String> notes = new ArrayList<>();
        if (b.lastRate != 0) b.infl = Math.max(0, Math.min(1, (b.lastRate / rateNow - 1) * 52));
        b.lastRate = rateNow;
        if (b.failed) return notes;
        Rates r = rates(b, age);
        if (reserve(age) >= 1) {
            for (String k : new ArrayList<>(b.dep.keySet())) { double fee = b.dep.get(k) * FEE_WEEK; b.dep.merge(k, -fee, Double::sum); b.fund += fee; }
            for (String k : new ArrayList<>(b.goods.keySet())) {
                EconState.GoodsLoan l = b.goods.get(k);
                l.owed += l.owed * VAULT_RATE / 52;
                l.late = l.paid > 0 ? 0 : l.late + 1;
                l.paid = 0;
                if (l.late >= DEFAULT_WEEKS) { b.fund += l.pledge.value * VAULT_SALE; b.goods.remove(k); notes.add("forfeit: " + k); }
            }
            return notes;
        }
        double income = 0;
        for (String k : new ArrayList<>(b.loans.keySet())) {
            EconState.Loan l = b.loans.get(k);
            double interest = l.owed * r.lend() / 52;
            l.owed += interest;
            income += interest;
            l.late = l.paid > 0 ? 0 : l.late + 1;
            l.paid = 0;
            if (l.late >= DEFAULT_WEEKS) {
                double taken = Math.min(l.collateral, b.dep.getOrDefault(k, 0.0));
                b.dep.merge(k, -taken, Double::sum);
                b.equity += taken - l.owed;
                b.loans.remove(k);
                notes.add("default: " + k);
            }
        }
        double paid = 0;
        for (String k : new ArrayList<>(b.dep.keySet())) { double i = b.dep.get(k) * r.save() / 52; b.dep.merge(k, i, Double::sum); paid += i; }
        b.equity += income - paid;
        if (b.equity < 0 && !b.failed) {
            b.failed = true;
            double owed = deposits(b);
            double share = owed != 0 ? Math.max(0, Math.min(1, (owed + b.equity) / owed)) : 1;
            b.dep.replaceAll((k, v) -> v * share);
            b.loans.clear();
            notes.add("The bank fails; depositors get back " + Math.round(share * 100) + "%.");
        }
        return notes;
    }

    /** A temple vault starts with an endowment, once: treasury, then wage pool, then the ruler makes up the rest. */
    public static void vaultEndow(EconState st, String culture, EconState.Bank b) {
        if (b.endowed) return;
        b.endowed = true;
        EconState.Cur s = st.cur(culture);
        double want = VAULT_ENDOWMENT;
        double t = Math.min(want, s.treasury); s.treasury -= t; want -= t;
        double w = Math.min(want, s.wagePool); s.wagePool -= w; want -= w;
        if (want > 0) s.money += want;
        b.fund += VAULT_ENDOWMENT;
    }

    public static Result vaultBorrow(EconState.Bank b, String who, double amount, EconState.Pledge pledge, String age) {
        if (reserve(age) < 1) return Result.no("Only a temple vault lends against goods.");
        if (b.failed) return Result.no("The vault has been emptied.");
        if (b.goods.containsKey(who)) return Result.no("Redeem your pledge first.");
        if (amount < 1) return Result.no("Ask for at least one coin.");
        if (amount > b.fund) return Result.no("The temple can lend only " + (long) Math.floor(b.fund) + ".");
        if (pledge.value < amount * VAULT_COVER) return Result.no("The goods must be worth " + (long) Math.ceil(amount * VAULT_COVER) + "; these are worth " + (long) Math.floor(pledge.value) + ".");
        b.fund -= amount;
        EconState.GoodsLoan l = new EconState.GoodsLoan();
        l.owed = amount;
        l.pledge = pledge;
        b.goods.put(who, l);
        return new Result(true, amount, null);
    }

    public static Repaid vaultRepay(EconState.Bank b, String who, double amount) {
        EconState.GoodsLoan l = b.goods.get(who);
        if (l == null) return new Repaid(0, List.of());
        double n = Math.min(amount, l.owed);
        l.owed -= n; l.paid += n; l.late = 0;
        b.fund += n;
        if (l.owed > 1e-9) return new Repaid(n, List.of());
        b.goods.remove(who);
        return new Repaid(n, l.pledge.items);
    }
}
