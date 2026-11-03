# diaithir-worldgen-structures
Data-only NeoForge 26.1.2 module. Clean-room: 197 original (186 + 5 Ascendant rite sites via tools/gen_rite_sites.py) structures designed from public behaviour descriptions of 40 all-rights-reserved or unknown-licence structure mods; no source code, templates or assets used.
- `tools/gen.py` turns `blueprints/*.json` (block-grid designs) plus `tools/specs.json` into `src/main/resources` (templates, jigsaw structures, structure sets, loot tables).
- Integration (INTEGRATION.md): biome tags use vanilla/`c:` tags, per-Age structure tags (`age_i`..), `diaithir_events.json` (calendar year, disaster, war, raid; ruin states), Codex entries and a quest per structure, `migration.json` maps old mod prefixes to new ids.
- TODO: upgrade rules (no working blocks here), cross-mod recipes (none), stronger biome tag coverage, client world-join and locate test.
- Build jar: `cd src/main/resources && zip -r ../../../diaithir-worldgen-structures-1.0.0.jar .`
