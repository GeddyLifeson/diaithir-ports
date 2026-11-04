// Written by diaithir-fauna/tools/crossmod_row8.py. Adds a standard drop only where the mob's own loot table has none (see NOTES.md).
// Bones (c:bones) for bony Ice and Fire / Vampirism mobs, and eggs (c:eggs) for ground birds of diaithir-fauna. UNTESTED in game.
const CM_BONES = ['iceandfire:hydra', 'iceandfire:sea_serpent', 'iceandfire:gorgon', 'iceandfire:siren', 'iceandfire:troll',
  'vampirism:vampire', 'vampirism:advanced_vampire', 'vampirism:vampire_baron', 'vampirism:hunter', 'vampirism:advanced_hunter']
const CM_EGG_PLANS = ['galliform', 'waterfowl', 'ratite', 'pigeon', 'seabird']
const cmHas = (event, id) => event.drops.stream().anyMatch(i => String(i.item.id) === id)
CM_BONES.forEach(t => EntityEvents.drops(t, event => {
  if (!cmHas(event, 'minecraft:bone') && Math.random() < 0.6) event.addDrop(Item.of('minecraft:bone', 1 + Math.floor(Math.random() * 2)))
}))
CM_EGG_PLANS.forEach(p => EntityEvents.drops('diaithirfauna:' + p, event => {
  if (!cmHas(event, 'minecraft:egg') && Math.random() < 0.25) event.addDrop(Item.of('minecraft:egg'))
}))
