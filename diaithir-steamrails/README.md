# Diaithir Steamrails

Clean-room steam locomotion for our port of Create (modId `diaithir_create`, registry namespace `create`) on NeoForge 26.1.2. It reproduces the behaviour of the idea "steam locomotives for Create" and takes the best of related train mods, from public descriptions only. No code, texture, model, sound or name comes from another mod. Licence: MIT. Textures, models and the whistle are drawn by `tools/gen_assets.py`.

- **Boiler.** A firebox (`boiler_firebox`) plus shell blocks touching it make a boiler. It burns solid fuel (anything with a burn time), takes water, and builds pressure. Steam is kept as pressure times volume: size is two units for a firebox and one for each shell, water volume is 2000 mB a unit, a lit fire makes steam in proportion to the square root of size, so a bigger boiler heats slower but holds its pressure under load. The weakest block sets the pressure rating (firebox 12 bar, copper shell 7, steel shell 16); above it the safety valve vents steam and water. A smokestack touching the boiler gives 25 percent more steam and makes the chimney smoke. Comparators read pressure. Water also comes in through the NeoForge fluid capability (pipes of other mods; Create's own pipes are not reached, see below).
- **Steam cylinders and trains.** `steam_cylinder` blocks against a boiler on a carriage that rides one of the steam bogeys give the whole Create train its power. Each cylinder turns pressure above 1 bar into thrust (full at 6 bar, up to 3 times that); the boiler feeds about one cylinder per two size units. Thrust against train weight (0.4 plus 0.3 per carriage) sets the train's top speed, turning speed and acceleration, through one mixin on Create's `Train` (`maxSpeed`, `maxTurnSpeed`, `acceleration`). A train with steam carriages cannot move without pressure. Steam used grows with speed.
- **Tender.** `fuel_tender` holds 16000 mB of water and nine stacks of fuel. Boilers beside it in the world, or on any carriage of the same train, draw from it. Buckets, fuel and an empty hand work on tenders and boilers in the world and on a moving train (through Create's moving interaction behaviour).
- **Bogeys.** Two styles registered through Create's `BogeyStyle` API: `diaithirsteamrails:driver` (large, wheels of 12.5 px radius with side rods) and `diaithirsteamrails:pony` (small leading truck). They sit in Create's standard cycle group, so a wrench on a plain bogey cycles to them. Own block entity, own Flywheel visual and block-entity renderer, original part models.
- **Whistle and smoke.** `steam_whistle` touching a boiler blows on a redstone signal and repeats while it holds; a blast costs steam and the pitch follows pressure. On a train every whistle of the boiler blows when the train pulls out. A smokestack puffs while the fire burns, in the world and on a moving carriage.
- **Ages.** `data/diaithirsteamrails/age_gate.json`: Age V for the parts, firebox, copper shell, stack, cylinder, whistle and tender; Age VI for the steel shell. The bogey styles have no item (Create makes bogeys from railway casings).

## Test

`/diaithirsteamrails selftest` (owners). Builds a boiler yard above spawn and runs it in real ticks: layout, size and rating (a copper shell lowers it), a tender feeding the boiler, fuel burning, pressure rising, steam leaving through a whistle that redstone blows, a refused draw when cold, the fluid capability, the safety valve, the boiler and tender numbers surviving NBT (how a moving carriage keeps them), a layout scan over a map of blocks (how a contraption is scanned), a small boiler heating faster and a big one holding pressure better, the bogey styles in Create's `BOGEY_STYLES` and the standard cycle group, the bogey blocks and their block entities, the boiler's movement behaviour and the tender's interaction registered in Create, and Create's `Train` taking speed from pressure through the mixin. It prints PASS or what failed.

To run it: put the Create port jar in `run/mods/`, `./gradlew runServer`, and send the command. `-Pcreate_jar=` points the compile at the jar (default `/mnt/project-files/create-neoforge-26.1.2-6.0.9-diaithir.8.jar`).

Not covered by the test: a real train on real track (the carriage is assembled by Create's station on a screen), the client drawing of the bogeys, and the sound.

## Limits

- The boiler on a moving carriage keeps its numbers in the block entity data of the contraption and is ticked by Create's `MovementBehaviour`, not by a block entity. Redstone does not reach a moving whistle, so there it blows on pulling out instead.
- Create's own fluid pipes use a fluid API of the port that is not NeoForge's capability, so they do not fill a tender or boiler. Hand and capability filling work.
- Steam is internal, there is no steam fluid.
