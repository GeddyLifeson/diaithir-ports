# diaithir-automation-crafting

Source tree for the consolidated automation and crafting module (FACTORIO_TECH.md section 1). First tenant: **Researcher**, the Foundry research bench.

## Researcher port
- Upstream: Tr100000/Researcher, tag `0.3.4+26.1.2` (Fabric), MIT. Licence text in `LICENSE-Researcher`.
- Support code from Tr100000/TrUtils (`26.1.x`, MIT), only the pieces Researcher uses, under `io.github.tr100000.trutils`. Licence text in `LICENSE-TrUtils`.
- Package `io.github.tr100000.researcher`, mod id and data namespace `researcher` are kept for credit; ids never change.
- Changes from upstream: Fabric API replaced by NeoForge events, payload registrar, `RegisterGuiLayersEvent`, `OnDatapackSyncEvent` recipe sync; fzzy_config replaced by `ModConfigSpec` (server options are COMMON so they are readable during datapack load); classtweaker replaced by `accesstransformer.cfg` and `interfaces.json`; JEI/REI/RRV compat, codec2schema and datagen API dropped; GUI art redrawn.
- Foundry/Age tie-in data lives under `data/researcher/research/` (see below).

## Art and audio
All GUI art (`textures/gui/*`, `textures/research/default.png`, `icon.png`) and the toast chime are our own, drawn by `tools/gen_researcher_art.py` and `tools/gen_research_chime.sh` in the Industrial Factory look (steel plates, rust orange, hazard yellow). No upstream or Factorio art ships. Research item icons are vanilla or pack item textures resolved at runtime.

## Testing
`testpack/` is a smoke-test datapack (one research, vanilla recipe unlock). It is NOT shipped in the jar. Run: copy it to `run/world/datapacks/`, start `./gradlew runServer`, and for a client `./gradlew runClient -PjoinServer=localhost:25598`. Press R in game to open the research screen.
Researches that lock vanilla recipes must never ship in this jar: the real tree (Foundry and Age nodes) is authored as data in the Foundry thread.

## Known follow-ups
- Foundry/Age tie-in data (research nodes, science-pack items) is separate content, not part of the port.
- Large research payloads: upstream used a "large" payload; NeoForge's play packet cap applies (8 MB), plenty for trees of a few thousand nodes.
- Datapack sync requests all crafting recipes so unlock tooltips can resolve them (same as upstream's synchronised serializers).
- Config: client options in `researcher-client.toml`, server options in `researcher-common.toml`.

---

# diaithir-automation-crafting: Package Mod port

Port of **The Package Mod** by whizzball1 (CurseForge 293833, https://github.com/whizzball1/The-Package-Mod, Minecraft 1.12.2, **LGPL-3.0**) to NeoForge 26.1.2.
Licence text: `licences/The-Package-Mod-LGPL-3.0.txt`. Source is a rewrite for the 26.1 APIs that keeps the original's design and the names `CraftingPackage`, Packager, Unpackager, Researcher. This module is LGPL-3.0-or-later; credit stays in `neoforge.mods.toml`, this file and `CREDITS`.
No original textures, models or sounds are used. All art is drawn by `tools/gen_assets.py` in our 64 px Industrial Factory style (16 px base textures, upscaled by the texture lane).

## What it does
Research then bulk crafting, Factorio lab and assembler style.
- **Researcher**: needs the research items and prerequisite package crates, then the package is researched for the whole server (shared like a Factorio team).
- **Packager**: pick a researched package with the **Package Reader**, feed it the inputs, it makes a crate.
- **Unpackager**: a crate goes in, the result (e.g. 256 torches) comes out.
- No GUI, as in the original: hoppers, pipes and belts feed and empty them (item capability); by hand, use items on a bench, use it empty-handed to take output, sneak to read status.
- Packages are datapack files `data/<ns>/package_defs/*.json` (replaces packageadder.json and CraftTweaker). Two samples ship. Commands: `/diaithirpackages list|check|setresearched`.

## Changes from the original
Shared server research instead of per-player; datapack JSON instead of config and CraftTweaker; no meta/damage values (item ids only); transactions-safe item handler.

## Plan fit (FACTORIO_TECH.md section 1)
Lives in `diaithir-automation-crafting` with ids prefixed `pkgmod_`. Pack items as Age tech node costs (the Research bench of FACTORIO_TECH) are the Researcher's research items; the Ages tree hook and a merge with the Researcher port (one bench, not two) are follow-ups.

## Status
Pure logic (`PackageDef`, `Tally`) compiled and run in the cloud. The NeoForge side is written against the same 26.1.2 API patterns as diaithir-storagenet, brewing and conduits but is **not compiled yet** (cloud has no JDK 25): compile, `PackageLogicTest`, and a spawn-world test are queued for the PC.
