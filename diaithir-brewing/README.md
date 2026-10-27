# Diaithir Brewing

Two hand-fed vessels for small-batch fermenting: the keg (Boidellienda, 6 items) and the fermenting barrel (Tundagrùduiri, 12 items). Clean-room, written from behaviour only (Brewin' and Chewin' is All Rights Reserved, so none of its code or art is used).

Right-click with a base (a bucket of water, milk and so on) to pour it in; the container comes back if the recipe names one. Right-click with each ingredient to drop it in. When the contents match a recipe exactly it starts fermenting and shows the percentage on an empty-hand click. When done, right-click with the recipe's container (a glass bottle unless it says otherwise) to draw one serving at a time. Sneak with an empty hand to dump an unfinished batch.

Recipes are data files `data/<namespace>/diaithirbrewing/<name>.json` from any datapack (KubeJS can write them too):

```json
{"station": "keg", "base": "minecraft:water_bucket", "base_returns": "minecraft:bucket",
 "ingredients": {"minecraft:wheat": 3}, "ticks": 6000, "drink": "minecraft:honey_bottle", "servings": 4,
 "container": "minecraft:glass_bottle"}
```

`station` is `keg` or `fermenting_barrel`; `base_returns` and `container` are optional. `tools/sample` holds a runnable example. Age gates are applied by the pack's `age_gate.js` on the items; the vessels themselves are recipes in `data/diaithirbrewing/recipe`.

`/brewing selftest` (owners) runs the first keg recipe through a fake player (pour, fill, ferment, save round trip, draw a serving) and prints PASS or what failed. `tools/gen_assets.py` writes the textures, models, loot, crafting and lang.

Source: https://github.com/GeddyLifeson/diaithir-ports/tree/claude/diaithir-brewing/diaithir-brewing. Licence: MIT.
