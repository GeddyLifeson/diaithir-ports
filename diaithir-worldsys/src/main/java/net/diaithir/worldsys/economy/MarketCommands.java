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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;

/** /market buy|sell|sellhand, /coin, /redeem, /economy. Port of market.js. */
final class MarketCommands {
    private MarketCommands() {}

    private interface Act { void go(ServerPlayer p, ServerPlayerHolder h, EconMarket.Ctx ctx, EconClaims.Claim at); }

    /** With the ages engine, only goods a nearby keeper's trade deals in may be bought or sold here. Without it any keeper deals in anything open. */
    private static java.util.function.Predicate<String> vendorGate(ServerPlayer p, EconQuests.Player h, EconMarket.Ctx ctx) {
        if (EconWorld.worldAge() == null || ctx.culture() == null || "wilds".equals(ctx.culture())) return id -> true;
        var vs = EconWorld.vendors(ctx.culture());
        if (vs.isEmpty()) return id -> true;
        var kinds = new java.util.ArrayList<EconOffers.Vendor>();
        for (Villager v : p.level().getEntitiesOfClass(Villager.class, p.getBoundingBox().inflate(24))) {
            if (!v.isAlive()) continue;
            var k = EconOffers.vendorFor(v.getVillagerData().profession().unwrapKey().map(r -> r.identifier().getPath()).orElse("none"), vs);
            if (k != null) kinds.add(k);
        }
        return id -> {
            if (kinds.stream().anyMatch(k -> k.items().contains(id))) return true;
            h.tell(kinds.isEmpty() ? "No keeper here deals in that." : "No keeper here deals in that. Try " + kinds.stream().map(k -> k.id().replace('_', ' ')).distinct().collect(java.util.stream.Collectors.joining(" or ")) + ".");
            return false;
        };
    }

    private static boolean nearKeeper(ServerPlayer p) { return !p.level().getEntitiesOfClass(Villager.class, p.getBoundingBox().inflate(8)).isEmpty(); }

    private static int at(CommandContext<CommandSourceStack> c, boolean needKeeper, Act fn) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        ServerPlayerHolder h = new ServerPlayerHolder(p);
        if (needKeeper && !nearKeeper(p)) { h.tell("You need a store keeper beside you."); return 1; }
        EconClaims.Claim at = EconWorld.claimAt(p.getX(), p.getZ());
        fn.go(p, h, EconWorld.ctx(at == null ? "wilds" : at.town(), at == null ? null : at.culture()), at);
        EconWorld.dirty();
        return Command.SINGLE_SUCCESS;
    }

    /** A visitor is a player whose home culture is another's. Home is the first culture a player trades in. */
    static boolean foreign(EconState st, EconQuests.Player h, String culture) {
        String home = st.home.computeIfAbsent(h.id(), k -> culture);
        return !home.equals(culture);
    }

    /** A storeroom full of the money good takes no more of it (8 times the usual stock). */
    private static boolean roomFor(EconMarket.Ctx ctx, String town, long units) {
        if (ctx.kind() != EconPolity.Kind.COMMODITY) return true;
        String g = ctx.design().design().commodityGood();
        return held(town, g) + units * ctx.design().design().perUnit() <= 8 * EconWorld.data().good(g).usual();
    }

    private static String town(EconClaims.Claim at) { return at == null ? "wilds" : at.town(); }

    private static double held(String town, String id) {
        Double v = EconWorld.state().market.stock.getOrDefault(town, Map.of()).get(id);
        return v == null ? EconWorld.data().good(id).usual() : v;
    }

    private static boolean open(String id, EconMarket.Ctx ctx) {
        return EconWorld.data().good(id) != null && EconPolity.AGES.indexOf(EconWorld.data().ageOfGood(id)) <= EconPolity.AGES.indexOf(ctx.age());
    }

    /** A commodity money is physical: what buyers pay joins the store's stock of the staple, what it pays a seller comes out of it. */
    private static boolean commodityPay(EconMarket.Ctx ctx, String town, long units) {
        if (ctx.kind() != EconPolity.Kind.COMMODITY) return true;
        String g = ctx.design().design().commodityGood();
        double n = units * ctx.design().design().perUnit(), have = held(town, g);
        if (n < 0 && have + n < 0) return false;
        EconWorld.state().market.stock.computeIfAbsent(town, k -> new java.util.HashMap<>()).put(g, have + n);
        return true;
    }

    static void buy(EconQuests.Player h, EconMarket.Ctx ctx, EconClaims.Claim at, String id, int n) { buy(h, ctx, at, id, n, x -> true); }

    static void buy(EconQuests.Player h, EconMarket.Ctx ctx, EconClaims.Claim at, String id, int n, java.util.function.Predicate<String> allows) {
        EconState st = EconWorld.state();
        String town = town(at);
        if (!open(id, ctx)) { h.tell("No store sells that in this Age."); return; }
        if (!allows.test(id)) return;
        double have = held(town, id);
        if (have < n) { h.tell("The store has only " + (long) Math.floor(have) + "."); return; }
        long price = EconModel.trade(st.market, EconWorld.REGIONS, EconWorld.data()::good, town, id, n, true, ctx.scale());
        double rate = at == null ? 0 : EconPolity.taxRate(null, ctx.culture(), ctx.age(), st);
        long tax = (long) Math.ceil(price * rate);
        if (!roomFor(ctx, town, price + tax)) {
            st.market.stock.get(town).put(id, have);
            h.tell("The keeper will take no more " + ctx.design().name() + " today: the storeroom is full of it. Pay in goods or in another money.");
            return;
        }
        if (!EconPurse.take(h, price + tax, ctx.denoms())) {
            st.market.stock.get(town).put(id, have);
            h.tell("That costs " + (price + tax) + " " + ctx.unit() + ". You have " + EconPurse.own(h, ctx.denoms()) + ".");
            return;
        }
        commodityPay(ctx, town, price + tax);
        EconPolity.mint(st, ctx.culture(), -(price + tax));
        st.cur(ctx.culture()).treasury += tax;
        if (at != null && foreign(st, h, ctx.culture())) EconPolity.flow(st, ctx.culture(), price * ctx.rate());
        st.cur(ctx.culture()).vol += price * ctx.rate();
        h.give(id, n);
        h.tell("Bought " + n + " " + h.itemName(id) + " for " + (price + tax) + " " + ctx.unit() + (tax > 0 ? " (tax " + tax + ")" : "") + ".");
    }

    static void sell(EconQuests.Player h, EconMarket.Ctx ctx, EconClaims.Claim at, String id, int n) { sell(h, ctx, at, id, n, x -> true); }

    static void sell(EconQuests.Player h, EconMarket.Ctx ctx, EconClaims.Claim at, String id, int n, java.util.function.Predicate<String> allows) {
        EconState st = EconWorld.state();
        String town = town(at);
        if (!open(id, ctx)) { h.tell("No store buys that in this Age."); return; }
        if (!allows.test(id)) return;
        n = Math.min(n, h.count(id));
        if (n < 1) { h.tell("You have none to sell."); return; }
        long paid = EconModel.trade(st.market, EconWorld.REGIONS, EconWorld.data()::good, town, id, n, false, ctx.scale());
        paid = Math.max(1, Math.round(paid * h.bottleFactor(id, n)));
        long tax = (long) Math.floor(paid * (at == null ? 0 : EconPolity.taxRate(null, ctx.culture(), ctx.age(), st)));
        if (paid - tax < 1) { st.market.stock.get(town).merge(id, (double) -n, Double::sum); h.tell("That is not worth a single coin here. Sell more at once."); return; }
        if (!commodityPay(ctx, town, -(paid - tax))) { st.market.stock.get(town).merge(id, (double) -n, Double::sum); h.tell("The keeper has not enough " + ctx.design().name() + " to pay you. Sell for goods, or sell less."); return; }
        h.take(id, n);
        EconPurse.give(h, paid - tax, ctx.denoms());
        EconPolity.mint(st, ctx.culture(), paid - tax);
        st.cur(ctx.culture()).treasury += tax;
        if (at != null && foreign(st, h, ctx.culture())) EconPolity.flow(st, ctx.culture(), -paid * ctx.rate());
        st.cur(ctx.culture()).vol += paid * ctx.rate();
        h.tell("Sold " + n + " " + h.itemName(id) + " for " + (paid - tax) + " " + ctx.unit() + (tax > 0 ? " (tax " + tax + ")" : "") + ".");
    }

    private static LiteralArgumentBuilder<CommandSourceStack> side(String name, boolean buying) {
        return Commands.literal(name).then(Commands.argument("item", StringArgumentType.string())
            .then(Commands.argument("count", IntegerArgumentType.integer(1)).executes(c -> at(c, true, (p, h, ctx, at) -> {
                String id = StringArgumentType.getString(c, "item");
                if (!id.contains(":")) id = "minecraft:" + id;
                int n = IntegerArgumentType.getInteger(c, "count");
                java.util.function.Predicate<String> ok = vendorGate(p, h, ctx);
                if (buying) buy(h, ctx, at, id, n, ok); else sell(h, ctx, at, id, n, ok);
            }))));
    }

    static LiteralArgumentBuilder<CommandSourceStack> market() {
        return Commands.literal("market")
            .executes(c -> at(c, true, (p, h, ctx, at) -> {
                String town = town(at);
                h.tell("Prices at " + (at == null ? "the wilds" : at.name() + ", " + EconWorld.cultureName(ctx.culture())) + ", in " + ctx.unit() + " (you have " + EconPurse.own(h, ctx.denoms()) + "):");
                EconState st = EconWorld.state();
                for (String id : EconWorld.data().openIds(ctx.age())) {
                    if (!st.market.stock.getOrDefault(town, Map.of()).containsKey(id)) continue;
                    double mid = EconModel.mid(st.market, EconWorld.REGIONS, EconWorld.data()::good, town, id, null, 0) * ctx.scale();
                    h.tell(h.itemName(id) + ": buy " + EconModel.buyPriceOf(mid) + ", sell " + EconModel.sellPriceOf(mid) + ", stock " + (long) Math.floor(held(town, id)));
                }
            }))
            .then(side("buy", true))
            .then(side("sell", false))
            .then(Commands.literal("sellhand").executes(c -> at(c, true, (p, h, ctx, at) -> {
                var st = p.getMainHandItem();
                if (st.isEmpty()) { h.tell("Hold what you sell."); return; }
                sell(h, ctx, at, BuiltInRegistries.ITEM.getKey(st.getItem()).toString(), st.getCount(), vendorGate(p, h, ctx));
            })));
    }

    static LiteralArgumentBuilder<CommandSourceStack> coin() {
        return Commands.literal("coin").then(Commands.argument("coins", IntegerArgumentType.integer(1)).executes(c -> at(c, true, (p, h, ctx, at) -> {
            if (at == null) return;
            EconState st = EconWorld.state();
            EconState.Cur s = st.cur(ctx.culture());
            int n = IntegerArgumentType.getInteger(c, "coins");
            if (s.peg == null || ctx.kind() == EconPolity.Kind.COMMODITY) { h.tell("This culture has no coin pegged to a staple."); return; }
            if (!EconPolity.redeemable(st, ctx.culture(), ctx.did())) { h.tell("The peg is broken: the keeper will not exchange the staple for coin."); return; }
            int need = (int) (n * s.peg.perUnit);
            if (h.count(s.peg.good) < need) { h.tell("You need " + need + " " + h.itemName(s.peg.good) + "."); return; }
            h.take(s.peg.good, need);
            st.market.stock.computeIfAbsent(at.town(), k -> new java.util.HashMap<>()).put(s.peg.good, held(at.town(), s.peg.good) + need);
            EconPurse.give(h, n, ctx.denoms());
            EconPolity.mint(st, ctx.culture(), n);
            h.tell("Exchanged " + need + " " + h.itemName(s.peg.good) + " for " + n + " " + ctx.unit() + ".");
        })));
    }

    static LiteralArgumentBuilder<CommandSourceStack> redeem() {
        return Commands.literal("redeem").then(Commands.argument("coins", IntegerArgumentType.integer(1)).executes(c -> at(c, true, (p, h, ctx, at) -> {
            if (at == null) return;
            EconState st = EconWorld.state();
            EconState.Cur s = st.cur(ctx.culture());
            int n = IntegerArgumentType.getInteger(c, "coins");
            if (s.peg == null) { h.tell("These coins are not pegged to anything."); return; }
            if (!EconPolity.redeemable(st, ctx.culture(), ctx.did())) { h.tell("The crown has suspended redemption: the coin has fallen too far from its peg."); return; }
            if (EconPurse.own(h, ctx.denoms()) < n || !EconPurse.take(h, n, ctx.denoms())) { h.tell("You have " + EconPurse.own(h, ctx.denoms()) + " " + ctx.unit() + "."); return; }
            double have = held(at.town(), s.peg.good), out = Math.min(have, n * s.peg.perUnit);
            st.market.stock.computeIfAbsent(at.town(), k -> new java.util.HashMap<>()).put(s.peg.good, have - out);
            EconPolity.mint(st, ctx.culture(), -n);
            h.give(s.peg.good, (int) Math.floor(out));
            h.tell("Redeemed " + n + " " + ctx.unit() + " for " + (long) Math.floor(out) + " " + h.itemName(s.peg.good) + ".");
        })));
    }

    static LiteralArgumentBuilder<CommandSourceStack> economy() {
        return Commands.literal("economy").executes(c -> at(c, false, (p, h, ctx, at) -> {
            if (at == null) { h.tell("No culture claims this ground."); return; }
            EconState.Cur s = EconWorld.state().cur(ctx.culture());
            h.tell(EconWorld.cultureName(ctx.culture()) + ": " + ctx.unit());
            h.tell(String.format(Locale.ROOT, "Prices this week %s %.1f%%. In circulation %d of %d at the start; trade balance %.2f; treasury %d; velocity %.2f a week.",
                s.infl >= 0 ? "up" : "down", Math.abs(s.infl * 100), Math.round(s.money), Math.round(s.ref), s.tb, Math.round(s.treasury), EconFrictions.velocity(s)));
            if (s.weak > 0) h.tell("The money has lost half its worth for " + (int) s.weak + " week(s); four and the culture changes its money.");
        }));
    }
}
