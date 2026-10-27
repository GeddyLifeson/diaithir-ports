# Diaithir Brewing

Two hand-fed vessels for small-batch fermenting: the keg (Boidellienda, 6 items) and the fermenting barrel (Tundagrùduiri, 12 items). Clean-room, written from behaviour only (Brewin' and Chewin' is All Rights Reserved, so none of its code or art is used).

Right-click with a base (a bucket of water, milk and so on) to pour it in; the container comes back if the recipe names one. Right-click with each ingredient to drop it in. When the contents match a recipe exactly it starts fermenting. If a bigger recipe could still be completed from what is inside, it waits 5 seconds for more input (or an empty-hand click starts it now), so a recipe that is a subset of another still works. An empty-hand click while it ferments shows the percentage. When done, right-click with the recipe's container (a glass bottle unless it says otherwise) to draw one serving at a time. Sneak with an empty hand to dump an unfinished batch.

Cellar (an idea from Vintner's public description, written clean-room): each vessel scores 0 to 3 for being dark (block light 4 or less), cool (no flame, lava or lit furnace within 2 blocks) and sheltered (no open sky above). Fermenting runs at 0.5, 0.75, 1 or 1.5 times speed by score, and in a score-0 cellar a recipe that names a `spoiled` item can sour into it. An empty-hand click shows the percentage and the cellar score.

Automation (the wine-cellar idea from Create: Winery, written from its public description only): hoppers, pipes and Create belts see an item handler with an input slot that takes one valid base or ingredient at a time and an output slot that gives the finished drinks directly, with full transaction rollback. A fluid handler takes one whole bucket (1000 mB) of any fluid whose bucket is a recipe's base (water, for example) from Create pipes or pumps, the same as pouring the bucket by hand (AlcoCraft+'s pipe-the-water-in idea, clean-room).

Recipes are data files `data/<namespace>/diaithirbrewing/<name>.json` from any datapack (KubeJS can write them too):

```json
{"station": "keg", "base": "minecraft:water_bucket", "base_returns": "minecraft:bucket",
 "ingredients": {"minecraft:wheat": 3}, "ticks": 6000, "drink": "minecraft:honey_bottle", "servings": 4,
 "container": "minecraft:glass_bottle"}
```

`station` is `keg`, `fermenting_barrel` or `oak_cask`. An optional `abv` (above 0) is stamped on every poured drink as `custom_data` `{abv:X.Xd}` (the oak cask adds `aged:N` days from `ticks`). The oak cask is for ageing: leave `base` out and list the young drinks as `ingredients` (for example four of one wine), and it gives back the aged drink after `ticks`, with the same cellar speed and spoilage rules (see `tools/sample/.../selftest_aged.json`); `base_returns`, `container`, `spoiled` (an item) and `spoil_chance` (default 0.25) are optional. `tools/sample` holds a runnable example. Age gates are applied by the pack's `age_gate.js` on the items; the vessels themselves are recipes in `data/diaithirbrewing/recipe`.

`/brewing selftest` (owners) runs the first keg recipe through a fake player (pour, fill, ferment, save round trip, draw a serving, and the same through the item handler with a rolled-back insert) and prints PASS or what failed. `tools/gen_assets.py` writes the textures, models, loot, crafting and lang.

Source: https://github.com/GeddyLifeson/diaithir-ports/tree/claude/diaithir-brewing/diaithir-brewing. Licence: MIT.

## Create bridge (data only)

`python3 tools/gen_create_bridge.py <dir with data/*/diaithirbrewing> <out data dir>` writes heated-basin `create:mixing` recipes for every brew recipe whose base is water or milk and that has at most 9 ingredient items. Each is gated on `diaithir_create` being loaded, so it is inert without our Create port. One basin batch is one bucket of base and yields the recipe's servings. Not yet tested against the Create jar in game.

For the drinks KubeJS catalogue (no recipe JSON on disk), `node tools/gen_create_bridge_kubejs.mjs <dir with a_data.js and brewing.js> <out data dir> [--have items.txt]` runs the drinks scripts' own keg/barrel generator with KubeJS stubbed and turns each brew (each already has its own ingredient set) into a heated-basin mixing recipe: one bucket of water plus the same items, three servings. Brews over Create's 9-item limit are skipped and reported; `--have` lists the item ids a pack contains.
