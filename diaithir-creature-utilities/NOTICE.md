# diaithir-creature-utilities

Fishing crates are a port of **Fishing Crates** by tsuniko (https://codeberg.org/tsuniko/FishingCrates, Modrinth `fishing-loot-crates`).

Licence: Modrinth lists GPL-3.0-only; the source's fabric.mod.json says MIT and the repo has no LICENSE file. Either allows this port. We treat it as GPL-3.0: this module's source stays GPL-3.0-compatible and keeps the credit above.

Changes: rewritten for NeoForge 26.1.2 (Fabric API loot events replaced by global loot modifiers, with the same odds: wooden 4/104 and iron 1/104 of fish catches, golden 1/7 of treasure catches), loot tables kept from upstream, our own 64 px crate art.

## Species bees (11-03)
Clean-room, MIT, written from the public feature description of Productive Bees (many species, wild solitary bees found by biome, resource bees bred from them, a comb per species); no code or art taken, the original is all-rights-reserved. 32 species in `data/diaithircreatureutilities/bees/species.json` (14 wild, 18 bred), one `species_bee` entity (vanilla bee behaviour, species synced and saved, own skin per species), a comb item per species that smelts to its resource. Rebuild data and art with `python3 -I tools/gen_bees.py`. Summon one: `/summon diaithircreatureutilities:species_bee ~ ~ ~ {Species:"iron"}`.
