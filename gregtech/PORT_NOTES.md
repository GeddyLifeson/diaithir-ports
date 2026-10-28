# Port notes

Port of upstream GregTech CEu Modern (1.21.1, 8.0.0) to NeoForge 26.1.2.116 / Minecraft 26.1.2. Follows the official Mekanism 26.1 branch for API changes.

## Status

- `./gradlew compileJava` succeeds and `./gradlew runServer` reaches `Done (` in a few seconds of server time after data load.
- Server side works without mixins: a dynamic in-memory data pack (`data/pack/`) generates about 28,000 GT recipes, dynamic tags and loot tables (ores with silk touch and secondary drops, material blocks, cables, pipes, machines, surface rocks). `GTRecipePostProcessor` applies recipe removals and fills the GT recipe lookup.
- `/gtceu selftest` (admin) builds a diesel combustion generator, a tin cable and a macerator and checks that the machine receives power and produces output. It passes.
- Client was not booted or screenshotted yet. Rendering and ModularUI screens are the main unknown.

## Not working or stubbed

Methods whose bodies could not be ported quickly carry a `/* ported stub */` marker (`grep -rl "ported stub" src`). They compile and return null, false, 0 or do nothing.

- Mixins: the mixin list is empty. Behaviour is replaced by events, access transformers (RecipeManager.recipes, ReloadableServerResources.newComponents, MappedRegistry tag creation) and reload listeners.
- Lost behaviour: tool relocate/magnetic mined blocks, vanilla to GT proxy recipes (`GTRecipeType.toGTRecipe`), clone recipes, dungeon loot, boats, cauldron washing, KubeJS events.
- Item capabilities (drums, quantum tanks, buckets) are not registered. `ChemicalHelper.getMaterialInfo` is a stub, so recycling and decomposition recipes are incomplete.
- Recipe loading logs about 25,000 "can't be placed due to empty ingredients" warnings because tag ingredients are unbound at that point. This only affects recipe book display.
- UI: ModularUI / LDLib screens and client helpers are stubbed or missing.
- Rendering: block and item tinting, render layers, models, armor models, fluid tint, item model predicates are untested.
- Datagen is stubbed.
- Gameplay: potion fluid (`c:potion` tag logs missing references), prospecting client cache, tooltips, food components, hazard and armor extras.
- Integrations removed or stubbed: JEI, EMI, REI, AE2, Create, FTB Quests, FTB Teams, Argonauts, Curios, KubeJS, CraftTweaker, Jade, TOP.
- Empty ingredients are not allowed in 26.1, so `SerializerIngredient.EMPTY` and `SerializerFluidIngredient.EMPTY` hold a barrier or water placeholder.
- `TOOL_ITEMS` and `ARMOR_ITEMS` are hash tables filled on generation instead of fixed arrays.

## Added

- `data/gtceu/age_gate.json`: every `gtceu` item at tier1 (Age 5).
