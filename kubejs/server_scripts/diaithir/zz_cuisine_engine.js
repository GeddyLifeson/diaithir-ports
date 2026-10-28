// Cooking engine: every cookware block holds items and, once its contents match a dish and a heat or cold source is under it,
// turns them into the dish. Recipes and sources come from global.CUISINE (tools/cuisine/build.py).
// Loads after tech_ages_data.js so its Age map can join TECH_AGES.
if (typeof TECH_AGES != 'undefined') {
  if (TECH_AGES.order.indexOf('FUTURE') < 0) TECH_AGES.order.splice(TECH_AGES.order.indexOf('RODOS'), 0, 'FUTURE') // the Age after VII
  Object.assign(TECH_AGES.items, global.CUISINE.ages)
}

const CuisineIndex = {}
const CuisineProgress = {}
const CuisineSelfHeat = { smokehouse: 1, kitchen_range: 3, convection_oven: 3, induction_hob: 3 }
const CuisineNeed = { none: 0, cold: 0, low: 1, med: 2, high: 3 }

function cuisineBuildIndex() {
  const C = global.CUISINE
  for (let k in CuisineIndex) delete CuisineIndex[k]
  for (let r of C.recipes) {
    let key = r.s + '|' + Object.keys(r.i).sort().join(',')
    if (!CuisineIndex[key]) CuisineIndex[key] = []
    CuisineIndex[key].push(r)
  }
  console.info('[cuisine] ' + C.recipes.length + ' recipes, ' + Object.keys(CuisineIndex).length + ' keys')
}
ServerEvents.loaded(() => cuisineBuildIndex())

// 0 = no heat; 1 low, 2 medium, 3 high. The block under the vessel heats it; a flame beside it gives a little.
function cuisineHeat(block) {
  const H = global.CUISINE.heat
  let below = block.down
  let t = H[below.id] || 0
  if (t && below.properties && below.properties.lit === 'false') t = 0
  let near = 0
  for (let side of [block.north, block.south, block.east, block.west]) if (H[side.id] && !(side.properties && side.properties.lit === 'false')) near = 1
  let id = String(block.id).substring(12)
  if (CuisineSelfHeat[id] && block.level.getBestNeighborSignal(block.pos) > 0) t = Math.max(t, CuisineSelfHeat[id])
  return Math.max(t, near)
}

function cuisineCold(block) {
  return global.CUISINE.cold.indexOf(String(block.down.id)) >= 0
}

function cuisineTick(event) {
  let block = event.block
  let id = String(block.id)
  let st = id.substring(12)
  let inv = block.inventory
  let have = {}, any = false
  const alias = global.CUISINE.alias
  for (let i = 0; i < inv.slots; i++) {
    let s = inv.getStackInSlot(i)
    if (s.empty) continue
    any = true
    have[s.id] = (have[s.id] || 0) + s.getCount()
    for (let a of (alias[String(s.id)] || [])) have[a] = (have[a] || 0) + s.getCount() // a butchered cut stands in for ours
  }
  let pk = block.dimension + ':' + block.pos.asLong()
  if (!any) { delete CuisineProgress[pk]; return }
  let list = CuisineIndex[st + '|' + Object.keys(have).sort().join(',')]
  let rec = null
  if (list) for (let r of list) {
    let ok = true
    for (let k in r.i) if (have[k] < r.i[k]) ok = false
    if (ok && !(typeof techLocked == 'function' && techLocked(r.o))) { rec = r; break }
  }
  if (!rec) { delete CuisineProgress[pk]; return }
  let def = global.CUISINE.stations.find(s => s.id == st)
  let need = CuisineNeed[def.heat] || 0
  let rate = 1
  if (def.heat == 'cold') rate = (def.kind == 'cold' || cuisineCold(block)) ? 1 : 0
  else if (need > 0) rate = Math.min(2, cuisineHeat(block) / need)
  if (rate <= 0) return
  let prog = (CuisineProgress[pk] || 0) + rate
  if (prog < rec.t) { CuisineProgress[pk] = prog; return }
  delete CuisineProgress[pk]
  for (let k in rec.i) {
    let left = rec.i[k]
    for (let i = 0; i < inv.slots && left > 0; i++) {
      let s = inv.getStackInSlot(i)
      if (s.id != k && (alias[String(s.id)] || []).indexOf(k) < 0) continue
      let take = Math.min(left, s.getCount())
      left -= take
      if (take > 0 && String(k).endsWith('_bucket')) block.popItem(Item.of('minecraft:bucket', take)) // a milk bucket gives its bucket back
      inv.setStackInSlot(i, take >= s.getCount() ? Item.empty : Item.of(String(s.id), s.getCount() - take))
    }
  }
  let out = Item.of(rec.o, rec.n)
  for (let i = 0; i < inv.slots; i++) {
    if (inv.getStackInSlot(i).empty) { inv.setStackInSlot(i, out); out = null; break }
  }
  if (out) block.popItem(out)
}
for (let s of global.CUISINE.stations) BlockEvents.blockEntityTick('diaithir:st_' + s.id, cuisineTick)

// Seeds from wild grass, by the climate of the biome.
const CuisineClimateTags = { tropical: ['minecraft:is_jungle', 'c:is_jungle'], arid: ['minecraft:is_savanna', 'minecraft:is_badlands', 'c:is_desert'], cold: ['minecraft:is_taiga', 'c:is_snowy', 'c:is_cold'] }

function cuisineClimate(level, pos) {
  let biome = level.getBiome(pos)
  for (let c in CuisineClimateTags) for (let t of CuisineClimateTags[c]) {
    try { if (biome.is(t)) return c } catch (e) { /* tag not present */ }
  }
  return 'temperate'
}

for (let grass of ['minecraft:short_grass', 'minecraft:tall_grass', 'minecraft:fern']) {
  BlockEvents.broken(grass, event => {
    if (Math.random() > 0.05) return
    let climate = cuisineClimate(event.level, event.block.pos)
    let pool = global.CUISINE.crops.filter(c => c[2] == climate && !(typeof techLocked == 'function' && techLocked(c[4])))
    if (pool.length) event.block.popItem(Item.of(pool[Math.floor(Math.random() * pool.length)][4]))
  })
}

// Cuts of meat from the animals that carry them.
const CuisineCutEntities = { beef: ['minecraft:cow', 'minecraft:mooshroom'], pork: ['minecraft:pig'], lamb: ['minecraft:sheep'], goat: ['minecraft:goat'],
  chicken: ['minecraft:chicken'], duck: ['minecraft:chicken'], goose: ['minecraft:chicken'], turkey: ['minecraft:chicken'], quail: ['minecraft:chicken'],
  pigeon: ['minecraft:chicken'], rabbit: ['minecraft:rabbit'], game: ['minecraft:horse', 'minecraft:donkey'], exotic: ['minecraft:camel', 'minecraft:llama'] }
let cuisineByEntity = {}
ServerEvents.loaded(() => {
  if (Platform.isLoaded('diaithirbutchery')) return // its carcasses give the cuts
  for (let [fam, id] of global.CUISINE.cuts) for (let e of (CuisineCutEntities[fam] || [])) {
    if (!cuisineByEntity[e]) cuisineByEntity[e] = []
    cuisineByEntity[e].push(id)
  }
})
for (let e of ['cow', 'mooshroom', 'pig', 'sheep', 'goat', 'chicken', 'rabbit', 'horse', 'donkey', 'camel', 'llama']) {
  EntityEvents.drops('minecraft:' + e, event => {
    if (Platform.isLoaded('diaithirbutchery')) return
    for (let id of (cuisineByEntity['minecraft:' + e] || [])) if (Math.random() < 0.3) event.addDrop(Item.of(id))
  })
}

// Eating bonus: every cuisine food, crop and mapped modded food grants its data-driven effects (global.CUISINE.bonus).
// Spoiled turns an overdue stack into a rot leaving, which has no entry here, so spoiled food gives no bonus.
ItemEvents.foodEaten(event => {
  let b = global.CUISINE.bonus[String(event.item.id)]
  if (!b) return
  for (let [eff, ticks, amp] of b) event.player.potionEffects.add('minecraft:' + eff, ticks, amp)
})

// Shelf lives for the Spoiled mod (content/spoilage): one spoil recipe per cuisine food, rotting into the spoilage module's leavings.
// Rot leavings carry no eating bonus, so spoiled food gives none.
ServerEvents.recipes(event => {
  if (!Platform.isLoaded('spoiled')) return
  const rot = global.SPOIL_ROT || { meat: 'minecraft:rotten_flesh', produce: 'minecraft:rotten_flesh', grain: 'minecraft:rotten_flesh' }
  const per = global.SPOIL_UPDATE || 600
  for (let id in global.CUISINE.shelf) {
    let [days, kind] = global.CUISINE.shelf[id]
    event.custom({ type: 'spoiled:spoil_recipe', ingredient: id, result: { id: rot[kind] || 'minecraft:rotten_flesh' }, spoiltime: Math.round(days * 24000 / per), priority: 10 })
  }
})
