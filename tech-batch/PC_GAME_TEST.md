# PC game test (Diaithir pack, review-2 branches)

Pack state: DiaithirRPG branch `claude/review-pack-2` (from claude/minevegas-casinos), pack repo branch `claude/review-ports-2` (from claude/project-thread-ysjkxr). Jars changed since the last merge, with sha256:
- Tellus `Tellus-neoforge-26.1.2-0.8.3-diaithir.16-fantasy.8.jar` `038907a947c37a9e616387963009937027d8bde555aea75394103def37e3307a` (PR 156). Only one Tellus jar is in the pack.
- diaithir-bestiary 0.3.2 `1beee3c7987eb9e0b78deb5023dd34c39b52c909bbe6c18b54b45bf59f0dff12`
- create diaithir.8 `9b1528b264c946a89a41a2a363def8f821cdc196d7e3435c04ebd9f6c0e3f457`
- diaithir-bridges 1.1.2 `0f888867f425e4164ee86329115dd6da1dc26ccb7845a3c8ef4b39590526a94b`
- diaithir-perf 0.3.1 `06a6e19a9f535a30eb006da3b8664f969d08007c09bdc25d7b732c52ee3311ec` (source: diaithir-ports branch `claude/perf-lightmans-untrack`)
Content: earth_timeline throttled heads #141 220db6a6b2 and #151 72e4eec20e (with the `/et_flag` Boolean fix), bridge-tiers #143 365cab16a5, 24 fantasy biome names in kubejs assets (#146), DH ignoredDimensionCsv with 207 ids, `config/c2me.toml` memory defaults, Prism max heap 12288 MB.

## 1. Get the build into Prism
1. Pack URL (public, once the branch is pushed): `https://raw.githubusercontent.com/GeddyLifeson/diaithir-ports/refs/heads/claude/review-ports-2/pack.toml`. Client metafile URLs point at `claude/project-thread-ysjkxr`, which now holds the merged PR 16 files, so no local server is needed for those.
2. Create a NeoForge 26.1.2 (26.1.2.116) instance, set Pre-launch command to `"$INST_JAVA" -jar packwiz-installer-bootstrap.jar <pack.toml URL>` (bootstrap jar from packwiz-installer-bootstrap releases in the instance `.minecraft`), launch once. Or import `Diaithir.zip` from the branch (its pack_url is the ysjkxr branch, so edit the pre-launch URL to the line above while review-ports-2 is unmerged).
3. Heap: the pack now sets max 12288 MB (min 2048) with the G1 flags `-XX:+UseCompactObjectHeaders -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:G1HeapRegionSize=16M`. An existing instance keeps its old memory setting: set it by hand. For a 16 GB PC use 8192.
4. An existing instance keeps old config files. For this build delete or replace `config/DistantHorizons.toml` and `config/c2me.toml`, or copy the two values: `generatorPlan = "CHUNKS_ONLY"`, the `ignoredDimensionCsv` list, and in c2me `globalExecutorParallelism = 6`, `chunkDataCacheSoftLimit = 512`, `chunkDataCacheLimit = 1024`, `lowMemoryMode = true`, `maxViewDistance = 24`.
5. Server: same mods (server_manifest.csv), at least 6 GB heap with the shipped c2me.toml (4500M OOM'd with the stock c2me cache after about 23 minutes of far teleports; with the shipped values it survived the same stress with 13 to 15 full GCs instead of 115 to 135).

## 1b. Memory and threads (measured in a 4 CPU / 14 GB container, not on a 32 GB PC)
- The world is 4064 blocks tall (254 sections per chunk). Singleplayer RD 8 / sim 8: 4.85 GB live at about 800 loaded chunks, on a 2.6 GB empty-world footprint. Model: about 2.7 MB per loaded client chunk, so RD 24 is about 10 GB and RD 32 about 15 GB (extrapolated, NOT measured; the container cannot run them). RD 56 / sim 32 (your OOM) would need 35 GB or more, so it cannot fit in 32 GB. Please test RD 16, then 24, and report the heap in F3. Do not go above 24 (c2me caps the slider there).
- c2me stock NBT cache is 8192/32678 chunks; at this height that alone fills the heap. Shipped: 512/1024.
- Tellus prefetch: 8 `tellus-prefetch` threads, not the heap problem (Tellus classes are small in the heap dump summary).
- Threads: baseline 1109 in singleplayer (your PC 1175). The big groups were Distant Horizons per-dimension pools, created for every server level even when the level is in ignoredDimensionCsv (DH-ChunkSaveIgnoreTimer 176, World Gen Queue and Progress Updater 89 each, four queues of 65). perf 0.3.1 skips them for ignored dimensions: 472 threads. The remaining 179 IOWorker or CNME Storage threads are one per dimension and idle; capping them needs a mixin and was not done. Growth after teleports (your 3,581) was NOT re-measured after the fix; please check `jstack` or the F3 thread count after the far-teleport route.
- Lightman's Currency freeze (149 to 257 s ticks): diaithir-perf 0.3.1 redirects `hasChunk` and `getChunk` inside `BlockEntityTrackingManager.untrackBlockEntity` to `getChunkNow`. Verified: the mixin applies (exported bytecode on a dedicated boot shows both redirects). NOT verified: a before and after far-teleport run with a trader block in the dropped chunks. Please repeat your teleport with the same trader and watch for "Can't keep up".

## 2. Checks (all commands need op; I verified these with a real client on the test jars noted)
Coordinates are block x z at world scale 5. A new world must be an Earth/Tellus world (era Age I).
1. Join: expect no login error. Known first-join quirk: the first connection can time out while spawn chunks generate; rejoin.
2. Fantasy sites (verified, fantasy.2 jar): `/tp @s -1857479 330 -945074` Serpent Mound (sd001, biome `biomesoplenty:mystic_grove`); `/tp @s 3231678 330 -1096084` Shiretoko (sd003, mystic_grove); `/tp @s 1927831 330 -1282601` Belukha (sd004, terrain `frozen_peaks`, check the site biome with F3); `/tp @s 2120650 330 -476569` Popa (sd188, alpha_grove at the centre). Rule of thumb: radius km/5 blocks from the centre. For safety fly up first, chunks take 10-60 s to generate.
3. Mont Blanc (verified, fantasy.3): `/tp @s 152846 1250 -1150713`. Surface about Y1183, `frozen_peaks`, snow.
4. Cave biome below Y0 (fantasy.3 at Mont Blanc): dripstone_caves at Y-200 under 152846,-1150713; dig down with `/tp` or fly in creative; `/locate biome` is slow, use F3 instead.
5. Giant cave (NOT verified by me): Better Caves 4.1.2 carves inside Tellus' carving step with the scaled carver `kubejs/data/bettercaves/worldgen/configured_carver/better_cave.json`. Dig down at any mountain such as Mont Blanc and look for caverns larger than vanilla below about Y100.
6. Fungal fen (fantasy.4 rule: low flat rain-fed forest): rule `regions_unexplored:fungal_fen`, about 2 percent of that ground. Look in wet lowland forest such as the Amazon, Congo or Pacific Northwest valleys; get x,z for a lat/lon from the `blockX`/`blockZ` in `/diaithir_probe <lat> <lon>`. Not found in my tests (the old rule's candidate cover class was almost absent).
7. Extinction species: `extinctions.js` cancels natural spawns by chance between the fade and extinct years (`global.etNS.EXTINCTIONS`: [fadeStartLc, extinctLc, ids]). Spawn by command, taming and animals already in the world are left alone. In my test none of the 19 ids exist in the pack (`untamedwilds:mammoth_columbian`, `untamedwilds:bison_steppe`, `glacialage:woolly_mammoth` and so on all give "Can't find element ... entity_type"), so there is nothing to see until the ids are fixed. To test: `/summon untamedwilds:mammoth_columbian ~ ~ ~` fails.
8. Timeline (throttled heads #141/#151; scripts load with the `/et_flag` fix; flood on the real client NOT re-run in this build): `/et_year 100` then `/et_year 5000` (op). Reply: `earth_timeline year N, flood M blocks, K chunks queued`. Flood values seen: 3673 gives 10, 4900 gives 12, 5000 gives 12, 100 gives 0. Standing at the coast chunk 66792 -1511683 with the player present, `/et_year 5000` floods it to Y213 within a minute. Chunks that are unloaded when the year changes and reload later did NOT flood in my test. A fresh chunk generated after the change stands at Y213. Fargo lake (same 512-block region): `/et_year 3673` at -2155145 -1185153 water Y288; `/et_year 4900` at -2155081 Y289; `/et_year 6500` at -2155017 dry grass. Winnipeg `-2161825 -1285795` at 3673: Y299.
9. Catch-up flags (off by default): `/et_flag vegetation_catchup true` and `/et_flag farmland true`. These need Tellus #152 (`EraRuntime.woodlandClearedAt`, `croplandAt`), which the pack's Tellus does not contain, so leave them off.
10. Bridge tiers (bridges 1.1.2 + Create diaithir.8, DiaithirRPG #143 content; accent render in a client join NOT re-verified on this build): Create tide wheel `/give @s create:tide_wheel`, bridge blocks `/give @s diaithirbridges:tobardealain`, `diaithirbridges:aibhnedealain`, `diaithirbridges:lochrandraoidheachd` (and the `umha`/`oir` variants), per DiaithirRPG #143.

## 3. Tellus commands (from `NeoForgeTellusRuntimePlatform.java`, fantasy.5/.6 source, all need gamemaster)
- `/diaithir_era` prints `ERA year=<lc> flood=<blocks>`; `/diaithir_era <lc>` (integer >= 1) sets the year.
- `/diaithir_probe <lat> <lon>` (lat -85..85, lon -180..180) prints `PROBE lat= lon= blockX= blockZ= surfaceY= ocean= cover= relM= overlay= roads= buildings=` without generating the chunk.
- `/diaithir_scan <lat> <lon> <radius 0..8>` loads chunks and prints `SCAN chunks= structureStarts= blockEntities= structureIds=... blockIds=...`.
- `/diaithir_column <lat> <lon>` prints `COLUMN blockX= blockZ= surfaceY= floorY= top=<block> | <water debug>`.
- `/diaithir_preload <latMin> <latMax> <lonMin> <lonMax>`, `/diaithir_preload_status`.
- `/et_year <lc>` and `/et_flag <vegetation_catchup|farmland> <true|false>` come from the pack's earth_timeline scripts, not Tellus.

## 4. Also fixed in this build
Walkers (`config/walkers.json5` blacklists `auroral:thrown_shimmer_spear`, so the ShapeType NPE is gone: 0 in a client log) and Immersive Vehicles (`config/mtsconfig.json` sets `performModCompatFunctions` false, which only gates the Entity Culling chat warning; the whitelist entries in `entityculling.json` stay).

## 5. Distant Horizons
The pack now sets `generatorPlan = "CHUNKS_ONLY"` in `config/DistantHorizons.toml` ([common.worldGenerator]). A PC that already has the file keeps its old value: change that one key (old `SURFACE_ONLY`, new `CHUNKS_ONLY`) in `<instance>/.minecraft/config/DistantHorizons.toml`.

## 6. Not verified in this round (say so rather than assume)
Giant caves in play, Backrooms void entrance and guard switch-on, Easy NPC creative-tab spawn egg path, coast chunk reload on fantasy.8, bestiary 0.3.2 render and animation, fungal fen at four cells, extinctions (the ids are species variants, not entity types: map to base entity types before enabling), accents of fantasy biome names and bridge/tide wheel names on a real client of this exact build, RD 24/32 heap, thread growth after teleports, Lightman before/after. First join on a fresh server still times out once at 30 to 33 s while c2me workers do first-time Tellus generation (server thread is idle); the retry joins in 8 to 12 s.
