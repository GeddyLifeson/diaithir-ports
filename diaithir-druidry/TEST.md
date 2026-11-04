# Test results

Server: NeoForge 26.1.2.116, Java 25, mods: `spell_engine-neoforge-1.10.9+26.1.2`, `spell_power-neoforge-1.6.2+26.1.2` (with its Tiny Config),
Cloth Config 26.1.154, PlayerAnimationLib 1.2.8, Curios 15.0.0, `diaithir_druidry-0.1.0`. Fresh flat world, rcon driven. The test datapack (not shipped)
filled `#c:ingots/bronze` and `#c:ingots/steel` with copper and iron, as the pack's real mods do.

| check | result |
|---|---|
| Boot with all jars, grep log for errors or warnings that name diaithir_druidry or a spell | none |
| 49 spell JSON files load into the `spell_engine:spell` registry | pass; 49/49 resolved by a loot function bound to a one-spell tag per spell, tier matched (a missing or rejected spell gives an empty stack) |
| Every JSON key used in the spells exists as a field of the Spell Engine API classes (javap of the shipped jar) | no unknown keys |
| 14 staff recipes parse | pass once the ingot tags have members; without members NeoForge logs "can't be placed due to empty ingredients" (expected on a bare server) |
| Loot tables age3, age4, age5 | scrolls bind the right spell; staves carry model, name, attribute, container components |
| Loot injection | simple_dungeon, stronghold_library, ancient_city give scrolls in 60 rolls (42, 38, 60 times); buried_treasure, not targeted, none |
| Static check `tools/validate.py` | all JSON parses; every item definition has a model and texture; every spell has lang and icon |

Not tested (needs a client): rendering of icons and item models, animations, spell effects in combat. Casting was not exercised headless.
