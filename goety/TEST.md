# Goety port test results
Build: `./gradlew build` -> goety-neoforge-26.1.2-3.2.2-diaithir.1.jar, NeoForge 26.1.2.116, Java 25.
Run: `tools/runtests.sh` boots a dedicated server, then `tools/smoke.py` drives it over rcon.

## Result (latest run)
- `/goetyselftest`: ok=1319 bad=0
  - all items build; all 464 blocks have a state and an item
  - all 34 creatures spawn and tick 40 ticks (7 first-batch servants, 27 new)
  - all 117 spells run through the spell engine (a few decline at the servant cap, which is allowed)
  - wands load foci; the soul totem round-trips souls
  - all 45 rites run at an altar with pedestals and a FakePlayer
- rcon: summon of every creature, give of every item and block, 770 ok, 0 failed
- rcon: `/place structure` for all 12 structures: generated; `/place feature goety:jade_ore`: placed
- rcon: `/loot spawn` for all 49 chest and creature loot tables: dropped
- Log: boot has 0 errors. The harness run shows one vanilla "Unprimed heightmap" line from placing a feature next to chunks that are not yet generated; it does not occur in normal generation.
- Earlier runs counted errors with a pattern that missed the `/ERROR]` log format; the pattern is fixed and the loot-table format errors it exposed (loot_table `value`, enchanted_count_increase, renamed items) are fixed.
- Not tested: client rendering and sound playback (headless server only).

## Ported in this pass
- 55 more foci (117 total) as spell archetypes on vanilla effects and the new servants.
- 27 creatures: servants (drowned, frayed, frozen, jungle zombies; mossy, rattled, sunken skeletons; bound evoker, storm caster, crusher; grave, ice and redstone golems; wraith), hostiles (frayed, zombie vindicator, rattled, skeleton pillager, crusher, storm caster, wraith, five necromancer variants) and the Skull Lord boss with a boss bar. Natural spawns via a NeoForge biome modifier.
- Worldgen: 12 jigsaw structures (254 templates), structure sets, jade ore feature, biome tags. Loot: 49 chest and creature tables.
- 34 sound events and 131 particle types from the original.

## Left
- Foci still missing: command, order, crafting, call, recall, ender chest, tunnel (they need screens or world state).
- Not ported: Apostle and other cult bosses, the 128x64 and custom-model creatures (their own models), Cursed Cage, Arca, infuser, research gating, Patchouli guide, 140 spell classes beyond the archetypes.
- Structures skipped (custom structure types): crypt, final terminal, spider den, wrecked mansion. Item frames and paintings are stripped from templates. Custom blocks the port lacks become air.
- Pine and windswept trees, corpse blossom: they use blocks and feature types this port lacks.
- Loot injection into vanilla chests (upstream loot modifiers) and cross-mod loot.
- Models of golems, necromancers and wraiths use vanilla bodies, not the original custom models.

## Dependencies
Curios (in pack, required). GeckoLib is in the pack but unused, since the original has no GeckoLib. Patchouli and JEI are not in the pack and were dropped.
