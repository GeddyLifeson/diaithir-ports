# Batch 4 follow-up (B7, B10, B11, B13), 2026-11-19

Everything here is UNTESTED: no server or client was started.

## B10 providers (done)
- `bundle-api` 3.0.0+26.1.2 (MIT, release) and `critical-strike` 1.0.6+26.1.2 (GPL-3.0-only, release), both linked as originals with sha256 and an `[update.modrinth]` block. bundle-api needs `cloth_config`, `curios`, `spell_power` (all in the pack) and `player-animation-library` (a playeranim class tree is present in the adventure and platform suites; modId not confirmed).
- `critical-strike` was in #361 as well; this branch adds it too. If #361 merges, keep one metafile.

## B11 wrong-version jars (checked, nothing swapped)
Modrinth now lists 26.1.2 for every flagged file except two client-only ones, so the 11-09 metadata was stale:
- ruins-and-towers 0.3, towns-and-towers 1.13.11, NotEnoughTrials v6.0, Structory 1.3.7, Structory Towers 1.0.17, grieflogger 20.1.4, PWIC 0.3: list 26.1.2 (ruins-and-towers and PWIC are beta; ruins-and-towers, PWIC are ARR and stay linked originals, untouched).
- rhino 2101.2.8 is beta on 26.1.2; release 2101.2.7-build.81 also lists 26.1.2. Not swapped: KubeJS fork behaviour is untested either way.
- flickerfix 7.0.0 (26.1 only) and yeetus-experimentus (26.1, 26.2) list no 26.1.2: client-only, left in place; confirm they load in the first client join.
Still needs a log line per mod at the first boot; metadata is not proof.

## B13 CurseForge links
`[update.curseforge]` (project-id and file-id, file-id verified against the CurseForge file list) added to 20 of 22 metafiles. Not done: `ftb-library` and `sophisticated-core` (project id not found through cfwidget). All 22 filenames carry a NeoForge or 26.1 marker, so the loader question is settled by name; boot log still confirms.
The CDN download URLs are unchanged on purpose: switching to CurseForge API mode needs an API key and author permission.

## B7 Megamachines and ships
Moved to V1.1 (review recommendation). Source stays on `diaithircompat` (`diaithir-ships` 2.4.12-diaithir.18). V1 checklist items for ships and megamachines should be marked V1.1.
