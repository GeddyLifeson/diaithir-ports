# Diaithir Furnishings

Rope, sack, jars, signposts, sconce, plus the everyday-objects roster below.

## Everyday objects (hand tools, kitchenware, household items)
Rule (Marlon 11-03): every distinct, widely produced design across history and cultures gets one entry, its most iconic version, no variants. Same rule as the firearm and melee rosters.

- `roster/roster.csv`: 460 designs (id, name, category, earliest widespread use, region, Wikipedia source, lead sentence, status). `roster/dropped_dupes_or_unsourced.json` lists candidates cut because the page did not exist or the design was already listed (same Wikipedia page).
- `tools/gen_everyday.py` turns the roster into items, models, textures, lang names, tag `diaithirfurnishings:everyday/<category>`, stonecutting recipes, Age gates (`age_gates.json`) and `data/diaithirfurnishings/everyday.json`, which `Everyday.java` registers. Rope and sack already exist as blocks and are skipped.
- Recipes: one stonecutting recipe per item from a base material that matches its category and Age (flint, copper, bronze, iron, clay, planks, string, leather, paper and so on). Bronze uses `diaithirfurnishings:metal/bronze`, which falls back to copper if no bronze mod is present.
- Tools (hand tools, farm, mining and so on) stack to 1 with 200 durability; the rest stack to 16.

Tested headless in the cloud: `./gradlew test` (every roster item has its model, texture, name and recipe) and a server boot (no data errors, 1993 recipes loaded).
Not done: art is generated per category and not yet drawn per object; no Diaithiris names yet (compute job); no tool or cookware mechanics (tools as crafting tools, cookware for cooking); no Codex/Library entries; not folded into other modules where a design overlaps a modded item (check against Farmer's Delight style cookware and the tools module at consolidation).
