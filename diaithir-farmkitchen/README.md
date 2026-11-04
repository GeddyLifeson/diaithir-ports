# Diaithir Farm Kitchen

Table-driven kitchen, storage, sprinkler and decor blocks (clean-room), plus the folded farm mods below. One jar.

## Reaper (farm.reaper, Age V)
A steel cutter bar at the edge of a field. Every 2 seconds it cuts every fully grown crop (any `CropBlock`) in the strip in front of it (5 wide, 5 deep, at its own height), replants each at age 0, and inserts the drops into the item handler behind it (chest, barrel, hopper, storage-network port, Create belt); with no room it spills them. Bare hand turns it, redstone switches it off. `Reaper.harvest(...)` is the shared field-work call for tractors. `/diaithirfarmkitchen reapertest` (owners) builds a ripe 5x5 wheat field and a chest and checks the cut count, replanting, that unripe, out-of-strip and too-deep crops are left alone, and the wheat in the chest. Assets: `tools/gen_farm_assets.py` (S6940). It first shipped as `diaithircarts:reaper` in ports #275 and moved here; that id was never in a pack.

## Folded mods (MIT)
Agritech: Evolved (`agritechevolved`) and Hydrofarm (`hydrofarm`) are copied unchanged into this jar as extra mods with their own ids and namespaces, so existing ids, recipes and Age gates keep working and their two jars can leave the pack. See `THIRD_PARTY/README.md` for sources, commits and licences; `tools/fold_third_party.py` redoes the copy from fresh checkouts. JEI and Jade are compile-only. Datagen and Fabric are left out.
