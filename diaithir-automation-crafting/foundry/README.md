# diaithir-automation-crafting / foundry

Foundry parts of the automation and crafting module, in the same layout as `diaithir-routing-networks/foundry`. Mod id `diaithircrafting`, package `net.diaithir.crafting.foundry`, ids `foundry_*`. Own clean-room design, own 64 px art (`tools/gen_assets.py`).

- **Operator Seat** (`foundry_operator_seat`): machines that need a worker call `Operators.work(level, pos, seconds)` with the `s` of their job and run it only on `true`. The worker eats food: nutrition x `secondsPerNutrition` (120 s default) seconds of work. Use the seat with food, or pipe food in (insert-only item handler, one kind). The lamp is lit while the worker has food or work in hand. Range `seatRange` (8).
- **Research packs 1 to 7** (`foundry_research_pack_N`): the items the Age gates consume. Recipes use stand-ins for the boss drops (echo shard, nether star, dragon breath) until the Ascendant drops exist.
- **machine_stats.json**: W and s per Foundry machine, keyed by progression node id, read through `MachineStats`. Graph values where the graph has them; the rest are set in `gen_assets.py` (SI, plans/UNITS.md).
- **datapack/foundry_ages**: one Researcher node per Age gate (`foundry:gate_ii` ... `gate_rodos`), chained, each unlocked by crafting packs. Not shipped in the jar (research that locks recipes never ships in the Researcher jar): drop it in the pack's datapacks.

Regenerate everything from the progression graph: `python3 -I tools/gen_assets.py [graph.json]`.
Test: `./gradlew runServer` then `diaithircrafting selftest`.
