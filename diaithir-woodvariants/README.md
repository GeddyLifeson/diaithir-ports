# diaithir-woodvariants

Furnishing and building variants for woods that other mods add. Clean-room, MIT, NeoForge 26.1.2.

Macaw's Furniture ships a fixed list of wood types, so a modded wood never gets a chair or table from it. The pack's wood mods already bring their own stairs, slabs, fences, gates, doors, trapdoors, signs and buttons, so this mod adds only what is missing, for each wood: chair, stool, bench, table, wall, vertical planks, framed planks, plank door and plank trapdoor (nine blocks).

The woods are rows in `src/main/resources/data/diaithirwoodvariants/woods.json` (id, mod, English and Diaithiris genitive names, planks and log block ids, and the three textures). A row is skipped at load when its mod is not installed. Shipped rows: Aether II skyroot, Biomes O' Plenty fir, redwood, mahogany, willow, maple and magic, Productive Trees alder, cedar, aspen, beech, elm, yew, rowan, hazel, holly, juniper and walnut.

To add a wood: add a row, run `python3 -I tools/gen.py` from this folder (it writes blockstates, models, item models, lang, recipes with a `mod_loaded` condition, loot tables and tags), rebuild. No Java changes. Textures are the wood mod's own, referenced in place and never copied.

Recipes: chair 3 sticks + 2 planks (1), stool (2), bench (2), table (1), wall (6), vertical planks (3 from 3 planks), framed planks (4). Everything breaks with an axe and drops itself.

Chairs, stools and benches can be sat on (right-click; sneak to stand; the seat is a never-saved entity that removes itself). Tables join neighbours. Names are English with the native name as a grey tooltip line (lang key native.<ns>.<id>). tools/derive_woods.py rebuilds woods.json from the pack dump. Not included: a carving tool.

Licence: MIT. Source: https://github.com/GeddyLifeson/diaithir-ports/tree/diaithircompat/diaithir-woodvariants.
