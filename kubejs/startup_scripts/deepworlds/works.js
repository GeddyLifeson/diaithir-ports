// The Works: blueprints and the charter stone. Requirements, surveying and holdings live in
// server_scripts/deepworlds/works.js; ids here must match DW_PROJECTS there.

const DW_BLUEPRINTS = [
  ['keep', 'Keep'], ['foundry', 'Foundry'], ['power_plant', 'Power Plant'], ['factory', 'Factory'],
  ['network', 'Storage Network'], ['farmstead', 'Farmstead'], ['apiary', 'Apiary'], ['sanctum', 'Arcane Sanctum'],
  ['coal_works', 'Coal Works'], ['harbour', 'Harbour'], ['waystation', 'Waystation'], ['airfield', 'Airfield'],
  ['launch_site', 'Launch Site'], ['citadel', 'Citadel']
]

StartupEvents.registry('item', event => {
  DW_BLUEPRINTS.forEach(b => {
    event.create(`deepworlds:blueprint_${b[0]}`)
      .displayName(`Blueprint: ${b[1]}`)
      .texture('deepworlds:item/blueprint')
      .maxStackSize(1)
      .rarity('uncommon')
      .tooltip(Text.gray('Use it on a Charter Stone to lay out the site.'))
      .tooltip(Text.darkGray('Right-click in the air to read the plan.'))
  })
})

StartupEvents.registry('block', event => {
  event.create('deepworlds:charter_stone')
    .displayName('Charter Stone')
    .stoneSoundType()
    .mapColor('stone')
    .hardness(3.0)
    .resistance(1200.0)
    .requiresTool(true)
    .tagBlock('minecraft:mineable/pickaxe')
})
