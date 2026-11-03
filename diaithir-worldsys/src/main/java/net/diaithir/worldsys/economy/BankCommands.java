package net.diaithir.worldsys.economy;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;

/** /bank and /bond: a banker is any villager keeper (or a temple vault block) in a culture's town. Port of bank.js. */
final class BankCommands {
    private BankCommands() {}

    private static final String VAULT_BLOCK = "diaithir:temple_vault";

    private interface Act { void go(ServerPlayer p, ServerPlayerHolder h, EconMarket.Ctx ctx, EconClaims.Claim at, String who); }
    private interface Amt { void go(ServerPlayer p, ServerPlayerHolder h, EconMarket.Ctx ctx, EconClaims.Claim at, String who, int n); }

    private static boolean nearBanker(ServerPlayer p) {
        if (!p.level().getEntitiesOfClass(Villager.class, p.getBoundingBox().inflate(8)).isEmpty()) return true;
        BlockPos c = p.blockPosition();
        for (BlockPos b : BlockPos.betweenClosed(c.offset(-5, -3, -5), c.offset(5, 3, 5)))
            if (BuiltInRegistries.BLOCK.getKey(p.level().getBlockState(b).getBlock()).toString().equals(VAULT_BLOCK)) return true;
        return false;
    }

    private static int at(CommandContext<CommandSourceStack> c, int minAge, String noAge, Act fn) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        ServerPlayerHolder h = new ServerPlayerHolder(p);
        if (!nearBanker(p)) { h.tell("You need a keeper or a temple vault beside you."); return 1; }
        EconClaims.Claim at = EconWorld.claimAt(p.getX(), p.getZ());
        if (at == null) { h.tell("There is no bank outside a culture's towns."); return 1; }
        if (EconPolity.AGES.indexOf(EconWorld.ageOf(at.culture())) < minAge) { h.tell(noAge); return 1; }
        fn.go(p, h, EconWorld.ctx(at.town(), at.culture()), at, p.getUUID().toString());
        EconWorld.dirty();
        return Command.SINGLE_SUCCESS;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> amt(String name, Amt fn) {
        return Commands.literal(name).then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes(c ->
            at(c, EconPolity.AGES.indexOf("III"), "There is no bank in this Age.", (p, h, ctx, at, who) -> fn.go(p, h, ctx, at, who, IntegerArgumentType.getInteger(c, "amount")))));
    }

    private static String pct(double r) { return String.format(Locale.ROOT, "%.1f", r * 100); }

    static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> bank() {
        return Commands.literal("bank")
            .executes(c -> at(c, EconPolity.AGES.indexOf("III"), "There is no bank in this Age.", (p, h, ctx, at, who) -> {
                EconState.Bank b = EconWorld.state().bank(ctx.culture());
                String age = EconWorld.ageOf(ctx.culture());
                var r = EconBank.rates(b, age);
                h.tell("Bank of " + EconWorld.cultureName(ctx.culture()) + ", in " + ctx.unit() + (b.failed ? " (failed)" : "") + ":");
                EconState.Loan l = b.loans.get(who);
                h.tell("Your deposit: " + (long) Math.floor(b.dep.getOrDefault(who, 0.0)) + (l != null ? ", locked " + (long) Math.ceil(l.collateral) + " against a debt of " + (long) Math.ceil(l.owed) : "") + ".");
                EconState.GoodsLoan g = b.goods.get(who);
                if (g != null) h.tell("You owe " + (long) Math.ceil(g.owed) + " against pledged goods worth " + (long) Math.floor(g.pledge.value) + ".");
                h.tell(EconBank.reserve(age) >= 1 ? "A vault: it keeps your money safe for a small fee and lends only from its own fund (" + (long) Math.floor(b.fund) + "), against goods worth " + EconBank.VAULT_COVER + " times the loan."
                    : "Lends at " + pct(r.lend()) + "% a year, pays " + pct(r.save()) + "%, may lend " + (long) Math.floor(EconBank.capacity(b, age)) + " more.");
            }))
            .then(amt("deposit", (p, h, ctx, at, who, n) -> {
                EconState.Bank b = EconWorld.state().bank(ctx.culture());
                if (!EconPurse.take(h, n, ctx.denoms())) { h.tell("You have " + EconPurse.own(h, ctx.denoms()) + " " + ctx.unit() + "."); return; }
                if (!EconBank.deposit(b, who, n)) { EconPurse.give(h, n, ctx.denoms()); h.tell("The bank has failed."); return; }
                h.tell("Deposited " + n + " " + ctx.unit() + ".");
            }))
            .then(amt("withdraw", (p, h, ctx, at, who, n) -> {
                double got = EconBank.withdraw(EconWorld.state().bank(ctx.culture()), who, n);
                EconPurse.give(h, (long) Math.floor(got), ctx.denoms());
                h.tell("Withdrew " + (long) Math.floor(got) + " " + ctx.unit() + ".");
            }))
            .then(amt("borrow", (p, h, ctx, at, who, n) -> {
                EconState.Bank b = EconWorld.state().bank(ctx.culture());
                String age = EconWorld.ageOf(ctx.culture());
                EconBank.Result r = EconBank.borrow(b, who, n, age);
                if (!r.ok()) { h.tell(r.why()); return; }
                EconPurse.give(h, n, ctx.denoms());
                EconPolity.mint(EconWorld.state(), ctx.culture(), n);
                h.tell("Borrowed " + n + " " + ctx.unit() + " at " + pct(EconBank.rates(b, age).lend()) + "% a year.");
            }))
            .then(amt("pledge", (p, h, ctx, at, who, n) -> {
                ItemStack st = p.getMainHandItem();
                if (st.isEmpty()) { h.tell("Hold the goods you pledge."); return; }
                EconState s = EconWorld.state();
                String id = BuiltInRegistries.ITEM.getKey(st.getItem()).toString();
                int cnt = st.getCount();
                EconWorld.data();
                long value = EconModel.quote(s.market, EconWorld.REGIONS, EconWorld.data()::good, at.town(), id, cnt, false, ctx.scale());
                EconState.Bank b = s.bank(ctx.culture());
                EconBank.vaultEndow(s, ctx.culture(), b);
                EconState.Pledge pl = new EconState.Pledge();
                pl.value = value;
                EconState.Item it = new EconState.Item();
                it.id = id; it.n = cnt;
                pl.items.add(it);
                EconBank.Result r = EconBank.vaultBorrow(b, who, n, pl, EconWorld.ageOf(ctx.culture()));
                if (!r.ok()) { h.tell(r.why()); return; }
                st.shrink(cnt);
                EconPurse.give(h, n, ctx.denoms());
                h.tell("The vault lends " + n + " " + ctx.unit() + " against " + cnt + " " + h.itemName(id) + " (worth " + value + "); pay it back with /bank repay to take them home.");
            }))
            .then(amt("repay", (p, h, ctx, at, who, n0) -> {
                EconState s = EconWorld.state();
                EconState.Bank b = s.bank(ctx.culture());
                long n = Math.min(n0, EconPurse.own(h, ctx.denoms()));
                if (n < 1 || !EconPurse.take(h, n, ctx.denoms())) { h.tell("You have nothing to pay with."); return; }
                if (b.goods.containsKey(who)) {
                    EconBank.Repaid r = EconBank.vaultRepay(b, who, n);
                    EconPurse.give(h, (long) Math.floor(n - r.taken()), ctx.denoms());
                    for (EconState.Item i : r.items()) h.give(i.id, i.n);
                    h.tell(r.items().isEmpty() ? "Paid " + (long) Math.floor(r.taken()) + " " + ctx.unit() + "; the vault still holds your goods." : "The debt is paid and the goods are yours again.");
                    return;
                }
                double paid = EconBank.repay(b, who, n);
                EconPurse.give(h, (long) Math.floor(n - paid), ctx.denoms());
                EconPolity.mint(s, ctx.culture(), -paid);
                h.tell("Paid " + (long) Math.floor(paid) + " " + ctx.unit() + ".");
            }));
    }

    static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> bond() {
        int v = EconPolity.AGES.indexOf("V");
        String no = "No realm sells bonds in this Age.";
        return Commands.literal("bond")
            .executes(c -> at(c, v, no, (p, h, ctx, at, who) -> {
                EconState s = EconWorld.state();
                EconState.Bonds b = s.bonds(ctx.culture());
                double rate = EconBonds.rate(s, ctx.culture(), EconBank.rates(s.bank(ctx.culture()), EconWorld.ageOf(ctx.culture())).policy());
                var mine = b.list.stream().filter(x -> who.equals(x.who)).toList();
                h.tell(EconWorld.cultureName(ctx.culture()) + " bonds, in " + ctx.unit() + ": " + pct(rate) + "% a year for " + EconBonds.WEEKS + " weeks" + (b.blot > 0 ? " (the realm has defaulted lately)" : "") + ".");
                h.tell((mine.isEmpty() ? "You hold none. " : "You hold " + mine.size() + ", face " + (long) mine.stream().mapToDouble(x -> x.face).sum() + ". ") + "Waiting to collect: " + (long) Math.floor(b.due.getOrDefault(who, 0.0)) + ".");
            }))
            .then(Commands.literal("buy").then(Commands.argument("amount", IntegerArgumentType.integer(1)).executes(c ->
                at(c, v, no, (p, h, ctx, at, who) -> {
                    int n = IntegerArgumentType.getInteger(c, "amount");
                    if (!EconPurse.take(h, n, ctx.denoms())) { h.tell("You have " + EconPurse.own(h, ctx.denoms()) + " " + ctx.unit() + "."); return; }
                    EconState s = EconWorld.state();
                    EconState.Bond bond = EconBonds.issue(s, ctx.culture(), who, n, EconBank.rates(s.bank(ctx.culture()), EconWorld.ageOf(ctx.culture())).policy());
                    h.tell("Bought a bond of " + n + " " + ctx.unit() + " at " + pct(bond.rate) + "% a year.");
                }))))
            .then(Commands.literal("collect").executes(c -> at(c, v, no, (p, h, ctx, at, who) -> {
                long n = (long) Math.floor(EconBonds.collect(EconWorld.state(), ctx.culture(), who));
                if (n > 0) EconPurse.give(h, n, ctx.denoms());
                h.tell("Collected " + n + " " + ctx.unit() + ".");
            })));
    }
}
