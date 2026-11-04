# diaithir-vehicles (MIT, clean-room)

Shared layer for road vehicles, ships and aircraft. Source tree only: no new jar. Modid `diaithirvehicles`, package `net.diaithir.vehicles`.

Reworked after ports #257 and #277: this module no longer has its own fuel table, fuel capability, `diaithir:fuel` tag or tank block. It sits on:
- **diaithir-units** (#257): `Fuels` (ids from `fuels.csv`: diesel, petrol, biodiesel, ...), `Engine.accepts`, `FuelStore`, `SimpleStore`.
- **diaithir-bridges** (#277): the Carrier Tank (`diaithirbridges:carrier_tank`, 8000 units by default, `tankCapacity` config), `Ports.FUEL` (block capability `diaithirbridges:fuel_store`), fluid tag `c:<fuel id>`, `feed(engine, powerW, seconds)`.

What is here:
- `FuelHolder`: a vehicle entity (car, trailer, chassis, ship, plane) implements `fuelStore()`; pumps and cans find it through `FuelLookup.entity`. Blocks are found with `FuelLookup.block` = `Ports.FUEL`.
- `FuelBurn`: the Carrier Tank's `feed` maths (P*t / efficiency / J-per-unit) for a vehicle's own store, plus `canFeed(store, engine)`.
- `JerryCan` (Age V, 20 L, liquid fuels only): a `FuelStore` kept in a data component. Use on a Carrier Tank or pump to pour in, sneak-use to draw out, use on a `FuelHolder` entity to refuel it.

`/diaithirvehicles selftest` puts a Carrier Tank high above spawn and checks fill, no-mixing, tank to can and back, no solids in the can, engine acceptance and a burn of 100 kW for 10 s at 35 % (1 MJ shaft).

## Integration
Fuel ids and J per litre from the shared carrier table; `Ports.FUEL` and the Carrier Tank for storage and pipes; Age V gate; library-sync entry. Not yet: Codex entry, quest link, Global Events hook, Diaithiris name, wages.

## Not done
- Immersive Vehicles (`mts`, ARR jar) has its own fuel and is not bridged. No client look at the can icon.

## Tracked drive (survey part 1: Flan's tank-track fold, clean-room)
Flan's Mod Reloaded is CC BY-NC-SA (not MIT as the survey table said; the licence was re-checked by Marlon's note), so nothing of it is used: no code, model, texture or text. The drive is written from how skid-steer tracked vehicles behave, in SI units.

- `tracks.TrackDrive` (plain Java, unit tested): two tracks driven to target speeds. Body speed is the mean of the tracks, yaw rate is their difference over the track gauge, so equal commands go straight, unequal ones turn, opposite ones pivot on the spot at half speed (ground drag). Engine power caps how fast the tracks can speed up; power is rolling resistance + acceleration + turn scrub; with no fuel the tracks only coast down. `Spec.LIGHT_TANK`: 12 t, 36 km/h, 300 kW, 2.4 m gauge, 35 % efficient.
- `tracks.TrackedChassis`: rideable entity. Sit in it, W/S throttle, A/D steer (reads the player's client input on the server). Its 600 L tank is a `SimpleStore` exposed through `FuelHolder`, diesel-engine fuels only (`Engine.accepts`), and the engine burns the drive's joules through `FuelBurn.feed`, so a Carrier Tank pump or jerry can refuels it and the crayfish tank trailer can too. Climbs one block. `tracked_chassis` item (Age VI) sets one down.
- `/diaithirvehicles trackstest`: stone pad high above spawn, drives 5 s, checks distance, speed, fuel burnt, no drift, pivot turn in place, and a dry tank coasting to a stop.

Not done: hull, turret and gun (the Immersive Vehicles tank pack in `diaithir-arms` has the bodies; its tracks are static), tread animation, engine sound, damage (the health capability lives in `diaithir-vehicles-crayfish`), client look, a real drive with a player in the seat (the test drives it by calling the same step the input uses).
