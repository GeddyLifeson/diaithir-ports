# Simple Belts into diaithir-routing-networks: findings and plan

## Finding 1: nothing to port, a 26.1.2 NeoForge build already exists
The survey said "26.1.2 Fabric only". Wrong: Modrinth project kNthsBTM has `belts-neoforge-2.0.0-exp1.jar` (MC 26.1.2, NeoForge 26.1.2.77+, 2026-07-29). It needs Architectury API 20.0.9+ (the pack has `architectury-neoforge-20.1.17`) and optionally FTB Filter System. By the mod-first rule we use the latest build: the unchanged jar goes into the routing-networks fold as an input, with credit (see NOTICE.md). Duplicate check: no other belt mod is in PACK_CONSOLIDATION_AUDIT.csv (Create belts stay in Create).

## Finding 2: it has no lanes, so Foundry belts extend it
Read from source (`ChuteBlockEntity`, 459 lines, `BeltItem` 251 lines):
- A belt is a point-to-point spline between two Chute blocks, optionally through Support blocks. Not a tile conveyor.
- One queue (`Deque<BeltItem> movingItems`), each entry a whole stack (up to 64). No lanes, no side-loading.
- Two tiers only (`MAX_BELT_TIER = 2`, speed `0.9 * tier` blocks/s, extraction every `26 / tier` ticks).
- One filter item per Chute, optional FTB filters. No splitter, no underground (a spline already crosses gaps and terrain).
- Output goes to any `ItemApi` inventory in front of the end Chute; input comes from the inventory behind the start Chute.

## Update: fork, not unchanged jar
Lanes change `ChuteBlockEntity` internals, so the Foundry additions are a modified fork in `belts/` (upstream snapshot stays pristine in `upstream/`). Mod id `belts` and all existing ids are unchanged, so upstream worlds load. The fork's jar replaces the upstream jar as the routing-networks fold input (CC-BY changes marked here and in git history). Generated through the Claude API (Sonnet, cached source prefix); **uncompiled and untested**: the PC build thread compiles it and fixes errors first.
Written: two lanes + insertOnLane + 4 tiers (`ChuteBlockEntity`, `ChuteBeltRenderer`, tier 3/4 belt items), `foundry/` splitter, tunnel and loader blocks with data (blockstates, models, loot, recipes, lang). Still open: textures (placeholder cube models), tier-specific belt sprites (renderer still picks improved sprite for tier>=2), Fabric platform dropped, Codex/age gate/names.

## Design for the Foundry additions (clean-room, our own code in the `foundry` package, ids `foundry_*`)
1. **Lanes.** Add a `lane` byte (0 left, 1 right) to `BeltItem`, keep two `Deque`s with the same spacing rule, so each lane holds the same item density as today. Start Chute extraction alternates lanes (or fills the lane chosen by the loader's side). End Chute output drains lane 0 and lane 1 alternately. NBT key `lane` defaults to 0, so old worlds load.
2. **Tiers.** Raise `MAX_BELT_TIER` to 4 and read speeds from `foundry:belt_tiers` data: 7.5 / 15 / 22.5 / 30 items/s per FACTORIO_TECH.md, converted to blocks/s and extraction interval. Tier item and recipe per tier; stack size per belt item capped at 1 on lane belts so items/s means items.
3. **Splitter.** New block entity with one input, two outputs, per-side filter and input/output priority, reusing `ItemApi` and the Chute end/start handshake. Balancers are then layouts, shipped as Codex blueprints.
4. **Tunnel belt.** Spline already bridges gaps; the Tunnel is a pair of blocks that hide the span underground with a max gap per tier and no collision above.
5. **Loader.** Chest-to-belt and belt-to-chest block (Chute with a side lane selector), tiered.

None of this needs the original art. Rules layer (tier numbers, recipes, quests) is data and ships first.

## Port work (adopting the upstream jar, in order)
1. Fold `belts-neoforge-2.0.0-exp1.jar` unchanged into the routing-networks jar (fold job, not this PR).
2. Foundry source module `diaithir-routing-networks/` (moddev, NeoForge 26.1.2, Java 25) depending on `belts` + `architectury`. Cloud gradle with Architectury Loom (the upstream build) needs JDK 25; this container has JDK 21, so the compile and any in-game test go to a PC Remote Control thread (no visible windows).
3. Retexture at 64 px Industrial Factory: `chute`, `conveyor_support`, `conveyorbelt` (16x256 animation strip), `improved_conveyorbelt` (16 frames), plus new tier belts (red/blue/green-style palette of our own), splitter, tunnel, loader. Hand to the Textures thread with this list.
4. Age gate in `tech_ages.json`, Diaithiris names for all new items, Library and Codex entries, `foundry_` prefix on every new id.
5. Client world-join test with the full pack before any publish.

## Status
Done: licence read (CC-BY-4.0 confirmed), upstream source snapshot (code only), findings, design.
Not done: lane/tier/splitter/tunnel code, textures, build, tests.
