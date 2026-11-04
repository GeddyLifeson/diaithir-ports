# diaithir-trials-overhaul

Integration layer for **Not Enough Trials** (Danmatisk, 6.0, Modrinth `1W5PhsYU`), the trial chamber overhaul (659 data files, no code). Upstream already runs on 26.1.2 NeoForge, so mod-first step 1 applies: use the latest build as is.

## Licence note (needs a ruling)
Modrinth lists the project as MIT, but the jar's own `mods.toml` and `fabric.mod.json` say "All Rights Reserved", and no public source repository was found. Until the author's terms are confirmed, the upstream jar is **not copied into this repo**: the pack points at the author's CDN file (`sources.json` entry, third-party rule). This module holds only our own data and is MIT.

## What it adds
- `age_gates.json`: namespace `notenoughtrials` at Age IV, heavy core at Age V (judgment, change in one place).
- `codex/trials_handbook.json`: chambers and the four rare enchantments, text taken from the upstream lang file.
- `codex/trials_quests.json`: three quests (first chamber, boss chamber, ominous vault), vanilla items only.
- Ids: nothing renamed, so no `id_migrations.json`.
- `tools/gen.py JAR` regenerates the three files from the real lang file.

## Not done
Diaithiris names (130-language pipeline), Global Events hooks (upstream fires none), upgrade rules (no working blocks), `sources.json` and `pack.json` entries in DiaithirRPG, client join.
