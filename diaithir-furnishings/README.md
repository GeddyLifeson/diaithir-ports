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
