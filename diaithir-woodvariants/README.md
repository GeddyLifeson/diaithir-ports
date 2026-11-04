# diaithir-woodvariants

Furnishing and building variants for woods that other mods add. Clean-room, MIT, NeoForge 26.1.2.

Macaw's Furniture ships a fixed list of wood types, so a modded wood never gets a chair or table from it. The pack's wood mods already bring their own stairs, slabs, fences, gates, doors, trapdoors, signs and buttons, so this mod adds only what is missing, for each wood: chair, stool, bench, table, wall, vertical planks, framed planks, plank door and plank trapdoor (nine blocks).

The woods are rows in `src/main/resources/data/diaithirwoodvariants/woods.json` (id, mod, English and Diaithiris genitive names, planks and log block ids, and the three textures). A row is skipped at load when its mod is not installed. Shipped rows: Aether II skyroot, Biomes O' Plenty fir, redwood, mahogany, willow, maple and magic, Productive Trees alder, cedar, aspen, beech, elm, yew, rowan, hazel, holly, juniper and walnut.

To add a wood: add a row, run `python3 -I tools/gen.py` from this folder (it writes blockstates, models, item models, lang, recipes with a `mod_loaded` condition, loot tables and tags), rebuild. No Java changes. Textures are the wood mod's own, referenced in place and never copied.

Recipes: chair 3 sticks + 2 planks (1), stool (2), bench (2), table (1), wall (6), vertical planks (3 from 3 planks), framed planks (4). Everything breaks with an axe and drops itself.

Chairs, stools and benches can be sat on (right-click; sneak to stand; the seat is a never-saved entity that removes itself). Tables join neighbours. Names are English with the native name as a grey tooltip line (lang key native.<ns>.<id>). tools/derive_woods.py rebuilds woods.json from the pack dump. Not included: a carving tool.

Licence: MIT. Source: https://github.com/GeddyLifeson/diaithir-ports/tree/diaithircompat/diaithir-woodvariants.

## Macaw-style building set (added 11-03)

Clean-room, from the public feature lists of the Macaw's mods (bridges, doors, windows, fences, roofs, trapdoors, paths, furniture); behaviour only, no code or art copied; the originals are all-rights-reserved. Every wood row with `"macaws": true` (the 12 vanilla woods and the 14 Biomes O' Plenty woods) gets 25 more blocks: picket, stockade, horse, wired and pyramid fences; barn, stable, cottage, glass and shoji doors; beach, barred, glass and cottage trapdoors; window, four-pane and barred windows (wood frame, glass pane, connect like panes); plank, log and rope bridges (deck with a railing on each side that does not touch another bridge); and decorative coffee table, end table, cupboard, counter and bookcase (face the placer; decor only, no inventory). Door and trapdoor designs are overlay textures drawn over the wood's own planks, so they work on any wood row.

Not wood bound: 16 colours of roof, roof stairs and roof slab (terracotta recipes), and 6 palettes x 8 patterns of paving (stonecutting from stone, sandstone, bricks, deepslate, mossy cobblestone, terracotta; one pixel low like a path). Lists: `macaws.json`. Textures: `tools/gen_textures.py` (procedural, seeded, committed under assets/.../textures/block). Models, blockstates, recipes, loot, lang and tags: `tools/gen_macaws.py` (run by gen.py). Biomes O' Plenty rows also load when diaithir-overworld-biomes (which registers the biomesoplenty ids) is installed.
