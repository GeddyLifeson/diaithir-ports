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
