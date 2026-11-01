# Diaithir Storage Net

A clean-room storage network for NeoForge 26.1.2, written from public descriptions of the idea only (no code, textures or names of any other mod). MIT.

Place a controller (Vorkaneth) and run cables (Ilthuvar) from it. Cables join themselves to chests, barrels and any other block with an item inventory, and those become the network's storage. The access terminal (Zhaldorith) opens a vanilla 9x6 screen (players install nothing) listing everything stored: left click takes a stack, right click one, shift click sends a stack to your inventory; click with an item on the cursor, or shift click your own items, to store them. The bottom row pages, changes the sort (name, count, mod) and clears the search. `/diaithirstoragenet search <text>` filters the grid (`@mod` searches by mod id).

Import cables (Draskovel) pull from the inventories touching them into the network, export cables (Ekthurmael) push network items out. Filter cables (Pelquanor) limit what the inventories touching them accept and make whitelisted items go there first. Use an item on any of these to add it to or remove it from the filter, a stick to flip whitelist and blacklist, sneak with an empty hand to clear. Inventories touching an import or export cable are not used as storage. The crafting terminal (Quenvarosh) is a crafting table whose emptied grid slots refill from the network after each craft.

Performance: the network is walked only after something next to a network block changes (a dirty flag, then one walk on next use), never per tick. The walk is capped by `maxNetworkBlocks` (600) and `maxInventories` (200). Import and export cables run on a scheduled tick every `cableIntervalTicks` (10) and move at most `itemsPerRun` (16) items.

Age gate data is in `data/diaithirstoragenet/age_gate.json` (cables Age V, controller and terminals Age VI); library text is in `library-sync/diaithirstoragenet.json`. `tools/gen_assets.py` writes the textures, models, lang, recipes, age gate and library entries.

Commands: `/diaithirstoragenet search <text>`; owners `selftest` (builds a controller, cables, chests, filter, import, export and terminal above spawn, checks scan, insert, extract, grid clicks, search, sort, filtering, import, export, crafting refill and splitting the network, then removes it).
