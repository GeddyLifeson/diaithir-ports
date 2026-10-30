# Diaithir Relics (Iarmadun)

MIT. Clean-room replacement for the all-rights-reserved unique-gear mods (Simply Swords, Epic Knights weapons, Relics RPG, Lost Relics, Reliquary-style gear): behaviour studied, no code, art or names copied.

26 relics for Ages III to VII: swords, spears, axes, a dagger, hammers, shields, rings, torques, charms and belts. Weapons and shields are held; trinkets sit in Curios `ring`, `necklace`, `charm` and `belt` slots (tags in `data/curios/tags/item`, slot assignment in `data/curios/curios/entities`) and also work from the off hand without Curios.

- Data: `data/diaithirrelics/relics.json` (written by `tools/gen.py`). Numbers live in item components (attributes, weapon, blocks_attacks, use_cooldown); powers are in `Abilities` (active, on use, sneak+use for spears), `Passives` and `RelicEvents` (small event handler). Particles and sounds are vanilla.
- Sources: 12 are crafted (a tier metal and a catalyst), 14 come from structure chests through `neoforge:add_table` loot modifiers (`data/diaithirrelics/loot_modifiers`, tables `chests/age_*`; vanilla chests and DungeonCrawl). Nemesis trophy hook: point a drop at `diaithirrelics:chests/age_iii` ... `age_vii`.
- Ages: `age_gates.json`. Names: `names_map.json` (Diaithiris from the lexicon). Handbook: `codex/relics_handbook.json`. Library: `library/relics_mechanics.json` (numbers); the lore lines come from the batch job `compute/jobs/relics_lore`, merged into `library-sync/relics.json`, and `tools/lore.json` feeds tooltips on the next `tools/gen.py` run.
- `/relicsselftest` grants and triggers every power with a fake player; see TEST.md.
