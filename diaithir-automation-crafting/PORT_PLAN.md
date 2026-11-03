# Port plan: OpenComputers to 26.1.2 NeoForge

Base chosen: OC2R 1.21.1 (Java, NeoForge, MIT). The older OpenComputers 1.12.2 is Scala on Forge and would need a full rewrite, so it is not the base. The 1.21.1 to 26.1.2 jump is the smallest of the options.
Licence read (see NOTICE.md): MIT code, CC0 assets. Port allowed. No overlap rule: CC:Tweaked stays separate (Lua turtles); OC2R adds RISC-V Linux computers, robots, network cards and bus devices, which is a different thing.

Target module: `diaithir-automation-crafting` (programmable machine side of the Foundry set; robots are the route-unit layer for `diaithir-logistics-utilities`). Package `li.cil.oc2` is kept for credit; block and item ids keep the `oc2r:` namespace so nothing migrates.

## State
Step 0 (merged, #106): licence check, upstream snapshot under `upstream/oc2r-1.21.1/` (sources and non-binary resources only, not built), NOTICE.md.
Step 1: Sedna (156 classes), Ceres (22) and the Minux `Buildroot` loader are vendored as source under `libsrc/` (MIT, licences in `licences/`) and compile on NeoForge 26.1.2 with JDK 25 (`./gradlew compileJava processResources`, cloud, no PC needed). Minux guest images (Image, rootfs.cramfs, bootfs, firmware; Buildroot, GPL, sources https://github.com/North-Western-Development/minux) are not committed: `fetchMinux` downloads the OC2R 2.2.13 release from the Modrinth CDN, checks its SHA-512 and unpacks them into `build/minux` for the jar. Not yet run: a Sedna boot of the images.

## Steps
1. Done (see State).
2. Gradle project (NeoForge 26.1.2.116, like diaithir-wiretech), drop JEI, ProjectRed, Markdown Manual and Architectury dependencies (none are imported in java except 12 JEI and 3 ProjectRed files, which go behind optional integration or are removed).
3. Port the API breaks: data components, ItemStackTemplate results, GuiGraphicsExtractor, input events, capabilities, and the four mixins (`FrustumMixin`, `LevelRendererMixin`, `MinecraftMixin`, `ServerChunkCacheMixin`).
4. Retexture every block and item in 64 px Industrial Factory style (Textures thread), redo the screen background from vanilla panels.
5. Selftest `/oc2r selftest` in the cloud (server boot, computer boot to a shell, bus device, robot step), then client join on the PC thread.
6. Age gate, Codex entry, quest, 130-language names via the names pipeline, Bible check.

Compile builds that cannot run in the cloud go to a PC Remote Control thread, hidden windows only.
