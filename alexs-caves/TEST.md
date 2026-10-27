# Test results: alexs-caves 2.0.2-diaithir.1

Build: NeoForge 26.1.2.116, Java 25, `./gradlew jar` (jar 73 MB, mostly the original sounds). Boot tests on a stand-alone NeoForge server with this jar, rcon-driven (tools/rcon.py, summon_all.py, place_all.py, scan_chambers.py).

| Check | Result |
|---|---|
| Server boots, no registry or datapack errors | pass (Done in ~5 s; no "Couldn't parse" lines after the recipe, advancement and loot-modifier conversion) |
| `/summon` of all 83 entity types, then 400 ticks | pass, no errors (needed the `tempt_range` attribute and a mixin-free frostmint freeze) |
| Every block id (344) placed with `/setblock`; every item id (189) summoned as an item entity; 100 ticks | pass |
| 63 non-block loot tables via `/loot spawn` | pass |
| Cave chambers, vanilla overworld, 192x192 forced area scan | pass: all six biomes found by `execute if biome`, no "far chunk" errors after centring on the chunk |
| Cave chambers with the pack's Tellus jar and `tellus:earth`, same scan | pass: all six biomes found |
| Age gate | data only (age_gates.json) |

Not tested (no client here): any rendering (fallback box renderers, block and item models, armour, GUIs), natural mob spawning inside chambers (spawn lists are in the biome JSON), the cave chambers' look. Item, block and equipment assets were checked by script for missing parents and textures.

Known gaps: see PORT_PLAN.md (cut features, structures, entity models).
