# Diaithir Routing Networks (Foundry sorting parts)

Source tree of the `diaithir-routing-networks` consolidated module for NeoForge 26.1.2, package `net.diaithir.routing.foundry`, ids prefixed `foundry_`. It is built as its own source tree, never inside a folded jar. MIT, ported from Factorization (see NOTICE).

- **Day Barrel** (`foundry_barrel`): one kind of item, 64 stacks (config `barrelStacks`). Item on it stores, empty hand takes a stack (sneak: one). Pipes and inserters fill it from the top and sides and empty it from the bottom. Spills when broken.
- **Sieve** (`foundry_sieve`): placed against an inventory, it shows only filtered items to everything else, in and out. Eight ghost entries in four pairs; two items of one mod in a pair match that whole mod. Sneak-use with an item adds it, sneak-use empty-handed clears. A redstone signal inverts it. An empty filter passes everything.
- **Sorting Router** (`foundry_router`): items pushed into one face leave by the others. Matching-filter faces first, else faces with no filter; a face whose filter does not match never gets the item; never back out the input face. Same sneak-use editing, per face.

`/diaithirrouting selftest` (owners) checks the barrel (capacity, side rules, one kind, rollback), the filter pairs, the sieve (pass, block, redstone invert) and the router (matching, overflow, source face, rollback). `tools/gen_assets.py` draws the 64 px textures and writes models, lang, recipes, age gate (Age V) and the library entry.

Not ported: the hopping, silky and sticky barrel variants, minecart barrels, and servo and socket machinery. Left for later Foundry steps. Codex entry, quest and the 130-culture names come through the usual pipelines.
