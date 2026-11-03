# diaithir-vehicles (MIT, clean-room)

Shared layer for road vehicles, ships and aircraft. Source tree only: no new jar. The existing Immersive Vehicles jar (`mts`) and the vehicle content packs in `diaithir-arms` are the vehicle bodies; this module is what they share. Modid `diaithirvehicles`, package `net.diaithir.vehicles`.

## Shared fuel capability (survey part 1, "Shared fuel API")
No standalone fuel API exists (survey: fuel lives inside MrCrayfish Vehicle, Immersive Vehicles and a few ARR mods), so this is written from the behaviour only: a tank holds one fuel, refuses a second kind until empty, and is filled and emptied by fluid pipes, cans and pumps.

- `FuelStore` (plain Java): `capacityL`, `amountL`, `fuelId`, `fill`, `drain`, `transferTo`, `energyJoules`. Amounts are whole litres (1 L = 1 mB). Player text shows litres and megajoules, never mB.
- `Fuel`: id (a `c:` fluid tag such as `c:diesel`), name and energy per litre in joules (gasoline 34.2 MJ/L, diesel 38.6, biodiesel 33.0, ethanol 21.1, kerosene 35.0, creosote oil 30.0). `Fuel.register(...)` adds a kind.
- `FuelTank`: the implementation vehicles embed. `burn(joules, efficiency)` turns shaft work into fuel use with whole-litre drains and carried credit, so per-tick calls add up right. `snapshot/restore` for rollback.
- `FuelCaps`: `ENTITY`, `BLOCK` (sided) and `ITEM` capabilities, all `diaithir:fuel_store`. A vehicle, ship or aircraft registers `FuelCaps.ENTITY`; anything that refuels asks `FuelLookup.entity/block`, which falls back to any other mod's NeoForge fluid handler (`FluidFuelStore`, fuel fluids only).
- `FuelFluidView`: a `FuelTank` shown as a NeoForge `ResourceHandler<FluidResource>` (transactional), so pipes and buckets work on it.
- Fluid tag `diaithir:fuel` (optional `#c:gasoline`, `#c:diesel`, `#c:biodiesel`, `#c:ethanol`, `#c:kerosene`, `#c:creosote`). A fluid is a fuel only if it is in `diaithir:fuel` and in its kind tag.
- Blocks and items: `fuel_tank` (8,000 L, Age V) and `jerry_can` (20 L, Age V; use on a tank to pour in, sneak-use to draw out, right-click any entity with a fuel store to refuel it).

`/diaithirvehicles selftest` places a tank high above spawn and checks capacity, filling, no-mixing, energy, can to tank both ways, and the fluid view amount. `./gradlew test` runs the pure-Java tests.

## Integration
Energy in SI (J per litre, MJ shown); shared `diaithir:fuel` and `c:` fluid tags; fluid handler capability on the tank so pipes and other mods' pumps work; Age V gate on both items; library-sync entry. Not yet: Codex entry, quest link, Global Events hook, 130-culture names (Diaithiris names come from the names pipeline), wages for fuel station workers.

## Not done
- No fuel fluids exist in the pack yet: the tag is optional and empty until a mod or `diaithir-units` Fuels supplies fluids. Until then the fluid view shows an empty tank and the fuel store works through cans and the capability only.
- Immersive Vehicles (`mts`, ARR jar) has its own fuel and is not changed here; bridging its engines onto `FuelCaps.ENTITY` needs a mixin or content-pack hook and a vehicle in a world (client join).
- No client look at the generated textures.

## Tracked drive (survey part 1: Flan's tank-track fold, clean-room)
Flan's Mod Reloaded is CC BY-NC-SA (not MIT as the survey table said; the licence was re-checked by Marlon's note), so nothing of it is used: no code, model, texture or text. The drive is written from how skid-steer tracked vehicles behave, in SI units.

- `tracks.TrackDrive` (plain Java, unit tested): two tracks driven to target speeds. Body speed is the mean of the tracks, yaw rate is their difference over the track gauge, so equal commands go straight, unequal ones turn, opposite ones pivot on the spot at half speed (ground drag). Engine power caps how fast the tracks can speed up; power is rolling resistance + acceleration + turn scrub; with no fuel the tracks only coast down. `Spec.LIGHT_TANK`: 12 t, 36 km/h, 300 kW, 2.4 m gauge, 35 % efficient.
- `tracks.TrackedChassis`: rideable entity. Sit in it, W/S throttle, A/D steer (reads the player's client input on the server). Its 600 L tank is a `FuelStore` (entity capability), and the engine burns the drive's joules through `FuelTank.burn`, so a pump, jerry can or the crayfish tank trailer refuels it. Climbs one block. `tracked_chassis` item (Age VI) sets one down.
- `/diaithirvehicles trackstest`: stone pad high above spawn, drives 5 s, checks distance, speed, fuel burnt, no drift, pivot turn in place, and a dry tank coasting to a stop.

Not done: hull, turret and gun (the Immersive Vehicles tank pack in `diaithir-arms` has the bodies; its tracks are static), tread animation, engine sound, damage (the health capability lives in `diaithir-vehicles-crayfish`), client look, a real drive with a player in the seat (the test drives it by calling the same step the input uses).
