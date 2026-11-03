package net.diaithir.worldsys.economy;

import java.util.List;
import java.util.function.Function;

/**
 * The company quest chain. "Safe passage": after a few talks with working people, one of them hands over a load for another town; delivering it
 * opens "Found a company", which needs an office (EconCompany.officeCheck). Port of caravanTalk and /company found. The game side supplies the player.
 */
public final class EconQuests {
    private EconQuests() {}

    public static final int QTY = 12, WEEKS = 4, TALKS = 3;

    /** What the quests need from a player and the world; the NeoForge side implements it, tests use a fake. */
    public interface Player extends EconPurse.Holder {
        String id();
        void tell(String text);
        void command(String command);
        String itemName(String item);
    }

    /** Where the player stands and what the town knows. */
    public record Place(String town, String name, String culture, String age) {}

    public record World(EconState state, EconData data, List<EconClaims.Claim> claims, Function<String, EconMarket.Ctx> ctxOf, boolean companiesOn) {}

    /** The player talked to a working person (profession 'none' for a wanderer). */
    public static void talk(World w, Player p, Place at, String profession) {
        if (!w.companiesOn() || at.culture() == null) return;
        EconState.PlayerQuest q = w.state().quest(p.id());
        if ("done".equals(q.caravan)) return;
        EconState.CaravanJob job = q.job;
        if (job != null && "active".equals(q.caravan)) {
            if (!at.town().equals(job.to)) return;
            int have = p.count(job.id);
            if (have < job.n) { p.tell(job.name + " is expecting " + job.n + " " + p.itemName(job.id) + " from " + job.fromName + ". You carry " + have + "."); return; }
            p.take(job.id, job.n);
            EconMarket.Ctx ctx = w.ctxOf().apply(at.culture());
            boolean late = w.state().week - job.week > WEEKS;
            double[] price = w.data().prices.get(job.id);
            long tw = EconCompany.reward(price == null ? 1 : price[0], job.n, late);
            double paid = EconPolity.payWage(w.state(), at.culture(), tw);
            long units = (long) Math.ceil(paid * ctx.scale());
            if (paid > 0) EconPurse.give(p, units, ctx.denoms());
            q.caravan = "done"; q.job = null;
            p.tell(late ? "The load arrives late but whole." : "The load arrives safe and on time.");
            p.tell(paid > 0 ? "You are paid " + units + " " + ctx.unit() + "." : "The town's till is empty, so it owes you.");
            p.tell("\"You do this well. You could do it for a living, with an office, a ledger and a few carts.\"");
            p.tell("New misc quest: Found a company. See /company quest.");
            q.company = "offered";
            p.command("ages quest report caravan delivered");
            return;
        }
        if (++q.talks < TALKS) return;
        EconCompany.Claim to = null;
        List<EconCompany.Claim> cl = w.claims().stream().map(c -> new EconCompany.Claim(c.town(), c.x(), c.z(), c.culture())).toList();
        to = EconCompany.dest(cl, at.town());
        if (to == null) return;
        EconCompany.Job j = EconCompany.job(profession, at.age());
        q.talks = 0;
        q.caravan = "active";
        EconState.CaravanJob nj = new EconState.CaravanJob();
        nj.id = j.id(); nj.n = QTY; nj.from = at.town(); nj.fromName = at.name(); nj.to = to.town();
        nj.name = w.claims().stream().filter(c -> c.town().equals(nj.to)).findFirst().map(EconClaims.Claim::name).orElse(to.town());
        nj.week = w.state().week;
        q.job = nj;
        p.tell("\"" + j.line() + "\"");
        p.tell("New misc quest: Safe passage. Take " + QTY + " " + p.itemName(j.id()) + " to " + nj.name + ", within " + WEEKS + " weeks. Talk to anyone there who works.");
        p.give(j.id(), QTY);
        p.command("ages quest report caravan accepted");
    }
}
