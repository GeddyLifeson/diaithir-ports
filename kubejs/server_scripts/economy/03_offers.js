// Trade screens. A keeper's trade screen is the villager's own, built from the live market each time the player opens it: the cost is
// the local money in coin items (two slots, so a price is one big and one small denomination), the result is the good; selling is the
// other way round. Pure planning here; trade_screens.js does the Java.

// A price in units, paid in at most two denominations (largest first: [[item, units in the smallest]]). Returns {cost: [[item, count]],
// units} where units is the exact amount the slots hold (the price rounded up to what two slots can carry), or null above 64 of the biggest.
function econSplit(price, denoms) {
  price = Math.max(1, Math.ceil(price))
  for (let i = 0; i < denoms.length; i++) {
    let hi = denoms[i], lo = denoms[i + 1], a = Math.floor(price / hi[1]), rem = price - a * hi[1], b = 0
    if (lo) { b = Math.ceil(rem / lo[1]); if (b * lo[1] >= hi[1]) { a++; b = 0 } } else if (rem) { a++; rem = 0 }
    if (a > 64 || b > 64) continue
    if (a === 0 && lo) continue // a price under this denomination is paid in a smaller one
    let cost = [[hi[0], a]]
    if (b) cost.push([lo[0], b])
    return { cost: cost, units: a * hi[1] + (lo ? b * lo[1] : 0) }
  }
  let small = denoms[denoms.length - 1]
  return price <= 64 * small[1] ? { cost: [[small[0], price / small[1]]], units: price } : null
}

// The money a keeper hands over for a sale, in one stack of the biggest denomination that fits in 64: returns [item, count, units].
function econPayout(units, denoms) {
  for (let i = 0; i < denoms.length; i++) {
    let n = Math.floor(units / denoms[i][1])
    if (n >= 1) return [denoms[i][0], Math.min(64, n), Math.min(64, n) * denoms[i][1]]
  }
  return null
}

// goods: item ids the keeper deals in (buyN of them for sale, sellN wanted). quote(id, n, buying) is the money for n units
// (econQuote × tax). Bundles are sized so that a bundle costs at least 3 units of money.
function econOfferPlan(goods, ctx, quote, held, have, buyN, sellN) {
  let out = []
  goods.slice(0, buyN).forEach(id => {
    if (held(id) < 1) return
    let n = 1
    while (quote(id, n, true) < 3 && n < 16 && held(id) >= n * 2) n *= 2
    let s = econSplit(quote(id, n, true), ctx.denoms)
    if (s) out.push({ dir: 'buy', id: id, n: n, cost: s.cost, units: s.units })
  })
  goods.slice(buyN, buyN + sellN).forEach(id => {
    let n = 1
    while (quote(id, n, false) < 3 && n < 16) n *= 2
    let pay = econPayout(quote(id, n, false), ctx.denoms)
    if (pay) out.push({ dir: 'sell', id: id, n: n, result: [pay[0], pay[1]], units: pay[2] })
  })
  return out
}

// Which goods a keeper of this profession deals in: ids matching the profession's pattern among those open in the Age, in an order
// fixed by the keeper (seed), so each keeper shows the same stall every visit.
const ECON_STALLS = {
  farmer: /wheat|bread|carrot|potato|beetroot|melon|pumpkin|hay_block|seeds$|apple|honey|cake|pie/, fisherman: /cod|salmon|kelp|fishing_rod|bucket|tropical|puffer/,
  shepherd: /wool|string|shears|carpet|dye$/, leatherworker: /leather|saddle|rabbit_hide|scute/, butcher: /beef|porkchop|mutton|chicken|rabbit|bone|feather/,
  toolsmith: /(pickaxe|axe|shovel|hoe)$|flint|stick/, armorer: /(helmet|chestplate|leggings|boots)$|iron_ingot|chain|shield/, weaponsmith: /sword$|bow$|crossbow|arrow|iron_ingot|axe$/,
  mason: /stone|brick|clay|cobble|andesite|diorite|granite|sandstone|terracotta/, fletcher: /arrow|bow$|stick|feather|flint|string/,
  cartographer: /paper|map|compass/, cleric: /redstone|lapis|amethyst|glowstone|quartz|potion|bottle/, librarian: /book|paper|ink|feather|lantern/,
  nitwit: /honeycomb|oak_log|clay_ball|flint|bone|salt/, none: /honeycomb|oak_log|leather|wheat|flint|salt|bone/
}
// The vendor kind each villager profession keeps in each Age (vendors.json, vendors_age2.json). A later Age's entries replace the earlier ones.
const ECON_PROF_VENDOR = [
  { farmer: 'forager', fisherman: 'fisher', butcher: 'hunter', shepherd: 'hide_worker', leatherworker: 'hide_worker', toolsmith: 'flint_smith', mason: 'flint_smith',
    armorer: 'flint_smith', weaponsmith: 'flint_smith', fletcher: 'timber_worker', cleric: 'herb_healer', librarian: 'herb_healer', cartographer: 'herb_healer',
    nitwit: 'salt_trader', none: 'salt_trader' },
  { farmer: 'farmer', mason: 'potter', nitwit: 'baker', shepherd: 'brewer' },
  { toolsmith: 'smith', armorer: 'smith', weaponsmith: 'smith', mason: 'smith', fletcher: 'sylvan_wright', fisherman: 'angler_hall', cleric: 'banker', priest: 'banker',
    librarian: 'rite_keeper', cartographer: 'jeweller', nitwit: 'trader', butcher: 'vintner', leatherworker: 'distiller' },
  { farmer: 'miller', librarian: 'alchemist', cartographer: 'scribe', fletcher: 'joiner', weaponsmith: 'chest_maker' },
  { toolsmith: 'engineer', armorer: 'engineer', weaponsmith: 'wireman', mason: 'pipefitter', cartographer: 'wireman' },
  { toolsmith: 'machinist', armorer: 'fitter', weaponsmith: 'locksmith', mason: 'power_engineer', cleric: 'locksmith', cartographer: 'machinist' },
  { toolsmith: 'fabricator', armorer: 'grid_engineer', weaponsmith: 'fabricator', mason: 'grid_engineer', librarian: 'network_architect', cartographer: 'network_architect',
    cleric: 'network_architect' },
  { toolsmith: 'plasma_armourer', armorer: 'aegis_warden', weaponsmith: 'plasma_armourer', mason: 'fusion_wright', librarian: 'mind_wright', cleric: 'mind_wright',
    cartographer: 'orbital_broker', farmer: 'nano_artisan' },
  { toolsmith: 'shardsmith', armorer: 'shardsmith', weaponsmith: 'shardsmith', mason: 'shardsmith', cleric: 'shard_listener', librarian: 'shard_listener',
    cartographer: 'shard_listener' }
]

// vendors: the active kinds [{ id, items: [id...] }] (market.js builds the list for the Age). With a vendor, the stall is exactly that vendor's items that are
// open in the Age; without one (no vendor list for the Age) it is the profession's pattern as before.
function econVendorFor(profession, vendors) {
  let has = id => vendors.filter(v => v.id === id)[0]
  let found = null
  // each Age's table in turn: a kind not open yet (no entry in vendors) leaves the Age before's choice standing
  ECON_PROF_VENDOR.forEach(m => { if (m[profession] && has(m[profession])) found = has(m[profession]) })
  return found || has('salt_trader') || null
}

function econStall(profession, openIds, seed, vendors) {
  let vendor = vendors && vendors.length ? econVendorFor(profession, vendors) : null
  let ids
  if (vendor) { let only = {}; vendor.items.forEach(i => { only[i] = true }); ids = openIds.filter(id => only[id]) }
  else { let re = ECON_STALLS[profession] || ECON_STALLS.none; ids = openIds.filter(id => re.test(id.split(':').pop())) }
  let h = n => { let x = (seed * 2654435761 + n * 40503) >>> 0; return x ^ (x >>> 13) }
  return ids.map((id, i) => [h(i), id]).sort((a, b) => a[0] - b[0]).map(p => p[1])
}
