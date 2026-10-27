// Shelf lives for the Spoiled mod, then preserving. Salt (a Twist of Salt) cures, smoke and the drying rack keep, brine pickles, bark and salt tan.
// Ages follow the ingredients: salt and smoking are Age I, the cooling coil is Age V (tech_ages.json).
ServerEvents.recipes(event => {
  // One Spoiled recipe per item the rate table names. spoilTime is in Spoiled updates: the days half of it takes to go bad.
  Item.getTypeList().forEach(full => {
    full = String(full)
    let ns = full.split(':')[0]
    if (ns !== 'minecraft' && ns !== 'diaithir' && ns !== 'diaithirbutchery') return
    let b = full.slice(ns.length + 1)
    if (global.SPOIL_NEVER.test(b)) return
    for (let s of global.SPOIL) if (s[0].test(b)) {
      let ticks = 7 * Math.log(0.5) / Math.log(1 - s[1]) * 24000
      event.custom({ type: 'spoiled:spoil_recipe', ingredient: full, result: { id: global.SPOIL_ROT[s[2]] },
        spoiltime: Math.round(ticks / global.SPOIL_UPDATE), priority: 5 })
      return
    }
  })
  const meats = ['minecraft:beef', 'minecraft:porkchop', 'minecraft:mutton', 'minecraft:chicken', 'minecraft:rabbit']
  const fish = ['minecraft:cod', 'minecraft:salmon', 'minecraft:tropical_fish']
  meats.forEach(m => event.shapeless('2x diaithir:salted_meat', [m, m, 'diaithir:salt_twist']))
  fish.forEach(f => event.shapeless('2x diaithir:salted_fish', [f, f, 'diaithir:salt_twist']))
  // Drinks: shelf_days from content/drinks (0 never spoils); they turn to vinegar.
  for (let id in global.SPOIL_DRINKS) if (Item.exists(`diaithir_drinks:${id}`)) {
    event.custom({ type: 'spoiled:spoil_recipe', ingredient: `diaithir_drinks:${id}`, result: { id: 'diaithir:vinegar' },
      spoiltime: Math.round(global.SPOIL_DRINKS[id] * 24000 / global.SPOIL_UPDATE), priority: 5 })
  }
  event.smoking('diaithir:smoked_meat', 'diaithir:salted_meat')
  event.campfireCooking('diaithir:dried_meat', 'diaithir:salted_meat')
  event.smoking('diaithir:dried_fish', 'diaithir:salted_fish')
  event.campfireCooking('diaithir:dried_fish', 'diaithir:salted_fish')
  ;['minecraft:sweet_berries', 'minecraft:apple', 'minecraft:melon_slice'].forEach(f => {
    event.campfireCooking('diaithir:dried_fruit', f)
    event.smoking('diaithir:dried_fruit', f)
  })
  // Salt from the sea (Pantry's idea): boil a bucket of salt water from Strainers over a fire.
  if (Item.exists('strainers:salty_water_bucket')) {
    event.campfireCooking('2x diaithir:salt_twist', 'strainers:salty_water_bucket').cookingTime(600)
    event.smelting('2x diaithir:salt_twist', 'strainers:salty_water_bucket')
  }
  event.shapeless('3x diaithir:pickled_roots', ['minecraft:carrot', 'minecraft:beetroot', 'minecraft:potato', 'diaithir:salt_twist', 'minecraft:water_bucket'])
    .keepIngredient('minecraft:water_bucket')
  event.shapeless('minecraft:leather', ['diaithir:raw_hide', 'diaithir:salt_twist', '#minecraft:oak_logs'])
  event.shaped('diaithir:cooling_coil', ['CRC', 'IBI', 'CRC'], { C: 'minecraft:copper_ingot', R: 'minecraft:redstone', I: 'minecraft:iron_ingot', B: 'minecraft:bucket' })
})
