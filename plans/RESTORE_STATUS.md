# Restore originals status (11-05 ~17:40Z)
Done (pushed, fb1fc423): 108 of 111 originals as mods/*.pw.toml (90 Modrinth sha512, 18 CurseForge sha256). Index refreshed, check_pack 0 errors.
Unresolved originals: sgearmetalworks-26.1.2-1.6.0.jar, logisticsnetworks-26.1.2-1.12.0.jar (not found by name on Modrinth or cfwidget under guessed slugs), Immersive_Vehicles-26.1-24.0.0-diaithir.2.jar (our build; in /mnt/project-files, add as local jar).
Not done: strip the 14 clean-room rewrites (WAVE17_CLEANROOM_SWAP.tsv) from the suite jars and add their originals (BoP, FTB Chunks, FTB Ultimine are CF-only, ids not yet resolved); the 37 open folds. patch_module.py (named in the handoff) is not on any DiaithirRPG branch checked (default, fold1, fold2); tools/distribution/patch_jars.py exists.
Scratch resolver output: not saved (scratchpad).

## B3 findings (11-05 17:45Z)
patch_jars.py only patches SecurityCraft/SFM/Trash Cans; it cannot strip modules. The folded modules are merged class and resource content, not nested jars, so removal means deleting namespaces, packages, [[mods]] blocks and mixin/service entries from the suite jars.
Where they live: rewrites jar holds incendium, nether_descent, netherex, nullscape, echoes_of_the_end, ftbchunks (+ diaithir_end/diaithir_nether glue that depends on them); worldgen-suite holds biomesoplenty, natures_spirit, natures_compass, yungscavebiomes (data/assets + com/diaithir/overworld_biomes/realms) and diaithir_overworld_biomes.
Stripping blind risks breaking diaithir_end/diaithir_nether and the biome glue, and the cloud cannot verify without a full server boot. Do it by rebuilding the suites from the DiaithirRPG module sources with those 14 modules removed, then boot-test. Needs the source module list (the old thread's patch_module.py / build files, possibly on the PC under E:\DiaithirRPG).
