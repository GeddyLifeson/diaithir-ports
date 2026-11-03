# Port plan: OpenComputers to 26.1.2 NeoForge

Base chosen: OC2R 1.21.1 (Java, NeoForge, MIT). The older OpenComputers 1.12.2 is Scala on Forge and would need a full rewrite, so it is not the base. The 1.21.1 to 26.1.2 jump is the smallest of the options.
Licence read (see NOTICE.md): MIT code, CC0 assets. Port allowed. No overlap rule: CC:Tweaked stays separate (Lua turtles); OC2R adds RISC-V Linux computers, robots, network cards and bus devices, which is a different thing.

Target module: `diaithir-automation-crafting` (programmable machine side of the Foundry set; robots are the route-unit layer for `diaithir-logistics-utilities`). Package `li.cil.oc2` is kept for credit; block and item ids keep the `oc2r:` namespace so nothing migrates.

## State
Step 1 done (cloud, 2026-11-03): `libs/ceres-0.0.4.jar` (Ceres e883ffe) and `libs/sedna-0.0.0.jar` (Sedna 8450eed) built from source with `tools/build_sedna.sh` (plain javac, release 17; both compile clean, 0 errors). Not yet run: the booted-VM test belongs to step 5. Sedna bundles only a 48 KB riscv resource folder, so Minux guest images are a separate open question.
Step 0 (earlier): licence check, upstream snapshot under `upstream/oc2r-1.21.1/` (sources and non-binary resources only, not built), NOTICE.md.

## Steps
1. Sedna, Ceres and the Minux images: Sedna and Ceres are on GitHub Packages (needs auth), so pull the sources, build them with JDK 25 and include them in the module (jar-in-jar or source). Check the Minux image licence and size.
2. Gradle project (NeoForge 26.1.2.116, like diaithir-wiretech), drop JEI, ProjectRed, Markdown Manual and Architectury dependencies (none are imported in java except 12 JEI and 3 ProjectRed files, which go behind optional integration or are removed).
3. Port the API breaks: data components, ItemStackTemplate results, GuiGraphicsExtractor, input events, capabilities, and the four mixins (`FrustumMixin`, `LevelRendererMixin`, `MinecraftMixin`, `ServerChunkCacheMixin`).
4. Retexture every block and item in 64 px Industrial Factory style (Textures thread), redo the screen background from vanilla panels.
5. Selftest `/oc2r selftest` in the cloud (server boot, computer boot to a shell, bus device, robot step), then client join on the PC thread.
6. Age gate, Codex entry, quest, 130-language names via the names pipeline, Bible check.

Compile builds that cannot run in the cloud go to a PC Remote Control thread, hidden windows only.
