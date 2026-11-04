# Diaithir Furnishings

Rope, sack, jars, signposts, sconce, plus the everyday-objects roster below.

## Everyday objects (hand tools, kitchenware, household items)
Rule (Marlon 11-03): every distinct, widely produced design across history and cultures gets one entry, its most iconic version, no variants. Same rule as the firearm and melee rosters.

- `roster/roster.csv`: 460 designs (id, name, category, earliest widespread use, region, Wikipedia source, lead sentence, status). `roster/dropped_dupes_or_unsourced.json` lists candidates cut because the page did not exist or the design was already listed (same Wikipedia page).
- `tools/gen_everyday.py` turns the roster into items, models, textures, lang names, tag `diaithirfurnishings:everyday/<category>`, Age gates (`age_gates.json`) and `data/diaithirfurnishings/everyday.json`, which `Everyday.java` registers. Rope and sack already exist as blocks and are skipped.
- Recipes: none here except furniture (stonecutting from planks). The rest are made at the Smithy and Workshop Bench in diaithir-gear (ids `ev_<id>`; add new items to `tools/gen_workshop.py` there).
- Tools (hand tools, farm, mining and so on) stack to 1 with 200 durability; the rest stack to 16.

Follow-ups (second PR):
- Art: `tools/install_art.py` copies the layered part textures from the gear workshop art drop (`/mnt/project-files/texture-drops/gear-art-pr180`) and rewrites 451 item models to use them. The 9 furniture items keep the generated sprite.
- Crafting: no recipes in this module at all. The Smithy and Workshop Bench in diaithir-gear (ids `ev_<id>`) make everything; furniture still needs a bench entry in `diaithir-gear/tools/gen_workshop.py`.
- Names: `roster/irish_terms.json` (modern Irish from a Haiku pass, needs a native-speaker review: some obscure terms are wrong, for example adze) run through `diaithiris-archaic/tools/archaic.py` by `tools/gen_names_codex.py`. Diaithiris names show as the grey tooltip line (`item.<id>.native`); `roster/names_map.json` maps English to Diaithiris. The 130-culture names are not done.
- Codex and Library: `codex/everyday_handbook.json` (27 chapters, one per category) and `library/everyday_library.json`, text from the roster's Wikipedia lead sentence.

Tested headless in the cloud: `./gradlew test` (every roster item has a model, existing textures, a name and a Diaithiris name) and a server boot (no data errors).
Not done: tool and cookware mechanics (the benches use these items as parts and bases; nothing else consumes them yet), the 130-culture names, sync of the Library and Codex files into library-sync and the handbook builder, overlap check against modded cookware.
Tested headless in the cloud: `./gradlew test` (every roster item has its model, texture, name and recipe) and a server boot (no data errors, 1993 recipes loaded).
Not done: art is generated per category and not yet drawn per object; no Diaithiris names yet (compute job); no tool or cookware mechanics (tools as crafting tools, cookware for cooking); no Codex/Library entries; not folded into other modules where a design overlaps a modded item (check against Farmer's Delight style cookware and the tools module at consolidation).


## Farm equipment (Marlon 11-03)
Every mass-produced farm implement and machine, one iconic version each, primitive to modern. 35 new rows in `roster/roster.csv` (category `farm_machine`, Wikipedia source, earliest-use year) plus working rules in `roster/farm_mechanics.csv` (kind, swath width in m, rated power in W, mode, predecessor). Ten #185 tools (hoe, mattock, sickle, scythe, machete, ard `plough`, moldboard plough, harrow, seed drill, watering can) get the same rules.
- **Modes**: `hand` (1 durability per swath), `draft` (needs a horse, donkey, mule or cow led by or carrying the player within 6 m), `powered` (fuel is the durability bar, 1 point = 1 kJ; tank = ten minutes at rated power; sneak-use with coal, charcoal, coal block, dried kelp block, blaze rod or stick in the off hand refuels; swath cost = rated W x length / speed in J).
- **Kinds**: `till` (dirt, grass, path to farmland), `plant` (any crop seed from the inventory onto farmland), `harvest` (mature crops, drops, replants at age 0), `fertilize` (bone meal from the inventory, +2 growth), `water` (farmland moisture to 7), `bale` (9 wheat to a hay block), `milk` (empty bucket to milk bucket on a cow). `none` is an item only for now.
- **Predecessors**: every Age V+ machine chains back to an Age I-IV tool (digging stick, ard, sickle, flail...), checked by `FarmDataTest`.
- **Crafting**: no stonecutter or crafting-table recipe. They are built on the Gear benches (`diaithir-gear` regenerates blueprints from this module's `everyday.json`).
- **Not done**: threshing machine, cotton gin, cream separator, chaff cutter, hay rake and tedder are items without a mechanic; no tractor or combine entity yet (seats and rollout belong to the assembly yards, PR #186); fuel table is local until it moves to `net.diaithir.units.fuel.Fuels` (PR #190); upgrade multipliers not wired; no in-world swath test (only data tests, FarmMath tests and a clean headless boot); art is generated per category.
