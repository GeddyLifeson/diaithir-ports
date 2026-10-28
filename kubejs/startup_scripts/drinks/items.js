// Drinks, brewing intermediates and the six brewing stations. Data comes from data.js (tools/drinks/gen_data.py).
const DRK_RARITY = { I: 'common', II: 'common', III: 'uncommon', IV: 'uncommon', V: 'rare', VI: 'rare', VII: 'epic', VIII: 'epic', RODOS: 'epic' }
const DRK_CAT_WORD = { beer: 'ale', wine: 'wine', fort: 'fortified wine', grain: 'grain drink', fruit: 'ferment', clear: 'clear spirit', brown: 'aged spirit', liq: 'liqueur' }

const DRK_TOOLTIP = {
  hydrometer: 'Right-click a fermenting vat to read how strong the batch is so far.',
  thermometer: 'Right-click a brewing vessel to read its temperature.',
  off_brew: 'A batch left too long. Sour and foul.'
}

function drkRow(r) {
  let o = {}
  DRINK_KEYS.forEach((k, i) => { o[k] = r[i] })
  return o
}

StartupEvents.registry('item', event => {
  DRINK_ROWS.forEach(r => {
    let d = drkRow(r)
    event.create('diaithir_drinks:' + d.id).displayName(d.name).texture('diaithir_drinks:item/' + d.id)
      .maxStackSize(16).rarity(DRK_RARITY[d.age]).useAnimation('drink')
      .food(f => f.nutrition(1 + Math.floor(d.richness / 2)).saturation(0.1 * d.richness).alwaysEdible().eatSeconds(1.6))
      .tooltip(Text.gray(`${DRK_CAT_WORD[d.cat]}. ${d.age === 'RODOS' ? 'Rodos' : d.age === 'VIII' ? 'The Future' : 'Age ' + d.age}.`))
      .tooltip(Text.darkGray(`Of ${d.culture}.`))
  })
  DRINK_EXTRAS.forEach(x => {
    let b = event.create('diaithir_drinks:' + x[0]).displayName(x[1]).texture('diaithir_drinks:item/' + x[0])
      .maxStackSize(x[0] === 'malted_barley' || x[0] === 'barm' ? 64 : 16).tooltip(Text.gray(DRK_TOOLTIP[x[0]] || 'A brewer\'s stock. Age ' + x[2] + '.'))
    if (x[0] === 'off_brew') b.useAnimation('drink').food(f => f.nutrition(1).saturation(0).alwaysEdible().eatSeconds(1.6))
  })
})

StartupEvents.registry('block', event => {
  Object.keys(DRINK_STATIONS).forEach(id => {
    let s = DRINK_STATIONS[id]
    event.create('diaithir_drinks:' + id).displayName(s[1]).hardness(2.5).resistance(6)
      .soundType(s[0] === 'vat' || s[0] === 'cask' ? 'wood' : 'metal')
      .parentModel('minecraft:block/cube_column')
      .textures({ end: 'diaithir_drinks:block/' + id + '_top', side: 'diaithir_drinks:block/' + id + '_side' })
      .requiresTool(false)
  })
})

// Growable herbs no pack mod has: seed block (also the seed item), four stages, harvest item registered from DRINK_EXTRAS
StartupEvents.registry('block', event => {
  DRINK_CROPS.forEach(c => {
    event.create('diaithir_drinks:' + c[0] + '_seeds', 'crop').displayName(c[1]).age(3, b => b.carrot())
      .crop('diaithir_drinks:' + c[2])
  })
})
