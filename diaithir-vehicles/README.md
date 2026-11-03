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
