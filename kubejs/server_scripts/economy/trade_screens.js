// Trade screens: a keeper's own villager screen, rebuilt from the live market each time a player opens it (03_offers.js plans it).
// Prices are in the local money as coin items, two cost slots; a trade is settled in the market when it completes, so the screen and
// /market move the same prices, tax and money. A keeper outside every claim (a wanderer) trades at the free market in salt.
// Needs NeoForge's PlayerInteractEvent.EntityInteract and TradeWithVillagerEvent; if either fails, the vanilla screen stays and /market works.

const ECON_STALL_BUY = 7, ECON_STALL_SELL = 4
const ECON_OFFERS = {} // keeper uuid -> { market, plan } for the offers last shown

function econOpenGoods(age) {
  let ids = Object.keys(ECON_PRICES).concat(Object.keys(ECON_GOODS)).filter(id => econOpen(id, age))
  return ids.filter((id, i) => ids.indexOf(id) === i)
}
let ECON_OPEN_CACHE = {} // Age -> ids open in it

function econBuildOffers(keeper) {
  let market = econMarketOf(keeper), ctx = econCtx(market)
  if (!ECON_OPEN_CACHE[ctx.age]) ECON_OPEN_CACHE[ctx.age] = econOpenGoods(ctx.age)
  let prof = econKeeperProfession(keeper)
  let seed = 0, u = String(keeper.uuid)
  for (let i = 0; i < u.length; i++) seed = (seed * 31 + u.charCodeAt(i)) >>> 0
  let taxRate = market.culture ? econTaxRate(ECON_TAXES, ctx.culture, econAge(), ECON) : 0
  let quote = (id, n, buying) => {
    let v = econQuote(ECON, ECON.regions, econGood, market.town, id, n, buying, ctx.scale)
    return buying ? Math.ceil(v * (1 + taxRate)) : Math.floor(v * (1 - taxRate))
  }
  let plan = econOfferPlan(econStall(prof, ECON_OPEN_CACHE[ctx.age], seed, econVendors(ctx.culture)), ctx, quote, id => econHeld(market.town, id), () => 0, ECON_STALL_BUY, ECON_STALL_SELL)
  return { market: market, ctx: ctx, plan: plan, taxRate: taxRate }
}

function econApplyOffers(keeper, built) {
  let Optional = Java.loadClass('java.util.Optional')
  let MerchantOffers = Java.loadClass('net.minecraft.world.item.trading.MerchantOffers')
  let MerchantOffer = Java.loadClass('net.minecraft.world.item.trading.MerchantOffer')
  let ItemCost = Java.loadClass('net.minecraft.world.item.trading.ItemCost')
  let cost = c => new ItemCost(Item.of(c[0]).item, c[1])
  let offers = new MerchantOffers()
  built.plan.forEach(o => {
    let costs = o.dir === 'buy' ? o.cost : [[o.id, o.n]]
    let result = o.dir === 'buy' ? Item.of(o.id, o.n) : Item.of(o.result[0], o.result[1])
    offers.add(new MerchantOffer(cost(costs[0]), costs[1] ? Optional.of(cost(costs[1])) : Optional.empty(), result.itemStack || result, 0, 8, 0, 0, 0))
  })
  keeper.overrideOffers(offers)
  ECON_OFFERS[String(keeper.uuid)] = { market: built.market, plan: built.plan, taxRate: built.taxRate, offers: offers }
}

// A trade done at the screen: the menu moved the items, the market settles prices, stock, tax and money.
function econSettleOffer(player, o, rec) {
  let market = rec.market, ctx = econCtx(market), region = market.town
  if (o.dir === 'buy') {
    let price = econTrade(ECON, ECON.regions, econGood, region, o.id, o.n, true, ctx.scale)
    let tax = Math.ceil(price * rec.taxRate)
    econCommodityPay(ctx, region, o.units)
    econMint(ECON, ctx.culture, -o.units)
    econCur(ECON, ctx.culture).treasury += tax
    if (market.culture && econForeign(player, ctx.culture)) econFlow(ECON, ctx.culture, o.units * ctx.rate)
  } else {
    let paid = econTrade(ECON, ECON.regions, econGood, region, o.id, o.n, false, ctx.scale)
    econCommodityPay(ctx, region, -o.units)
    econMint(ECON, ctx.culture, o.units)
    econCur(ECON, ctx.culture).treasury += Math.max(0, paid - o.units)
    if (market.culture && econForeign(player, ctx.culture)) econFlow(ECON, ctx.culture, -o.units * ctx.rate)
  }
  econCur(ECON, ctx.culture).vol += o.units * ctx.rate
  ECON_PUB.dirty = true
  econSave()
  econTradeEureka(player)
}

try {
  let Interact = Java.loadClass('net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$EntityInteract')
  NativeEvents.onEvent(Interact, e => {
    let keeper = e.target
    if (!keeper || ECON_TRADERS.indexOf(String(keeper.type)) < 0 || e.level.isClientSide()) return
    try { if (typeof caravanTalk === 'function') caravanTalk(e.entity, keeper) } catch (err) { console.warn('[economy] caravan: ' + err) }
    try { econApplyOffers(keeper, econBuildOffers(keeper)) } catch (err) { console.error('[economy] trade screen not built: ' + err) }
  })
  let Trade = Java.loadClass('net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent')
  NativeEvents.onEvent(Trade, e => {
    let keeper = e.abstractVillager, rec = ECON_OFFERS[String(keeper.uuid)]
    if (!rec) return
    let i = rec.offers.indexOf(e.merchantOffer)
    if (i >= 0 && rec.plan[i]) econSettleOffer(e.entity, rec.plan[i], rec)
  })
} catch (e) { console.warn('[economy] trade screens off (no native events): ' + e) }
