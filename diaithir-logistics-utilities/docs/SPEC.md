# Foundry courier depots and ghost placement: behaviour spec

Written for a clean-room build. It describes what the player sees and what the server does. It was drafted from the public
feature descriptions of construction-bot mods in general (a roboport-style service area, bots that fetch and build planned blocks),
not from any mod's source. No code, texture, model or text from another mod is used.

## Player view
1. **Plan wand.** Hold the wand in one hand and a block in the other. Use the wand on a face: a ghost of that block appears at the
   place the block would go (facing and other placement state follow normal placement rules). Nothing is used up. Sneak and use
   on a ghost to clear it. Ghosts have no collision and show as a pale blue grid cube.
2. **Courier depot.** Chests and barrels touching a depot are its stock. The depot serves every ghost within its reach
   (default 48 blocks). Using it bare-handed shows couriers out, items in stock and reach.
3. **Couriers.** A depot has a fixed number (default 4). A courier takes one item from stock, flies to the nearest unclaimed ghost
   whose item the stock holds, builds the block, and flies home. Sparks mark the flight. A ghost is claimed by one courier at a time.
4. **Edge cases.** No stock means no trip. A ghost cleared in flight sends the item back to stock (dropped at the depot if the stock
   is full). Breaking a depot drops what its couriers carry. A claim lapses after a timeout so a restart cannot strand a ghost.

## Server view
- Depots run on a scheduled tick (default every 10 ticks). Ghost positions are kept in a per-level set so no run scans the world.
- At most 64 ghost candidates are checked per run per depot.
- Couriers are jobs (ghost, item, arrive time, home time) saved in the depot, not entities, so they cost no entity ticks.
- Items move through the NeoForge transfer API, so any inventory with an item capability can be stock.

## Not in this first slice
Courier entities with models, request/provider chests, deconstruction and pickup plans, area and blueprint planning, ghost preview of
the target block, upgrades and range extenders. Foundry design (plans/FACTORIO_TECH.md) lists them as follow-ups.
