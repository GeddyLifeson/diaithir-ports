# Batch 4 report (pack build and licences), 2026-11-19

Branch: `claude/project-thread-avvvun`, based on PR #364 head (`claude/project-thread-htoezl`). Everything here is UNTESTED: no server or client was started.

## Fixed in this branch
- **C1/B3, Terralith (fold rule):** stripped from `diaithir-realms-suite` with `strip_module.py` (Tectonic kept); original linked as `mods/terralith.pw.toml` 2.6.2. The Aether II, Undergarden, Backwoods, Oritech, Regions Unexplored, Relics, Apotheosis and Immersive Vehicles strips were already in #364's head (PR text says they were not; the head is right).
- **B15:** `check_licenses.py` now fails on any hosted jar that declares a modId in `tools/integrity/restricted_modids.json` and on any ARR/unknown `LICENSE-FIELD.txt`. It passes on this branch (it would have failed on Terralith).
- **B6/C2:** all 18 metafile URLs that pointed at `claude/pack-v1-3*` branches now point at `claude/pack-v1-3-wave17` (file exists and sha256 matches for each; `tools/integrity/repoint_pack_urls.py`). `Diaithir.zip` instance launches wave17's `pack.toml`; README import link fixed. Release branch rule: wave17 must not be deleted or rewritten.
- **B14:** `diaithirmigrate` class, mods.toml block and licence note removed from `diaithir-rewrites`; `diaithir-migrate/` source folder deleted (still in git history).
- **B16:** four stale hosted-jar rows removed; README size "about 1.3 GB"; server address removed from the public README. The other non-hosted rows are provenance for folded mods and are now labelled as such.
- **B5:** CC-BY-NC-SA section added to LICENSES.md (Aerial Hell, Stellaris, Blooming Biosphere alias). Upstream author names still need filling from each Modrinth page (UNVERIFIED).
- Index and `pack.toml` hash refreshed; `check_pack.py` ran with no errors.

## Findings left for a decision or a boot test
- **B10:** no jar or metafile provides `bundle-api` (required by runes, archers) or `critical-strike` (required by skill-tree; added only by #361). Check the server log on this commit for "missing" lines before adding anything.
- **B13:** 28 CurseForge metafiles use `edge.forgecdn.net`; 22 have no `[update]` block (ftb-*, sophisticated-*, productive*, camol, cupboard, logistics-network, puffish-skills, silent-gear-metalworks, utilitarian). Moving them to packwiz CurseForge mode needs the API key mode and author permission, so it is not changed here. 136 of 186 metafiles have no `[update]` section.
- **B11:** wrong-version or alpha files (ruins-and-towers 1.21.6-1.21.8, towns-and-towers 1.21.1, NotEnoughTrials 1.21.11, Structory 26.2, rhino 1.21.1 line) need a boot log showing their structures load; not changed.
- **B7:** Megamachines and ships (`diaithir-ships` 2.4.12-diaithir.18 on `diaithircompat`) are not in the pack. Needs your scope call: add the jar and boot-test, or list as V1.1 in the checklist. Recommendation: V1.1.
- **B8/B9:** the fold and patch tools are still split across #361, #364 and DiaithirRPG; merge order #364, D&T fix, redo #361 on top (it commits a `.pyc`), then d11 options, then #360. No jar was rebuilt from a manifest.
- **C2/C5/C14/C15:** these concern DiaithirRPG `engine` inputs. Not changed in this PR (see the DiaithirRPG note in the thread reply).
- **Oritech:** upstream licence disagrees (jar says ARR, Modrinth says CC-BY-4.0); it is linked as the original, so no repack risk.
