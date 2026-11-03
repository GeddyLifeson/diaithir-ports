package net.diaithir.worldsys.economy;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Locale;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/** /exchange, /exchange buy|sell <company> <count>. Port of exchange.js. */
final class ExchangeCommands {
    private ExchangeCommands() {}

    private interface Act { void go(ServerPlayerHolder h, EconMarket.Ctx ctx, String who); }

    private static int open(CommandContext<CommandSourceStack> c, Act fn) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        ServerPlayerHolder h = new ServerPlayerHolder(p);
        EconClaims.Claim at = EconWorld.claimAt(p.getX(), p.getZ());
        if (EconPolity.AGES.indexOf(EconWorld.ageOf(at == null ? null : at.culture())) < EconPolity.AGES.indexOf(EconExchange.AGE)) { h.tell("There is no exchange in this Age."); return 1; }
        EconMarket.Ctx ctx = EconWorld.ctx(at == null ? null : at.town(), at == null ? null : at.culture());
        fn.go(h, ctx, p.getUUID().toString());
        EconWorld.dirty();
        return Command.SINGLE_SUCCESS;
    }

    /** Dividends that came due while the holder was away. */
    private static void collect(ServerPlayerHolder h, EconMarket.Ctx ctx, String who) {
        EconState st = EconWorld.state();
        double due = st.div.getOrDefault(who, 0.0);
        long units = (long) Math.floor(due * ctx.scale());
        if (units < 1) return;
        st.div.put(who, due - units / ctx.scale());
        EconPurse.give(h, units, ctx.denoms());
        if (ctx.culture() != null) EconPolity.mint(st, ctx.culture(), units);
        h.tell("Dividends: " + units + " " + ctx.unit() + ".");
    }

    private static void trade(ServerPlayerHolder h, EconMarket.Ctx ctx, String who, String co, int n, boolean buying) {
        EconState st = EconWorld.state();
        EconState.ShareCompany c = st.shares.get(co);
        if (c == null) { h.tell("No such company on the exchange."); return; }
        if (buying) {
            long need = (long) Math.ceil(EconExchange.cost(c, n) * (1 + EconExchange.FEE) * ctx.scale());
            if (!EconPurse.take(h, need, ctx.denoms())) { h.tell("That costs " + need + " " + ctx.unit() + "."); return; }
            EconExchange.trade(st, who, co, n, true, ctx.scale());
            if (ctx.culture() != null) EconPolity.mint(st, ctx.culture(), -need);
            h.tell("Bought " + n + " shares of " + c.name + " for " + need + " " + ctx.unit() + ".");
        } else {
            int held = st.holdings.getOrDefault(who, Map.of()).getOrDefault(co, 0);
            if (held < 1) { h.tell("You hold no shares in that company."); return; }
            int sold = Math.min(n, held);
            long units = EconExchange.trade(st, who, co, sold, false, ctx.scale());
            EconPurse.give(h, units, ctx.denoms());
            if (ctx.culture() != null) EconPolity.mint(st, ctx.culture(), units);
            h.tell("Sold " + sold + " shares of " + c.name + " for " + units + " " + ctx.unit() + ".");
        }
    }

    static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("exchange")
            .executes(c -> open(c, (h, ctx, who) -> {
                collect(h, ctx, who);
                EconState st = EconWorld.state();
                h.tell("The exchange, in " + ctx.unit() + " per share (you have " + EconPurse.own(h, ctx.denoms()) + "):");
                for (var e : st.shares.entrySet())
                    h.tell(e.getValue().name + " [" + e.getKey() + "]: " + String.format(Locale.ROOT, "%.1f", EconExchange.price(e.getValue()) * ctx.scale())
                        + ", you hold " + st.holdings.getOrDefault(who, Map.of()).getOrDefault(e.getKey(), 0));
            }))
            .then(side("buy", true))
            .then(side("sell", false));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> side(String name, boolean buying) {
        return Commands.literal(name).then(Commands.argument("company", StringArgumentType.string())
            .then(Commands.argument("count", IntegerArgumentType.integer(1)).executes(c -> open(c, (h, ctx, who) ->
                trade(h, ctx, who, StringArgumentType.getString(c, "company"), IntegerArgumentType.getInteger(c, "count"), buying)))));
    }
}
