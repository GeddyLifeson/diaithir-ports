# Test results: alexs-caves 2.0.2-diaithir.1

Build: NeoForge 26.1.2.116, Java 25, `./gradlew jar` (jar 73 MB, mostly the original sounds). Boot tests on a stand-alone NeoForge server with this jar, rcon-driven (tools/rcon.py, summon_all.py, place_all.py, scan_chambers.py).

| Check | Result |
|---|---|
| Server boots, no registry or datapack errors | pass (Done in ~5 s; no "Couldn't parse" lines after the recipe, advancement and loot-modifier conversion) |
| `/summon` of all 83 entity types, then 400 ticks | pass, no errors (needed the `tempt_range` attribute and a mixin-free frostmint freeze) |
| Every block id (344) placed with `/setblock`; every item id (189) summoned as an item entity; 100 ticks | pass |
| 63 non-block loot tables via `/loot spawn` | pass |
| Cave chambers, vanilla overworld, 192x192 forced area scan | pass: all six biomes found by `execute if biome`, no "far chunk" errors after centring on the chunk |
| Cave chambers with the pack's Tellus jar and `tellus:earth`, same scan | pass: all six biomes found |
| Age gate | data only (age_gates.json) |
| Dev client (Xvfb, software GL) with the Citadel module, all 83 entities summoned in 21 batches of four | pass: real Citadel models and textures render, no render exceptions after the fixes below; log is clean apart from the OpenAL device warning |
| Client crashes found by that run | fixed: `(ServerLevel)` casts reached from client ticks (acid block, tesla bulb, entity hurt and UUID lookups, now `misc/ACSide`), Teletor `popPose` on a fresh PoseStack, uranium rod and abyssal altar block models with UVs over 16 |
| Structures, vanilla overworld (seed 424242), `/locate structure` for all 14 | pass: found (cake cave, dino bowl, acid pit, ferrocave, forlorn canyon, ocean trench as parents; volcano, donut arch, gingerbread town, licowitch tower, soda bottle, forlorn bridge, abyssal ruins inside them; underground cabin standalone) |
| Parent bowls generated (forceload plus `execute if biome` at the centre) | pass for all six: cake cave, dino bowl, acid pit, ferrocave, forlorn canyon, ocean trench carve and relabel to their cave biome |
| Child structures generated (forceload around each), server log | pass: no exceptions |
| Structures over `tellus:earth` (Tellus 0.8.3-diaithir.12, inland spawn) | pass: 12 of 14 found, cake cave and dino bowl generate and relabel; ocean trench and abyssal ruins not found (spawn is inland, nothing in the 100 chunk search), not tested over water |
| Natural spawning in the cake cave bowl (creative player, 9000 ticks) | pass: caniac, gumbeeper, candicorn, caramel cube, gummy bear, gingerbread man, melted caramel and sweetish fish spawned by themselves (plus mobs of neighbouring bowls) |

Dev client (Xvfb, 26.1.2 client, magenta scan on every shot): pass for entities, blocks, items, particles, block entity renderers, armour, spelunkery GUI, equipment assets, Nuclear Furnace (multiblock, GUI, smelting), Cave Book (flat 2D screen: links, locks, entity/crafting/item/image widgets), irradiated / blue irradiated / darkness layers (player, F5), bubbled wobble, sugar rush post effect, Sack of Sating tooltip. Watcher and submarine post chains verified only with the effect forced on.

Not tested: real watcher trigger (darkness_incarnate self-removes quickly, cause unknown), effect layers on other entities (effect is not synced to other clients except via the upstream message), Raygun beam, cave map, biome sky/fog overrides, coastal Tellus (ocean trench and abyssal ruins: server thread blocks on Tellus elevation tile fetch for far uncached chunks and the client times out, so no coastal spawn could be reached; `/place` of both fails outside their biomes), structure interiors (loot, spawners, data markers; only `/place` of underground cabin and acid pit succeeded and were not inspected), animations in motion, bowls from inside and under heavy relief.

Known gaps: see PORT_PLAN.md (cut features, structures, entity models).
