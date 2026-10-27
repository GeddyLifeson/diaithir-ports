// Spoiled leavings and the preserved foods. Names are Diaithiris (display only; ids never change). Preserving recipes are in
// server_scripts/spoilage/recipes.js; the shelf lives are in rates.js.
StartupEvents.registry('item', event => {
  const make = (id, name, tex, tip) => event.create(`diaithir:${id}`).displayName(name).texture(tex).tooltip(Text.gray(tip))
  make('raw_hide', 'Lietur Ùar', 'minecraft:item/leather', 'Green from the beast. Salt and bark make it leather before it turns.')
  make('spoiled_hide', 'Lietur Groud', 'minecraft:item/rotten_flesh', 'Rotted through. Good for nothing but the midden.')
  make('spoiled_produce', 'Mies Groud', 'minecraft:item/rotten_flesh', 'Rotten greens. The compost heap takes it.')
  make('vinegar', 'Fìungeir', 'minecraft:item/potion', 'What wine and ale become when left too long.')
  make('spoiled_grain', 'Grànu Fuoraid', 'minecraft:item/wheat_seeds', 'Musty and black. Only the compost wants it.')
  ;[['salted_meat', 'Feòili Shaildti', 'minecraft:item/cooked_porkchop', 6, 0.8, 'Cured in salt. Keeps through the winter.'],
    ['salted_fish', 'Iaskach Shaildti', 'minecraft:item/cooked_cod', 5, 0.6, 'Cured in salt. Smoke it and it keeps longer.'],
    ['smoked_meat', 'Feòili Dietach', 'minecraft:item/cooked_beef', 8, 0.9, 'Salted, then hung in the smoke.'],
    ['dried_meat', 'Feòili Thiurmaichti', 'minecraft:item/cooked_mutton', 6, 0.8, 'Dried in the smoke. Travels well.'],
    ['dried_fish', 'Iaskach Thiurmaichti', 'minecraft:item/cooked_salmon', 5, 0.7, 'Wind and smoke dried.'],
    ['dried_fruit', 'Mies Thiurmaichti', 'minecraft:item/sweet_berries', 3, 0.4, 'Dried over the embers.'],
    ['pickled_roots', 'Freimun Saildti', 'minecraft:item/beetroot', 4, 0.5, 'Roots kept in brine.']
  ].forEach(f => {
    event.create(`diaithir:${f[0]}`).displayName(f[1]).texture(f[2]).tooltip(Text.gray(f[5]))
      .food(food => food.nutrition(f[3]).saturation(f[4]))
  })
})

StartupEvents.registry('block', event => {
  // Cooling coil: Age V mechanical refrigeration. Set it beside any chest or barrel.
  event.create('diaithir:cooling_coil').displayName('Cuirled Fuor').texture('minecraft:block/iron_block')
    .soundType('metal').hardness(3).tagBlock('minecraft:mineable/pickaxe')
})
