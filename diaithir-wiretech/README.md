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

## Circuit network (package `net.diaithir.wiretech.circuit`)

Factorio-style signal networks, clean-room, in three eras that each lead to the next. Programs and rules: `circuit/Logic.java`.

- **Wires.** Red and green circuit wire coils per tier. Use a coil on one terminal, then another. Click the output half of a block (the side it faces) for its output, the other half for its input. Each colour is its own network; the signals on a network are the sum of every output wired to it. A node never sees its own output unless a wire closes the loop, and then one update later.
- **Tiers.** Mechanical (Age III, drum and cord: signals up to 99, 4 signal types, updates once a second, wires reach 8 m), Relay (Age IV: up to 9999, 16 types, every 4 ticks, 16 m), Electronic (Age VI: full 32-bit, 256 types, every tick, 32 m). A wire must be at least as fine as the blocks it joins.
- **Blocks** (each in all three tiers): constant combinator, arithmetic combinator (each, `+ - * / % ^ << >> and or xor`), decider combinator (each, anything, everything, and/or conditions, output 1 or the input count, red or green only operands), selector combinator (max, min, count, random, stack size), circuit reader (items by id, fluids in L, energy in J, charging power in W, `si:` signals, from the block behind it), circuit gate, lamp and speaker (act on a condition).
- **Control.** A gate gives redstone power while its condition holds, so anything that reads redstone obeys it: belt splitters and inserters, pumps, train stops and signals. Wiretech machines also stop while a touching gate is off. Other modules can call `CircuitBlock.gatesAllow(level, pos)`.
- **Programming.** Look at a block and run `/circuit set <program>`; `/circuit` or right-click with an empty hand shows its program, what it sees and what it says. Examples: `arith each * 2 -> each`, `decide a > 5 and red:b < 3 -> c input`, `select max 0`, `when each > 100`, `const a=10 minecraft:iron_ingot=5`. Not yet: a GUI screen (client work), wireless links, train-stop and belt-specific signal readers beyond redstone.
- **Making things.** Mechanical parts are hand-assembled (nothing else exists at Age III). Relay parts are made from mechanical ones in the arc furnace, electronic parts from relay ones. Run `python3 tools/gen_assets.py` then `python3 tools/gen_circuit_assets.py` to rebuild assets.
- **Checks.** `javac -d /tmp/cc src/main/java/net/diaithir/wiretech/circuit/{Signals,Tier,Color,In,Operand,Cmp,ArithOp,Cond,Logic,Net,Sim,CoreCheck}.java && java -ea -cp /tmp/cc net.diaithir.wiretech.circuit.CoreCheck` tests the pure core. `/wiretech selftest` now also builds a live circuit (wires, gate, lamp, reader on a chest, save, errors).
