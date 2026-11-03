# Diaithir Bosses (MIT)

Clean-room boss framework, bosses and champion elites for Diaithir (NeoForge 26.1.2). Behaviour of boss mods was studied; no code, names or art are used.

- Framework (`BossController`): boss bar, phases with shield windows, telegraphed attacks (slam, charge, leap, bolt, summon, lightning, cloud, wave, heal, roar), arena lock-in and leash, Age scaling and player-count scaling, per-hit damage cap, Age-gated loot, no-despawn, altar respawn rules (cooldown, once). State survives reloads in scoreboard tags.
- Data: `data/diaithirbosses/boss/<id>.json` (one profile per boss, reloadable by datapack; a profile may name another mod's `entity` and the kit attaches to it), `elites.json`.
- Seven own bosses: Tribal Chief (I), Stone Colossus (II), Bronze War-King (III), Plague Swarm Lord (III), Iron Walker (IV), Steel Marshal (V), Ceramic Warlord (VII). A raider admiral, storm elemental and forge giant were dropped for overlapping the myth creature list (drowned captain, dust storm king, furnace-heart golem). Names come from tools/archaic.py via gen.py.
- 19 mythic-court kits keyed by `creature_id` (bosses.csv). The four creatures diaithir_myth spawns (`toimsechunleomund`, `natuirkriegach`, `natuirmoir`, `tarbiumachuoirtach`) get the kit attached to that entity; without that mod they spawn our own body. The other 15 spawn as our entities (vanilla model, generated texture).
- Champions: `Kuraid` plus 1-3 affixes on creeper, zombie, skeleton and others; chance and count rise with the Age.
- Arenas: jigsaw structures per own boss with a `boss_altar` block (Age gate and rest rule live there). Commands: `/boss spawn|spawnall|list|age|champion`, `/bossesselftest`.
- `tools/gen.py <vanilla textures dir> <bosses.csv>` regenerates all data, textures, lang, structures, `age_gates.json`, codex. Library prose comes from the compute job `diaithir-bosses_lore`.

## Planet bosses (1,767 on 216 planets)
Every Panscriptum planet has 6 to 12 bosses (more for bigger planets, one per Age tier first so the roster spans the planet's Ages). Each is a real Library entry of that planet (BOSS entries first, then MOB, NPC, then any being), named by the entry; the body plan is picked from the entry's own first sentences (dragon, serpent, hydra, golem, spirit, treant, quadruped, humanoid kinds by realm and so on, 21 plans), else by the planet's realm.
- One shared entity (`PlanetBoss`, four hitbox classes) whose synced look id selects the boss. The client builds that boss's own model (cuboid parts, per-boss proportions, horns, wings, tails, props, extra limbs) from `assets/diaithirbosses/looks/<id>.json` and draws its own texture `textures/entity/planet/<id>.png` (box-unwrapped sheet, palette from the planet's soil and the entry's words, one of 8 patterns, eyes). No two bosses share a model-and-texture signature.
- Stats and attacks come from a per-body-plan template (`planet_templates.json`) scaled by the gate Age (`opens_age + tier - 1`, max 8). Profiles are expanded from `planet_bosses.json` at startup; `/boss spawn <id>` works with the lower-case entry id.
- `rite_bosses.json` maps each rite id (druidry `rite.v1`) to the boss it wakes, on the same planet and Age.
- `tools/gen_planet_bosses.py` regenerates all of it from planets.tsv, the content and library databases and the rites list; `tools/planet_bosses_report.md` has the counts.
