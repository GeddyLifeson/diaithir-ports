// Frictions Minecraft removes and real economies never lose: goods rot, rust and get stolen while they wait; coins wear thin in
// the purse; credit costs a fee to arrange. Pure functions over the ECON state.
//
// Spoilage touches only the stock a market holds above its usual level. A market short of a good loses nothing, so a shortage is never
// made worse by it, and a glut of grain or fish rots down toward normal at the rate its kind keeps. That is a loss of goods, not a pull
// of the price toward its base: price still moves only by trades.

const ECON_HOLD_LOSS = 0.003 // weekly: handling, theft, damp, rust; every good
// Weekly loss of the surplus. The rates are the food-spoilage thread's (PR #163, startup_scripts/spoilage/rates.js: global.SPOIL and global.SPOIL_NEVER), read at
// call time when that pack is loaded so the shelf and the pack rot at one rate. The table below is the same list for a pack without it (honey, salt and tanned
// leather keep; raw hide and fresh food go).
const ECON_SPOIL = [
  [/^(honey_bottle|honeycomb|honeycomb_block|leather|salt_.*|sugar|rotten_flesh|spider_eye|rabbit_foot|rabbit_hide)$/, 0],
  [/^(salted_|smoked_|pickled_|dried_(meat|fish|fruit)$)/, 0.02],
  [/^(dried_kelp|hay_block)$/, 0.01],
  [/^(raw_hide)$/, 0.25],
  [/^(cooked_|bread|baked_potato|pumpkin_pie|cake|cookie|mushroom_stew|rabbit_stew|beetroot_soup)/, 0.15],
  [/^(beef|porkchop|mutton|chicken|rabbit|cod|salmon|tropical_fish|pufferfish)$/, 0.2],
  [/^(apple|carrot|potato|beetroot|melon_slice|sweet_berries|glow_berries|kelp|sea_pickle|pumpkin|melon)$/, 0.1],
  [/^(wheat|wheat_seeds|beetroot_seeds|melon_seeds|pumpkin_seeds)$/, 0.03],
  [/^egg$/, 0.06],
  [/^milk_bucket$/, 0.3]
]
const ECON_WEAR = 0.0005 // weekly fraction of a coin's metal rubbed off in circulation

// A drink's shelf life in days (global.SPOIL_DRINKS from the spoilage pack, half the batch gone by then) is a weekly rate of 1 - 0.5^(7 / days); unlisted drinks keep.
const econDrinkRate = days => days > 0 ? 1 - Math.pow(0.5, 7 / days) : 0
// Cold keeps food: the RimWorld curve (nothing spoils at or below 0 C, the full rate from 10 C up). null (no temperature system) is room temperature.
const econWarmth = t => t === null || t === undefined || isNaN(t) ? 1 : t <= 0 ? 0 : t >= 10 ? 1 : t / 10
const econSpoilRate = id => {
  let b = String(id).replace(/^[^:]*:/, '')
  let g = typeof global !== 'undefined' ? global : null
  if (String(id).indexOf('diaithir_drinks:') === 0) return g && g.SPOIL_DRINKS ? econDrinkRate(g.SPOIL_DRINKS[b] || 0) : 0
  if (g && g.SPOIL && g.SPOIL_NEVER) { if (g.SPOIL_NEVER.test(b)) return 0; for (let s of g.SPOIL) if (s[0].test(b)) return s[1]; return 0 }
  for (let s of ECON_SPOIL) if (s[0].test(b)) return s[1]
  return 0
}

// good(id) -> [base, usual]. Loses stock above usual in every region; returns the units lost.
// warmth(region) is the share of the room-temperature rate the region's weather allows (1 when omitted).
function econSpoil(state, good, warmth) {
  let lost = 0
  Object.keys(state.stock).forEach(region => {
    let held = state.stock[region], w = warmth ? warmth(region) : 1
    Object.keys(held).forEach(id => {
      let g = good(id)
      if (!g || held[id] <= g[1]) return
      let loss = (held[id] - g[1]) * Math.min(1, econSpoilRate(id) * w + ECON_HOLD_LOSS)
      held[id] -= loss
      lost += loss
    })
  })
  return lost
}

// Coin wears. The crown recoins when its treasury can pay for the metal lost; until then the coin buys a little less.
// Returns the units paid for recoinage.
function econWear(state, culture, did) {
  let s = econCur(state, culture), d = econDes(state, did)
  let w = (1 - d.debase) * ECON_WEAR
  d.debase += w
  d.worn = (d.worn || 0) + w
  let cost = d.worn * Math.max(1, s.money)
  if (s.treasury < cost) return 0
  s.treasury -= cost
  d.debase = Math.max(0, d.debase - d.worn)
  d.worn = 0
  return cost
}

// Trade over money in circulation, per week: the velocity the quantity theory sets prices by.
const econVelocity = s => s.money > 0 ? s.vol / s.money : 0

// Seasons. Farm goods come in at harvest and are eaten through the year: a market's stock of them follows a cycle around its usual level
// (amplitude 20%, peak at the harvest, a quarter of a year before the new spring), with zero net over a year because each tick adds only
// the change in the cycle since the last tick. Nothing pulls a price back; the stock is what the season brings.
const ECON_SEASONAL = /^(wheat|potato|carrot|beetroot|apple|melon_slice|melon|pumpkin|sweet_berries|glow_berries|sugar_cane|bread|hay_block)$/
const ECON_SEASON_SWING = 0.2
const ECON_HARVEST = 0.75 // year fraction (0 = early spring) of the harvest peak

// phaseOf(region) -> the year fraction 0-1 of the crops' own calendar there, or null. Without a climate it is a 52-week clock.
function econSeason(state, good, week, phaseOf) {
  state.season = state.season || {}
  Object.keys(state.stock).forEach(region => {
    let y = phaseOf ? phaseOf(region) : null
    if (y === null || y === undefined) y = (week / 52) % 1
    let cyc = ECON_SEASON_SWING * Math.cos(2 * Math.PI * (y - ECON_HARVEST))
    let prev = state.season[region]
    state.season[region] = cyc
    if (prev === undefined) return // the first tick only records the phase
    let held = state.stock[region]
    Object.keys(held).forEach(id => {
      let g = good(id)
      if (g && ECON_SEASONAL.test(String(id).replace(/^[^:]*:/, ''))) held[id] = Math.max(1, held[id] + g[1] * (cyc - prev))
    })
  })
}
