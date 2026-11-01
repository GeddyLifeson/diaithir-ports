# Diaithir Forgeworks

A clean-room FE machine family for NeoForge 26.1.2, written from public descriptions of the idea only (no code, textures or names of any other mod). MIT.

Four machines, each in three casing tiers (Age IV, V, VI; speed 1x, 2x, 4x, FE buffer and intake scale the same way):

- Crusher (Brakkulon): ore to dust, with a chance of a bonus item. Ids `crusher_age4`, `_age5`, `_age6`.
- Alloy furnace (Meldrathex): two inputs to an alloy. `alloy_furnace_age4` and up.
- Sawmill (Skerrivane): log to planks, with a chance of sawdust.
- Crucible (Ulmarkesh): item to fluid in an internal tank, for example cobblestone to lava. Take it with a bucket (use an empty bucket on it) or pipes.

Every machine has a GUI: FE bar, progress arrow, input and output slots, one augment slot, a redstone button (ignore, needs a signal, stops on a signal) and a six-face side config. In the side config, left-click a face to cycle item access (closed, in, out, in and out) and right-click to switch FE intake for that face. Faces are world directions: D U N S W E. Hoppers and pipes see the machine through the normal item, energy and fluid capabilities.

Augments (one kind in the slot, stack size is the strength, up to 4): speed (+50% each, a little more FE per job), efficiency (-15% FE per job each), output chance (bonus chance x1.5 each).

Recipes are data. Each machine has its own recipe type (`diaithirforgeworks:crusher`, `alloy_furnace`, `sawmill`, `crucible`):

```json
{"type": "diaithirforgeworks:crusher",
 "inputs": [{"ingredient": "#c:ores/iron", "count": 1}],
 "result": {"id": "diaithirforgeworks:iron_dust", "count": 2},
 "bonus": {"id": "diaithirforgeworks:copper_dust"}, "bonus_chance": 0.1,
 "energy": 2400, "time": 200}
```

`inputs` has one or two entries (alloy furnace takes them in either slot order). `result`, `bonus` and `fluid` (`{"id": "minecraft:lava", "amount": 250}`) are each optional. `energy` is FE for the whole job, `time` is ticks at speed 1. A datapack or KubeJS can add recipes of these types. Defaults shipped: iron, copper and gold ore and raw ore to dust, cobblestone, gravel, blaze rod and bone, bronze from copper and iron (ingots or dust), planks from every log and stem type, lava from cobblestone, netherrack and obsidian. Dusts smelt and blast back to ingots.

Self test: `/diaithirforgeworks selftest` (owners) places machines high above spawn, powers them through the energy capability, feeds them through the item capability and checks dust, tier speed, side config, redstone mode, the speed augment, the menu, bronze, planks and lava, then removes them. Server config `diaithirforgeworks-server.toml` has every number.

Textures, models and recipes are written by `tools/gen_assets.py`. Display names are invented words; ids are English. The Age gate is `data/diaithirforgeworks/age_gate.json`.
