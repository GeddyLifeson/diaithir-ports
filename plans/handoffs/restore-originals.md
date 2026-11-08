# Handoff: restore-originals (11-08, context 298k/500k, over the 125k limit)
Branch: diaithir-ports claude/pack-v1-3-wave17 (head e8bdf96b+). Tools: python3 -I tools/integrity/refresh_index.py . and check_pack.py . (0 errors). Push after every change; the PC classifier needs Marlon's Allow for PC pushes, so do edits in the cloud clone.
## Done
- 111 originals restored: 110 as mods/*.pw.toml (Modrinth sha512 / CurseForge sha256), Immersive Vehicles diaithir.2 as local jar.
- 13 clean-room swap originals added as downloads (BoP, natures_spirit, natures_compass, yungs_cave_biomes, incendium, netherex, nullscape, nether_descent, echoes_of_the_end, forbidden_arcanus, runes, cristellib, ftb-chunks, ftb-ultimine). Earlywindow is our own jar.
- Suites made disjoint from standalones: mts, betteradvancedtooltips, silentgear files, bundleapi stripped from suites. Non-vanilla entries in minecraft/c tags made optional. Missing BoP/NS ids remapped. FTB set bumped (library .12, xmod-compat .6, quests .10...). T&T grass override in kubejs/data/kaisyn. Pack booted "Done (88.7s)" at cf2683a9 per Verify.
## Not done
1. The 37 open-licence folds: plans/WAVE17_OPEN_FOLDS.tsv (stem, licence, route, target module). Per mod: read licence from its own page, fold into a suite module with id migration, smallest first. Verify each fold boot-tests (Verify thread boots via C:\MCTestServer on the PC).
2. Four of the 14 had no alias block in the suites (earlywindow, forbidden_arcanus, ftb-ultimine, runes); /ftbchunks alias in Commands.class may clash with the original ftb-chunks.
3. Equivox wants mekanism <=10.7.8 (pack has 10.8.0); possibly-missing deps listed in thread (productivelib, endercore, ae2wtlib_api, whitenoise...), only if FML complains.
## Scripts (scratch, not saved): resolve (Modrinth by filename), cf.py (cfwidget + resolve_cf.py in DiaithirRPG tools/distribution), strip.py (path-overlap strip), remap.py, tags.py. Rewrite them from the approach above if needed.
