# Vehicles, fuel, pumps, roads and drivers (design and status)

Mod-first: everything rides on the Immersive Vehicles (IV) stack already in the pack. IV already has fuel pumps, fuel hoses and jerrycans, so we reuse them and add only what is missing. No new jars: the road-vehicle pack is `assets/diaithirvehicles` inside the diaithir-carts jar (as the tank pack is inside diaithir-arms); the fuel API is in diaithir-units.

## Done in this PR
- `roster.csv`: one iconic real design per useful type, with year, Age, engine, fuel, source and a confidence flag. Ground: primitives first (travois, litter, chariot, ox cart, stagecoach, steam carriage, safety bicycle), then cars by role: military (jeep, armoured car, APC), utility (pickup, van, ambulance, fire truck, tow truck), travel (sedan, off-roader, bus, motorcycle, electric). Aircraft are owned by the planes thread (PR #188). `pack=pack` rows get an IV vehicle; `pack=data` rows are roster only (needs animal mounts, steam, electric, bike or aircraft parts: follow-ups).
- Age rule used (tech-tree thread owns the final call): up to 1885 = V (steam and animals), 1886 to 1945 = VI, 1946 on = VII; earlier primitives take I to III.
- `tools/gen_vehicles.py` writes the pack (13 vehicles) from the roster and fuel table; `age_gates_vehicles.json` and `vehicle_manifest.json` beside it. Blank mass, power or tank cells use body-class defaults (design defaults, not sourced).
- Fuel API in diaithir-units (`net.diaithir.units.fuel`): `Fuels` (registry from `fuels.csv`, 17 real fuels with MJ per L or kg, flow, range, IV consumption, upgrade-adjusted efficiency), `Engine` classes with typical efficiency, `FuelStore` (tank, jerrycan, pump and vehicle all speak it; transfer never creates or loses fuel), `SimpleStore`. SI only. Upgrade hook: `Fuels.upgradedEfficiency(base, lossCut)` takes the efficiency-module effect from diaithir-upgrades `Resolver`.
- Roads, built: six road blocks (dirt track, gravel, cobbled, macadam, asphalt, concrete motorway) from `roads.csv` via `tools/gen_roads.py`, Age gated, with vanilla's block speed factor 1.0 to 2.0, so players, mounts, NPCs and carts move faster on better roads. `Roads.factorAt(level, pos)` is the lookup for other vehicle code. `/diaithircarts roadtest` passes on a headless server (the table, the lookup in a real world). Recipes are vanilla placeholders; Foundry/tech chains replace them. Not done: IV wheel grip on roads (IV reads its own friction), road generation in villages.

## Fueling by Age (Marlon 11-03: both, gated)
1. Ages I to V manual: feed bags and troughs for draft animals (hay, oats), wood and coal shovelled into steam carriages, then jerrycans and barrels with a hand pump.
2. Age VI to VII pump stations: IV `fuel_pump` decor tied to storage tanks and pipes, refuels any parked vehicle automatically, takes payment via the economy thread. Loading racks for tanker trucks, rail tank cars and ships (all through `FuelStore.transfer`).
3. Refining comes from Foundry / tech-suite chains (crude to petrol, diesel, kerosene, avgas); the pack only needs the fluid ids in `fuels.csv`.

## What Immersive Vehicles already has (read from the jar, 11-03)
- Fuel: every vehicle has `fuelTank` (`EntityFluidTank`: `fill`, `drain`, `getFluidLevel`, `getMaxLevel`, `getFluid`, fluid name plus mod); the `fuel_pump` decor holds a tank and a hose connects it to a vehicle; engines name a `fuelType` that the IV config maps to fluids. So our pump station should fill IV's tanks through `EntityFluidTank`, with the pack's `FuelStore` as the storage side.
- Roads: IV has its own road network (`BlockRoad`, `TileEntityRoad`, pack-defined `JSONRoadComponent` lanes and curves, `RoadFollowingState`). Our six surface blocks above are the walking, mount and cart roads; the vehicle roads should be IV road components in the `diaithirvehicles` pack (dirt, gravel, cobbled, macadam, asphalt, concrete) so IV lane joins and following work. Not written yet.
- Driving: controls are `ComputedVariable`s on the vehicle (`throttleVar`, `brakeVar`, `parkingBrakeVar`, `autopilotValueVar`, steering angle through the physics class). An NPC driver should set these, not add physics.
- Vehicles are Minecraft entities of `mcinterface261.BuilderEntityExisting` with a protected `entity` field, so a bridge from our mods needs reflection or a compile-only IV jar.

## Pumps and storage (built, in diaithir-units, `net.diaithir.units.fuel.station`)
Fuel barrel (Age II, 200 L), hand pump (Age III, 5 L per use), fuel tank (Age VI, 20,000 L), pump station (Age VI, 1 L/s, automatic, 5-block reach) and loading rack (Age VII, 20 L/s). A pump or rack pulls from an adjacent tank into every fuelable target in reach. Targets are anything registered in `Targets.SOURCES`; Immersive Vehicles is bridged by reflection (its vehicle fluid tank), so tank cars and ships add their own source. `/diaithirfuel fill <pos> <fuel> <litres>` fills a tank (owners) until Foundry refining supplies it. `/diaithirfuel selftest` passes on a headless server: pump and rack rates, fuel conserved, no mixing, full targets respected, save and load. Not tested: the IV reflection against a real vehicle entity, pipes into tanks, client look (cube blocks with drawn textures), recipes are vanilla placeholders.

## Cargo decks (freight, diaithir-ports #229)
`roster.csv` has `deck_cells` (w x h x l in 1 m cells) and `deck_rating_kg` for vehicles with a load deck: ox cart, Ford F-1 pickup, VW Type 2 van (half-ton and van ratings, approximate). Plan: each such vehicle entity carries a `CargoBay` (attachment on the IV entity) sized from these columns, and loading uses `Containers.lift` / `Containers.lower` with the deck rating as the lifter weight so docks can reach containers riding a vehicle. Waiting on #229 to merge (CargoBay is only on that branch). Trucks, rolling stock and ships set their own decks.

## Integration tickets (plans/INTEGRATION_TICKETS.tsv, lane Cars)
About 60 pairings (CARxMAG, CARxELE, CARxFLU, CARxFUL, CARxHEA, CARxMEC, CARxPNE, CARxTRN, CARxNPC, CARxUPG, CARxWTH and so on). First: CARxMAG, magic-burning engines. ports #257 adds the `arcane_motor` engine class and magic fuel rows to the shared table (`Engine.accepts(id)`); it is not on the base yet. When it lands: merge it, add arcane engine variants to the pack, and decide how mana reaches an IV tank (IV fuel is a fluid name, so mana needs its own fluid name and a pump source). The rest follow the Age and upgrade rules of the lanes that own the other side.

## Port sources from the mod survey (plans/MOD_SURVEY.md)
MrCrayfish's Vehicle Unofficial (GPL-3) for car features, MineColonies (GPL-3) and the Guard Villagers fork (MIT) for NPC driver behaviour. Porting GPL-3 code makes the receiving module GPL-3, so anything ported from them goes in its own module or jar section with its licence recorded, not into this MIT module. Check IV first (it already has pumps, tanks, roads and control variables), then these.

## NPC drivers and roads (plan, not built)
- Wages (RPG #259): every NPC driver is paid crew, registered when hired with `global.WAGE_HIRE(entity, job, companyKey, vehicle)` where `vehicle` is the unit they crew. The crew retires from the payroll only when that vehicle has an autonomy module fitted (Age VII and later); `global.AUTONOMOUS_CREW` no longer exists. The autonomy and wages threads own those rules.
- Drivers: an NPC takes the controller seat of a parked vehicle and follows a route of waypoints between stations or depots (courier depots #112 and the waypoints module are the existing hooks). Steering and throttle go through IV's own controller variables, not new physics. Ground first, trucks and trains threads reuse the same route format.
- Road use: NPC drivers prefer higher tiers. Settlement and worldgen threads decide generated road networks.

## Tested
Headless NeoForge 26.1.2.109 server with the IV jar and the pack as its own mod jar (a dev run loads carts as a folder, which IV does not scan): all 13 vehicles, an engine, a wheel, the seat and the APC track register as items (`summon item ... mts:diaithirvehicles.<id>` gives the named item, a bogus id gives Air) and the log has no pack parse errors. `/diaithircarts roadtest` passes.

## Not tested
Driving, consumption units (`fuelConsumption` assumed mB per tick), client rendering, spawning a vehicle entity.
