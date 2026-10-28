// Crates: use one to roll its loot table (kubejs/data/diaithir/loot_table/crates/<tier>.json).
// Bound Packs and Storied Kists also use up a Seal Knife from the inventory.
// Loot is cumulative by Age: the table for Age N holds the items of Age N and earlier (tools/economy/loot.py writes them).
// The Telling shares Age I; with no Ages engine (Deep Worlds) the last table is used.
const crateAge = () => typeof AGES === 'undefined' ? 'vii' : (String(AGES.age) === 'TELLING' ? 'i' : String(AGES.age).toLowerCase())

;['common', 'rare', 'storied'].forEach(tier => {
  ItemEvents.rightClicked(`diaithir:crate_${tier}`, event => {
    let player = event.player
    if (tier !== 'common') {
      if (econCount(player, 'diaithir:crate_key') < 1) return player.tell(Text.red('It is sealed. You need a Seal Knife.'))
      player.server.runCommandSilent(`clear ${player.username} diaithir:crate_key 1`)
    }
    event.item.shrink(1) // count is a method in 26.1, so -- would do nothing
    player.server.runCommandSilent(`loot give ${player.username} loot diaithir:crates/${tier}_${crateAge()}`)
  })
})
