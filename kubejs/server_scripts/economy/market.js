// The market: NPC stores with supply-and-demand prices, one market per town and one culture's money and prices over each town's
// claim. Selling to a store adds to its stock and lowers the price, buying draws stock down and raises it. A price moves only
// through trades: nothing drifts back on its own, so a crashed price recovers only when enough of that item is bought.
// Trade happens near a store keeper (a villager or wandering trader). Player shops are Lightman's Currency traders.
// Culture and territory (01_polity.js): the town is the settlement whose claim covers the player, its culture decides the money and
// the tax, and a keeper outside every claim is a travelling trader at the free market (barter in salt, no published prices).
// Age gating: goods, money and the exchange unlock by Age. With no Ages engine (Deep Worlds) everything is open.

const ECON_AGES = ['TELLING', 'I', 'II', 'III', 'IV', 'V', 'VI', 'VII', 'VIII', 'RODOS']
const ECON_FILE = 'kubejs/data/economy.json'
const ECON_STORE_RANGE = 24
// Who keeps a store: villagers and wandering traders (with trade screens), and the temple priest (gods mod), who keeps the vault's bank at the counter without a screen.
const ECON_TRADERS = ['minecraft:villager', 'minecraft:wandering_trader']
const ECON_KEEPERS = ECON_TRADERS.concat(['diaithir_gods:priest'])
const ECON_MONEY_REF = 20000 // tw of money a culture starts with in circulation
const ECON_WEEKLY_BILL = 0.004 // of the reference supply, what a culture's treasury pays out a week

// Money designs (tools/economy/currencies.py writes kubejs/data/diaithir/currencies.json). Without that file, one commodity (salt)
// and one coin design (Lightman's standard chain, a twist of salt is one copper) keep the Deep Worlds fallback working.
let ECON_DESIGNS = {}
try { ECON_DESIGNS = econRead('kubejs/data/diaithir/currencies.json') || {} } catch (e) { console.warn('[economy] no currency table: ' + e) }
if (!Object.keys(ECON_DESIGNS).length) {
  ECON_DESIGNS = {
    salund: { name: 'Salund', commodity: { good: 'diaithir:salt_twist', perUnit: 1, denoms: [['diaithir:salt_cake', 12], ['diaithir:salt_twist', 1]] } },
    lightman: { name: 'Copper', coin: { metal: 'minecraft:copper_ingot', content: 1 / 12, denoms: [['lightmanscurrency:coin_netherite', 100000],
      ['lightmanscurrency:coin_diamond', 10000], ['lightmanscurrency:coin_emerald', 1000], ['lightmanscurrency:coin_gold', 100],
      ['lightmanscurrency:coin_iron', 10], ['lightmanscurrency:coin_copper', 1]] } }
  }
}
// Cultures that keep a design of their own choosing (otherwise a stable pick from the Age's designs), and Diaithiris display names.
let ECON_OWN = {}, ECON_NAMES = {}
try { ECON_OWN = econRead('kubejs/data/diaithir/culture_money.json') || {} } catch (e) { }
try { ECON_NAMES = econRead('kubejs/data/diaithir/culture_names.json') || {} } catch (e) { }
// culture_names.json (the names thread) names 65; the unnamed sites' culture is "Daeini" (people). A culture not in it is "Fini" (tribe) plus its name respelled with the island's laws (c > k).
// currency_native.json (the names thread, copied from /mnt/project-files/economy/): each unit's coin and money in its own language. The naming rule: the game's name
// on top, the player's starting culture's name under it in grey italic.
let ECON_NATIVE = {}
try { ECON_NATIVE = econRead('kubejs/data/diaithir/currency_native.json') || {} } catch (e) { }
function econNativeLine(player, ctx) {
  let home = ECON.home && ECON.home[String(player.uuid)], n = home && ECON_NATIVE[home]
  let word = n && (ctx.kind === 'commodity' ? n.currency : n.coin)
  return word ? Text.gray(word).italic(true) : null
}
const econCultureName = slug => ECON_NAMES[slug] || (slug === 'free' ? 'Daeini' : null) || 'Fini ' + slug.split('_').map(w => w.charAt(0).toUpperCase() + w.slice(1).replace(/c(?!h)/g, 'k')).join(' ')

// id: [base price, usual stock, first Age]. A twist of salt is worth one copper coin when coin arrives.
const ECON_GOODS = {
  'minecraft:wheat': [2, 256, 'I'], 'minecraft:cod': [3, 128, 'I'], 'minecraft:salmon': [4, 128, 'I'],
  'minecraft:beef': [5, 96, 'I'], 'minecraft:mutton': [4, 96, 'I'], 'minecraft:leather': [8, 64, 'I'],
  'minecraft:white_wool': [6, 64, 'I'], 'minecraft:flint': [2, 128, 'I'], 'minecraft:bone': [2, 128, 'I'],
  'minecraft:string': [3, 96, 'I'], 'minecraft:feather': [2, 96, 'I'], 'minecraft:clay_ball': [2, 128, 'I'],
  'minecraft:honeycomb': [10, 32, 'I'], 'minecraft:oak_log': [3, 256, 'I'], 'minecraft:amethyst_shard': [20, 32, 'I'],
  'diaithir:crate_common': [40, 16, 'I'], 'diaithir:crate_key': [90, 8, 'I'],
  'minecraft:copper_ingot': [12, 64, 'II'], 'minecraft:iron_ingot': [30, 64, 'III'],
  'minecraft:gold_ingot': [60, 32, 'III'],
  // the metal ladder, whichever mod makes them: copper 12 < tin 20 < bronze 24 (copper and tin) < iron 30 < silver 38 < gold 60
  // nuggets are a ninth of their ingot (rounded down to whole tw), so working a metal in either direction wins nothing
  'minecraft:copper_nugget': [1, 128, 'II'], 'minecraft:iron_nugget': [3, 128, 'III'], 'minecraft:gold_nugget': [6, 128, 'III'],
  'occultism:silver_nugget': [4, 128, 'III'], 'forestry:tin_nugget': [2, 128, 'II'], 'mekanism:nugget_bronze': [2, 128, 'II'],
  // worked forms of the shell and metal moneys (moneygoods.js): a ring, bar or axe is its ingot; a string or belt is twelve of its units
  'diaithir:cowrie_shell': [2, 256, 'I'], 'diaithir:cowrie_string': [24, 24, 'I'], 'diaithir:wampum_bead': [2, 256, 'I'], 'diaithir:wampum_belt': [24, 24, 'I'],
  'diaithir:deben_ring': [12, 32, 'II'], 'diaithir:axe_blade': [12, 32, 'II'], 'diaithir:currency_bar': [30, 32, 'III'],
  'diaithir:tin_animal': [20, 32, 'II'], 'diaithir:arm_ring': [38, 32, 'III'], 'diaithir:manilla': [24, 32, 'II'],
  'forestry:tin_ingot': [20, 48, 'II'], 'mekanism:ingot_tin': [20, 48, 'II'], 'silentgear:bronze_ingot': [24, 48, 'II'],
  'mekanism:ingot_bronze': [24, 48, 'II'], 'occultism:silver_ingot': [38, 40, 'III'], 'minecraft:coal': [6, 128, 'IV'], 'minecraft:emerald': [80, 32, 'V'],
  'minecraft:diamond': [400, 16, 'V'], 'diaithir:salt_twist': [1, 512, 'I'], 'minecraft:redstone': [8, 128, 'VI']
}

// Every item in the pack has a price: data/diaithir/prices.json is {id: [price, score]} written by tools/economy/prices.py.
// ECON_GOODS above sets the hand prices and usual stock of the staples; any other priced item trades at its table price
// with a stock that thins out as the price climbs, and opens in the Age tech_gate.js puts it in.
let ECON_PRICES = {}
try { ECON_PRICES = econRead('kubejs/data/diaithir/prices.json') || {} } catch (e) { console.warn('[economy] no price table: ' + e) }
const econGood = id => ECON_GOODS[id] || (ECON_PRICES[id] ? [ECON_PRICES[id][0], Math.max(8, Math.round(256 / Math.sqrt(ECON_PRICES[id][0]))),
  typeof techAgeOf === 'function' ? (techAgeOf(id) || 'I') : 'I'] : null)

// stock: {town: {id: units}}, regions: {town: [x, z]}, cur/des/bank: see 01_polity.js and 02_bank.js, home: {player: culture}.
const ECON = { stock: {}, shares: {}, holdings: {}, regions: {}, cur: {}, des: {}, bank: {}, home: {}, week: 0 }
try {
  let saved = econRead(ECON_FILE)
  if (saved) Object.assign(ECON, saved)
  // an older save kept one flat stock per id: that becomes the wilds market
  if (Object.keys(ECON.stock).some(k => typeof ECON.stock[k] === 'number')) ECON.stock = { wilds: ECON.stock }
} catch (e) {
  console.warn('[economy] state unreadable, starting fresh: ' + e)
}

function econSave() {
  try { JsonIO.write(ECON_FILE, ECON) } catch (e) { console.error('[economy] state not saved: ' + e) }
}

function econAgeAtLeast(age) {
  if (typeof AGES === 'undefined') return true
  return ECON_AGES.indexOf(AGES.age) >= ECON_AGES.indexOf(age)
}
const econAge = () => typeof AGES === 'undefined' ? 'VII' : String(AGES.age)
// The Age a culture has reached. The tech thread publishes global.TECH_AGE(culture) (per culture, from its researched techs and the calendar cap);
// until it exists, or for a culture it does not know, this is the world's Age.
function econLocalAge(culture) {
  try { if (typeof global !== 'undefined' && typeof global.TECH_AGE === 'function') { let a = global.TECH_AGE(culture); if (a) return ECON_AGES.indexOf(String(a)) >= 0 ? String(a) : econAge() } } catch (e) { }
  return econAge()
}
// May a culture's keepers stock a vendor kind? A kind may name a tech it needs ("needs" in the vendor file); global.TECH_KNOWN(culture, tech) answers.
function econTechAllows(culture, v) {
  if (!v.needs) return true
  try { if (typeof global !== 'undefined' && typeof global.TECH_KNOWN === 'function') return !!global.TECH_KNOWN(culture, v.needs) } catch (e) { }
  return true
}
// Vendor kinds open to a culture: one file for each Age (Age I's vendors.json first), each added when the culture reaches that Age, as [{ id, items: [item ids] }].
// There is a file for every Age, so a keeper always has a trade.
const ECON_VENDOR_FILES = [['I', 'vendors.json'], ['II', 'vendors_age2.json'], ['III', 'vendors_age3.json'], ['IV', 'vendors_age4.json'], ['V', 'vendors_age5.json'], ['VI', 'vendors_age6.json'], ['VII', 'vendors_age7.json'], ['VIII', 'vendors_age8.json'], ['RODOS', 'vendors_age9.json']]
let ECON_VENDOR_DATA = {}
ECON_VENDOR_FILES.forEach(f => { try { ECON_VENDOR_DATA[f[0]] = (econRead('kubejs/data/diaithir/' + f[1]) || { vendors: [] }).vendors } catch (e) { ECON_VENDOR_DATA[f[0]] = [] } })
function econVendors(culture) {
  let out = [], at = Math.max(1, ECON_AGES.indexOf(econLocalAge(culture))) // the Telling shares Age I's vendors
  ECON_VENDOR_FILES.forEach(f => {
    if (ECON_AGES.indexOf(f[0]) > at) return
    ;(ECON_VENDOR_DATA[f[0]] || []).forEach(v => {
      if (!econTechAllows(culture, v)) return
      let same = out.filter(o => o.id === v.id)[0] // a kind that recurs in later Ages (the drinks trades) gains that Age's goods
      if (same) Object.keys(v.items).forEach(i => { if (same.items.indexOf(i) < 0) same.items.push(i) })
      else out.push({ id: v.id, needs: v.needs, items: Object.keys(v.items) })
    })
  })
  return out
}
// The profession a keeper plies, or 'none' for a wanderer.
// 26.1's profession toString is "Reference{ResourceKey[minecraft:villager_profession / minecraft:toolsmith]=VillagerProfession[...", so the name is read from that
// key, with the entity's nbt as the second way and the last word as the third.
function econProfessionName(k) {
  let s = ''
  try { s = String(k.villagerData.profession().toString()) } catch (e) { }
  let m = /villager_profession\s*\/\s*(?:[a-z0-9_.-]+:)?([a-z0-9_]+)/.exec(s)
  if (m) return m[1]
  try { let n = /(?:[a-z0-9_.-]+:)?([a-z0-9_]+)$/.exec(String(k.nbt.VillagerData.profession)); if (n) return n[1] } catch (e) { }
  let w = s.match(/[a-z_]+/g)
  return w && w.length ? w[w.length - 1] : 'none'
}
const econKeeperProfession = k => String(k.type) === 'diaithir_gods:priest' ? 'priest' : String(k.type) === 'minecraft:wandering_trader' ? 'none' : econProfessionName(k)
// May a keeper near the player deal in this item? Yes when no vendor list is in force; otherwise some keeper in reach must keep a kind that lists it.
function econVendorAllows(player, id) {
  if (typeof AGES === 'undefined') return true // no Ages engine (Deep Worlds): any keeper deals in anything open
  let vs = econVendors(econMarketOf(player).culture)
  if (!vs.length) return true
  let near = player.level.getEntitiesWithin(player.boundingBox.inflate(ECON_STORE_RANGE)).toArray().filter(e => e.isAlive() && ECON_KEEPERS.indexOf(String(e.type)) >= 0)
  let kinds = near.map(k => econVendorFor(econKeeperProfession(k), vs)).filter(v => v)
  if (kinds.some(v => v.items.indexOf(id) >= 0)) return true
  player.tell(Text.red(kinds.length ? 'No keeper here deals in that. Try ' + kinds.map(v => v.id.replace(/_/g, ' ')).join(' or ') + '.' : 'No keeper here deals in that.'))
  return false
}

const econOpen = (id, age) => econGood(id) && ECON_AGES.indexOf(age || econAge()) >= ECON_AGES.indexOf(econGood(id)[2])

// ---- Territory: claims from the engine's WorldSim, refreshed once a month ----
let ECON_CLAIMS = []
function econLoadClaims(server) {
  try {
    let Saved = Java.loadClass('net.diaithir.ages.mc.AgesSavedData')
    ECON_CLAIMS = econClaims(JSON.parse(String(Saved.get(server).sim.toJson())))
  } catch (e) {
    if (!ECON_CLAIMS.length && typeof CITIES !== 'undefined') // no engine: every historical town claims a disc for one culture
      ECON_CLAIMS = Object.keys(CITIES).map(id => ({ x: CITIES[id][1], z: CITIES[id][2], r: 600, culture: 'diaithir', realm: 'diaithir', town: id, name: CITIES[id][0] }))
  }
  ECON_CLAIMS.forEach(c => { ECON.regions[c.town] = [c.x, c.z] })
}

// The market a store keeper stands in: { town, name, culture } inside a claim; the free market (culture null) outside every one.
function econMarketOf(entity) {
  let c = econClaimAt(ECON_CLAIMS, entity.x, entity.z)
  return c ? { town: c.town, name: c.name, culture: c.culture } : { town: 'wilds', name: 'the free market', culture: null }
}

// ---- Money in a culture's market ----
// ctx: { culture, kind, did, design, denoms, rate (tw per unit), scale (units per tw), unit }. The free market deals in salt.
function econCtx(m) {
  let age = m.culture ? econLocalAge(m.culture) : econAge()
  let kind = m.culture ? econKindAt(age) : 'commodity'
  let culture = m.culture || 'wilds'
  let did = m.culture ? econDesignOf(ECON_DESIGNS, culture, kind, [ECON.own || {}, ECON_OWN], age) : ECON_DESIGNS.salund ? 'salund' : Object.keys(ECON_DESIGNS).filter(k => ECON_DESIGNS[k].commodity).sort()[0]
  let design = ECON_DESIGNS[did]
  let mid = id => econGood(id) ? econMid(ECON, ECON.regions, econGood, m.town, id) : 1
  let s = econCur(ECON, culture)
  if (s.did !== did + ':' + kind) { // first sight, or the Age moved the culture to another money
    // moving from a commodity money to coin: the first coin is pegged to the staple
    if (kind !== 'commodity' && (s.did || '').endsWith(':commodity')) {
      let old = ECON_DESIGNS[s.did.split(':')[0]]
      if (old && old.commodity) s.peg = { good: old.commodity.good, perUnit: old.commodity.perUnit }
    }
    s.did = did + ':' + kind
    s.ref = s.money = Math.round(ECON_MONEY_REF / Math.max(0.01, econDesignValue(ECON, design, did, kind, mid)))
    s.tb = 0
  }
  let rate = econRate(ECON, culture, design, did, kind, mid)
  let part = kind === 'commodity' ? design.commodity : design.coin
  return { culture: culture, age: age, kind: kind, did: did, design: design, denoms: part.denoms, rate: rate, scale: 1 / rate, mid: mid,
    unit: design.name + (m.culture ? ' ' + econCultureName(culture) : '') }
}

// Other designs of the same family of money (commodity, or coin and fiat) with their kind for econDesignValue.
// A tradition holds both a commodity money (Ages I-IV) and a coin (Age V on): the part for the kind is what counts.
const econPartOf = (d, kind) => kind === 'commodity' ? d.commodity : d.coin
const econForeignDesigns = ctx => Object.keys(ECON_DESIGNS).filter(did => did !== ctx.did && !!econPartOf(ECON_DESIGNS[did], ctx.kind))

// The player's money in ctx's currency: its own items, plus other designs at their metal or goods value less the changer's fee.
function econPurse(player, ctx) {
  // an item counts once: designs share goods (a silver nugget is the unit of three traditions), and the culture's own money claims them first
  let seen = {}
  let own = ctx.denoms.reduce((sum, d) => { seen[d[0]] = true; return sum + econCount(player, d[0]) * d[1] }, 0)
  let foreign = 0
  econForeignDesigns(ctx).forEach(did => {
    let d = ECON_DESIGNS[did]
    let n = econPartOf(d, ctx.kind).denoms.reduce((sum, x) => {
      if (seen[x[0]]) return sum
      seen[x[0]] = true
      return sum + econCount(player, x[0]) * x[1]
    }, 0)
    if (n) foreign += n * econDesignValue(ECON, d, did, ctx.kind, ctx.mid) / ctx.rate * (1 - ECON_CHANGER_FEE)
  })
  return Math.floor(own + foreign)
}

function econGive(player, amount, ctx) {
  ctx.denoms.forEach(d => {
    let n = Math.floor(amount / d[1])
    amount -= n * d[1]
    if (n > 0) player.server.runCommandSilent(`give ${player.username} ${d[0]} ${n}`)
  })
}

// Takes the whole purse and hands back change in the market's own money, so any mix of denominations pays.
function econTake(player, amount, ctx) {
  let purse = econPurse(player, ctx)
  if (purse < amount) return false
  ctx.denoms.forEach(d => player.server.runCommandSilent(`clear ${player.username} ${d[0]}`))
  econForeignDesigns(ctx).forEach(did => econPartOf(ECON_DESIGNS[did], ctx.kind).denoms.forEach(x => player.server.runCommandSilent(`clear ${player.username} ${x[0]}`)))
  econGive(player, purse - amount, ctx)
  return true
}

function econNearStore(player) {
  let near = player.level.getEntitiesWithin(player.boundingBox.inflate(ECON_STORE_RANGE))
    .stream().anyMatch(e => e.isAlive() && ECON_KEEPERS.indexOf(String(e.type)) >= 0)
  if (!near) player.tell(Text.red('There is no store here. Find a village or a wandering trader.'))
  return near
}

const econHeld = (region, id) => (ECON.stock[region] || {})[id] === undefined ? econGood(id)[1] : ECON.stock[region][id]
const econMidNow = (region, id, stock) => econMid(ECON, ECON.regions, econGood, region, id, stock)
const econBuyPrice = (region, id, ctx) => econBuyPriceOf(econMidNow(region, id) * (ctx ? ctx.scale : 1))
const econSellPrice = (region, id, ctx) => econSellPriceOf(econMidNow(region, id) * (ctx ? ctx.scale : 1))

// A commodity money is physical: what buyers pay into a store joins its stock of the staple (so a flood of it shows in the staple's price,
// and the money cheapens), and what the store pays a seller comes out of that stock. A store cannot pay out more than it holds.
function econCommodityPay(ctx, region, units) {
  if (ctx.kind !== 'commodity') return true
  let g = ctx.design.commodity.good, n = units * ctx.design.commodity.perUnit, held = econHeld(region, g)
  if (n < 0 && held + n < 0) return false
  ECON.stock[region] = ECON.stock[region] || {}
  ECON.stock[region][g] = held + n
  return true
}

// A storeroom full of the money good takes no more of it (8 times the usual stock), however it was made, a farm or a salt pan.
const ECON_ROOM = 8
function econRoomFor(ctx, region, units) {
  if (ctx.kind !== 'commodity') return true
  let g = ctx.design.commodity.good
  return econHeld(region, g) + units * ctx.design.commodity.perUnit <= ECON_ROOM * econGood(g)[1]
}

// A visitor is a player whose home culture is another's. Home is the first culture a player trades in.
function econForeign(player, culture) {
  let key = String(player.uuid)
  if (!ECON.home[key]) ECON.home[key] = culture
  return ECON.home[key] !== culture
}

// Each completed trade boosts the player's trade techs (the tech tree's "trade" eureka); no tech engine, no effect.
function econTradeEureka(player) {
  try { player.server.runCommandSilent(`ages tech eureka ${player.username} trade any`) } catch (e) { }
}

function econBuy(player, id, n, region, market) {
  market = market || econMarketOf(player)
  region = region || market.town
  if (!econOpen(id)) return player.tell(Text.red('No store sells that in this Age.'))
  if (!econVendorAllows(player, id)) return
  let held = econHeld(region, id)
  if (held < n) return player.tell(Text.red(`The store has only ${Math.floor(held)}.`))
  let ctx = econCtx(market)
  let price = econTrade(ECON, ECON.regions, econGood, region, id, n, true, ctx.scale)
  let rate = market.culture ? econTaxRate(ECON_TAXES, ctx.culture, ctx.age, ECON) : 0
  let tax = Math.ceil(price * rate)
  if (!econRoomFor(ctx, region, price + tax)) {
    ECON.stock[region][id] = held // the storeroom is full of this money
    return player.tell(Text.red(`The keeper will take no more ${ctx.design.name} today: the storeroom is full of it. Pay in goods or in another money.`))
  }
  if (!econTake(player, price + tax, ctx)) {
    ECON.stock[region][id] = held // the purse was short: nothing was bought
    return player.tell(Text.red(`That costs ${price + tax} ${ctx.unit}. You have ${econPurse(player, ctx)}.`))
  }
  econCommodityPay(ctx, region, price + tax)
  econMint(ECON, ctx.culture, -price)
  econMint(ECON, ctx.culture, -tax)
  econCur(ECON, ctx.culture).treasury += tax
  if (market.culture && econForeign(player, ctx.culture)) econFlow(ECON, ctx.culture, price * ctx.rate)
  econCur(ECON, ctx.culture).vol += price * ctx.rate
  ECON_PUB.dirty = true
  player.server.runCommandSilent(`give ${player.username} ${id} ${n}`)
  econSave()
  econTradeEureka(player)
  player.tell(Text.green(`Bought ${n} ${Item.of(id).hoverName.string} for ${price + tax} ${ctx.unit}${tax ? ` (tax ${tax})` : ''}.`))
}

function econSell(player, id, n, region, market) {
  market = market || econMarketOf(player)
  region = region || market.town
  if (!econOpen(id)) return player.tell(Text.red('No store buys that in this Age.'))
  if (!econVendorAllows(player, id)) return
  n = Math.min(n, econCount(player, id))
  if (n < 1) return player.tell(Text.red('You have none to sell.'))
  let ctx = econCtx(market)
  let paid = econTrade(ECON, ECON.regions, econGood, region, id, n, false, ctx.scale)
  paid = Math.max(1, Math.round(paid * econBottleFactor(player, id, n))) // an aged or strong bottle fetches more
  let tax = Math.floor(paid * (market.culture ? econTaxRate(ECON_TAXES, ctx.culture, ctx.age, ECON) : 0))
  if (paid - tax < 1) {
    ECON.stock[region][id] -= n // not worth a single unit of money: nothing changes hands
    return player.tell(Text.red('That is not worth a single coin here. Sell more at once.'))
  }
  if (!econCommodityPay(ctx, region, -(paid - tax))) {
    ECON.stock[region][id] -= n // the store cannot pay in kind: the sale is off
    return player.tell(Text.red(`The keeper has not enough ${ctx.design.name} to pay you. Sell for goods, or sell less.`))
  }
  ECON_PUB.dirty = true
  player.server.runCommandSilent(`clear ${player.username} ${id} ${n}`)
  econGive(player, paid - tax, ctx)
  econMint(ECON, ctx.culture, paid - tax)
  econCur(ECON, ctx.culture).treasury += tax // the tax never reached circulation
  if (market.culture && econForeign(player, ctx.culture)) econFlow(ECON, ctx.culture, -paid * ctx.rate)
  econCur(ECON, ctx.culture).vol += paid * ctx.rate
  econSave()
  econTradeEureka(player)
  player.tell(Text.green(`Sold ${n} ${Item.of(id).hoverName.string} for ${paid - tax} ${ctx.unit}${tax ? ` (tax ${tax})` : ''}.`))
}

// "[Buy 1] [Buy n] [Sell 1] [Sell all]" appended to a line; cmd is "/market" or "/exchange", key the item or company.
function econButtons(line, cmd, key, big) {
  return line.append(Text.aqua('[Buy 1]').clickRunCommand(`${cmd} buy ${key} 1`)).append(' ')
    .append(Text.aqua(`[Buy ${big}]`).clickRunCommand(`${cmd} buy ${key} ${big}`)).append(' ')
    .append(Text.yellow('[Sell 1]').clickRunCommand(`${cmd} sell ${key} 1`)).append(' ')
    .append(Text.yellow('[Sell all]').clickRunCommand(`${cmd} sell ${key} 1000000`))
}

function econShow(player) {
  let market = econMarketOf(player), ctx = econCtx(market)
  player.tell(Text.gold(`Prices at ${market.name}${market.culture ? ', ' + econCultureName(market.culture) : ''}, in ${ctx.unit} (you have ${econPurse(player, ctx)}):`))
  let native = econNativeLine(player, ctx)
  if (native) player.tell(native)
  Object.keys(ECON_GOODS).filter(econOpen).forEach(id => player.tell(econButtons(
    Text.white(`${Item.of(id).hoverName.string}: buy ${econBuyPrice(market.town, id, ctx)}, sell ${econSellPrice(market.town, id, ctx)}, stock ${Math.floor(econHeld(market.town, id))} `),
    '/market', `"${id}"`, 16)))
}

// ---- The week: tax and wages, trade balance, bank interest ----
// The culture moves to the commodity design whose staple has held its value best, and starts a fresh money.
function econReform(culture, town, ctx) {
  let s = econCur(ECON, culture), best = null, bv = -1
  Object.keys(ECON_DESIGNS).forEach(did => {
    let d = ECON_DESIGNS[did]
    if (!d.commodity || did === ctx.did || !econAgeLe(d.age, ctx.age)) return
    let v = econMidNow(town, d.commodity.good) / econGood(d.commodity.good)[0]
    if (v > bv) { bv = v; best = did }
  })
  if (!best) return
  ECON.own = ECON.own || {}
  ECON.own[culture] = best
  s.weak = 0; s.reform = false; s.rateRef = 0; s.prevRate = 0; s.did = ''
  console.info(`[economy] ${culture} reforms its money from ${ctx.design.name} to ${ECON_DESIGNS[best].name}`)
}
const ECON_TAXES = {} // per-culture tax rates, when a realm sets its own; otherwise the Age's
// The crops' year fraction at a market (0 = early spring), from the climate thread's global.SEASON_AT when it is loaded. The call adds half a year
// in the south (z > 0 on Tellus) but crops and weather do not flip there, so that half year is taken off again.
// KubeJS 8 has no Utils.server, so the server is kept from the first tick (a reload re-runs this file before ticks resume)
let ECON_SERVER = null
ServerEvents.tick(event => { ECON_SERVER = event.server })

function econCropYear(region) {
  try {
    let pos = ECON.regions[region], f = typeof global !== 'undefined' && global.SEASON_AT
    if (!pos || !f || !ECON_SERVER) return null
    let r = f(ECON_SERVER.overworld(), pos[0], pos[1])
    if (!r) return null
    return r.hemisphere === 'south' ? (r.year + 0.5) % 1 : r.year
  } catch (e) { return null }
}

// The region's outdoor temperature now, as the share of the room-temperature spoilage rate it allows (winter markets keep stock, summer ones lose it).
function econRegionWarmth(region) {
  try {
    let pos = ECON.regions[region], g = typeof global !== 'undefined' ? global : {}
    if (!pos || !ECON_SERVER) return 1
    // the outdoor surface (TEMP_SURFACE); TEMP_AT at y 64 is the ground in this Y200 world, so it only stands in when the surface call is absent
    let level = ECON_SERVER.overworld()
    return econWarmth(g.TEMP_SURFACE ? g.TEMP_SURFACE(level, pos[0], pos[1]) : g.TEMP_AT ? g.TEMP_AT(level, pos[0], 64, pos[1]) : null)
  } catch (e) { return 1 }
}

function econWeekly() {
  ECON.week++
  econSpoil(ECON, econGood, econRegionWarmth)
  econSeason(ECON, econGood, ECON.week, econCropYear)
  let seat = {}
  ECON_CLAIMS.forEach(c => { if (!seat[c.culture]) seat[c.culture] = c.town })
  Object.keys(seat).forEach(culture => {
    let ctx = econCtx({ town: seat[culture], culture: culture })
    if (ctx.kind === 'coin' && ctx.did) econWear(ECON, culture, ctx.did)
    let s = econWeek(ECON, culture, ctx.did, ctx.kind, ECON_WEEKLY_BILL * econCur(ECON, culture).ref, ctx.rate)
    if (s.reform) econReform(culture, seat[culture], ctx)
    if (econAgeAtLeast('V')) bondWeek(ECON, culture).forEach(n => console.info('[economy] ' + culture + ': ' + n))
    if (econAgeAtLeast('III')) bankWeek(econBank(ECON, culture), econAge(), ctx.rate).forEach(n => console.info('[economy] ' + culture + ': ' + n))
  })
  if (COMPANY_ON) companyWeek(companyEnv(), ECON).forEach(n => console.info('[economy] ' + n))
  econSave()
}

// The Codex table: before Age V once a week, from Age V on every trade.
const ECON_DAYS_PER_MC_DAY = { TELLING: 14, I: 14, II: 14, III: 14, IV: 14, V: 7, VI: 7, VII: 3, VIII: 3, RODOS: 3 }
const ECON_INSTANT_FROM = 'V'
const ECON_PUB = { date: '', day: 0, sinceWeek: 0, dirty: true, lastTick: 0 }

// <world>/diaithir/prices.json, read by the Codex:
//   updated, updatedDay, currency (legacy), prices {id: mean tw price} (legacy),
//   base {id: tw price at par}, rates {currency name: tw per unit},
//   markets {town id: {name, culture, currency, perTw, territory: [{x, z, r}], prices: {id: price in the currency, traded items only}}}.
// An item missing from a market's prices costs base[id] * perTw there. Cultures and currencies are named in Diaithiris.
function econTable() {
  let prices = {}, base = {}, markets = {}, rates = {}, traders = {}
  Object.keys(ECON.stock).forEach(r => Object.keys(ECON.stock[r]).forEach(id => { (traders[id] = traders[id] || []).push(r) }))
  Object.keys(ECON_PRICES).concat(Object.keys(ECON_GOODS)).forEach(id => {
    let b = econGood(id)[0], rs = traders[id]
    base[id] = b
    prices[id] = Math.round((rs ? rs.reduce((t, r) => t + econMidNow(r, id), 0) / rs.length : b) * 100) / 100
  })
  let ctxs = {}
  ECON_CLAIMS.forEach(c => {
    let ctx = ctxs[c.culture] = ctxs[c.culture] || econCtx({ town: c.town, culture: c.culture })
    rates[ctx.unit] = Math.round(ctx.rate * 10000) / 10000
    let mk = markets[c.town] = { name: c.name, culture: econCultureName(c.culture), currency: ctx.unit, perTw: Math.round(ctx.scale * 10000) / 10000,
      territory: [{ x: c.x, z: c.z, r: c.r }], prices: {} }
    Object.keys(ECON.stock[c.town] || {}).forEach(id => { if (base[id] !== undefined) mk.prices[id] = Math.round(econMidNow(c.town, id) * ctx.scale * 100) / 100 })
  })
  return { updated: ECON_PUB.date, updatedDay: ECON_PUB.day, currency: 'tw', prices: prices, base: base, rates: rates, markets: markets }
}

function econPublishTable(server) {
  try {
    let LevelResource = Java.loadClass('net.minecraft.world.level.storage.LevelResource')
    // KubeJS's class filter refuses java.nio.file.Files; JsonIO writes a game-directory-relative path and makes its folders
    let world = String(server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().getFileName())
    JsonIO.write(world + '/diaithir/prices.json', econTable())
    ECON_PUB.dirty = false
    console.info('[economy] price table published ' + ECON_PUB.date)
  } catch (e) { console.error('[economy] price table not written: ' + e) }
}

try {
  NativeEvents.onEvent(Java.loadClass('net.diaithir.ages.mc.StoryEvent'), e => {
    if (String(e.kind) === 'age') ECON_PUB.dirty = true
    if (String(e.kind) !== 'day') return
    ECON_PUB.date = String(e.key)
    ECON_PUB.day = parseInt(String(e.value)) || 0
    ECON_PUB.sinceWeek += ECON_DAYS_PER_MC_DAY[econAge()] || 7
    if (ECON_PUB.sinceWeek >= 7) {
      ECON_PUB.sinceWeek = 0
      let server = Java.loadClass('net.neoforged.neoforge.server.ServerLifecycleHooks').getCurrentServer()
      if (ECON.week % 4 === 0) econLoadClaims(server) // territories move with the realms, a month at a time
      econWeekly()
      exchWeek()
      if (!econAgeAtLeast(ECON_INSTANT_FROM)) econPublishTable(server)
    }
  })
} catch (e) { console.warn('[economy] no Ages engine: the price table is published on trades only') }

// Age V and later: a trade updates the table at once (at most every 5 seconds, since the table is large).
ServerEvents.tick(event => {
  if (!ECON_PUB.dirty || !econAgeAtLeast(ECON_INSTANT_FROM) || event.server.tickCount - ECON_PUB.lastTick < 100) return
  ECON_PUB.lastTick = event.server.tickCount
  econPublishTable(event.server)
})
// First table of a session, so the Codex has one before the first week turns.
ServerEvents.loaded(event => { econLoadClaims(event.server); econPublishTable(event.server) })

ServerEvents.commandRegistry(event => {
  let { commands: Commands, arguments: Arguments } = event
  let atStore = fn => ctx => { let p = ctx.source.getPlayerOrException(); if (econNearStore(p)) fn(p, ctx); return 1 }
  let trade = (name, fn) => Commands.literal(name).then(Commands.argument('item', Arguments.STRING.create(event))
    .then(Commands.argument('count', Arguments.INTEGER.create(event)).executes(atStore((p, ctx) =>
      fn(p, Arguments.STRING.getResult(ctx, 'item'), Math.max(1, Arguments.INTEGER.getResult(ctx, 'count')))))))
  event.register(Commands.literal('coin').then(Commands.argument('coins', Arguments.INTEGER.create(event)).executes(cx => {
    let p = cx.source.getPlayerOrException(), market = econMarketOf(p)
    if (!market.culture || !econNearStore(p)) return 1
    let ctx = econCtx(market), s = econCur(ECON, market.culture), n = Math.max(1, Arguments.INTEGER.getResult(cx, 'coins'))
    if (!s.peg || ctx.kind === 'commodity') return p.tell(Text.red('This culture has no coin pegged to a staple.')) || 1
    if (!econRedeemable(ECON, market.culture, ctx.did)) return p.tell(Text.red('The peg is broken: the keeper will not exchange the staple for coin.')) || 1
    let need = n * s.peg.perUnit
    if (econCount(p, s.peg.good) < need) return p.tell(Text.red(`You need ${need} ${Item.of(s.peg.good).hoverName.string}.`)) || 1
    p.server.runCommandSilent(`clear ${p.username} ${s.peg.good} ${need}`)
    ECON.stock[market.town] = ECON.stock[market.town] || {}
    ECON.stock[market.town][s.peg.good] = econHeld(market.town, s.peg.good) + need
    econGive(p, n, ctx)
    econMint(ECON, market.culture, n)
    p.tell(Text.green(`Exchanged ${need} ${Item.of(s.peg.good).hoverName.string} for ${n} ${ctx.unit}.`))
    econSave()
    return 1
  })))
  event.register(Commands.literal('redeem').then(Commands.argument('coins', Arguments.INTEGER.create(event)).executes(cx => {
    let p = cx.source.getPlayerOrException(), market = econMarketOf(p)
    if (!market.culture || !econNearStore(p)) return 1
    let ctx = econCtx(market), s = econCur(ECON, market.culture), n = Math.max(1, Arguments.INTEGER.getResult(cx, 'coins'))
    if (!s.peg) return p.tell(Text.red('These coins are not pegged to anything.')) || 1
    if (!econRedeemable(ECON, market.culture, ctx.did)) return p.tell(Text.red('The crown has suspended redemption: the coin has fallen too far from its peg.')) || 1
    if (econPurse(p, ctx) < n || !econTake(p, n, ctx)) return p.tell(Text.red(`You have ${econPurse(p, ctx)} ${ctx.unit}.`)) || 1
    let held = econHeld(market.town, s.peg.good), out = Math.min(held, n * s.peg.perUnit)
    ECON.stock[market.town] = ECON.stock[market.town] || {}
    ECON.stock[market.town][s.peg.good] = held - out
    econMint(ECON, market.culture, -n)
    p.server.runCommandSilent(`give ${p.username} ${s.peg.good} ${Math.floor(out)}`)
    p.tell(Text.green(`Redeemed ${n} ${ctx.unit} for ${Math.floor(out)} ${Item.of(s.peg.good).hoverName.string}.`))
    econSave()
    return 1
  })))
  event.register(Commands.literal('economy').executes(cx => {
    let p = cx.source.getPlayerOrException(), market = econMarketOf(p)
    if (!market.culture) { p.tell(Text.gray('No culture claims this ground.')); return 1 }
    let ctx = econCtx(market), s = econCur(ECON, market.culture)
    p.tell(Text.gold(`${econCultureName(market.culture)}: ${ctx.unit}`))
    p.tell(Text.white(`Prices this week ${s.infl >= 0 ? 'up' : 'down'} ${Math.abs(s.infl * 100).toFixed(1)}%. In circulation ${Math.round(s.money)} of ${Math.round(s.ref)} at the start; trade balance ${s.tb.toFixed(2)}; treasury ${Math.round(s.treasury)}; velocity ${econVelocity(s).toFixed(2)} a week.`))
    if (s.weak) p.tell(Text.red(`The money has lost half its worth for ${s.weak} week(s); four and the culture changes its money.`))
    return 1
  }))
  event.register(Commands.literal('market').executes(atStore((p, ctx) => econShow(p)))
    .then(trade('buy', econBuy))
    .then(trade('sell', econSell))
    .then(Commands.literal('sellhand').executes(atStore((p, ctx) => econSell(p, String(p.mainHandItem.id), p.mainHandItem.getCount())))))
})
