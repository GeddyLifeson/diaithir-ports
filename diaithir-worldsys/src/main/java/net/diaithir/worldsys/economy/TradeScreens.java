package net.diaithir.worldsys.economy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;

/** A keeper's trade screen, built from the live market each time it is opened; the market settles each trade done at it. Port of trade_screens.js. */
final class TradeScreens {
    private TradeScreens() {}

    static final int BUY = 7, SELL = 4;

    private record Rec(EconClaims.Claim at, EconMarket.Ctx ctx, List<EconOffers.Offer> plan, MerchantOffers offers, double taxRate) {}

    private static final Map<UUID, Rec> OFFERS = new HashMap<>();

    private static net.minecraft.world.item.Item item(String id) { return BuiltInRegistries.ITEM.getValue(Identifier.parse(id)); }

    private static ItemCost cost(EconOffers.Cost c) { return new ItemCost(item(c.item()), (int) Math.round(c.count())); }

    /** Builds this keeper's offers and installs them on the villager. */
    static void apply(Villager v, String profession) {
        EconClaims.Claim at = EconWorld.claimAt(v.getX(), v.getZ());
        if (at == null) return;
        EconState st = EconWorld.state();
        var data = EconWorld.data();
        EconMarket.Ctx ctx = EconWorld.ctx(at.town(), at.culture());
        double seed = 0;
        for (char ch : v.getUUID().toString().toCharArray()) seed = (long) (seed * 31 + ch) & 0xFFFFFFFFL;
        double taxRate = EconPolity.taxRate(null, ctx.culture(), ctx.age(), st);
        EconOffers.QuoteFn quote = (id, n, buying) -> {
            long p = EconModel.quote(st.market, EconWorld.REGIONS, data::good, at.town(), id, n, buying, ctx.scale());
            return buying ? Math.ceil(p * (1 + taxRate)) : Math.floor(p * (1 - taxRate));
        };
        List<String> stall = EconOffers.stall(profession, data.openIds(ctx.age()), seed, EconWorld.vendors(ctx.culture()));
        List<EconOffers.Offer> plan = EconOffers.offerPlan(stall, ctx.denoms(), quote, id -> {
            Double h = st.market.stock.getOrDefault(at.town(), Map.of()).get(id);
            return h == null ? data.good(id).usual() : h;
        }, BUY, SELL);
        MerchantOffers offers = new MerchantOffers();
        List<EconOffers.Offer> kept = new ArrayList<>();
        for (EconOffers.Offer o : plan) {
            List<EconOffers.Cost> costs = o.dir().equals("buy") ? o.cost() : List.of(new EconOffers.Cost(o.id(), o.n()));
            ItemStack result = o.dir().equals("buy") ? new ItemStack(item(o.id()), o.n()) : new ItemStack(item(o.result().item()), (int) Math.round(o.result().count()));
            offers.add(new MerchantOffer(cost(costs.get(0)), costs.size() > 1 ? Optional.of(cost(costs.get(1))) : Optional.empty(), result, 0, 8, 0, 0f, 0));
            kept.add(o);
        }
        v.setOffers(offers);
        OFFERS.put(v.getUUID(), new Rec(at, ctx, kept, offers, taxRate));
    }

    /** A trade done at the screen: the menu moved the items, the market settles prices, stock, tax and money. */
    static void settle(TradeWithVillagerEvent e) {
        if (!(e.getAbstractVillager() instanceof Villager v)) return;
        Rec rec = OFFERS.get(v.getUUID());
        if (rec == null) return;
        int i = rec.offers().indexOf(e.getMerchantOffer());
        if (i < 0 || i >= rec.plan().size()) return;
        EconOffers.Offer o = rec.plan().get(i);
        EconState st = EconWorld.state();
        var data = EconWorld.data();
        EconMarket.Ctx ctx = rec.ctx();
        String town = rec.at().town();
        String culture = ctx.culture();
        if (o.dir().equals("buy")) {
            long price = EconModel.trade(st.market, EconWorld.REGIONS, data::good, town, o.id(), o.n(), true, ctx.scale());
            long tax = (long) Math.ceil(price * rec.taxRate());
            payStaple(ctx, town, o.units());
            EconPolity.mint(st, culture, -o.units());
            st.cur(culture).treasury += tax;
        } else {
            long paid = EconModel.trade(st.market, EconWorld.REGIONS, data::good, town, o.id(), o.n(), false, ctx.scale());
            payStaple(ctx, town, -o.units());
            EconPolity.mint(st, culture, o.units());
            st.cur(culture).treasury += Math.max(0, paid - o.units());
        }
        st.cur(culture).vol += o.units() * ctx.rate();
        EconWorld.dirty();
    }

    private static void payStaple(EconMarket.Ctx ctx, String town, long units) {
        if (ctx.kind() != EconPolity.Kind.COMMODITY) return;
        String g = ctx.design().design().commodityGood();
        Map<String, Double> stock = EconWorld.state().market.stock.computeIfAbsent(town, k -> new HashMap<>());
        stock.put(g, stock.getOrDefault(g, (double) EconWorld.data().good(g).usual()) + units * ctx.design().design().perUnit());
    }
}
