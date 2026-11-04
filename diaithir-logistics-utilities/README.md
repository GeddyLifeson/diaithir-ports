# Diaithir Logistics Pipes

Routed item pipes for NeoForge 26.1.2. Part of the Foundry feature set (depot and request network, `diaithir-logistics-utilities` module). LGPL-3.0-only.

## Origin and credit
The design is taken from **Not Logistics Pipes** (https://gitea.rs485.network/pipes/notlogisticspipes, Modrinth `notlogisticspipes`), (c) Contributors to Not Logistics Pipes, licence LGPL-3.0-only (code) and CC-BY-SA-4.0 (its assets). Licence read from the upstream `LICENSE.md` and `REUSE.toml` on 2026-11-02; the licence text is in `LICENSE.md` here.

What was carried over: connected pipes form a network; items go to the sink with the highest priority, then the nearest, found with Dijkstra over the pipe graph; sinks take everything or a listed set of items; extractors pull from touching inventories on a timer; a stack that cannot be inserted is retried, rerouted, then dropped; a speed upgrade makes items faster.
What was rewritten: Java instead of Kotlin, no coroutines (stacks move hop by hop on the pipe's block-entity tick with the route stored in the stack), NeoForge 26.1 item transfer (`ResourceHandler<ItemResource>`) instead of Yarn/Architectury, no Fabric or Forge layers.
Not carried over yet: the PDA screen, per-pipe logic unit stacking, the Blockbench model loader and the travelling-box particle (items are not drawn in flight yet), chunk-boundary consistency repair (not needed: nothing is cached across ticks).
No upstream or Factorio art ships. Textures are drawn by `tools/gen_assets.py` at 64 px in our own steel look.

## Blocks
Vethrin (pipe), Dolvareth (sink pipe), Quelmurak (extractor pipe), Ashkirel (speed pipe). Sink pipe: use an item to list it, a stick for priority (+10, sneak -10), sneak with an empty hand to take everything.

## Test
`./gradlew runServer`, then `/diaithirlogistics selftest` (owners): builds an extractor, plain and speed pipes and two sinks above spawn, drives the ticks by hand, and checks priority, item filter, route, delivery and that nothing is left in flight.
