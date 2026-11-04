# aw2-research (GPL-3.0 module)

Own module inside the diaithir-automation-crafting jar's source tree (data only, not shipped in the Researcher jar: a datapack for the pack's `datapacks/`).
The research tree of **Ancient Warfare 2** (shadowmage45 / P3pp3rF1y fork, Minecraft 1.12.2, **GPL-3.0**, https://github.com/P3pp3rF1y/AncientWarfare2 at 446b7db) rewritten as Researcher nodes.
Licence text: `LICENSE-AncientWarfare2-GPL-3.0.txt`. This directory is GPL-3.0-or-later; the rest of the jar keeps its own licences.

- 30 goals, 200 s each in AW2, dependency edges copied as-is (`aw2_research_costs.json` keeps time, dependencies and the resource list).
- `datapack/aw2_research/data/aw2research/research/*.json`: one Researcher node per goal. AW2's bench consumed a resource set while researching; the Researcher node unlocks by holding the first mappable resource at its count (a placeholder trigger until the research bench consumes the full list from the cost table).
- AW2 ore-dictionary names map to 26.1 items or `#minecraft:` tags (see `tools/gen_aw2_research.py`). Steel, gear, shaft and bearing entries have no pack item yet and stay `null` in the cost table (no invented items).
- No turn-based mechanics are involved: AW2 research is a real-time timed goal.
- Not ported: the Research Book / Notes items, the GUI and JEI categories (Researcher's own screen is used). Textures are not copied.
- Not boot-tested yet (data only; run the Researcher server with the datapack and check `researcher` logs).
Regenerate: `python3 -I tools/gen_aw2_research.py <AW2 registry/research dir> .`
