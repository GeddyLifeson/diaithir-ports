# Vehicles, fuel, pumps, roads and drivers (design and status)

Mod-first: everything rides on the Immersive Vehicles (IV) stack already in the pack. IV already has fuel pumps, fuel hoses and jerrycans, so we reuse them and add only what is missing. No new jars: the road-vehicle pack is `assets/diaithirvehicles` inside the diaithir-carts jar (as the tank pack is inside diaithir-arms); the fuel API is in diaithir-units.

## Done in this PR
- `roster.csv`: one iconic real design per useful type, with year, Age, engine, fuel, source and a confidence flag. Ground: primitives first (travois, litter, chariot, ox cart, stagecoach, steam carriage, safety bicycle), then cars by role: military (jeep, armoured car, APC), utility (pickup, van, ambulance, fire truck, tow truck), travel (sedan, off-roader, bus, motorcycle, electric). Air, first kinds to latest: balloon, airship, Wright Flyer, Bleriot, Camel, Trimotor, DC-3, Spitfire, Me 262, Comet, Cessna 172, Huey, 707, Concorde, 747, F-16, F-22. `pack=pack` rows get an IV vehicle; `pack=data` rows are roster only (needs animal mounts, steam, electric, bike or aircraft parts: follow-ups).
- Age rule used (tech-tree thread owns the final call): up to 1885 = V (steam and animals), 1886 to 1945 = VI, 1946 on = VII; earlier primitives take I to III.
- `tools/gen_vehicles.py` writes the pack (13 vehicles) from the roster and fuel table; `age_gates_vehicles.json` and `vehicle_manifest.json` beside it. Blank mass, power or tank cells use body-class defaults (design defaults, not sourced).
- Fuel API in diaithir-units (`net.diaithir.units.fuel`): `Fuels` (registry from `fuels.csv`, 17 real fuels with MJ per L or kg, flow, range, IV consumption, upgrade-adjusted efficiency), `Engine` classes with typical efficiency, `FuelStore` (tank, jerrycan, pump and vehicle all speak it; transfer never creates or loses fuel), `SimpleStore`. SI only. Upgrade hook: `Fuels.upgradedEfficiency(base, lossCut)` takes the efficiency-module effect from diaithir-upgrades `Resolver`.
- `roads.csv`: dirt, gravel, cobbled, macadam, asphalt, concrete motorway with Age and speed factor.

## Fueling by Age (Marlon 11-03: both, gated)
1. Ages I to V manual: feed bags and troughs for draft animals (hay, oats), wood and coal shovelled into steam carriages, then jerrycans and barrels with a hand pump.
2. Age VI to VII pump stations: IV `fuel_pump` decor tied to storage tanks and pipes, refuels any parked vehicle automatically, takes payment via the economy thread. Loading racks for tanker trucks, rail tank cars and ships (all through `FuelStore.transfer`).
3. Refining comes from Foundry / tech-suite chains (crude to petrol, diesel, kerosene, avgas); the pack only needs the fluid ids in `fuels.csv`.

## NPC drivers and roads (plan, not built)
- Drivers: an NPC takes the controller seat of a parked vehicle and follows a route of waypoints between stations or depots (courier depots #112 and the waypoints module are the existing hooks). Steering and throttle go through IV's own controller variables, not new physics. Ground first, trucks and trains threads reuse the same route format.
- Roads: placeable road blocks per `roads.csv` tier with the speed factor applied to wheel friction for vehicles on them; Age gated, built with the bridges module's placement code where possible; NPC drivers prefer higher tiers. Settlement and worldgen threads decide generated road networks.

## Not tested
IV loading of the new pack (no headless boot with the full pack was run; the part files parse with IV's own classes, vehicle files need IV's custom point reader), consumption units (`fuelConsumption` assumed mB per tick), driving, any client rendering.
