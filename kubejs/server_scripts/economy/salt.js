// Salt in Age I, every old way, with no cap. Salt can be made fast (kelp farms, furnace arrays) and that is fine: spent into the stores
// it piles up there, the price falls, the salt money cheapens, and a culture whose salt has lost half its worth for a month changes to
// another staple (market.js). The market, not a quota, is the limit.
//   Seaweed ash:  dried kelp cooked in a furnace, smoker or campfire.            Sea pickles likewise (brine boiled down).
//   Shore pans:   rake a sand block at the water's edge, under open sky, with a hoe.
//   Rock salt:    calcite seams break out salt now and then (a third of the time).
//   Dried salt:   Biomes O' Plenty's dried salt crystals, two twists each.
// Twelve twists press into a Salt Cake and back.

const SALT_HOES = ['minecraft:wooden_hoe', 'minecraft:stone_hoe', 'minecraft:iron_hoe', 'minecraft:golden_hoe', 'minecraft:diamond_hoe', 'minecraft:netherite_hoe', 'minecraft:copper_hoe']
const SALT_ROCK_CHANCE = 1 / 3

BlockEvents.rightClicked(event => {
  let player = event.player, block = event.block
  if (!player || String(event.hand) !== 'MAIN_HAND' || String(block.id) !== 'minecraft:sand') return
  if (SALT_HOES.indexOf(String(player.mainHandItem.id)) < 0) return
  if (block.y > 70 || !block.level.canSeeSky(block.pos.above())) return
  let wet = [[1, 0], [-1, 0], [0, 1], [0, -1]].some(d => String(block.offset(d[0], 0, d[1]).id) === 'minecraft:water')
  if (!wet) return player.tell(Text.gray('A pan needs sea water at its edge.'))
  player.give('diaithir:salt_twist')
  event.cancel()
})

BlockEvents.broken('minecraft:calcite', event => {
  if (Math.random() < SALT_ROCK_CHANCE) event.block.popItem('diaithir:salt_twist')
})

ServerEvents.recipes(event => {
  event.smelting('diaithir:salt_twist', 'minecraft:dried_kelp').xp(0.1)
  event.smoking('diaithir:salt_twist', 'minecraft:dried_kelp')
  event.campfireCooking('diaithir:salt_twist', 'minecraft:dried_kelp').cookingTime(300)
  event.smelting('diaithir:salt_twist', 'minecraft:sea_pickle').xp(0.1)
  event.shapeless('2x diaithir:salt_twist', 'biomesoplenty:dried_salt')
  event.shapeless('diaithir:salt_cake', [0,1,2,3,4,5,6,7,8,9,10,11].map(() => 'diaithir:salt_twist'))
  event.shapeless('12x diaithir:salt_twist', 'diaithir:salt_cake')
})
