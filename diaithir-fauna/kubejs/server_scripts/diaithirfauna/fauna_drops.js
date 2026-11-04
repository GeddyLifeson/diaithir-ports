// Written by diaithir-fauna/tools/integrate_gen.py. Fauna drops joined to cuisine (#167 cuts), spoilage (#163 raw_hide) and clothing (#164 fur).
// The mod itself drops vanilla meat, leather, feather and bone; this swaps the hide by species and adds the cuisine cut of the animal's family.
const FAUNA_PLANS = ["ammonite", "ankylosaur", "archaic_whale", "armadillo", "bat", "beetle", "cephalopod", "ceratopsian", "corvid", "crab", "crocodilian", "dolphin", "dromaeosaur", "early_tetrapod", "eel", "eurypterid", "fish_deep", "fish_flat", "fish_fusiform", "fish_primitive", "frog", "galliform", "giant_arthropod", "giraffid", "hadrosaur", "ichthyosaur", "insect_flying", "insectivore", "jellyfish", "lagomorph", "lizard", "macropod", "marsupial_stout", "monotreme", "mosasaur", "mustelid", "ornithopod_small", "owl", "parrot", "penguin", "pigeon", "pinniped", "placoderm", "plesiosaur", "primate_ape", "primate_monkey", "primate_small", "pterosaur", "raptor", "ratite", "ray", "rodent_large", "rodent_small", "sauropod", "scorpion", "sea_turtle", "seabird", "sloth_tree", "songbird", "starfish", "stegosaur", "synapsid_sail", "syngnathid", "tapir", "terror_bird", "therapsid", "theropod", "trilobite", "uw:arowana", "uw:bear", "uw:big_cat", "uw:boar", "uw:bovid", "uw:camel", "uw:canid", "uw:cervid", "uw:elephantid", "uw:equid", "uw:glyptodont", "uw:ground_sloth", "uw:hippo", "uw:hyena", "uw:large_snake", "uw:manatee", "uw:monitor", "uw:opossum", "uw:rhino", "uw:shark", "uw:spadefish", "uw:tarantula", "uw:tortoise", "viverrid", "wader", "waterfowl"]
let FAUNA_DROPS = {}
ServerEvents.loaded(() => {
  try { FAUNA_DROPS = (JsonIO.read('kubejs/data/diaithirfauna/fauna_drops.json') || { species: {} }).species } catch (e) { console.warn('[fauna] no drop table: ' + e) }
})
const faunaSpeciesOf = e => {
  try { let n = e.nbt; let v = n.getStringOr ? n.getStringOr('Species', '') : n.getString('Species'); return String(v) } catch (x) { return '' }
}
FAUNA_PLANS.forEach(plan => EntityEvents.drops('diaithirfauna:' + plan, event => {
  let d = FAUNA_DROPS[faunaSpeciesOf(event.entity)]
  if (!d) return
  // hide: the mod drops leather for land animals of 20 kg and up
  if (d.hide === 'none') event.drops.removeIf(i => String(i.item.id) === 'minecraft:leather')
  else event.drops.forEach(i => {
    if (String(i.item.id) !== 'minecraft:leather') return
    i.setItem(Item.of(d.hide === 'fur' ? 'minecraft:rabbit_hide' : 'diaithir:raw_hide', d.hn))
  })
  if (d.hide === 'fur' && d.hn > 0 && !event.drops.stream().anyMatch(i => String(i.item.id) === 'minecraft:rabbit_hide')) event.addDrop(Item.of('minecraft:rabbit_hide', d.hn))
  // cuisine cut of the family (global.CUISINE.cuts is [family, main cut id]); skipped when the butchery mod gives carcasses
  if (d.fam && global.CUISINE && !Platform.isLoaded('diaithirbutchery') && Math.random() < d.chance)
    for (let c of global.CUISINE.cuts) if (c[0] === d.fam) { event.addDrop(Item.of(c[1])); break }
}))
