// Companies: a ledger, lines between the towns of one culture, and cargo demand (the Transport Fever / Railway Empire loop).
// A town whose store holds less than its usual stock of a good has a DEMAND for the difference; one that holds more has a SUPPLY. A line hauls the good with the
// widest gap in price from supply to demand: the company buys it at the origin (origin store, price moves), delivers it, and the destination's treasury pays the
// delivery price, which is the destination's sale price plus a freight bonus for distance and speed. The treasury pays what it has, so nothing is minted.
// Money is in the units of the company's home culture. Vehicles, their Age, tech, calendar year, capacity (units a trip), speed (blocks a day), price (tw) and
// weekly upkeep (tw):
const COMPANY_VEHICLES = [
  { id: 'pack_train', name: 'Pack-animal train', age: 'I', tech: 'herding', year: -9000, cap: 16, speed: 700, cost: 120, upkeep: 4 },
  { id: 'ox_cart', name: 'Ox cart', age: 'II', tech: 'wheel', year: -3500, cap: 40, speed: 1100, cost: 260, upkeep: 8 },
  { id: 'sailing_ship', name: 'Sailing ship', age: 'III', tech: 'sail', year: -3200, cap: 160, speed: 2600, cost: 1100, upkeep: 30 },
  { id: 'steam_train', name: 'Steam train', age: 'V', tech: 'railway', year: 1825, cap: 600, speed: 9000, cost: 6000, upkeep: 160 },
  { id: 'motor_truck', name: 'Motor truck', age: 'V', tech: 'factory', year: 1900, cap: 120, speed: 12000, cost: 3000, upkeep: 90 },
  { id: 'cargo_aircraft', name: 'Cargo aircraft', age: 'VI', tech: 'aviation', year: 1910, cap: 250, speed: 60000, cost: 30000, upkeep: 700 }
]
const COMPANY_BANKRUPT_WEEKS = 4 // weeks in debt with nothing to sell before the lines close
const COMPANY_FOUND_FEE = 0 // tw; none yet

// The calendar year from the tech/calendar thread (global.CALENDAR_YEAR, negative before the common era); null when it does not publish one.
function companyYear() {
  try { if (typeof global !== 'undefined' && typeof global.CALENDAR_YEAR === 'function') { let y = Number(global.CALENDAR_YEAR()); if (!isNaN(y)) return y } } catch (e) { }
  return null
}

// Vehicles a culture may buy: its Age is reached, its tech known, and the calendar has come to the vehicle's first year.
function companyVehicles(age, known, year) {
  return COMPANY_VEHICLES.filter(v => econAgeLe(v.age, age) && (!v.tech || known(v.tech)) && (year === null || year >= v.year))
}

function companyOf(state, key) {
  state.company = state.company || {}
  return state.company[key] || null
}


// Founding needs a real office, RimWorld style: an enclosed room with a door and a charter desk (lectern), a strongbox and
// ledger shelves from Age III, and a depot yard of barrels or hay bales close by. Starting capital is paid in as the company's money.
// at(x,y,z) gives {passable, id}. Pure, so the node test can build rooms out of a map.
const COMPANY_OFFICE = {
  area: [6, 10, 16],       // floor cells at the player's level
  strongbox: [0, 1, 1],
  shelves: [0, 1, 2],
  depot: [1, 2, 4],        // barrels or hay bales within 24 blocks, outside the office
  fee: [30, 120, 400],     // starting capital, in the home culture's units
  tech: [null, 'writing', 'railway'],
  volume: 400
}
const companyTier = age => ['I', 'II', 'III', 'IV'].indexOf(String(age)) < 0 ? 2 : (['I', 'II'].indexOf(String(age)) >= 0 ? 0 : 1)
const companyIs = (id, re) => re.test(String(id))
function companyOfficeCheck(at, start, age, known) {
  let t = companyTier(age), R = COMPANY_OFFICE, miss = []
  if (R.tech[t] && known && !known(R.tech[t])) miss.push('the ' + R.tech[t] + ' tech')
  let key = (x, y, z) => x + ',' + y + ',' + z
  let seen = {}, q = [[start[0], start[1], start[2]]], cells = [], open = false
  seen[key(q[0][0], q[0][1], q[0][2])] = 1
  while (q.length && !open) {
    let c = q.pop(); cells.push(c)
    if (cells.length > R.volume) { open = true; break }
    ;[[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]].forEach(d => {
      let n = [c[0] + d[0], c[1] + d[1], c[2] + d[2]], k = key(n[0], n[1], n[2])
      if (seen[k]) return
      seen[k] = 1
      if (at(n[0], n[1], n[2]).passable) q.push(n)
    })
  }
  if (open) { miss.push('walls and a roof (the room is open)'); return { ok: false, missing: miss } }
  let floor = cells.filter(c => c[1] === start[1]).length
  if (floor < R.area[t]) miss.push(`a floor of ${R.area[t]} blocks (it has ${floor})`)
  let walls = {}, have = { door: 0, desk: 0, box: 0, shelf: 0 }
  cells.forEach(c => [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]].forEach(d => {
    let k = key(c[0] + d[0], c[1] + d[1], c[2] + d[2])
    if (seen[k] && !walls[k]) { let b = at(c[0] + d[0], c[1] + d[1], c[2] + d[2]); if (!b.passable) walls[k] = b.id }
  }))
  Object.keys(walls).forEach(k => {
    let id = walls[k]
    if (companyIs(id, /_door$/)) have.door++
    else if (companyIs(id, /:lectern$/)) have.desk++
    else if (companyIs(id, /:(chest|barrel|trapped_chest)$/)) have.box++
    else if (companyIs(id, /bookshelf$/)) have.shelf++
  })
  if (!have.door) miss.push('a door')
  if (!have.desk) miss.push('a lectern as the charter desk')
  if (have.box < R.strongbox[t]) miss.push('a chest or barrel as the strongbox')
  if (have.shelf < R.shelves[t]) miss.push(`${R.shelves[t]} bookshelf${R.shelves[t] > 1 ? 's' : ''} for the ledgers`)
  let inRoom = seen, yard = 0, r = 24
  for (let x = start[0] - r; x <= start[0] + r; x++) for (let z = start[2] - r; z <= start[2] + r; z++) for (let y = start[1] - 6; y <= start[1] + 6; y++) {
    if (inRoom[key(x, y, z)] && !walls[key(x, y, z)]) continue
    if (companyIs(at(x, y, z).id, /:(barrel|hay_block)$/) && !walls[key(x, y, z)]) yard++
  }
  if (yard < R.depot[t]) miss.push(`a depot yard of ${R.depot[t]} barrels or hay bales outside, within ${r} blocks (${yard} found)`)
  return { ok: miss.length === 0, missing: miss, fee: R.fee[t] }
}

// The caravan job: any working person can ask for safe transport. The profession picks the goods and the complaint.
// [goods before Age III, goods from Age III, what they say]. Anyone not listed gets the default line.
const CARAVAN_JOBS = {
  toolsmith: ['minecraft:flint', 'minecraft:iron_ingot', 'My ore and billets pile up in the yard. Nobody I trust will carry them to the next town.'],
  armorer: ['minecraft:leather', 'minecraft:iron_ingot', 'I can make a hundred plates a season and I cannot get the metal here or the plates out.'],
  weaponsmith: ['minecraft:flint', 'minecraft:iron_ingot', 'Every blade I sell is a blade that crossed a road, and every road eats a third of the load.'],
  mason: ['minecraft:cobblestone', 'minecraft:stone_bricks', 'Dressed stone is worth three times more one town over, if it arrives whole.'],
  farmer: ['minecraft:wheat', 'minecraft:wheat', 'The harvest rots in my barn while the next town goes short. I need it carried and I need it safe.'],
  shepherd: ['minecraft:white_wool', 'minecraft:white_wool', 'Wool keeps no better than I do. It needs to be at the weavers, not in my shed.'],
  butcher: ['minecraft:beef', 'minecraft:beef', 'Meat will not wait for a slow cart. I need it hauled quickly and not robbed on the way.'],
  fisherman: ['minecraft:cod', 'minecraft:cod', 'The catch is best on the day it is landed. Getting it inland is the whole trade.'],
  leatherworker: ['minecraft:leather', 'minecraft:leather', 'Hides pile up at my door and the tanner you need is a road away.'],
  fletcher: ['minecraft:feather', 'minecraft:arrow', 'Shafts and feathers come from three places and none of them is where I am.'],
  librarian: ['minecraft:paper', 'minecraft:book', 'Books are worth little here and a great deal elsewhere, if someone honest will carry them.'],
  cartographer: ['minecraft:paper', 'minecraft:map', 'My charts only pay when they reach the people who sail by them.'],
  cleric: ['minecraft:glass_bottle', 'minecraft:glass_bottle', 'The temple has stores for the other shrine, and nobody to take them safely.'],
  priest: ['minecraft:candle', 'minecraft:candle', 'The rites need offerings from the next town, and the road is not what it was.'],
  nitwit: ['minecraft:bread', 'minecraft:bread', 'I was told to move this somewhere. I do not remember where. Can you?'],
  none: ['minecraft:bread', 'minecraft:bread', 'I have goods and no way to get them across the country in one piece.'],
  _default: ['minecraft:bread', 'minecraft:bread', 'Moving things around is the hard part of every trade I know. I wish someone offered safe transport.']
}
function caravanJob(profession, age) {
  let j = CARAVAN_JOBS[profession] || CARAVAN_JOBS._default
  return { id: companyTier(age) === 0 ? j[0] : j[1], line: j[2] }
}
// Nearest other town; the same culture's, if there is one close enough to matter.
function caravanDest(claims, from) {
  let me = claims.filter(c => c.town === from)[0]
  if (!me) return null
  let d = c => Math.hypot(c.x - me.x, c.z - me.z)
  let others = claims.filter(c => c.town !== from && d(c) >= 100).sort((a, b) => d(a) - d(b))
  let same = others.filter(c => c.culture === me.culture)
  return same[0] || others[0] || null
}
// The carrying fee: more than the goods are worth where they are made, less than the gap they close. Late loads earn half.
function caravanReward(priceTw, n, late) { return Math.max(6, Math.round(priceTw * n * 0.6 * (late ? 0.5 : 1))) }

function companyFound(state, key, name, culture) {
  state.company = state.company || {}
  if (state.company[key]) return null
  return state.company[key] = { name: name, home: culture, money: 0, loan: 0, lines: [], broke: 0, founded: state.week || 0 }
}

// What a town wants and has, in units. usual is the good's usual stock; held is what the store holds now.
function companyGap(state, good, town, id) {
  let usual = good(id)[1], held = (state.stock[town] || {})[id]
  held = held === undefined ? usual : held
  return held - usual // negative: demand; positive: supply
}

// The best good for a line this week: the widest price gap from supply to demand times the units that fit. Returns { id, units } or null.
function companyPlan(state, regions, good, from, to, capacity, ids) {
  let best = null
  ids.forEach(id => {
    let units = Math.min(capacity, Math.floor(companyGap(state, good, from, id)), Math.floor(-companyGap(state, good, to, id)))
    if (units < 1) return
    let margin = econMid(state, regions, good, to, id) - econMid(state, regions, good, from, id)
    if (margin <= 0) return
    if (!best || margin * units > best.score) best = { id: id, units: units, score: margin * units }
  })
  return best
}

// Freight bonus on the destination's price: grows with distance (to 6000 blocks) and speed (to twice 2000 blocks a day).
const companyBonus = (dist, speed) => 0.5 * Math.min(1, dist / 6000) * Math.min(2, speed / 2000)

// One week of one line. env: { state, regions, good, ids (goods open to the culture), scale (units per tw), cur(culture) -> the culture's currency state }.
// Returns the line's notes. The company's money, the line's books and both treasuries move.
function companyLineWeek(env, co, line) {
  let v = COMPANY_VEHICLES.filter(x => x.id === line.vehicle)[0]
  if (!v) return ['unknown vehicle ' + line.vehicle]
  let dist = Math.max(100, econDist(env.regions, line.from, line.to))
  let trips = Math.max(1, Math.min(14, Math.floor(7 * v.speed / (2 * dist))))
  let upkeep = Math.ceil(v.upkeep * line.n * env.scale)
  let notes = []
  co.money -= upkeep
  env.cur(co.home).treasury += upkeep // wages and fodder, spent in the home culture
  line.upkeep = (line.upkeep || 0) + upkeep
  line.weeks = (line.weeks || 0) + 1
  let plan = companyPlan(env.state, env.regions, env.good, line.from, line.to, trips * v.cap * line.n, env.ids)
  if (!plan) return ['nothing to haul']
  let units = plan.units
  while (units > 0 && econQuote(env.state, env.regions, env.good, line.from, plan.id, units, true, env.scale) > co.money) units = Math.floor(units / 2)
  if (units < 1) return ['not enough money to buy cargo']
  let cost = econTrade(env.state, env.regions, env.good, line.from, plan.id, units, true, env.scale)
  let sale = econTrade(env.state, env.regions, env.good, line.to, plan.id, units, false, env.scale)
  let dest = env.cur(co.home) // lines stay inside one culture, so one treasury
  let pay = Math.min(Math.floor(sale * (1 + companyBonus(dist, v.speed))), Math.max(0, Math.floor(dest.treasury)))
  if (pay < Math.floor(sale * (1 + companyBonus(dist, v.speed)))) notes.push('the treasury could not pay in full')
  co.money += pay - cost
  dest.treasury += cost - pay
  line.rev = (line.rev || 0) + pay
  line.cost = (line.cost || 0) + cost
  line.units = (line.units || 0) + units
  line.cargo = plan.id
  return notes
}

// Once a week for every company. env as for companyLineWeek, but with scaleOf(culture) and idsOf(culture) in place of scale and ids.
function companyWeek(env, state) {
  let notes = []
  Object.keys(state.company || {}).forEach(key => {
    let co = state.company[key]
    if (co.closed) return
    let before = co.money
    co.lines.forEach(line => { companyLineWeek(Object.assign({}, env, { scale: env.scaleOf(co.home), ids: env.idsOf(co.home) }), co, line).forEach(n => notes.push(co.name + ': ' + n)) })
    if (co.money < 0 && before <= 0) co.broke++
    else if (co.money >= 0) co.broke = 0
    if (co.broke >= COMPANY_BANKRUPT_WEEKS) { co.closed = true; co.lines = []; notes.push(co.name + ' is bankrupt: its lines are closed') }
  })
  return notes
}

// The profit of a line so far (revenue less cargo and upkeep), in the company's units.
// What a company's vehicles are worth as a pledge: 70% of cost, as used carts.
function companyAssets(co) {
  return co.lines.reduce((t, l) => { let v = COMPANY_VEHICLES.filter(x => x.id === l.vehicle)[0]; return t + (v ? v.cost * l.n * 0.7 : 0) }, 0)
}
const companyProfit = line => (line.rev || 0) - (line.cost || 0) - (line.upkeep || 0)
