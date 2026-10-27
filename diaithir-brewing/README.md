# Diaithir Brewing

Two hand-fed vessels for small-batch fermenting: the keg (Boidellienda, 6 items) and the fermenting barrel (Tundagrùduiri, 12 items). Clean-room, written from behaviour only (Brewin' and Chewin' is All Rights Reserved, so none of its code or art is used).

Right-click with a base (a bucket of water, milk and so on) to pour it in; the container comes back if the recipe names one. Right-click with each ingredient to drop it in. When the contents match a recipe exactly it starts fermenting. If a bigger recipe could still be completed from what is inside, it waits 5 seconds for more input (or an empty-hand click starts it now), so a recipe that is a subset of another still works. An empty-hand click while it ferments shows the percentage. When done, right-click with the recipe's container (a glass bottle unless it says otherwise) to draw one serving at a time. Sneak with an empty hand to dump an unfinished batch.

Automation (the wine-cellar idea from Create: Winery, written from its public description only): hoppers, pipes and Create belts see an item handler with an input slot that takes one valid base or ingredient at a time and an output slot that gives the finished drinks directly, with full transaction rollback.

Recipes are data files `data/<namespace>/diaithirbrewing/<name>.json` from any datapack (KubeJS can write them too):

```json
{"station": "keg", "base": "minecraft:water_bucket", "base_returns": "minecraft:bucket",
 "ingredients": {"minecraft:wheat": 3}, "ticks": 6000, "drink": "minecraft:honey_bottle", "servings": 4,
 "container": "minecraft:glass_bottle"}
```

`station` is `keg` or `fermenting_barrel`; `base_returns` and `container` are optional. `tools/sample` holds a runnable example. Age gates are applied by the pack's `age_gate.js` on the items; the vessels themselves are recipes in `data/diaithirbrewing/recipe`.

`/brewing selftest` (owners) runs the first keg recipe through a fake player (pour, fill, ferment, save round trip, draw a serving, and the same through the item handler with a rolled-back insert) and prints PASS or what failed. `tools/gen_assets.py` writes the textures, models, loot, crafting and lang.

Source: https://github.com/GeddyLifeson/diaithir-ports/tree/claude/diaithir-brewing/diaithir-brewing. Licence: MIT.
