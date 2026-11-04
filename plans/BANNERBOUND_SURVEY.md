# Bannerbound survey (Core + Antiquity)

Surveyed 2026-11-03 for claude/survey-bannerbound. Source: github.com/bannerbound/bannerbound (mirror of Nitsu430/Bannerbound; moved to codeberg.org/bannerbound/bannerbound-core on 2026-09-18). Not on Modrinth (search returns nothing). Core and Antiquity live in one repo (`Core/`, `Antiquity/`); NeoForge 21.1.230, Minecraft 1.21.1, Parchment, JEI/Jade/Ponder soft deps. No 26.1 build exists.

## Licence (read from the actual LICENSE file, not the repo description)
- The GitHub "About" line says MIT, but `LICENSE` at HEAD (a2840f9) is the "BANNERBOUND LICENSE": All Rights Reserved, no derivatives, no reuse of code or assets in any other project, modpack use only of unmodified official release files. It governs every version after 7 July 2026. This covers current Core AND Antiquity (one LICENSE for both).
- The same file states all commits up to and including `aac8526a1b7ae3a7385db9f0389885e3e3a2b074` (main) and `c82d845...` (code-cleanup) and their release files were MIT, irrevocably. aac8526 (2026-07-06) is in the clone; root `LICENSE` there is MIT (c) Nitsu; both `gradle.properties` say `mod_license=MIT`. `Antiquity/LICENSE` at that commit is a stray unmodified GPL-3 template text; the project grant is MIT, GPL-3 is compatible with our GPL module rule, so use the stricter of the two for credit and keep it in its own module.
- Verdict: FORKABLE at aac8526 (Core and Antiquity, code and assets, MIT, attribution required). NOT portable at HEAD or anything newer (ARR): later features (post 07-06 commits, ~225 files changed) get a clean-room spec only, never copied.
- Credit line: "Bannerbound by Nitsu (Nitsu430) and DracoFAAD, MIT at aac8526". Art: MIT covers assets at that commit, but the pack rule is ARR assets redrawn, so redraw anyway where cheap; keep MIT notice for anything copied.

## What it is (aac8526)
Core: 1,080 Java files incl. settlement (api/settlement, territory, walls, building, workshop), research/culture/faith trees as data loaders (`api/research`), city-states with war and economy, barbarians, crises, trade, codex, journal, social/thoughts, celestial sky, language, ruins, citizen entities. Antiquity: blocks, items, workshops (kiln, anvil, drying rack, carpentry, masonry), recipes, rope, poison, food, 48 research nodes, 15 culture policies, ages `ancient`/`classical`/`medieval` (`min_age`).

## Turn-based audit (Marlon rule: no turn-based)
Grep of `.java`/`.json` at aac8526 for turn-based/next-turn/end-turn/per-turn finds only a sprite "quarter turn" in DryingRackRenderer (rendering, unrelated). Research, culture, city-state, crisis and war code run on game ticks and the day cycle. Nothing to strip found; the port review must still check `CrisisManager`, `CityStateWarManager` and `CultureManager` timers for round or tick-batch semantics and keep them continuous.

## Fit with our modules
- Research/eras: our progression graph (diaithir-ages, source outside this repo) and Researcher in diaithir-automation-crafting. Bannerbound's `ResearchDefinition` nodes (cost, prerequisites, `unlocks.items/features/flags`, `min_age`) map to Researcher nodes with `recipe_unlocks`; the 48 Antiquity nodes can be folded as a data module the same way as aw2-research. Ages: map ancient/classical/medieval onto our Age I..III gates, gating per item (rule: per item and per entry).
- Settlements: diaithir-claims (Claim, Guard, MapScreen). Bannerbound territory/settlement is far larger (citizen NPCs, workshops, walls); fold in stages: territory + claim levels first, then citizens/jobs, then city-states and diplomacy.
- Duplicates (Create-style workshops, kiln, drying racks vs our existing crafting): merge with id migration, not parallel items.

## Port plan (1.21.1 to 26.1.2, staged)
1. Fork aac8526 into a new module `diaithir-claims/bannerbound/` (own source set, MIT notice, own mod id kept for credit), same pattern as automation-crafting's Researcher port.
2. Mechanical port: ModDevGradle/NeoForge 26.1 API changes (ResourceLocation to Identifier, data components, payload registrar); use /mnt/project-files/tools/compile_fix_loop.py.
3. Stage A data: research + culture JSON into our tree; Stage B settlement/territory; Stage C citizens and workshops; Stage D city-states, barbarians, crises (real-time only).
4. Everything shipped must appear in the Panscriptum and vice versa; no invented entries.
Not started: step 2 onward is a multi-day compile effort (about 3,000 files); this branch holds only the licence verdict and plan.
