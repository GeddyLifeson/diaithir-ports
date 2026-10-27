# Diaithir Wiretech

Clean-room power wires and generators for NeoForge 26.1.2. Written from the general idea of the genre only; no code, textures or names of any other mod. MIT.

- Wire connectors in three tiers (copper 16 blocks, electrum 24, steel 32), FE only. Wire coil items join two connectors of the same tier; wires are drawn in world as sagging ropes. Loss per block: 1.5%, 0.8%, 0.4%. Throughput per wire: 256, 1024, 4096 FE/t.
- Hand crank, windmill (needs a mostly open 5x5 frame, better higher up and in rain) and water wheel (per side touching flowing water) make torque. A dynamo beside them turns it into FE.
- Crusher and arc furnace with GUI, redstone control, side config and augments; data-driven recipes (`diaithir_wiretech:crusher`, `diaithir_wiretech:arc_furnace`).
- Sheetmetal blocks and climbable scaffolding.
- Self test: `/wiretech selftest` (owners) builds a crank, dynamo, wire and machines high above spawn and checks range, tier, loss, generators, machines, wire removal and the age gate.
- Diesel generator (right-click with diesel or biodiesel canisters), coke oven, blast furnace (needs sheetmetal above and on its left, right and back), and tier transformers (take FE from any side but the front, 5% loss).
- Not yet: multiblocks larger than these checks.

Assets and recipes come from `tools/gen_assets.py`. Age gate: `data/diaithir_wiretech/age_gate.json` (Age 4). Library entries: `library-sync/wiretech.json`.
