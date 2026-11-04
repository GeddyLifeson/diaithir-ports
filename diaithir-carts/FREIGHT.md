# Freight layer (inside diaithir-carts, package `net.diaithir.freight`)

Pallets, barrels, chests, crates and shipping containers (20 and 40 ft, reefer, tank). No new jar. MIT, clean-room.

## The rule
Nothing here opens a storage screen on a container or trailer. A container is a space. You fill it by putting loads in it, and the loads stay where they were put. Capacity is cells and kilograms, never slots.

## Two forms, one model
- **Deployed** (a container standing in the world): real blocks. A ring of wall cells round a walkable 2 by 2 interior, four door cells at one end (use one to swing all four), and the loads inside as ordinary load blocks. You walk in, place pallets and barrels by hand, stack them, and they stay. Reefers have a refrigeration unit row at the closed end (4 kW). A tank container is a solid white vessel block with a 26,000 L fluid tank.
- **Carried** (on a crane hook, forklift, trailer, flatcar, ship): one item per container with the component `diaithircarts:bay` holding every load and its items. The same loads as plain data are a `CargoBay`. A vehicle, flatcar, ship hold or depot floor is also a `CargoBay`, so a container is just a load that is itself a bay.

`CargoBay` (plain Java, unit tested) checks bounds, overlap, support from below, how much weight each load may bear on top (ISO 192 t on container corner posts, 2 t on a pallet), payload limit, and Age. A load cannot be taken from under another.

## Sizes (1 cell = 1 m cube)
| id | outside (w x h x l) | interior | tare kg | payload kg | Age |
|---|---|---|---|---|---|
| chest | 1x1x1 | 27 stacks | 30 | 450 | I |
| pallet | 1x1x1 | 18 stacks | 25 | 1,500 | II |
| barrel | 1x1x1 | 18 stacks | 20 | 300 | II |
| crate | 1x1x1 | 54 stacks | 55 | 1,200 | II |
| container_20 | 4x4x7 | 2x2x5 | 2,300 | 28,180 | VI |
| container_40 | 4x4x13 | 2x2x11 | 3,800 | 26,680 | VI |
| reefer_20 / _40 | 4x4x7 / 13 | 2x2x4 / 10 | 3,000 / 4,800 | 27,480 / 25,680 | VI |
| tank_20 | 4x4x7 | 26,000 L | 3,700 | 32,300 | VI |

Containers are drawn larger than real (a 1 m grid), so a player can walk in. Items weigh 0.25 kg each.

## The one interface for everything else
Everything uses NeoForge's own item and fluid handlers, so storage cables, hoppers, pipes and depot workers need no special code.
- **Loading dock** (Age V): stands up to three blocks from a container, facing it, and offers all the container's loads as one item handler (and the tank's fluid handler). The storage network (diaithir-storagenet) picks it up like any inventory. The handler looks at what is ahead on every call, so cables that cache it stay correct as containers arrive and leave. The comparator reads how full the container is, for the circuit network.
- **Rift dock** (Age VIII): serves the container at its partner, any other rift dock on the same channel (1 to 64, bare hand cycles) in any dimension. Load at one end, unload at the other. This is how later-Age interdimensional shipping plugs into the same handlers.
- **Lift and lower** (for cranes #206, forklifts, trucks #210, rolling stock, ships #216, depot workers): `Containers.lift(level, pos, liftKg, simulate)` returns `Result.OK` and the item, `TOO_HEAVY` over the lifter's rating, `NOT_EMPTY` for a tank with fluid. `Containers.lower(level, stack, origin, facing)` sets it down. The cargo hook (Age VI, 35 t) does it by hand.
- **Vehicle bays** (`CargoCarrier`): a trailer, truck bed, flatcar, wagon, ship hold or depot floor implements `CargoCarrier` (its `CargoBay`, `columnAt(worldPoint)`, `changed()`) and offers it through the entity capability `Freight.CARRIER`; carriers that are not entities add a finder to `Carriers.SOURCES`. Then everything works with no more code: `Carriers.put(carrier, stack, x, z)` / `takeTop` load and unload a pallet, barrel, chest, crate or container item (the load goes on the aimed column and stays; containers stack to the ISO limit); a player right-clicks the carrier with a load, or sneaks with an empty hand to take the top load; docks facing a carrier serve every load on it (container interiors included) through the same item handler. Walkable decks stay with the vehicle thread.

## Made at stations
Nothing here is a grid recipe. `data/diaithircarts/freight/station_recipes.csv` lists output, Age, station and inputs for the station lanes (workshop bench, cooperage, machine works, container works). The container works is the Assembly Yard tier that builds containers.

## Known gaps
- No vehicle implements `CargoCarrier` yet; the vehicle, rail and ship threads register theirs. Tested with a stub carrier.
- Carried loads are not drawn on the vehicle (vehicle thread).
- Tank fluid is not carried when lifting; pump it empty first. A tank container is a plain vessel block, not an ISO frame.
- No client test: models and textures are generated but unseen.
- Door leaves do not animate.

Tests: `CargoBayTest` (6 unit tests) and `/diaithircarts freighttest` (owners; deploy, doors, dock insert and extract through the real block capability, lift weight rule, carrier put, stack, dock and take with cargo intact, lower, rift pair, tank fluid rule). Assets: `tools/gen_freight_assets.py`.
