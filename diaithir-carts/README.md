# Diaithir Carts

Clean-room rail mod for NeoForge 26.1.2, written from public descriptions of the ideas only (faster rail tiers, minecart chains, a cheaper rail tick). No code, art or names from any other mod. Licence: MIT.

- **Rail tiers.** Bronze (Age III), steel (Age V) and high-speed (Age VI) rails, each with a plain and a powered variant. A cart riding one is capped at 0.6, 0.9 or 1.2 blocks per tick (vanilla is 0.4). The plain rails extend the vanilla rail block, so curves, slopes and junctions behave as usual; the powered rails extend the vanilla powered rail (redstone chaining) and boost per tier. Vanilla detector and activator rails work beside them. The cap is lifted by one mixin on `AbstractMinecart.getMaxSpeed`; it only ever raises the cap, and only while the cart is on one of our rails.
- **Coupling chain.** Use it on a cart, then on a second one: the first leads, the second follows at 1.5 blocks and is pulled, so a row of carts is a train (each cart has one leader and one follower; loops are refused). A link breaks past 8 blocks, or when a cart is destroyed, and drops the chain.
- **Chain cutters.** Sneak and use on a cart to cut its link and get the chain back.
- **Station rail.** Stops a cart that rolls onto it and holds it until it is powered or its timer ends (use bare-handed to cycle: signal only, 2, 5, 15, 60 s). Coupled followers follow their leader instead of holding.
- **Block signal.** Faces a rail and guards the stretch of connected track from there (up to 32 rails each way, round curves and slopes). Red with a redstone signal of 15 while a minecart is anywhere on that stretch, green and 0 otherwise; wire it to a station rail or powered rail upstream to keep trains apart. Checks every 10 ticks, no block entity.
- Out of scope: keeping chunks loaded for carts. Not built from the Railcraft idea list: coke oven and blast furnace, tank and loader carts, routing, one-way and locking tracks, signal boxes with distant signals.

`/diaithircarts signaltest` (owners) builds a rail with a corner and a signal beside it, and checks the stretch (25 rails), green with no cart, red with a cart on the far end, the redstone output, and green again. `/diaithircarts selftest` (owners) builds a yard high above spawn and checks the caps of vanilla, bronze, steel and high-speed rails, powered and unpowered powered rails, coupling through the chain logic, follow and spacing, cutting, and both station releases. It reports PASS or what failed.

`tools/gen_assets.py` writes the textures (drawn from scratch), models, blockstates, lang, recipes, loot, tags, `data/diaithircarts/age_gate.json` and `../library-sync/diaithircarts.json`.

## One-way rail, destination rail, cart loader
- **One-way rail**: carts go north or east, or south or west when reversed (bare hand flips it). A cart rolling on against the arrow is turned back. Straight stretches only.
- **Destination rail**: gives redstone 15 while a cart whose name matches its channel stands on it. Channel 0 matches any cart; 1 to 16 are the letters A to P, matched against the cart name (name tag). Bare hand cycles the channel. Power a three-way junction rail with it so named carts take the branch.
- **Cart loader**: put it over a rail with a chest, barrel or tank on top. Every 8 ticks it moves up to 8 items, or 250 mB of fluid, between the container and the cart under it (bare hand flips loading and unloading; redstone switches it off). It uses the item and fluid handlers of the cart, so it works with chest and hopper carts and with the tank and hopper wagons the yards build (wag_tank, wag_open_hopper, wag_covered_hopper); no cart entities are added here, so nothing duplicates the rolling-stock roster.
- Test: `/diaithircarts routingtest` (owners) runs carts against and with one-way rails, named and unnamed carts on a destination rail, and a cart loader loading, unloading and switched off by redstone.
- **Locking rail**: stops a cart and keeps it. Bare hand cycles the mode. Latch (amber) holds until the rail goes from unpowered to powered (a pulse; already powered on arrival does not release). Interlock (green) holds until the 32 rails ahead are clear of other carts. The routing test covers both.
- Coke oven and blast furnace are not built here: they already exist in diaithir-wiretech (ports #163).

## Freight
Pallets, barrels, chests, crates, walkable shipping containers (20 and 40 ft, reefer, tank), loading docks and rift docks. See FREIGHT.md. Test: `/diaithircarts freighttest` (owners).
