// Market glue: market.js sells by item id, so fossil stacks of different species would all fetch one price.
// Wraps econSell so a fossil fragment / cleaned fossil is paid per stack from global.FAUNA_FOSSIL_TW (zz_fauna.js), at the market's usual 85% sell rate.
// Anything else, and stacks with no species, go through the original code.
;(function () {
  if (typeof econSell === 'undefined' || typeof ECON_GOODS === 'undefined') return
  const base = econSell
  const fossil = id => /^diaithirfauna:(fossil_fragment|cleaned_fossil)$/.test(String(id))
  econSell = function (player, id, n) {
    if (!fossil(id) || !global.FAUNA_FOSSIL_TW || !econOpen(id)) return base(player, id, n)
    let inv = player.inventory, left = n, paid = 0, sold = 0
    for (let i = 0; i < inv.containerSize && left > 0; i++) {
      let st = inv.getItem(i)
      if (st.empty || String(st.id) !== String(id)) continue
      let tw = global.FAUNA_FOSSIL_TW(st)
      if (tw === null || tw === undefined) tw = ECON_GOODS[id][0]
      let k = Math.min(left, st.count)
      paid += k * Math.floor(tw * 0.85)
      st.shrink(k); left -= k; sold += k
    }
    if (sold < 1) return player.tell(Text.red('You have none to sell.'))
    econGive(player, paid)
    ECON.stock[id] = econStock(id) + sold
    econSave()
    player.tell(Text.green(`Sold ${sold} ${Item.of(id).hoverName.string} for ${paid} ${econUnit()}, each by its species.`))
  }
})()
