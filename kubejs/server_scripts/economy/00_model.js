// JsonIO.read hands back Java lists and maps (a java.util.ArrayList has no usable .reduce in Rhino: KubeJS's adapter calls the callback with one argument).
// econPlain turns them into plain JS arrays and objects, all the way down; every file the economy reads goes through it.
function econPlain(x) {
  if (x === null || x === undefined || typeof x !== 'object') return x
  if (Array.isArray(x)) return x.map(econPlain)
  // a boxed Java number or boolean (their values are read, never walked as objects)
  if (typeof x.doubleValue === 'function') return Number(x.doubleValue())
  if (typeof x.booleanValue === 'function') return x.booleanValue()
  // a Java list: walked by index, which hands back JS numbers and strings as the adapter does elsewhere
  if (typeof x.size === 'function' && typeof x.get === 'function' && typeof x.entrySet !== 'function') {
    let a = []
    for (let i = 0; i < x.size(); i++) a.push(econPlain(x[i]))
    return a
  }
  // a Java map or a plain object: its keys and properties, as the rest of the economy has always read them
  let o = {}
  Object.keys(x).forEach(k => { o[k] = econPlain(x[k]) })
  return o
}
const econRead = path => econPlain(JsonIO.read(path))

// The price model. Pure functions: no KubeJS globals, so tools/economy/test_economy.js runs them under Node.
// Prices move only through trades (nothing drifts back on its own). What a trade moves, and how far:
//  1. Demand curve with its own price elasticity e: necessities are inelastic (0.4), luxuries elastic (1.8). The market clears
//     where stock meets demand, so price = base * (usual stock / stock) ^ (1 / e): a flood of an inelastic good crashes its
//     price, a flood of a luxury barely dents it, and every extra unit is worth less than the last (diminishing marginal value).
//  2. Substitutes move together: a glut of one metal ingot pulls down every other metal ingot traded in the same market
//     (cross elasticity), likewise logs, meats, grains and gems.
//  3. Production cost is a soft floor. Sellers hold stock rather than sell below it: raw goods floor at 30% of base; made goods
//     (tools, armor, storage blocks) floor at 80% of their inputs' current prices, so tools follow their inputs up and down.
//  4. Money: prices here are real, in tw (the unit of account, 01_polity.js). What a culture's money is worth, and so what a price
//     reads in it, comes from econRate (its backing, its supply in circulation, its trade balance), not from this file.
//  5. Markets are regional: each town has its own stock. Goods flow from the cheaper town to the dearer one until the gap is
//     only the freight, so the cheap price rises and the dear one falls by half the excess each; hauling goods between
//     towns pays only for the gap beyond the freight.
// State: { stock: { region: { id: units } } }, regions: { region: [x, z] }.

const ECON_FREIGHT = 0.00002 // of base price per block per unit
const ECON_FLOOR_RAW = 0.3
const ECON_FLOOR_MADE = 0.8
const ECON_LABOUR = 0.25 // tw of hand work per input unit: a smith's hour is in the price of the blade, not just the ingots
const ECON_SUBSTITUTION = 0.25

const ECON_LUXURY = /diamond|emerald|ruby|sapphire|netherite|nether_star|amethyst|totem|golden_apple|music_disc|enchanted|elytra|spawn_egg|trident|beacon|echo|gold(_ingot|_block)?$|ender_|dragon|smithing_template|pottery_sherd/
const ECON_NECESSITY = /^(bread|wheat|beef|porkchop|mutton|chicken|cod|salmon|rabbit|potato|carrot|beetroot|apple|cooked_.*|salt_.*|oak_.*|spruce_.*|birch_.*|jungle_.*|acacia_.*|dark_oak_.*|cobblestone|stone|dirt|flint|leather|string|bone|feather|stick|coal|charcoal|torch|water_bucket|white_wool|clay_ball|sand|gravel)$/

function econElasticity(id) {
  let path = String(id).split(':').pop()
  if (ECON_LUXURY.test(path)) return 1.8
  if (ECON_NECESSITY.test(path)) return 0.4
  return 1.0
}

const ECON_GROUPS = [
  ['metal', /_ingot$/], ['log', /_log$|_stem$/], ['meat', /^(cooked_)?(beef|porkchop|mutton|chicken|rabbit|cod|salmon)$/],
  ['grain', /^(wheat|bread|baked_potato|potato|carrot|beetroot)$/], ['gem', /^(diamond|emerald|amethyst_shard|lapis_lazuli|quartz)$|_gem$/]
]
function econGroup(id) {
  let path = String(id).split(':').pop()
  for (let g of ECON_GROUPS) if (g[1].test(path)) return g[0]
  return null
}

// Made goods: [input item, units of input]. Tools and armor from the ingot or gem of their material, blocks from nine parts.
const ECON_MATERIAL = { iron: 'minecraft:iron_ingot', golden: 'minecraft:gold_ingot', diamond: 'minecraft:diamond',
  netherite: 'minecraft:netherite_ingot', copper: 'minecraft:copper_ingot', stone: 'minecraft:cobblestone', wooden: 'minecraft:oak_planks',
  chainmail: 'minecraft:iron_ingot', leather: 'minecraft:leather' }
const ECON_UNITS = { sword: 2, axe: 3, pickaxe: 3, shovel: 1, hoe: 2, helmet: 5, chestplate: 8, leggings: 7, boots: 4 }
function econInputs(id, known) {
  let path = String(id).split(':').pop()
  let m = /^(\w+?)_(sword|axe|pickaxe|shovel|hoe|helmet|chestplate|leggings|boots)$/.exec(path)
  if (m && ECON_MATERIAL[m[1]]) return [[ECON_MATERIAL[m[1]], ECON_UNITS[m[2]]]]
  let b = /^(\w+)_block$/.exec(path)
  if (b) {
    let ns = String(id).split(':')[0]
    for (let part of [b[1] + '_ingot', b[1], b[1] + '_gem', 'raw_' + b[1]]) if (known(ns + ':' + part)) return [[ns + ':' + part, 9]]
  }
  return null
}

const econSoftMax = (a, b) => Math.pow(Math.pow(a, 8) + Math.pow(b, 8), 1 / 8)
const econDist = (regions, a, b) => {
  let pa = regions[a], pb = regions[b]
  if (!pa || !pb) return 8000
  return Math.hypot(pa[0] - pb[0], pa[1] - pb[1])
}

// good(id) -> [base price, usual stock] or null. Mid price of one unit in a region at the given stock (default: now).
function econMid(state, regions, good, region, id, stock, depth) {
  let g = good(id)
  if (!g) return 0
  depth = depth || 0
  let base = g[0], usual = g[1]
  let held = state.stock[region] || {}
  let s = Math.max(1, stock === undefined ? (held[id] === undefined ? usual : held[id]) : stock)
  let e = econElasticity(id)
  let raw = base * Math.min(12, Math.max(0.01, Math.pow(usual / s, 1 / e)))
  // substitutes in this market: a glut in the group pushes this price down, a shortage up
  let grp = econGroup(id)
  if (grp) {
    let sum = 0, n = 0
    for (let other of Object.keys(held)) {
      if (other === id || econGroup(other) !== grp || !good(other)) continue
      sum += Math.max(-3, Math.min(3, Math.log(Math.max(1, held[other]) / good(other)[1]))); n++
    }
    if (n) raw *= Math.exp(-ECON_SUBSTITUTION * sum / n)
  }
  // production cost: made goods from their inputs' current prices, raw goods from base
  let floor = base * ECON_FLOOR_RAW
  let ins = depth < 3 ? econInputs(id, k => !!good(k)) : null
  if (ins) {
    let cost = 0
    ins.forEach(i => { cost += i[1] * (ECON_LABOUR + econMid(state, regions, good, region, i[0], undefined, depth + 1)) })
    floor = Math.max(floor, cost * ECON_FLOOR_MADE)
  }
  let p = econSoftMax(raw, floor)
  // arbitrage: goods flow from the cheaper market to the dearer one until the gap is only the freight; the flow raises the
  // cheap price and lowers the dear one, so each moves half of the excess
  let adj = p
  for (let r of Object.keys(state.stock)) {
    if (r === region || state.stock[r][id] === undefined) continue
    let other = econRegionRaw(state, good, r, id)
    let freight = ECON_FREIGHT * base * econDist(regions, region, r)
    if (p < other - freight) adj += (other - p - freight) / 2
    else if (p > other + freight) adj -= (p - other - freight) / 2
  }
  return adj
}

// Price in another market without its own arbitrage terms (avoids recursion).
function econRegionRaw(state, good, region, id) {
  let g = good(id), held = state.stock[region] || {}
  let s = Math.max(1, held[id] === undefined ? g[1] : held[id])
  return econSoftMax(g[0] * Math.min(12, Math.max(0.01, Math.pow(g[1] / s, 1 / econElasticity(id)))), g[0] * ECON_FLOOR_RAW)
}

const econBuyPriceOf = mid => Math.ceil(mid * 1.15)
const econSellPriceOf = mid => Math.floor(mid * 0.85)

// Run n units through the market one at a time (each unit moves the price). scale is units of money per tw. Returns the money paid
// or received, in units of the money (each unit rounded the way a shopkeeper rounds it).
function econTrade(state, regions, good, region, id, n, buying, scale) {
  let held = state.stock[region] = state.stock[region] || {}
  if (held[id] === undefined) held[id] = good(id)[1]
  scale = scale || 1
  let total = 0
  for (let i = 0; i < n; i++) {
    let mid = econMid(state, regions, good, region, id) * scale
    total += buying ? econBuyPriceOf(mid) : econSellPriceOf(mid)
    held[id] += buying ? -1 : 1
  }
  return total
}

// What n units would cost or fetch right now, without moving the market.
function econQuote(state, regions, good, region, id, n, buying, scale) {
  let held = state.stock[region] = state.stock[region] || {}
  let before = held[id]
  let total = econTrade(state, regions, good, region, id, n, buying, scale)
  held[id] = before
  if (before === undefined) delete held[id]
  return total
}

// How many of an item a player carries. inventory.count(string) is ambiguous once the Create port is loaded (its ContainerExtension adds count(ItemPredicate)), so the
// slots are walked instead; the stack is matched on its item string, with or without data ("diaithir:x[custom_data={...}]").
// Is this stack the item? By its id when it reads, else by its item string, which KubeJS wraps in quotes ('40x minecraft:gold_nugget') and follows
// with [data] when the stack has any; the id must stand alone in the text, not be part of a longer id.
function econStackIs(st, id) {
  let want = String(id)
  try { if (st.id !== undefined && st.id !== null && typeof st.id !== 'function') return String(st.id) === want } catch (e) { }
  let text = String(st.toItemString ? st.toItemString() : st), at = text.indexOf(want)
  while (at >= 0) {
    let before = at > 0 ? text.charAt(at - 1) : '', after = text.charAt(at + want.length)
    if (!/[a-z0-9_.:\/-]/.test(before) && !/[a-z0-9_.:\/-]/.test(after)) return true
    at = text.indexOf(want, at + 1)
  }
  return false
}

function econCount(player, id) {
  let inv = player.inventory, n = 0
  if (typeof inv.containerSize === 'undefined' || typeof inv.getItem !== 'function') return inv.count(id)
  for (let i = 0; i < inv.containerSize; i++) {
    let st = inv.getItem(i)
    if (!st || (typeof st.isEmpty === 'function' ? st.isEmpty() : st.empty)) continue
    if (econStackIs(st, id)) n += typeof st.getCount === 'function' ? st.getCount() : st.count
  }
  return n
}
