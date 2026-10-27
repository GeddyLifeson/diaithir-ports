# Create addon shim (NeoForge 1.21.1 addons -> diaithir_create port, MC 26.1.2)

Scripted, dependency-free (Python 3 only, constant-pool rewriting, no ASM).

1. `scan.py port.jar scan_out.json addon.jar...`  count com.simibubi.create refs, report classes/members missing in the port.
2. `build_map.py map.json runtime.jar... -- addon.jar...`  class map. Rules: com/simibubi/create -> com/zurrtum/create[/client],
   net/createmod/{catnip,ponder} and dev/engine_room/flywheel -> port-embedded copies under com/zurrtum/create/{catnip,client/ponder,client/flywheel},
   then unique simple-name match inside net/minecraft, net/neoforged/neoforge, com/mojang (the 1.21.1 -> 26.1 table).
3. `remap.py in.jar out.jar map.json`  rewrites class names in all Utf8 entries, mixin json, and neoforge.mods.toml (create -> diaithir_create, minecraft/neoforge/create version ranges -> "*").
4. `mcdiff.py out.json addon.jar... -- runtime.jar...`  residual table: classes/members that still do not resolve (second mapping table: MC/NeoForge/third-party).
5. `boot_one.sh srvdir jdk port.jar addon.jar`  boots a server with port + one addon, prints LOADS/FAILS + first errors (own port 25791).

Outputs committed: map.json, residual.json, scan_out.json, boot_results.txt.
Findings: package rename alone resolves about half of Create refs. Blockers: (a) Registrate (com.tterrag.registrate + CreateRegistrate, foundation.data.*) absent in port,
(b) ProcessingRecipe/StandardProcessingRecipe/AllTags/BlockEntityBehaviour API reshaped, (c) JEI/KubeJS/other addon deps, (d) Fabric transfer vs NeoForge capabilities, (e) client Flywheel/Ponder member signatures.
