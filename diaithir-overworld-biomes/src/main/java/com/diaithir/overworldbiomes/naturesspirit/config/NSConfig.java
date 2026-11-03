package com.diaithir.overworldbiomes.naturesspirit.config;

import java.nio.file.Path;

public class NSConfig extends Config {

  public int terra_ferax_weight;
  public int terra_solaris_weight;
  public int terra_flava_weight;
  public int terra_laeta_weight;
  public int terra_mater_weight;

  public boolean calcite_generator;
  public boolean deepslate_generator;
  public boolean creative_tab;
  public boolean sugi_and_stratified_pillars;

  public boolean vanilla_trees_toggle;
  public boolean birch_forest_toggle;
  public boolean flower_forest_toggle;
  public boolean jungle_toggle;
  public boolean swamp_toggle;
  public boolean badlands_toggle;
  public boolean mountain_biomes_toggle;
  public boolean savanna_toggle;
  public boolean dark_forest_toggle;
  public boolean windswept_hills_toggle;

  public boolean has_sugi_forest;
  public boolean has_windswept_sugi_forest;
  public boolean has_blooming_sugi_forest;
  public boolean has_lavender_fields;
  public boolean has_marsh;
  public boolean has_bamboo_wetlands;
  public boolean has_wisteria_forest;
  public boolean has_redwood_forest;
  public boolean has_snowy_redwood_forest;
  public boolean has_aspen_forest;
  public boolean has_maple_woodlands;
  public boolean has_golden_wilds;
  public boolean has_marigold_meadows;
  public boolean has_fir_forest;
  public boolean has_snowy_fir_forest;
  public boolean has_cypress_fields;
  public boolean has_cedar_thicket;
  public boolean has_carnation_fields;
  public boolean has_stratified_desert;
  public boolean has_blooming_dunes;
  public boolean has_lively_dunes;
  public boolean has_drylands;
  public boolean has_wooded_drylands;
  public boolean has_xeric_plains;
  public boolean has_white_cliffs;
  public boolean has_prairie;
  public boolean has_oak_savanna;
  public boolean has_heather_fields;
  public boolean has_tundra;
  public boolean has_alpine_clearings;
  public boolean has_alpine_highlands;
  public boolean has_coniferous_covert;
  public boolean has_boreal_taiga;
  public boolean has_tropical_shores;
  public boolean has_tropical_woods;
  public boolean has_sparse_tropical_woods;
  public boolean has_tropical_basin;
  public boolean has_arid_savanna;
  public boolean has_scorched_dunes;
  public boolean has_flowering_shrubland;
  public boolean has_shrubland;
  public boolean has_arid_highlands;
  public boolean has_shrubby_highlands;
  public boolean has_woody_highlands;
  public boolean has_red_peaks;
  public boolean has_dusty_slopes;
  public boolean has_snowcapped_red_peaks;
  public boolean has_sleeted_slopes;
  public boolean has_blooming_highlands;
  public boolean has_chaparral;
  public boolean has_floral_ridges;

  public NSConfig(Path path) {
    super(path);
  }

  @Override
  public void load() {
    has_sugi_forest = add("biome.has_sugi_forest", true, "");
    has_windswept_sugi_forest = add("biome.has_windswept_sugi_forest", true, "");
    has_blooming_sugi_forest = add("biome.has_blooming_sugi_forest", true, "");
    has_lavender_fields = add("biome.has_lavender_fields", true, "");
    has_marsh = add("biome.has_marsh", true, "");
    has_bamboo_wetlands = add("biome.has_bamboo_wetlands", true, "");
    has_wisteria_forest = add("biome.has_wisteria_forest", true, "");
    has_redwood_forest = add("biome.has_redwood_forest", true, "");
    has_snowy_redwood_forest = add("biome.has_snowy_redwood_forest", true, "");
    has_aspen_forest = add("biome.has_aspen_forest", true, "");
    has_maple_woodlands = add("biome.has_maple_woodlands", true, "");
    has_golden_wilds = add("biome.has_golden_wilds", true, "");
    has_marigold_meadows = add("biome.has_marigold_meadows", true, "");
    has_fir_forest = add("biome.has_fir_forest", true, "");
    has_snowy_fir_forest = add("biome.has_snowy_fir_forest", true, "");
    has_cypress_fields = add("biome.has_cypress_fields", true, "");
    has_cedar_thicket = add("biome.has_cedar_thicket", true, "");
    has_carnation_fields = add("biome.has_carnation_fields", true, "");
    has_stratified_desert = add("biome.has_stratified_desert", true, "");
    has_blooming_dunes = add("biome.has_blooming_dunes", true, "");
    has_lively_dunes = add("biome.has_lively_dunes", true, "");
    has_drylands = add("biome.has_drylands", true, "");
    has_wooded_drylands = add("biome.has_wooded_drylands", true, "");
    has_xeric_plains = add("biome.has_xeric_plains", true, "");
    has_white_cliffs = add("biome.has_white_cliffs", true, "");
    has_prairie = add("biome.has_prairie", true, "");
    has_oak_savanna = add("biome.has_oak_savanna", true, "");
    has_heather_fields = add("biome.has_heather_fields", true, "");
    has_tundra = add("biome.has_tundra", true, "");
    has_alpine_clearings = add("biome.has_alpine_clearings", true, "");
    has_alpine_highlands = add("biome.has_alpine_highlands", true, "");
    has_coniferous_covert = add("biome.has_coniferous_covert", true, "");
    has_boreal_taiga = add("biome.has_boreal_taiga", true, "");
    has_tropical_shores = add("biome.has_tropical_shores", true, "");
    has_tropical_woods = add("biome.has_tropical_woods", true, "");
    has_sparse_tropical_woods = add("biome.has_sparse_tropical_woods", true, "");
    has_tropical_basin = add("biome.has_tropical_basin", true, "");
    has_arid_savanna = add("biome.has_arid_savanna", true, "");
    has_scorched_dunes = add("biome.has_scorched_dunes", true, "");
    has_flowering_shrubland = add("biome.has_flowering_shrubland", true, "");
    has_shrubland = add("biome.has_shrubland", true, "");
    has_arid_highlands = add("biome.has_arid_highlands", true, "");
    has_shrubby_highlands = add("biome.has_shrubby_highlands", true, "");
    has_woody_highlands = add("biome.has_woody_highlands", true, "");
    has_red_peaks = add("biome.has_red_peaks", true, "");
    has_dusty_slopes = add("biome.has_dusty_slopes", true, "");
    has_snowcapped_red_peaks = add("biome.has_snowcapped_red_peaks", true, "");
    has_sleeted_slopes = add("biome.has_sleeted_slopes", true, "");
    has_blooming_highlands = add("biome.has_blooming_highlands", true, "");
    has_chaparral = add("biome.has_chaparral", true, "");
    has_floral_ridges = add("biome.has_floral_ridges", true, "");

    terra_ferax_weight = addNumber("region.terra_ferax_frequency", 4, 0, Integer.MAX_VALUE, "");
    terra_solaris_weight = addNumber("region.terra_solaris_frequency", 4, 0, Integer.MAX_VALUE, "");
    terra_flava_weight = addNumber("region.terra_flava_frequency", 4, 0, Integer.MAX_VALUE, "");
    terra_laeta_weight = addNumber("region.terra_laeta_frequency", 4, 0, Integer.MAX_VALUE, "");
    terra_mater_weight = addNumber("region.terra_mater_frequency", 4, 0, Integer.MAX_VALUE, "");

    calcite_generator = add("misc.calcite_generator", true, "Calcite clusters from coral feature");
    deepslate_generator = add("misc.deepslate_generator", true, "Toggle the Deepslate Generator");
    creative_tab = add("misc.creative_tab", true, "Toggle the additional creative inventory tab");
    sugi_and_stratified_pillars = add("misc.sugi_and_stratified_pillars", true, "Toggle the Pillar Generation (turn off for lower end devices)");

    vanilla_trees_toggle = add("datapacks.vanilla_trees_toggle", false, "");
    birch_forest_toggle = add("datapacks.birch_forest_toggle", true, "");
    flower_forest_toggle = add("datapacks.flower_forest_toggle", true, "");
    jungle_toggle = add("datapacks.jungle_toggle", true, "");
    swamp_toggle = add("datapacks.swamp_toggle", true, "");
    badlands_toggle = add("datapacks.badlands_toggle", true, "");
    mountain_biomes_toggle = add("datapacks.mountain_biomes_toggle", true, "");
    savanna_toggle = add("datapacks.savanna_toggle", true, "");
    dark_forest_toggle = add("datapacks.dark_forest_toggle", true, "");
    windswept_hills_toggle = add("datapacks.windswept_hills_toggle", true, "");
  }
}
