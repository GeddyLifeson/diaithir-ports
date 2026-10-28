# Port notes

Port of upstream GregTech CEu Modern (1.21.1, 8.0.0) to NeoForge 26.1.2.116 / Minecraft 26.1.2. Follows the official Mekanism 26.1 branch for API changes.

## Status

- `./gradlew compileJava` succeeds.
- `./gradlew runServer` reaches `Done (`. All registries fill: about 9,200 `gtceu` items, blocks, block entities, machines, recipe types, sounds and materials.
- Not play-ready. The gaps below mean most crafting, loot, UI and rendering do not work yet. Treat this as a base for further porting.
- Client was not booted or screenshotted in this pass.

## Not working or stubbed

Methods whose bodies could not be ported quickly carry a `/* ported stub */` marker (about 300 files; `grep -rl "ported stub" src`). They compile and return null, false, 0 or do nothing.

- Mixins: the mixin list is empty. Everything mixin-based needs events, access transformers or reload listeners.
- Recipes: GT machine and crafting recipes are not loaded. The dynamic data pack, post-recipe processing (recipe removal, clone and proxy recipes, RecipeMap rebuild) is not ported. Only vanilla and other mods' recipes load. `GTRecipeType.toGTRecipe` (vanilla to GT proxy recipes) is stubbed.
- Loot tables and dynamic tags: generation (MixinHelpers) is stubbed. Ore blocks do not drop, the `miner_pipe` loot table is invalid and the `gtceu:tool` global loot modifier does not decode.
- UI: ModularUI / LDLib screens, widgets, and `ClientLevelAccess`-style client helpers are stubbed or missing.
- Rendering: block and item tinting, render layers, block models and blockstate generation, armor models and equipment assets, fluid tint, item model predicates.
- Datagen: stubbed.
- Gameplay: cauldron washing of dusts, potion fluid (the `c:potion` tag logs missing references), world map and prospecting client cache (`ServerCache` is a no-op), item and block tooltips, food components, hazard and armor extras, `Tier` replacement (`compat/Tier`, `compat/SimpleTier`).
- Integrations removed or stubbed: JEI, EMI, REI, AE2, Create, FTB Quests, FTB Teams, Argonauts, Curios, KubeJS, CraftTweaker, Jade, TOP.
- Recipe ingredient placeholders: empty ingredients are not allowed in 26.1, so `SerializerIngredient.EMPTY` and `SerializerFluidIngredient.EMPTY` hold a barrier or water placeholder.
- `TOOL_ITEMS` and `ARMOR_ITEMS` tables are hash tables filled on generation instead of fixed arrays.

## Added

- `data/gtceu/age_gate.json`: every `gtceu` item at tier1 (Age 5).
