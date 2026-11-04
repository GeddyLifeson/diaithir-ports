# diaithir-automation-crafting / foundry

Foundry parts of the automation and crafting module, in the same layout as `diaithir-routing-networks/foundry`. Mod id `diaithircrafting`, package `net.diaithir.crafting.foundry`, ids `foundry_*`. Own clean-room design, own 64 px art (`tools/gen_assets.py`).

- **Operator Seat** (`foundry_operator_seat`): machines that need a worker call `Operators.work(level, pos, seconds)` with the `s` of their job and run it only on `true`. The worker eats food: nutrition x `secondsPerNutrition` (120 s default) seconds of work. Use the seat with food, or pipe food in (insert-only item handler, one kind). The lamp is lit while the worker has food or work in hand. Range `seatRange` (8).
- **Research packs 1 to 7** (`foundry_research_pack_N`): the items the Age gates consume. Recipes use stand-ins for the boss drops (echo shard, nether star, dragon breath) until the Ascendant drops exist.
- **machine_stats.json**: W and s per Foundry machine, keyed by progression node id, read through `MachineStats`. Graph values where the graph has them; the rest are set in `gen_assets.py` (SI, plans/UNITS.md).
- **datapack/foundry_ages**: one Researcher node per Age gate (`foundry:gate_ii` ... `gate_rodos`), chained, each unlocked by crafting packs. Not shipped in the jar (research that locks recipes never ships in the Researcher jar): drop it in the pack's datapacks.

Regenerate everything from the progression graph: `python3 -I tools/gen_assets.py [graph.json]`.
Test: `./gradlew runServer` then `diaithircrafting selftest`.

## Stations (diaithir_stations)
Second mod in the same jar, package `net.diaithir.stations`. Crafting grid only for simple everyday things; everything else is a part cast or forged at a station, an assembly of a few major parts, or a process.
- Recipes are data under `data/<ns>/diaithir_stations/station_recipe/*.json`: `station` (graph key), `stage` (part, assembly, process), `ingredients` (item or tag, count, up to 9), `result` (item template), `time_s`, and optional `operator` (needs a fed Operator Seat in range), `power` ("steam") with `w` (watts; the job draws w x time_s joules from a fuelled Boiler in range) and `basins` N (needs N Cast Basins touching the station). A reload-listener data type, not a vanilla recipe type, so not in the recipe book.
- Stations (`diaithir_stations:*`): `foundry_hearth_bench` (I), `kitchen_counter` (I), `altar_basic` (III), `foundry_wright_bench` (III), `foundry_research_bench` (III), `foundry_smeltery` (IV), `foundry_steam_forge` (V), `foundry_assembler` (VI), `foundry_reclaimer` (VII), `foundry_fabricator` (VIII). Graph keys: station.hearth_bench, station.kitchen, altar.basic, station.wright_bench, research.bench, station.smeltery, station.steam_forge, machine.assembler, machine.reclaimer, station.fabricator.
- Support blocks: `foundry_cast_basin` (IV, the smeltery pour needs them touching it: a multiblock of controller plus up to four basins, not a free-form wall yet) and `foundry_boiler` (V, hand-fed: one fuel item burns for its burn time at 50 kW and is stored as joules).
- Own recipes in the jar: research packs 2 to 7 at the research bench, the Operator Seat at the assembler. Pack 1 (a clay tablet) stays a grid recipe.
- Use: use with items to load (sneak: one), empty hand to start the first satisfied recipe, sneak empty hand to take the contents back. The result drops on top when the job ends.
- The kitchen counter here is the framework station. The graph also names `diaithirfarmkitchen:kitchen_counter` (built, crafts from nearby chests); the two need reconciling.
- Test: `diaithir_stations selftest`.
