// Cultures, their currencies, exchange rates, territory, taxes and wages. Pure functions over the ECON state (no KubeJS globals),
// so tools/economy/test_economy.js runs them under Node. docs/design/economy.md has the reasoning.
//
// Every real price is in tw, the unit of account: one tw is a quarter hour of unskilled work, and a twist of salt is one tw at par.
//
// A culture has its own market and its own money. The money is a design (one physical item family) that the culture strikes or
// favours (econDesignOf); several cultures may share a design, like a currency union. What backs a design depends on the Age
// (econKindAt):
//   commodity (Ages I-IV)  units of a staple (salt, grain, hides, honey, amber): worth what the staple is worth in the market,
//                          so a glut of it makes the money cheaper and goods dearer;
//   coin (Age V)           a fixed weight of metal: worth its metal times the sound fraction left after debasement;
//   fiat (Age VI on)       backed by the culture's word: worth what the amount in circulation allows.
// A culture's rate also moves with how much of its money is in circulation ((reference / money) ^ 0.4, coin and fiat) and with its
// trade balance (a culture whose goods are bought more than it buys sees its money firm; exp(0.15 * balance), balance in [-1, 1]).
// Money is created by the stores' payouts to sellers, by minting (a treasury short of cash pays its wage bill from the mint:
// debasement for coin, printing for fiat) and by lending (02_bank.js); it is destroyed by buyers' payments, by tax and by repaying loans.
//
// Territory: a culture's prices apply only inside its claim, the discs around its settlements (econClaims, econClaimAt). Outside
// every claim there is no culture and no price: the wilds and travelling traders deal in barter at the free market.

const ECON_WAGE_HOUR = 4 // tw an hour of unskilled work
const ECON_SKILL = { unskilled: 1, trained: 1.6, skilled: 2.5, master: 4 }
const ECON_TAX_BY_AGE = { TELLING: 0.04, I: 0.04, II: 0.06, III: 0.08, IV: 0.1, V: 0.1, VI: 0.15, VII: 0.18, VIII: 0.18, RODOS: 0.18 }
const ECON_CHANGER_FEE = 0.03
const ECON_TB_REF = 3000 // tw of net cross-border trade in a week that moves the balance by 1
const ECON_CLAIM_BASE = 160 // blocks around a settlement; grows with its people and with the realm's tier
const ECON_TIER_CLAIM = { band: 1, town: 1, county: 1.5, kingdom: 2, country: 3 }

const econClamp = (v, lo, hi) => Math.min(hi, Math.max(lo, v))
const econSlug = s => String(s || '').toLowerCase().replace(/[^a-z0-9]+/g, '_').replace(/^_+|_+$/g, '') || 'free'

function econKindAt(age) {
  if (typeof ECON_AGES === 'undefined') return 'coin'
  let i = ECON_AGES.indexOf(age)
  return i >= ECON_AGES.indexOf('VI') ? 'fiat' : i >= ECON_AGES.indexOf('V') ? 'coin' : 'commodity'
}

// ---- Territory --------------------------------------------------------------------------------------------------------------

// sim: the engine's WorldSim as JSON ({settlements, factions}). Returns claims [{x, z, r, culture, realm, town, name}], one per standing
// settlement. Radius: ECON_CLAIM_BASE plus 8 blocks per square root of its people, times the tier of its top realm.
function econClaims(sim) {
  let out = []
  let top = id => { let f = sim.factions[id], n = 0; while (f && f.parent && sim.factions[f.parent] && n++ < 8) f = sim.factions[f.parent]; return f }
  Object.keys(sim.settlements).forEach(sid => {
    let s = sim.settlements[sid]
    if (s.state === 'ruined') return
    let f = sim.factions[s.faction], root = top(s.faction)
    let culture = econSlug(s.culture || (f && f.culture))
    let r = (ECON_CLAIM_BASE + 8 * Math.sqrt(Math.max(1, s.population || 1))) * (ECON_TIER_CLAIM[root ? root.tier : 'town'] || 1)
    out.push({ x: s.x, z: s.z, r: Math.round(r), culture: culture, realm: root ? root.id : s.faction, town: sid, name: s.name })
  })
  return out
}

// The claim covering (x, z), or null in the wilds. Where claims overlap the one the point sits deepest inside (distance over radius) wins.
function econClaimAt(claims, x, z) {
  let best = null, bd = 1
  for (let c of claims) {
    let d = Math.hypot(c.x - x, c.z - z) / c.r
    if (d <= bd) { bd = d; best = c }
  }
  return best
}

// ---- Currencies -------------------------------------------------------------------------------------------------------------

// designs: { id: { name, commodity: {good, perUnit}, coin: {metal, content} } }; the culture picks one by a stable hash of its name
// among those the Age allows (commodity designs before Age V, coin designs from V), unless data names its own in cultures.
// own: a map, or a list of maps (a reform's choice first, then the culture's tradition from culture_money.json); the first entry naming
// a design of the right kind for the Age wins, so a culture's staple money and its coin style do not collide. A commodity money exists
// only once the Age has every good in it (design.age), so an Age I culture whose tradition works copper keeps to what the Age has.
function econDesignOf(designs, culture, kind, own, age) {
  let ok = d => kind === 'commodity' ? d.commodity && econAgeLe(d.age, age) : d.coin
  let maps = Array.isArray(own) ? own : [own]
  for (let m of maps) {
    let d = m && designs[m[culture]]
    if (d && ok(d)) return m[culture]
  }
  let ids = Object.keys(designs).filter(id => ok(designs[id])).sort()
  if (!ids.length) return null
  let h = 0
  for (let i = 0; i < culture.length; i++) h = (h * 31 + culture.charCodeAt(i)) >>> 0
  return ids[h % ids.length]
}

// Is Age `a` no later than Age `age`? (the Telling counts as Age I; no Age given, or no age on the design, means yes)
function econAgeLe(a, age) {
  if (!a || !age || typeof ECON_AGES === 'undefined') return true
  let at = x => Math.max(1, ECON_AGES.indexOf(x))
  return at(a) <= at(age)
}

// A culture's running figures: money in circulation, the supply it was set against, trade balance and flow, and the treasury
// that backs its wage bill. vol is this week's trade in tw (what the merchants' company earns from).
function econCur(state, culture) {
  state.cur = state.cur || {}
  return state.cur[culture] = state.cur[culture] || { money: 0, ref: 0, tb: 0, flow: 0, vol: 0, infl: 0, weak: 0, treasury: 0, wagePool: 0, arrears: 0 }
}
// A design's: its debasement (shared by every culture that strikes it) and the rate fiat started from.
function econDes(state, did) {
  state.des = state.des || {}
  return state.des[did] = state.des[did] || { debase: 0, rate0: 0 }
}

// Value in tw of one unit of a design, before any culture's own factors. mid(id) is the real price in tw of a good.
function econDesignValue(state, design, did, kind, mid) {
  if (kind === 'commodity') return design.commodity.perUnit * mid(design.commodity.good)
  let d = econDes(state, did)
  let coin = design.coin.content * mid(design.coin.metal) * (1 - d.debase)
  if (kind === 'coin') return coin
  if (!d.rate0) d.rate0 = coin
  return d.rate0
}

// Tw per unit of the culture's money. A design that the Age does not support (commodity money, a culture with no design) is 1.
function econRate(state, culture, design, did, kind, mid) {
  let s = econCur(state, culture)
  if (!design) return 1
  let base = econDesignValue(state, design, did, kind, mid)
  // A culture's first coin is pegged to the staple it used as money (s.peg, set when it moves to coin): one coin is worth one unit of
  // it, however much metal is in the coin, until debasement or overprinting breaks the peg (econRedeemable turns false).
  if (kind === 'coin' && s.peg) base = s.peg.perUnit * mid(s.peg.good) * (1 - econDes(state, did).debase)
  if (kind !== 'commodity') base *= econClamp(Math.pow(Math.max(1, s.ref) / Math.max(1, s.money), kind === 'fiat' ? 0.5 : 0.4), 0.4, 2.5)
  return Math.max(1e-6, base * Math.exp(0.15 * econClamp(s.tb, -1, 1)))
}

// Units of `to` for `amount` of `from`, less the changer's fee. rate(culture) gives tw per unit there.
function econConvert(rate, from, to, amount) {
  return from === to ? amount : amount * rate(from) / rate(to) * (1 - ECON_CHANGER_FEE)
}

// Money enters or leaves a culture's circulation. A negative amount burns; circulation never falls below zero.
function econMint(state, culture, amount) {
  let s = econCur(state, culture)
  s.money = Math.max(0, s.money + amount)
  return s.money
}

// A realm that sets its own rate (taxes) keeps it; otherwise the Age's rate times the realm's fiscal stance (econWeek raises it after a
// shortfall and eases it when the treasury is full), between half and double.
function econTaxRate(taxes, culture, age, state) {
  if (taxes && taxes[culture] !== undefined) return taxes[culture]
  let mul = state && state.cur && state.cur[culture] && state.cur[culture].taxMul || 1
  return (ECON_TAX_BY_AGE[age] || 0.1) * mul
}

// Tax on a trade worth `value` units: leaves circulation and lands in the treasury.
function econTax(state, culture, rate, value) {
  let t = value * rate
  econMint(state, culture, -t)
  econCur(state, culture).treasury += t
  return t
}

// A cross-border trade adds to (or takes from) the balance: a visitor buying here adds, a visitor selling here subtracts. tw value.
function econFlow(state, culture, twValue) { econCur(state, culture).flow += twValue }

// Wage in units of the culture's money for hours of work. skill is a key of ECON_SKILL.
function econWage(rate, culture, hours, skill) {
  return hours * ECON_WAGE_HOUR * (ECON_SKILL[skill || 'unskilled'] || 1) / rate(culture)
}

// A job or bounty draws from the culture's wage pool; when the pool is dry the work goes unpaid in money (arrears) and the caller
// offers goods instead. Returns the paid amount (0 when dry).
function econPayWage(state, culture, amount) {
  let s = econCur(state, culture)
  if (s.wagePool < amount) { s.arrears += amount; return 0 }
  s.wagePool -= amount
  econMint(state, culture, amount)
  return amount
}

// Once a week. wageBill is what the culture owes its staff and works, in its money; the treasury pays it and a share of what is
// left goes to the wage pool for jobs. A shortfall is met from the mint, and the money kind decides who pays for it.
function econWeek(state, culture, did, kind, wageBill, rate) {
  let s = econCur(state, culture)
  if (rate) econWatch(s, kind, rate)
  s.tb = econClamp(s.tb * 0.8 + s.flow / ECON_TB_REF, -1, 1)
  s.flow = 0
  let short = Math.max(0, wageBill - s.treasury)
  s.treasury = Math.max(0, s.treasury - wageBill)
  if (short > 0) {
    if (kind === 'commodity') s.arrears += short // salt cannot be printed: the bill goes unpaid
    else {
      let before = Math.max(1, s.money)
      econMint(state, culture, short)
      if (kind === 'coin' && did) {
        let d = econDes(state, did)
        d.debase = Math.min(0.9, 1 - (1 - d.debase) * Math.pow(before / (before + short), 0.5))
      }
    }
  }
  s.taxMul = econClamp((s.taxMul || 1) * (short > 0 ? 1.05 : s.treasury > 4 * wageBill && wageBill > 0 ? 0.97 : 1), 0.5, 2)
  let pool = Math.min(s.treasury * 0.3, wageBill)
  s.treasury -= pool
  s.wagePool += pool
  return s
}

// Watching a commodity money. Its value is the staple's price in the market, and a staple that is spent into the stores piles up
// there (market.js), so a cow farm that pours hides into a hide culture's stores cheapens its money: `infl` is the week's change in
// the price level (1/rate), and `weak` counts weeks below 55% of the rate the money started at. Four weeks of that and the culture
// reforms its money to another staple (s.reform), as real economies moved off a money that had become too easy to make.
function econWatch(s, kind, rate) {
  if (!s.rateRef) s.rateRef = rate
  s.infl = s.prevRate ? s.prevRate / rate - 1 : 0
  s.prevRate = rate
  s.weak = kind === 'commodity' && rate < 0.55 * s.rateRef ? (s.weak || 0) + 1 : 0
  if (s.weak >= 4) s.reform = true
}

// Coins can be turned in for the staple they are pegged to while the peg holds: the culture's supply is not above its start by so much,
// and the coin has lost under a tenth of its silver. Past that the crown suspends redemption (as the gold standard was suspended).
function econRedeemable(state, culture, did) {
  let s = econCur(state, culture)
  if (!s.peg) return false
  return Math.pow(Math.max(1, s.ref) / Math.max(1, s.money), 0.4) * (1 - econDes(state, did).debase) >= 0.9
}
