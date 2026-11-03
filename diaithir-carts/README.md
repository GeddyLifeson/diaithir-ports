# Diaithir Carts

Clean-room rail mod for NeoForge 26.1.2, written from public descriptions of the ideas only (faster rail tiers, minecart chains, a cheaper rail tick). No code, art or names from any other mod. Licence: MIT.

- **Rail tiers.** Bronze (Age III), steel (Age V) and high-speed (Age VI) rails, each with a plain and a powered variant. A cart riding one is capped at 0.6, 0.9 or 1.2 blocks per tick (vanilla is 0.4). The plain rails extend the vanilla rail block, so curves, slopes and junctions behave as usual; the powered rails extend the vanilla powered rail (redstone chaining) and boost per tier. Vanilla detector and activator rails work beside them. The cap is lifted by one mixin on `AbstractMinecart.getMaxSpeed`; it only ever raises the cap, and only while the cart is on one of our rails.
- **Coupling chain.** Use it on a cart, then on a second one: the first leads, the second follows at 1.5 blocks and is pulled, so a row of carts is a train (each cart has one leader and one follower; loops are refused). A link breaks past 8 blocks, or when a cart is destroyed, and drops the chain.
- **Chain cutters.** Sneak and use on a cart to cut its link and get the chain back.
- **Station rail.** Stops a cart that rolls onto it and holds it until it is powered or its timer ends (use bare-handed to cycle: signal only, 2, 5, 15, 60 s). Coupled followers follow their leader instead of holding.
- **One-way rail.** A steel-speed rail with an arrow, set to the way you face when placed. A cart rolling against the arrow is stopped; carts going with it pass. Clean-room (survey part 1: one-way rails, none found to port).
- **Block signal.** Faces a rail and guards the stretch of connected track from there (up to 32 rails each way, round curves and slopes). Red with a redstone signal of 15 while a minecart is anywhere on that stretch, green and 0 otherwise; wire it to a station rail or powered rail upstream to keep trains apart. Checks every 10 ticks, no block entity.
- Out of scope: keeping chunks loaded for carts. Not built from the Railcraft idea list: coke oven and blast furnace, tank and loader carts, routing, locking tracks, signal boxes with distant signals.

`/diaithircarts signaltest` (owners) builds a rail with a corner and a signal beside it, and checks the stretch (25 rails), green with no cart, red with a cart on the far end, the redstone output, and green again. `/diaithircarts selftest` (owners) builds a yard high above spawn and checks the caps of vanilla, bronze, steel and high-speed rails, powered and unpowered powered rails, coupling through the chain logic, follow and spacing, cutting, and both station releases. It reports PASS or what failed.

`tools/gen_assets.py` writes the textures (drawn from scratch), models, blockstates, lang, recipes, loot, tags, `data/diaithircarts/age_gate.json` and `../library-sync/diaithircarts.json`.
