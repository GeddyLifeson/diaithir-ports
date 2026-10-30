# Diaithir Bosses (MIT)

Clean-room boss framework, bosses and champion elites for Diaithir (NeoForge 26.1.2). Behaviour of boss mods was studied; no code, names or art are used.

- Framework (`BossController`): boss bar, phases with shield windows, telegraphed attacks (slam, charge, leap, bolt, summon, lightning, cloud, wave, heal, roar), arena lock-in and leash, Age scaling and player-count scaling, per-hit damage cap, Age-gated loot, no-despawn, altar respawn rules (cooldown, once). State survives reloads in scoreboard tags.
- Data: `data/diaithirbosses/boss/<id>.json` (one profile per boss, reloadable by datapack; a profile may name another mod's `entity` and the kit attaches to it), `elites.json`.
- Seven own bosses: Tribal Chief (I), Stone Colossus (II), Bronze War-King (III), Plague Swarm Lord (III), Iron Walker (IV), Steel Marshal (V), Ceramic Warlord (VII). A raider admiral, storm elemental and forge giant were dropped for overlapping the myth creature list (drowned captain, dust storm king, furnace-heart golem). Names come from tools/archaic.py via gen.py.
- 19 mythic-court kits keyed by `creature_id` (bosses.csv). The four creatures diaithir_myth spawns (`toimsechunleomund`, `natuirkriegach`, `natuirmoir`, `tarbiumachuoirtach`) get the kit attached to that entity; without that mod they spawn our own body. The other 15 spawn as our entities (vanilla model, generated texture).
- Champions: `Kuraid` plus 1-3 affixes on creeper, zombie, skeleton and others; chance and count rise with the Age.
- Arenas: jigsaw structures per own boss with a `boss_altar` block (Age gate and rest rule live there). Commands: `/boss spawn|spawnall|list|age|champion`, `/bossesselftest`.
- `tools/gen.py <vanilla textures dir> <bosses.csv>` regenerates all data, textures, lang, structures, `age_gates.json`, codex. Library prose comes from the compute job `diaithir-bosses_lore`.
