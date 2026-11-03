# Ascendant biomes and rite sites (S6857)

Source: plans/ASCENDANT.md (own design). No Panscriptum page exists for these three, so none was written; text is the design's only.

| Biome | Gate | Rite tier | Site structure |
|---|---|---|---|
| `diaithir_overworld_biomes:thornwake` | explore.thornwake, Age VII | I | `ascendant_thornwake_site` (mossy stone brick) |
| `diaithir_overworld_biomes:cinderfen` | explore.cinderfen, Rodos | II | `ascendant_cinderfen_site` (blackstone) |
| `diaithir_overworld_biomes:unwound` | explore.unwound, Rodos | III | `ascendant_unwound_site` (deepslate) |

Each site is an 11x3x11 template: a ruined floor disc, an `diaithirbosses:ascendant_altar` in the middle and nine `diaithirbosses:ogham_stone`s (the nine-rune order of the rite, #153). Sunk one block, spacing 20 / separation 8 chunks inside its biome. Structure and structure_set carry a `mod_loaded diaithirbosses` condition.

Regenerate: `python3 -I tools/ascendant_gen.py 4790` (4790 = MC 26.1.2 world version).

Not done: world placement (needs the Tellus fantasy palette / dimension biome source to include the three biomes), ores hollowsteel/emberglass/slagiron (foundry owns the items), mobs, 64 px art, client view.
