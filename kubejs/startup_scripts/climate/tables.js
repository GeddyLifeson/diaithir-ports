// Climate tables: how rooms gain and lose heat. Read by server_scripts/climate/climate.js.
// The object lives here because KubeJS 8 server scripts cannot assign to `global`; they only fill in what this makes.
// Power is heat per tick against a room's wall conductance G, so a room settles at ambient + power / G.
// A lit campfire (700) in a small stone hut (G ~ 25) settles about 28 C above the outside; in a hall (G ~ 150), 5 C.
// minAge is the first Age the block is craftable (content/tech_ages.json gates the item itself); it is documentation here.

global.CLIMATE = {
  tempAt: null,       // set by climate.js: (level, x, y, z) => degrees C
  seasonAt: null,     // set by climate.js: (level, x, z) => {sub, name, frac, year, yearDays, hemisphere, lat}
  roomAt: null,       // set by climate.js: (level, x, y, z) => room report
  cap: 9000,          // ticks of heat storage per interior cell; time constant = cap * volume / G
  maxCells: 2500,     // a space larger than this counts as open air
  rescanTicks: 200,   // how long a room's shape is trusted
  metersPerBlock: 5,  // Tellus world_scale of the Diaithir preset
  equatorZ: 0,        // Tellus puts the equator at z = 0; north is negative z

  // [pattern, conductance per wall face]: first match wins. Low is a good insulator.
  walls: [
    [/wool|carpet|hay_block|thatch|straw|fur|hide|fleece|turf_roof/, 0.12],
    [/snow|powder_snow/, 0.15],
    [/dirt|mud|grass_block|podzol|clay|sod|moss_block|peat|mycelium|rooted/, 0.25],
    [/plank|log|wood|stem|hyphae|bamboo|trapdoor|fence_gate|barrel|bookshelf|chest|crafting_table|wattle|daub/, 0.30],
    [/_door$/, 0.45],
    [/glass|pane/, 1.0],
    [/iron|steel|copper|gold|tin|bronze|aluminum|metal|casing|plating|silver|lead|nickel|netherite|diamond|machine|chest_metal/, 1.3],
    [/ice/, 0.55],
    [/stone|brick|cobble|deepslate|andesite|diorite|granite|tuff|concrete|terracotta|basalt|blackstone|sandstone|prismarine|obsidian|quartz|calcite|dripstone|purpur|mud_brick/, 0.45]
  ],
  wallDefault: 0.5,
  openDoor: 4.0,       // an open door, gate or trapdoor leaks like a window left open

  // Heat sources next to or inside a room. lit/burning is read from the block's LIT property when it has one.
  heat: {
    'minecraft:campfire': 700, 'minecraft:soul_campfire': 700,
    'minecraft:furnace': 500, 'minecraft:smoker': 450, 'minecraft:blast_furnace': 900,
    'minecraft:fire': 150, 'minecraft:soul_fire': 150, 'minecraft:lava': 400, 'minecraft:magma_block': 120,
    'minecraft:torch': 25, 'minecraft:wall_torch': 25, 'minecraft:soul_torch': 25, 'minecraft:soul_wall_torch': 25,
    'minecraft:lantern': 30, 'minecraft:soul_lantern': 30, 'minecraft:candle': 8, 'minecraft:jack_o_lantern': 20,
    'create:blaze_burner': 1500, 'minecraft:respawn_anchor': 0,
  },
  // Cold faces: a wall, floor or stack of these holds the room's face at its own temperature (C) instead of ambient,
  // so an ice house settles near freezing however warm the day is. Their conductance still comes from `walls`.
  cold: {
    'minecraft:snow_block': -4, 'minecraft:powder_snow': -4, 'minecraft:ice': -2,
    'minecraft:packed_ice': -6, 'minecraft:blue_ice': -12, 'minecraft:frosted_ice': -2, 'diaithir:ice_house_block': -8
  },

  // Outdoor model. Biome base temperature (vanilla scale) maps to a yearly mean in C.
  meanBase: -6, meanPerTemp: 22, meanMin: -25, meanMax: 30,
  lapsePerBlock: 0.0065,        // C per metre; multiplied by metersPerBlock
  groundDepthStart: 3, groundDepthFull: 15, geothermalPer10: 0.4,
  // Whole-dimension temperatures for dimensions that are not Earth-like.
  dimensions: { 'minecraft:the_nether': 38, 'minecraft:the_end': -30 },
  dimensionPrefixes: { 'backrooms:': 20, 'brx:': 20 }
}

// The calls other scripts use. They only forward, so they exist before climate.js fills in the work.
global.TEMP_AT = (level, x, y, z) => global.CLIMATE.tempAt ? global.CLIMATE.tempAt(level, x, y, z) : null
// Outdoor temperature at the surface of the column (x, z), season included, day and night averaged out: read this for markets and spoilage ticks instead of guessing a y.
global.TEMP_SURFACE = (level, x, z) => global.CLIMATE.surfaceAt ? global.CLIMATE.surfaceAt(level, x, z) : null
global.SEASON_AT = (level, x, z) => global.CLIMATE.seasonAt ? global.CLIMATE.seasonAt(level, x, z) : null

// Crop growth bands in C: [min, optimal, max]. Below min or above max a crop does not grow; between, growth is thinned.
// Read by server_scripts/climate/crops.js against global.TEMP_AT, so greenhouses and cold cellars work.
global.CLIMATE.crops = {
  'minecraft:wheat': [4, 16, 30], 'minecraft:carrots': [5, 16, 28], 'minecraft:potatoes': [4, 15, 26], 'minecraft:beetroots': [3, 14, 26],
  'minecraft:melon_stem': [12, 22, 34], 'minecraft:pumpkin_stem': [12, 22, 34], 'minecraft:sweet_berry_bush': [0, 12, 26],
  'minecraft:cocoa': [20, 28, 40], 'minecraft:sugar_cane': [10, 24, 36], 'minecraft:cactus': [12, 28, 45],
  'minecraft:torchflower_crop': [10, 20, 32], 'minecraft:pitcher_crop': [10, 20, 32]
}
