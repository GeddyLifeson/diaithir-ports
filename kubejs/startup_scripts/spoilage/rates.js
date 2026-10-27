// Shelf life. The clock is the Spoiled mod (MIT): a per-stack timer, tooltips, container modifiers. This file is our data for it.
// rate = weekly fraction of a food that goes bad at room temperature (a week is 7 game days), same shape and ids as ECON_SPOIL in the
// economy's 04_frictions.js so stock on the shelf and stock in a pack rot at one rate. recipes.js turns each rate into a Spoiled
// shelf life (the time half of it has gone) and a rotten leaving. Honey, salt and tanned leather never spoil, as in history.
// The cold multipliers below are applied by server_scripts/spoilage/spoilage.js.
global.SPOIL_WEEK = 168000
global.SPOIL_HOOK = { tempAt: null } // the temperature system sets tempAt = (level, x, y, z) => degrees C (mutate it; server scripts can't assign global)
global.SPOIL = [
  // preserved first: salted, smoked, dried and pickled keep for months
  [/^(salted_|smoked_|pickled_|dried_(meat|fish|fruit)$)/, 0.02, 'preserved'],
  [/^(dried_kelp|hay_block)$/, 0.01, 'grain'],
  [/^(cooked_|bread|baked_potato|pumpkin_pie|cake|cookie|mushroom_stew|rabbit_stew|beetroot_soup)/, 0.15, 'meat'],
  [/^(beef|porkchop|mutton|chicken|rabbit|cod|salmon|tropical_fish|pufferfish)$/, 0.2, 'meat'],
  [/^(apple|carrot|potato|beetroot|melon_slice|sweet_berries|glow_berries|kelp|sea_pickle|pumpkin|melon)$/, 0.1, 'produce'],
  [/^(wheat|wheat_seeds|beetroot_seeds|melon_seeds|pumpkin_seeds)$/, 0.03, 'grain'],
  [/^(egg)$/, 0.06, 'meat'],
  [/^(raw_hide)$/, 0.25, 'hide'],
  [/^(milk_bucket)$/, 0.3, 'meat']
]
// Never spoil (also listed in config/spoiled-common.toml spoilBlacklist).
global.SPOIL_NEVER = /^(honey_bottle|honeycomb|honeycomb_block|sugar|salt_.*|leather|rabbit_hide|rabbit_foot|rotten_flesh|spider_eye|golden_.*|enchanted_.*|spoiled_.*|dried_kelp_block|chorus_fruit|suspicious_.*|potion|ominous_.*)$/
global.SPOIL_UPDATE = 600 // ticks per Spoiled update (its spoilRate config is in seconds: 30 s)
global.SPOIL_ROT = {
  meat: 'minecraft:rotten_flesh', produce: 'diaithir:spoiled_produce',
  grain: 'diaithir:spoiled_grain', hide: 'diaithir:spoiled_hide', preserved: 'minecraft:rotten_flesh'
}
// Cold storage the environment gives, as a share of the room-temperature rate. Fridges and freezers are Spoiled container modifiers
// (config/spoiled-common.toml). Here: the best of the container's surroundings (ice about it, a roof of earth, running water, a cooling coil).
global.SPOIL_COLD_NEAR = { // radius-1 neighbours of the container
  'diaithir:cooling_coil': 0.12, 'diaithir_drinks:cold_cellar': 0.25,
  'minecraft:blue_ice': 0.15, 'minecraft:packed_ice': 0.25, 'minecraft:ice': 0.3, 'minecraft:snow_block': 0.4
}
global.SPOIL_ROOT_CELLAR = 0.5 // roofed over by 4+ blocks of earth, no sky
global.SPOIL_SPRINGHOUSE = 0.6 // roofed with running or still water within 2 blocks
