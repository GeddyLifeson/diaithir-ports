# Ascendant: post-endgame boss tiers (original design)

Marlon 11-02: "put Calamity stuff from Terraria in too." Followed as the mod-first rule: find, port, else build our own.

## Mod-first result (checked 11-02)
| Candidate | State | Licence | Verdict |
|---|---|---|---|
| Calamity Mod (Terraria) | Not a Minecraft mod; source and assets belong to its authors, all rights reserved | ARR | Not recreated. No bosses, names, sprites, lore or item lists used. |
| Terramity (Modrinth) | 1.20.1 only, no 26.1 build | All Rights Reserved | Cannot port. Clean-room only. |
| Mortality / Brutality / Lethality (Terramity addons) | 1.20.1 only | ARR / MIT / CC0 | No 26.1 build, and all depend on Terramity. Not ported. |
| MC Calamity (datapack) | 1.21.x, a recreation of the Terraria mod | Unconfirmed | Skipped: it is the thing we must not recreate. |
| Terraria Items, BetterGrim | Item packs, not boss progression | MIT / GPL | Nothing relevant to take. |
No Minecraft mod for 26.1 offers this genre. So: our own content, same genre only (hard post-endgame boss tiers, boss rush, harder modes, endgame materials).

## What this adds (inside diaithir-bosses, no new jar)
- **Nine Ascendant bosses**, three tiers. Tier I at Age VII, tiers II and III at Rodos (Age 8). Each needs the previous tier beaten once. Names are Diaithiris, built from `archaic.py`; English name on the second line.
  - I: Antlered Hollow (`damh falamh`), Cauldron-Heart (`coire cridhe`), Tide Reaver (`tonn creachadair`).
  - II: Foundry Sovereign, Mist Weaver, Ash Seraph.
  - III: Dusk Wyrm, Rootbreaker, The Last Wheel (capstone, the Ages' own clock).
  - 3 phases, 4 to 7 telegraphed attacks each, from the existing attack set. HP 950 to 2025 at base, plus the framework's Age and player-count scaling; per-hit cap 6%.
- **Boss rush** (`data/diaithirbosses/rush.json`): all 6 base bosses then all 9 Ascendant bosses in one arena, no healing between, rewards at 6, 9, 12 and 15 kills, scoreboard best time.
- **Difficulty modes** (`difficulty.json`): standard, relentless, merciless, ordeal. Multipliers on HP, damage, tempo, Champion elite chance, loot and XP. Raised only, never lowered. Reached through quests, so it is a Bethesda-style quest unlock, not a switch.
- **Quests** (`quests/ascendant.json`, Codex Quests tab): Raise the altar, Ascendant I to III, The Gauntlet, and a quest per mode.
- **Global Events** (`diaithir_events.json`, #111): blood moon halves respawn cooldown, eclipse buffs Dusk Wyrm, storm buffs Tide Reaver and Ash Seraph, calendar year change resets cooldowns. Fires `ascendant_boss_defeated`, `boss_rush_cleared`, `ascendant_mode_raised`.
- **Codex**: two chapters appended to `codex/bosses_handbook.json`. Boss Checklist (RPG #218) reads `boss_index.json` and `age_gates.json`, both updated.
- **Hostile encounters** (#113, #120): Ascendant bosses are valid anchors for an encounter; the encounter module spawns the boss through `/boss spawn <id>` semantics. Combat rewrite thread (cmsg_...N7D6ck) owns the attack-type additions listed below.

## Not in this PR (listed so nothing is claimed that is not there)
1. **Java**: a rush controller that reads `rush.json`, a mode store that reads `difficulty.json`, quest and event glue (the files are the contract), the Ascendant altar block (`summon.rite`). Today the bosses are testable with spawn eggs and `/boss spawn`. `SelfTest` Age check widened to Rodos.
2. **Endgame biomes and materials**: designed here, built in the worldgen and biomes rewrite threads.
   - *Thornwake* (Age VII): old-growth forest with antler-bone groves; ore `hollowsteel` (tag `c:ores/hollowsteel`, `diaithir:ore/hollowsteel`).
   - *Cinderfen* (Rodos): ash marsh over a buried foundry; `emberglass` (gem) and `slagiron`.
   - *The Unwound* (Rodos II): floating arcs of broken clockwork; `ageshard`, dropped only by tier III.
   - Recipes use `c:` tags; units per UNITS.md (hardness in kg-based terms, machine draw in W, J costs). Every working block gets an upgrade rule (#117).
3. **Attack types**: `gravity_well`, `rewind` (Last Wheel returns players to a position from 5 s ago).
4. **Art**: textures are luminance-ramped vanilla skins in the project's recolour style at the vanilla skin size; 64 px bespoke art goes to the texture lanes.
5. No client world-join test has been run (cloud session); needs the PC lane.

## Regenerate
`python3 -I tools/gen_ascendant.py <vanilla skins dir>` (S0312 in Global-Repo). Additive: does not wipe `gen.py` output.
