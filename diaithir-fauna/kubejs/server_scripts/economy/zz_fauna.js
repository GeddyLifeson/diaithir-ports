// Written by diaithir-fauna/tools/integrate_gen.py. Runs after market.js (zz): adds the fossil goods and the antiquarian to the economy tables.
// Fossils are Age III goods (Cenozoic digs); the antiquarian takes the cartographer's place in Age III (ANTIQUARIAN_PROFESSION), Age IV's scribe replaces it as before.
const ANTIQUARIAN_PROFESSION = 'cartographer'
;(function () {
  if (typeof ECON_GOODS === 'undefined' || typeof ECON_VENDOR_DATA === 'undefined') return
  let prices = JsonIO.read('kubejs/data/diaithir/prices_fauna.json') || {}
  for (let id in prices) {
    ECON_PRICES[id] = prices[id]
    ECON_GOODS[id] = [prices[id][0], Math.max(8, Math.round(256 / Math.sqrt(prices[id][0]))), 'III']
  }
  let v = JsonIO.read('kubejs/data/diaithir/vendors_fauna.json')
  if (v && v.vendors) v.vendors.forEach(x => { if (!(ECON_VENDOR_DATA.III || []).some(y => y.id === x.id)) (ECON_VENDOR_DATA.III = ECON_VENDOR_DATA.III || []).push(x) })
  if (typeof ECON_PROF_VENDOR !== 'undefined' && ECON_PROF_VENDOR[2]) ECON_PROF_VENDOR[2][ANTIQUARIAN_PROFESSION] = 'antiquarian'
  // per-species worth of a fossil stack, for a market hook: tw for one item, or null
  let values = (JsonIO.read('kubejs/data/diaithirfauna/fossil_values.json') || { species: {} }).species
  global.FAUNA_FOSSIL_TW = stack => {
    try {
      let sp = String(stack.nbt.getCompound('components').getCompound('minecraft:custom_data').getStringOr('Species', ''))
      let r = values[sp]
      return r ? (String(stack.id).endsWith('cleaned_fossil') ? r.cleaned : r.fragment) : null
    } catch (e) { return null }
  }
})()
