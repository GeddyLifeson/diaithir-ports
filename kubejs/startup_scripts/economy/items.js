// Economy items. Age I has no coin (canon: the first coin is the AE 17 mark), so trade there is reckoned in salt,
// carried inland by the sea-salt traders. Lightman's Currency reads the two salt items as its "salt" chain.
// Crates are opened in server_scripts/economy/crates.js. IDs are stable across Ages; only names may change.

StartupEvents.registry('item', event => {
  event.create('diaithir:salt_twist').displayName('Twist of Salt').texture('minecraft:item/sugar')
    .tooltip(Text.gray('Sea-salt in a twist of hide. The traders reckon in it.'))
  event.create('diaithir:salt_cake').displayName('Salt Cake').texture('minecraft:item/clay_ball')
    .tooltip(Text.gray('Twelve twists pressed and dried.'))

  ;[['common', "Peddler's Pack", 'common', 'Open it (use).'],
    ['rare', 'Bound Pack', 'uncommon', 'Bound with a seal. A Seal Knife opens it.'],
    ['storied', 'Storied Kist', 'rare', 'Sealed long ago. A Seal Knife opens it.']].forEach(c => {
    event.create(`diaithir:crate_${c[0]}`).displayName(c[1]).texture('minecraft:item/bundle')
      .rarity(c[2]).tooltip(Text.gray(c[3]))
  })
  event.create('diaithir:crate_key').displayName('Seal Knife').texture('minecraft:item/flint')
    .tooltip(Text.gray('Cuts the seal on a Bound Pack or a Storied Kist.'))
})
