// What a bottle is worth. A drink's table price is the fresh bottle at the style's own strength; an oak-aged bottle is worth more, and so is a stronger
// one (re-distilled spirits), scaled by days and style. The bottle carries its data in custom_data: abv:43.2d (real ABV in percent) and aged:6.0d (game days
// in oak). A bottle with none is old stock: the style's own strength, not aged. Style data (drink_value.json, from the drinks catalogue by prices.py):
// abv, aged (improves in oak), peak (days at which it peaks, at most 12), shelf (days it keeps; 0 never spoils).
let ECON_DRINKS = {}
try { ECON_DRINKS = econRead('kubejs/data/diaithir/drink_value.json') || {} } catch (e) { }

const ECON_AGED_PREMIUM = 0.5 // an aged-to-peak bottle is worth a half more

// The factor on the table price for one bottle; 1 for anything that is not a catalogued drink.
function econDrinkFactor(id, abv, aged) {
  let rec = ECON_DRINKS[String(id).replace(/^[^:]*:/, '')]
  if (!rec) return 1
  let strength = abv > 0 && rec.abv > 0 ? Math.pow(abv / rec.abv, 0.5) : 1
  let age = rec.aged && rec.peak > 0 ? 1 + ECON_AGED_PREMIUM * Math.min(1, Math.max(0, aged || 0) / Math.min(12, rec.peak)) : 1
  return strength * age
}

// The bottle data in a stack's item string ("diaithir_drinks:stout[custom_data={abv:5.2d,aged:6.0d}]"); numbers are NaN-safe 0 when absent.
function econBottleData(text) {
  let abv = /abv:([0-9.]+)d/.exec(text), aged = /aged:([0-9.]+)d/.exec(text)
  return { abv: abv ? Number(abv[1]) : 0, aged: aged ? Number(aged[1]) : 0 }
}

// KubeJS 8 exposes some stack members as methods and some as fields (st.count is a function there): call the method, else read the field.
function econStackCall(st, method, field) {
  if (typeof st[method] === 'function') return st[method]()
  let v = st[field]
  return typeof v === 'function' ? v.call(st) : v
}

// The mean factor of the first n bottles of `id` in the player's inventory (the order `clear` takes them in); 1 when the stacks cannot be read.
function econBottleFactor(player, id, n) {
  if (!ECON_DRINKS[String(id).replace(/^[^:]*:/, '')]) return 1
  try {
    let left = n, sum = 0, inv = player.inventory
    for (let i = 0; i < inv.containerSize && left > 0; i++) {
      let st = inv.getItem(i)
      if (!st || econStackCall(st, 'isEmpty', 'empty')) continue
      if (!econStackIs(st, id)) continue
      let text = String(st.toItemString ? st.toItemString() : st)
      let take = Math.min(left, econStackCall(st, 'getCount', 'count')), d = econBottleData(text)
      sum += take * econDrinkFactor(id, d.abv, d.aged)
      left -= take
    }
    return n - left > 0 ? (sum + left) / n : 1
  } catch (e) { return 1 }
}
