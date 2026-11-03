# diaithir-vehicles-crayfish (GPL-3.0-only)

Fuel pump, trailer hitch and vehicle crash/repair rules from MrCrayfish's Vehicle Mod (Unofficial), rewritten for NeoForge 26.1.2 in its own GPL module (survey part 1: "FOLD MrCrayfish crash/health logic", "trailer hitch FOLDed", "copy the pump/tank design"). Art is drawn fresh (`tools/gen_assets.py`); the upstream models, textures and sounds are not used. See `NOTICE.md` for the licence check and the list of what was taken.

The cars thread owns the main MrCrayfish port; at the time of writing no PR for it was open, so this module does not touch or duplicate a vehicle framework. It hooks into whatever vehicles register: `FuelCaps.ENTITY` (from `diaithir-vehicles`) to be refuelled, `CrayfishCaps.HEALTH` to be hit and repaired, `CrayfishCaps.TOW` to pull a trailer.

- **Fuel pump** (Age V): stands on a fuel tank block (or anything with a fuel store). Use it to take the hose, use it again to hang it up; with the hose and an empty hand use a vehicle or the tank trailer to move up to 50 L. The hose lets go beyond 10 m. Pump on top, store below: same layout as upstream.
- **Trailer hitch** (Age V) and **tank trailer**: a two-wheel bowser holding 2,000 L. Hitch on the trailer hooks it to you or unhooks it; hitch on a vehicle with a tow bar passes it over. It hangs a 2 m hitch behind the bar, faces it, tilts to slopes (20 degrees at most) and snaps free past the hitch plus 6 m. Only a player link is saved.
- **Crash and repair**: `VehicleHealth` (100, hits, falls from 4 blocks while falling fast, half the distance in damage, destroyed below zero); **repair hammer** gives 5 health per blow. The server config `vehicleDamage` turns damage off.
- Server config (`diaithirvehiclescrayfish-server.toml`): `vehicleDamage`, `maxHoseDistance`, `trailerDetachThreshold`, `pumpLitresPerUse`.

`/diaithircrayfish selftest` (owners) builds a pump on a tank high above spawn, a tank trailer and a stand-in tow entity, and checks pump refuel, hitch distance and facing, damage, repair, fall rule and destruction. `/diaithircrayfish trailer` spawns a trailer. `./gradlew test` covers the pure logic.

## Integration
SI litres and joules through the shared fuel store; pump and trailer use `diaithir:fuel_store`, `diaithir:vehicle_health`, `diaithir:tow_point` entity capabilities so every vehicle thread's vehicles can join; fuel tank from `diaithir-vehicles` is the pump's tank; Age V gate and library entries; recipes use vanilla and the fuel tank. Gaps: Codex entry, quest link, Global Events (fuel shortage) hook, Diaithiris names, fuel-station wages.

## Not done
- Client look: textures and the trailer renderer (a stretched fuel tank block as placeholder body) were not seen. No hose rendering.
- Crash from running into things: upstream only does fall and hit damage (collisions are bumper cars), and that is all that is ported. Speed-based collision damage is for the vehicle bodies.
- Trailers for the Immersive Vehicles (`mts`) bodies: they need their own tow bar hook (content pack `fifthwheel` groups exist already in `diaithir-arms`); the capability is ready.
- The hose is a held state only (not saved over a restart).
