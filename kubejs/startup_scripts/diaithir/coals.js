// The Age I coals, their ores and the coal-works items. Rebuilt 2026-10-21 from the ids the server's data uses
// (loot_table/blocks and worldgen *_coal_ore, coals_age1.js, the Age I beats); the production copy was never committed.
// Lighting a coal is server_scripts/diaithir/age_1/coals_age1.js; ore drops are the server's loot tables.

const DIAITHIR_COALS = [
  ['gold', 'Gold Coal'], ['white', 'White Coal'], ['black', 'Black Coal'], ['red_brown', 'Red-Brown Coal'],
  ['silver', 'Silver Coal'], ['fire', 'Fire Coal'], ['rainbow', 'Rainbow Coal']
]

const DIAITHIR_COAL_ITEMS = [
  ['coal_dust', 'Coal Dust', 64], ['dubhan_fuel_cell', 'Dubhan Fuel Cell', 16], ['fuel_ledger', 'Fuel Ledger', 1],
  ['culoare_adevarata', 'Culoare Adevarata', 1], ['silence_bell', 'Silence Bell', 1], ['storm_sigil', 'Storm Sigil', 1],
  ['the_chord', 'The Chord', 1]
]

StartupEvents.registry('item', event => {
  DIAITHIR_COALS.forEach(c => {
    event.create(`diaithir:${c[0]}_coal`).displayName(c[1]).texture(`diaithir:item/${c[0]}_coal`)
      .rarity(c[0] === 'rainbow' ? 'epic' : 'uncommon')
  })
  DIAITHIR_COAL_ITEMS.forEach(i => {
    event.create(`diaithir:${i[0]}`).displayName(i[1]).texture(`diaithir:item/${i[0]}`).maxStackSize(i[2])
  })
})

StartupEvents.registry('block', event => {
  DIAITHIR_COALS.forEach(c => {
    event.create(`diaithir:${c[0]}_coal_ore`).displayName(`${c[1]} Ore`).texture(`diaithir:block/${c[0]}_coal_ore`)
      .stoneSoundType().mapColor('stone').hardness(3.0).resistance(3.0).requiresTool(true)
      .tagBlock('minecraft:mineable/pickaxe').tagBoth('c:ores')
  })
})
