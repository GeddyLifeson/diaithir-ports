// How the money goods of each tradition are made, gathered or earned (Ages I-IV, before coin). tools/economy/currencies.py names the goods (GOODS);
// data/diaithir/money_sources.json lists the way to each, and tools/economy/test_money_sources.py checks every unit has one.
// Coins themselves can never be made (nomint.js). These are goods: a ring of copper is its copper, a belt of beads is its beads.
//   Shells:   walk the shore: with an empty hand, use a sand block that has water at its edge, under open sky, and sometimes a cowrie is there.
//             A nautilus shell breaks into four. A string holds twelve.
//   Beads:    a shell and a flint to drill it give four wampum beads; twelve beads weave into a belt and back.
//   Metal:    nuggets from ingots and back as in vanilla (the mods bring their own); rings, bars, animals, axe blades and manillas are one ingot
//             worked by hand, and melt back into it.
//   Earning:  wages and bounties through econPayWage, and every sale to a store.

const SHELL_CHANCE = 0.5
const SHELL_SEA_Y = 70

BlockEvents.rightClicked(event => {
  let player = event.player, block = event.block
  if (!player || String(event.hand) !== 'MAIN_HAND' || String(block.id) !== 'minecraft:sand' || !player.mainHandItem.isEmpty()) return
  if (block.y > SHELL_SEA_Y || !block.level.canSeeSky(block.pos.above())) return
  let wet = [[1, 0], [-1, 0], [0, 1], [0, -1]].some(d => String(block.offset(d[0], 0, d[1]).id) === 'minecraft:water')
  if (!wet) return
  if (Math.random() < SHELL_CHANCE) player.give('diaithir:cowrie_shell')
  else player.tell(Text.gray('Nothing but sand.'))
  event.cancel()
})

// An absent mod's item must not end the recipes event (PR #166), so each recipe is its own try.
ServerEvents.recipes(event => {
  let n = (k, id) => { let a = []; for (let i = 0; i < k; i++) a.push(id); return a }
  let tryRecipe = fn => { try { fn() } catch (e) { console.warn('[economy] money goods recipe skipped: ' + e) } }
  // Ring money: a worked form of any ingot of the metal (the unified c:ingots/<metal> tag, so whichever mod's ingot is at hand), and back to the one canonical ingot.
  // It is one ingot for one ring both ways, so it is exchange, not minting (no_mint.py allows no coin recipe).
  let worked = (big, ingot, metal) => {
    if (!Item.exists(ingot)) return console.warn('[economy] ' + ingot + ' is not in this pack: no recipe for ' + big)
    tryRecipe(() => event.shapeless(big, '#c:ingots/' + metal))
    tryRecipe(() => event.shapeless(ingot, big))
  }
  tryRecipe(() => event.shapeless('4x diaithir:cowrie_shell', 'minecraft:nautilus_shell'))
  tryRecipe(() => event.shapeless('diaithir:cowrie_string', n(12, 'diaithir:cowrie_shell')))
  tryRecipe(() => event.shapeless('12x diaithir:cowrie_shell', 'diaithir:cowrie_string'))
  tryRecipe(() => event.shapeless('4x diaithir:wampum_bead', ['diaithir:cowrie_shell', 'minecraft:flint']))
  tryRecipe(() => event.shapeless('diaithir:wampum_belt', n(12, 'diaithir:wampum_bead')))
  tryRecipe(() => event.shapeless('12x diaithir:wampum_bead', 'diaithir:wampum_belt'))
  worked('diaithir:deben_ring', 'minecraft:copper_ingot', 'copper')
  worked('diaithir:axe_blade', 'minecraft:copper_ingot', 'copper')
  worked('diaithir:currency_bar', 'minecraft:iron_ingot', 'iron')
  worked('diaithir:tin_animal', 'forestry:tin_ingot', 'tin')
  worked('diaithir:arm_ring', 'occultism:silver_ingot', 'silver')
  worked('diaithir:manilla', 'mekanism:ingot_bronze', 'bronze')
})
