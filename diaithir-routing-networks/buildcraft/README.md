# diaithir-routing-networks / buildcraft

Vendored BuildCraft Community Edition 8.0.26 for MC 26.1.2 NeoForge. Pipes, engines, quarry, builders, robotics belong to Foundry's routing-networks (pipes) and miners (quarry) parts; the quarry/builders split to diaithir-miners happens when that module is created.

Build (PC thread, hidden windows only): `python scripts/materialize_project.py 26.1.2-neoforge --output <dir>` then `gradlew build` in the output, JDK 25. The cloud session could not run this (external-code check), so the build is untested here.

Status: source vendored unmodified. Open: compile + join test on the PC, 1025-texture retexture (TEXTURE_MANIFEST.csv), Diaithiris names, age gates, Codex entries/quests.

## Integration data (INTEGRATION.md)
`diaithir-data/` holds our data layer, kept apart from upstream files: `c:gears/*` tags, a draft upgrade rule for the machines, draft Age gates, a Codex entry and a first quest. Draft means schemas follow UPGRADES.md text; confirm against #117 and the tech-ages file.
Energy: upstream default is 1 MJ = 10 FE, so with 1 FE = 1 J, 1 MJ = 10 J (Marlon OK). Tooltip J/W check on the PC. #117 has the Resolver/Effects/Target core but no UpgradeHost capability yet, so the quarry, pump and engine hooks wait for its NeoForge glue PR.
