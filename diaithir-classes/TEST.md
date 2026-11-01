# Test results

Server: NeoForge 26.1.2.116, Java 25, mods spell_engine 1.10.9, spell_power 1.6.2, curios 15.0.0, ranged_weapon_api 4.0.0, cloth config, PlayerAnimationLib, diaithir-arms 1.0.0, diaithir-classes 1.0.0. Fresh world, rcon driven, test datapack fills `#c:ingots/bronze` and `#c:ingots/steel`.

| check | result |
|---|---|
| Boot, grep for errors/warnings naming diaithir_classes, spells, equipment sets | none (recipes parse, no empty-ingredient warnings with the test datapack) |
| `/classesselftest` | items=66/66 runes=11/11 rings=6/6 spells=28/28 sets=12/12 containers=18/18 bad=0 |
| Spell JSON keys vs Spell Engine API classes (`tools/validate.py`, javap) | 0 problems |
| `summon item` for all 87 ids | 87 summoned |
| `item replace` of all 87 ids on an armor stand | 87 replaced |
| Ring with sockets component set by command, read back | `["beith","dair","coll"]` persists |
| Socket logic in selftest (every rune into every ring, duplicates, full) | pass |

Not tested (needs a client or player): combat effect of spells and passives, set bonus application, Curios rune attributes on a worn ring, haste/crit attribute scaling, rendering.
Library and lore text come from compute jobs classes_library and classes_lore; the *_retry jobs hold rows that failed the check and must be run, then merge.py re-run.
