// Tag families for server_scripts/diaithir/unify.js: canonical tag -> the aliases other mods use for the same thing.

global.diaithirItemFamilies = {
  "c:ingots/aluminum": ["c:ingots/aluminium"],
  "c:nuggets/aluminum": ["c:nuggets/aluminium"],
  "c:dusts/aluminum": ["c:dusts/aluminium"],
  "c:plates/aluminum": ["c:plates/aluminium"],
  "c:storage_blocks/aluminum": ["c:storage_blocks/aluminium"],
  "c:raw_materials/aluminum": ["c:raw_materials/aluminium"],
  "c:ores/aluminum": ["c:ores/aluminium", "c:ores/bauxite"],
  "c:dusts/sulfur": ["c:dusts/sulphur", "c:gems/sulfur"],
  "c:gems/certus_quartz": ["c:gems/certus"],
  "c:silicon": ["c:gems/silicon", "c:ingots/silicon"],
  "c:coal_coke": ["c:gems/coal_coke", "c:fuels/coke"]
}

global.diaithirFluidFamilies = {
  "c:oil": ["c:crude_oil", "c:fuels/crude_oil"]
}

// Filled by the item tag pass with the aliases that exist in this pack; the recipe pass rewrites only those.
global.diaithirLinkedAliases = {}
