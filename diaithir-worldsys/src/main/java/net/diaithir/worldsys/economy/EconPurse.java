package net.diaithir.worldsys.economy;

import java.util.List;

/** Money in a purse: the coin items a player carries, counted in the smallest unit. Port of econPurse's own-coins part (foreign designs are added by the game side). */
public final class EconPurse {
    private EconPurse() {}

    /** The inventory side: how many of an item are carried, and giving and taking items. */
    public interface Holder {
        int count(String item);
        void take(String item, int n);
        void give(String item, int n);
    }

    public static long own(Holder p, List<EconOffers.Denom> denoms) {
        long sum = 0;
        for (EconOffers.Denom d : denoms) sum += (long) p.count(d.item()) * d.units();
        return sum;
    }

    /** Hands over an amount in coins, largest denomination first. */
    public static void give(Holder p, long amount, List<EconOffers.Denom> denoms) {
        for (EconOffers.Denom d : denoms) {
            long n = amount / d.units();
            amount -= n * d.units();
            if (n > 0) p.give(d.item(), (int) n);
        }
    }

    /** Takes the whole purse and hands back change in the same coins, so any mix of denominations pays. False when the purse is short. */
    public static boolean take(Holder p, long amount, List<EconOffers.Denom> denoms) {
        long purse = own(p, denoms);
        if (purse < amount) return false;
        for (EconOffers.Denom d : denoms) p.take(d.item(), p.count(d.item()));
        give(p, purse - amount, denoms);
        return true;
    }
}
