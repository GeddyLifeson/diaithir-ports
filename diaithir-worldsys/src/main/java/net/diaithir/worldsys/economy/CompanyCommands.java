package net.diaithir.worldsys.economy;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.regex.Pattern;

/** /company found, fund, withdraw, line, add, borrow, repay, vehicles. Port of company.js. */
final class CompanyCommands {
    private CompanyCommands() {}

    private static final Pattern PASS = Pattern.compile("(torch|lantern|carpet|button|sign|banner|flower|rail|pressure_plate|grass|vine|candle)");

    private record Ctx(ServerPlayer p, ServerPlayerHolder h, EconClaims.Claim at, EconMarket.Ctx m) {}

    private static String key(ServerPlayer p) { return p.getUUID().toString(); }

    private static int run(CommandContext<CommandSourceStack> c, Consumer<Ctx> fn) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        ServerPlayerHolder h = new ServerPlayerHolder(p);
        EconClaims.Claim at = EconWorld.claimAt(p.getX(), p.getZ());
        fn.accept(new Ctx(p, h, at, at == null ? null : EconWorld.ctx(at.town(), at.culture())));
        EconWorld.dirty();
        return Command.SINGLE_SUCCESS;
    }

    private static void mine(Ctx x, Consumer<EconState.Company> then) {
        EconState.Company co = EconWorld.state().company.get(key(x.p()));
        if (co == null) x.h().tell("You have no company. /company found <name> in a built office.");
        else if (co.closed) x.h().tell("Your company is bankrupt.");
        else then.accept(co);
    }

    private interface Amount { void go(Ctx x, EconState.Company co, int n); }

    private static com.mojang.brigadier.builder.ArgumentBuilder<CommandSourceStack, ?> amountCmd(String name, Amount fn) {
        return Commands.literal(name).then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes(c ->
            run(c, x -> mine(x, co -> fn.go(x, co, IntegerArgumentType.getInteger(c, "amount"))))));
    }

    private static boolean home(Ctx x, EconState.Company co, String what) {
        if (x.at() == null || !x.at().culture().equals(co.home)) { x.h().tell(what + " in the company's home culture."); return false; }
        return true;
    }

    static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("company")
            .executes(c -> run(c, x -> {
                EconState.Company co = EconWorld.state().company.get(key(x.p()));
                if (co == null) { x.h().tell("No company yet. /company found <name> in a built office."); return; }
                String unit = EconWorld.ctx(null, co.home).unit();
                x.h().tell(co.name + (co.closed ? " (bankrupt)" : "") + ": " + (long) Math.floor(co.money) + " " + unit + ", owes " + (long) Math.floor(co.loan) + ".");
                if (co.lines.isEmpty()) { x.h().tell("No lines. /company towns, /company vehicles, then /company line <from> <to> <vehicle>."); return; }
                int i = 1;
                for (EconState.Line l : co.lines) {
                    EconCompany.Vehicle v = EconCompany.vehicle(l.vehicle);
                    x.h().tell(i++ + ". " + l.from + " to " + l.to + ", " + l.n + " x " + (v == null ? l.vehicle : v.name()) + ": " + l.units + " units hauled over " + l.weeks
                        + " weeks, profit " + (long) Math.floor(EconCompany.profit(l)) + (l.cargo != null ? ", now " + x.h().itemName(l.cargo) : "") + ".");
                }
            }))
            .then(Commands.literal("towns").executes(c -> run(c, x -> {
                EconState.Company co = EconWorld.state().company.get(key(x.p()));
                String culture = co != null ? co.home : x.at() == null ? null : x.at().culture();
                if (culture == null) { x.h().tell("Stand in a culture's town or found a company."); return; }
                var ids = EconWorld.data().openIds(EconWorld.ageOf(culture));
                EconState st = EconWorld.state();
                for (EconClaims.Claim t : EconWorld.claims()) {
                    if (!t.culture().equals(culture)) continue;
                    var gaps = new java.util.ArrayList<java.util.Map.Entry<String, Double>>();
                    for (String id : ids) gaps.add(java.util.Map.entry(id, EconCompany.gap(st, EconWorld.data()::good, t.town(), id)));
                    String want = gaps.stream().filter(g -> g.getValue() < 0).sorted(java.util.Map.Entry.comparingByValue()).limit(3).map(g -> x.h().itemName(g.getKey()) + " " + (long) Math.floor(-g.getValue())).collect(java.util.stream.Collectors.joining(", "));
                    String have = gaps.stream().filter(g -> g.getValue() > 0).sorted(java.util.Map.Entry.<String, Double>comparingByValue().reversed()).limit(3).map(g -> x.h().itemName(g.getKey()) + " " + (long) Math.floor(g.getValue())).collect(java.util.stream.Collectors.joining(", "));
                    x.h().tell(t.town() + " (" + t.name() + "): wants " + (want.isEmpty() ? "nothing" : want) + "; has spare " + (have.isEmpty() ? "nothing" : have) + ".");
                }
            })))
            .then(Commands.literal("found").then(Commands.argument("name", StringArgumentType.string()).executes(c -> run(c, x -> found(x, StringArgumentType.getString(c, "name"))))))
            .then(amountCmd("fund", (x, co, n) -> {
                if (!home(x, co, "Fund the company")) return;
                if (!EconPurse.take(x.h(), n, x.m().denoms())) { x.h().tell("You have " + EconPurse.own(x.h(), x.m().denoms()) + " " + x.m().unit() + "."); return; }
                co.money += n;
                x.h().tell("Paid in " + n + " " + x.m().unit() + ".");
            }))
            .then(amountCmd("withdraw", (x, co, n) -> {
                if (!home(x, co, "Take money out")) return;
                int t = (int) Math.min(n, Math.floor(co.money));
                if (t < 1) { x.h().tell("The company has no money to spare."); return; }
                co.money -= t;
                EconPurse.give(x.h(), t, x.m().denoms());
                x.h().tell("Took out " + t + " " + x.m().unit() + ".");
            }))
            .then(Commands.literal("vehicles").executes(c -> run(c, x -> {
                String culture = x.at() == null ? null : x.at().culture();
                if (culture == null) { x.h().tell("Stand in a culture's town."); return; }
                for (EconCompany.Vehicle v : vehicles(culture))
                    x.h().tell(v.id() + ": " + v.name() + ", carries " + v.cap() + ", " + v.speed() + " blocks a day, costs " + v.cost() + " tw, upkeep " + v.upkeep() + " tw a week.");
            })))
            .then(Commands.literal("line").then(Commands.argument("from", StringArgumentType.string()).then(Commands.argument("to", StringArgumentType.string())
                .then(Commands.argument("vehicle", StringArgumentType.string()).executes(c -> run(c, x -> mine(x, co -> line(x, co,
                    StringArgumentType.getString(c, "from"), StringArgumentType.getString(c, "to"), StringArgumentType.getString(c, "vehicle")))))))))
            .then(amountCmd("add", (x, co, n) -> {
                if (n > co.lines.size()) { x.h().tell("No such line."); return; }
                EconState.Line l = co.lines.get(n - 1);
                EconCompany.Vehicle v = EconCompany.vehicle(l.vehicle);
                long price = (long) Math.ceil(v.cost() * scale(co.home));
                if (co.money < price) { x.h().tell("Another " + v.name() + " costs " + price + "; the company has " + (long) co.money + "."); return; }
                co.money -= price;
                EconWorld.state().cur(co.home).treasury += price;
                l.n++;
                x.h().tell("Line " + n + " now runs " + l.n + " " + v.name() + "s.");
            }))
            .then(amountCmd("borrow", CompanyCommands::borrow))
            .then(amountCmd("repay", CompanyCommands::repay));
    }

    private static double scale(String culture) { return EconWorld.ctx(null, culture).scale(); }

    private static List<EconCompany.Vehicle> vehicles(String culture) {
        return EconCompany.vehicles(EconWorld.ageOf(culture), t -> EconHooks.techKnown.test(culture, t), EconHooks.calendarYear.get());
    }

    private static void found(Ctx x, String name) {
        EconState st = EconWorld.state();
        EconState.PlayerQuest q = st.quest(key(x.p()));
        if (x.at() == null) { x.h().tell("A company is founded in a culture's town."); return; }
        if (st.company.containsKey(key(x.p()))) { x.h().tell("You have a company already."); return; }
        if (!"offered".equals(q.company)) { x.h().tell("No one has asked you to found a company yet. Help the people in the towns first: talk to the working people."); return; }
        var level = x.p().level();
        EconCompany.Office r = EconCompany.officeCheck((bx, by, bz) -> {
            BlockState s = level.getBlockState(new BlockPos(bx, by, bz));
            String id = BuiltInRegistries.BLOCK.getKey(s.getBlock()).toString();
            boolean air = id.endsWith(":air") || id.endsWith(":cave_air") || id.endsWith(":void_air");
            return new EconCompany.Block(id, air || (PASS.matcher(id).find() && !id.endsWith("_door")));
        }, new int[] {(int) Math.floor(x.p().getX()), (int) Math.floor(x.p().getY()), (int) Math.floor(x.p().getZ())}, EconWorld.ageOf(x.at().culture()), t -> EconHooks.techKnown.test(x.at().culture(), t));
        if (!r.ok()) {
            x.h().tell("You cannot found a company here yet. Build an office and stand in it. It still needs:");
            for (String m : r.missing()) x.h().tell(" - " + m);
            return;
        }
        if (!EconPurse.take(x.h(), r.fee(), x.m().denoms())) { x.h().tell("The charter needs " + r.fee() + " " + x.m().unit() + " as starting capital. You have " + EconPurse.own(x.h(), x.m().denoms()) + "."); return; }
        EconState.Company co = EconCompany.found(st, key(x.p()), name, x.at().culture());
        co.money += r.fee();
        q.company = "done";
        x.h().command("ages quest report company found");
        x.h().tell(name + " is founded in " + x.at().name() + " with " + r.fee() + " in the till. Fund it with /company fund <amount>.");
    }

    private static void line(Ctx x, EconState.Company co, String from, String to, String vid) {
        List<String> towns = EconWorld.claims().stream().filter(c -> c.culture().equals(co.home)).map(EconClaims.Claim::town).toList();
        if (from.equals(to) || !towns.contains(from) || !towns.contains(to)) { x.h().tell("A line joins two different towns of your culture."); return; }
        EconCompany.Vehicle v = vehicles(co.home).stream().filter(q -> q.id().equals(vid)).findFirst().orElse(null);
        if (v == null) { x.h().tell("That vehicle is not open to your culture yet (/company vehicles)."); return; }
        long price = (long) Math.ceil(v.cost() * scale(co.home));
        if (co.money < price) { x.h().tell("A " + v.name() + " costs " + price + "; the company has " + (long) co.money + "."); return; }
        co.money -= price;
        EconWorld.state().cur(co.home).treasury += price;
        EconState.Line l = new EconState.Line();
        l.from = from; l.to = to; l.vehicle = vid;
        co.lines.add(l);
        x.h().tell("Line " + co.lines.size() + " opened: " + from + " to " + to + " with a " + v.name() + ".");
    }

    private static void borrow(Ctx x, EconState.Company co, int n) {
        if (!home(x, co, "Borrow")) return;
        EconState st = EconWorld.state();
        EconState.Bank b = st.bank(co.home);
        String who = "co:" + key(x.p()), age = EconWorld.ageOf(co.home);
        if (EconBank.reserve(age) >= 1) {
            EconBank.vaultEndow(st, co.home, b);
            EconState.Pledge pl = new EconState.Pledge();
            pl.value = EconCompany.assets(co);
            EconBank.Result r = EconBank.vaultBorrow(b, who, n, pl, age);
            if (!r.ok()) { x.h().tell(r.why()); return; }
            co.money += n; co.loan = b.goods.get(who).owed;
            x.h().tell("The temple lends " + n + " against your carts; " + (long) co.loan + " to repay.");
            return;
        }
        int need = (int) Math.ceil(n * EconBank.COLLATERAL);
        if (co.money < need) { x.h().tell("A loan of " + n + " needs " + need + " kept in the bank; the company has " + (long) co.money + "."); return; }
        EconBank.deposit(b, who, need);
        EconBank.Result r = EconBank.borrow(b, who, n, age);
        if (!r.ok()) { EconBank.withdraw(b, who, need); x.h().tell(r.why()); return; }
        co.money += n - need;
        co.loan = b.loans.get(who).owed;
        EconPolity.mint(st, co.home, n);
        x.h().tell("The bank lends " + n + ", keeping " + need + " as collateral; " + (long) co.loan + " to repay.");
    }

    private static void repay(Ctx x, EconState.Company co, int n) {
        EconState st = EconWorld.state();
        EconState.Bank b = st.bank(co.home);
        String who = "co:" + key(x.p());
        double pay = Math.min(n, Math.floor(co.money));
        if (b.goods.containsKey(who)) {
            EconBank.Repaid r = EconBank.vaultRepay(b, who, pay);
            co.money -= r.taken();
            co.loan = b.goods.containsKey(who) ? b.goods.get(who).owed : 0;
            x.h().tell("Repaid " + (long) r.taken() + "; " + (long) co.loan + " still owed.");
            return;
        }
        double paid = EconBank.repay(b, who, pay);
        co.money -= paid;
        EconPolity.mint(st, co.home, -paid);
        if (!b.loans.containsKey(who)) co.money += EconBank.withdraw(b, who, b.dep.getOrDefault(who, 0.0));
        co.loan = b.loans.containsKey(who) ? b.loans.get(who).owed : 0;
        x.h().tell("Repaid " + (long) paid + "; " + (long) co.loan + " still owed.");
    }
}
