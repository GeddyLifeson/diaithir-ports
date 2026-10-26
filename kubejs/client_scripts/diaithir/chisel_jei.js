// Chisel registers about 11,800 decorative variants (27 % of all ingredients in this pack). They stay in the game and in the chisel
// GUI; this only takes them out of JEI's ingredient list so the list and its search index are a quarter smaller.
// Kept in JEI: the tools and upgrades, every item that is the base of a variant family or the result of a Chisel recipe, and the
// worldgen blocks (*_raw). RecipeViewerEvents.removeEntries was checked against kubejs-neoforge-26.1.2-8.0.6 (RecipeViewerEvents.GROUP,
// JEIRemoveEntriesKubeEvent -> IIngredientManager.removeIngredientsAtRuntime); a RegExp is accepted by ItemPredicate.wrap.
const CHISEL_KEEP = [
  'aluminum_bad_greggy', 'ancient_stone_array', 'antiblock_black', 'arcane_border', 'auto_chisel', 'ballomoss',
  'bright_glass', 'bronze_bad_greggy', 'builders_guide', 'certus_array', 'charcoal_array', 'chisel_diamond',
  'chisel_iron', 'chisel_obsidian', 'cloud_grid', 'cloudinabottle', 'coal_coke_array', 'cobalt_bad_greggy',
  'electrum_bad_greggy', 'energized_voidstone_bevel', 'factory_circuit', 'futura_controller', 'grimstone_blocks',
  'hex_plating_0', 'holystone_blocks', 'invar_bad_greggy', 'kitchen_checker', 'laboratory_checkerboard',
  'lavastone_array', 'lead_bad_greggy', 'light_black_regular', 'light_blue_regular', 'light_brown_regular',
  'light_cyan_regular', 'light_gray_regular', 'light_green_regular', 'light_light_blue_regular',
  'light_light_gray_regular', 'light_lime_regular', 'light_magenta_regular', 'light_orange_regular',
  'light_pink_regular', 'light_purple_regular', 'light_red_regular', 'light_white_regular', 'light_yellow_regular',
  'liminal_wall_1', 'measuring_tape', 'military_imperial_camo', 'mossy_temple_bricks', 'neovitae_arranged', 'nexus_core',
  'nickel_bad_greggy', 'oak_framed_glass', 'oak_framed_stained_glass_black', 'oak_framed_stained_glass_blue',
  'oak_framed_stained_glass_brown', 'oak_framed_stained_glass_cyan', 'oak_framed_stained_glass_gray',
  'oak_framed_stained_glass_green', 'oak_framed_stained_glass_light_blue', 'oak_framed_stained_glass_light_gray',
  'oak_framed_stained_glass_lime', 'oak_framed_stained_glass_magenta', 'oak_framed_stained_glass_orange',
  'oak_framed_stained_glass_pink', 'oak_framed_stained_glass_purple', 'oak_framed_stained_glass_red',
  'oak_framed_stained_glass_white', 'oak_framed_stained_glass_yellow', 'offset_tool', 'paperwall_boxed',
  'platinum_bad_greggy', 'road_line_double_white_center', 'shingles_1', 'silver_125', 'sky_stone_array', 'smashingrock',
  'steel_125', 'steel_framed_glass', 'steel_framed_stained_glass_black', 'steel_framed_stained_glass_blue',
  'steel_framed_stained_glass_brown', 'steel_framed_stained_glass_cyan', 'steel_framed_stained_glass_gray',
  'steel_framed_stained_glass_green', 'steel_framed_stained_glass_light_blue', 'steel_framed_stained_glass_light_gray',
  'steel_framed_stained_glass_lime', 'steel_framed_stained_glass_magenta', 'steel_framed_stained_glass_orange',
  'steel_framed_stained_glass_pink', 'steel_framed_stained_glass_purple', 'steel_framed_stained_glass_red',
  'steel_framed_stained_glass_white', 'steel_framed_stained_glass_yellow', 'tallow_block', 'technical_125',
  'temple_cobble', 'thaumium_bevel', 'tin_125', 'tyrian_black', 'upgrade_automation', 'upgrade_reversion',
  'upgrade_speed', 'upgrade_stack', 'uranium_125', 'valentines_block', 'voidstone_bevel', 'warning_biohazard',
  'waterstone_array', 'waxed_copper_bad_greggy', 'waxed_exposed_copper_bad_greggy', 'waxed_oxidized_copper_bad_greggy',
  'waxed_weathered_copper_bad_greggy'
]
RecipeViewerEvents.removeEntries('item', event => {
  event.remove(new RegExp('^chisel:(?!(?:' + CHISEL_KEEP.join('|') + ')$)(?!.*_raw$).+$'))
})
