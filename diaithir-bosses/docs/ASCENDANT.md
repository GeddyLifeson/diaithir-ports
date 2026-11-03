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
| coop | Tier II and III: 2 or 3 fighters must stand at the altar together. **Solo path:** give the altar a champion token for each absent fighter and a rite spirit (a player-made iron golem) rises to stand in; it also helps in the defend. |
| defend | Hold the altar for 60 to 150 s against waves that scale with fighters present; leave it alone and the step resets. |
Tier I rites have 6 steps, tier II 7, tier III 7 to 8. Each rite ends in the boss waking at the altar. Progress is saved per altar. Tiers II and III still need the lower tier beaten once.
Not copied from anything: the steps, riddles, trees and items are ours, built from Celtic lore (ogham, the tree calendar) and the Age clock.

## Materials and the Shardfall Gate (plans/PROGRESSION.md)
What bosses give, by item (ids as the progression graph names them):
| Item | From | Graph node it feeds |
|---|---|---|
| `boss_catalyst` | tier I bosses | research pack 5, tier 3 modules (`combat.bosses`) |
| `boss_hollow_core` | every Ascendant boss | fusion core, pack 6 (`combat.asc1`) |
| `boss_catalyst_t2` | tier I and II bosses | the rush, legendary modules |
| `boss_sovereign_core` | Foundry Sovereign | Last Wheel summon (`combat.asc3`) |
| `ageshard` | The Last Wheel | shard resonator, pack 7, shard focus, the shard land (`research.pack_7`, `tech.shard_resonator`, `magic.shard_focus`, `explore.shard_land`) |
| rush rewards | the rush | champion tokens, netherite, a nether star, `rush_laurel`; `AscendantData` marks `ascendant_rush` cleared (graph resource `rush.cleared`, needed by `win.gate` and by legendary modules) |
The Gate (`win.gate`) needs `ageshard`-made items, `rush.cleared` and `module.legendary` among 16 inputs. Not built: the Gate itself, its rite and ceremony (design task for Codex and campaign threads). The boss side that the Gate reads is `AscendantData.beaten(id)` and `RushClearedEvent`.
Offering feast comes from the kitchen module through the tag `#diaithirbosses:offering_feast`.

## Java now in the PR
`Rush` (rush.json), `Difficulty` + `AscendantData` (mode, wins, rites, best time; `/boss mode`, `/boss rush start|stop`), `AscendantAltarBlock`, `OghamStoneBlock`, `Ritual`. Built on JDK 25 / NeoForge 26.1.2.116. `/bossesselftest` on a flat server: 35 bosses, 1964 checks, 0 failed, including one full rite (feast, offering, hunt, wrong and right runes, hour window, defend, boss wakes).

## Also done
- Attack types `gravity_well` (Dusk Wyrm: drags fighters in range to the boss and hurts them) and `rewind` (The Last Wheel: puts fighters back where they stood about five seconds ago, from a per-fighter one-second trail). Both run in the self test.
- `/bossesrushtest`: the whole rush (6 base then 9 Ascendant bosses) with a fake fighter on a headless server, real server ticks: killed 15/15, cleared flag set, best time stored, rewards given. A fake player stands in for a bot client; a real client is still the PC lane.

## Not done
2. Global Events: wired by name to #111 (`EventHooks`) and run on a booted server with the #111 jar: `/worldevents start ascendant_eclipse` works (registered through `WorldEvents.register`) and lifts Dusk Wyrm health x1.1; a started blood moon halves altar rest times (`/boss events` shows the active effects); storms sharpen Tide Reaver and Ash Seraph; `BossDefeatedEvent` and `RushClearedEvent` go out on the NeoForge bus. Note: #111's `start` posts `WorldEventStartedEvent` even when the event refuses to run (no player in the overworld), so the blood-moon hook also fires then. Quest completion stays a data contract until the quest module lands.
3. **Biomes and materials**: designed here (Thornwake, Cinderfen, The Unwound; ores `hollowsteel`, `emberglass`, `slagiron`), built by the worldgen threads. Hidden, world-spread rite sites need that worldgen.
5. Art: all 35 boss skins, items and the three blocks are the Textures lane's own art.
6. No client world-join test yet (needs the PC lane); no multiplayer balance pass.

## Regenerate
`python3 -I tools/gen_ascendant.py <vanilla skins dir>` (S0312 in Global-Repo). Additive: does not wipe `gen.py` output.
