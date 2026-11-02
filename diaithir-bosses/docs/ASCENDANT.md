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
- **Hostile encounters** (#113, #120): Ascendant bosses are valid anchors for an encounter; the encounter module spawns the boss through `/boss spawn <id>` semantics. Combat rewrite thread (combat rewrite thread) owns the attack-type additions listed below.

## Rites: how an Ascendant boss is woken (own design)
No boss appears from a single click. A rite is a chain of steps with a cryptic clue each; the clue shows when the step opens and stays in the Codex Rites page (`codex/ritual_hints.json`, one quest per clue). The altar is the Ascendant altar (overworld only). It starts with a **champion token** (the next boss you have not beaten) or a **trophy** (a rematch).
| Step | What the players do |
|---|---|
| feast | Set one dish from `#diaithirbosses:offering_feast` on the altar (kitchen module adds to the tag; PROGRESSION.md: needs cold store). |
| offer | Give a stack of a named item (bones, iron blocks, ender eyes, clocks...). |
| hunt | Kill a count of a named creature within 48 blocks of the altar. |
| stones | Place **ogham stones** nearby. Use one to turn its rune (Birch, Rowan, Alder, Willow, Ash, Hawthorn, Oak, Holly, Hazel); sneak-use seals the rune it shows. The order is hidden in the clue as nine tree riddles. A wrong rune hurts and resets the step. |
| hour | Use the altar only inside a time window (midnight, dawn, dusk, noon). |
| coop | Tier II and III: 2 or 3 fighters must stand at the altar together. |
| defend | Hold the altar for 60 to 150 s against waves that scale with fighters present; leave it alone and the step resets. |
Tier I rites have 6 steps, tier II 7, tier III 7 to 8. Each rite ends in the boss waking at the altar. Progress is saved per altar. Tiers II and III still need the lower tier beaten once.
Not copied from anything: the steps, riddles, trees and items are ours, built from Celtic lore (ogham, the tree calendar) and the Age clock.

## Materials and the progression web (plans/PROGRESSION.md)
Drops: `diaithirbosses:boss_catalyst` (tier I), `hollow_core` (tier II and III), `ageshard` (The Last Wheel only). Offering feast comes from the kitchen module through the tag above. Ids as proposed by the progression thread.

## Java now in the PR
`Rush` (rush.json), `Difficulty` + `AscendantData` (mode, wins, rites, best time; `/boss mode`, `/boss rush start|stop`), `AscendantAltarBlock`, `OghamStoneBlock`, `Ritual`. Built on JDK 25 / NeoForge 26.1.2.116. `/bossesselftest` on a flat server: 35 bosses, 1964 checks, 0 failed, including one full rite (feast, offering, hunt, wrong and right runes, hour window, defend, boss wakes).

## Not done
1. Rush has no run in the self test yet (needs a real player to fight); the rush controller compiles and loads `rush.json`.
2. Global Events and quest completion are data contracts (`diaithir_events.json`, `quests/ascendant.json`); the glue to those modules waits for their APIs.
3. **Biomes and materials**: designed here (Thornwake, Cinderfen, The Unwound; ores `hollowsteel`, `emberglass`, `slagiron`), built by the worldgen threads. Hidden, world-spread rite sites need that worldgen.
4. Attack types `gravity_well` and `rewind`.
5. Art: textures are recolours of vanilla skins; 64 px art goes to the texture lanes. Ogham stone and altar use vanilla block textures.
6. No client world-join test yet (needs the PC lane); no multiplayer balance pass.

## Regenerate
`python3 -I tools/gen_ascendant.py <vanilla skins dir>` (S0312 in Global-Repo). Additive: does not wipe `gen.py` output.
