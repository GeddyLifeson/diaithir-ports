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

## Client rendering and GUI status (update)

- Client boots and renders machines, ores, material blocks, items and (static) pipes. Models for the custom loaders
  (`gtceu:machine`, `gtceu:pipe`, fluid containers) are generated into a dynamic resource pack
  (`data/pack/GTDynamicResourcePack`, registered as `gtceu:dynamic_assets`). Block tints use `BlockTintSource`.
- LDLib ModularUI (bundled) is ported to the 26.1 `GuiGraphicsExtractor` API through the compat `GuiGraphics`.
  Right-clicking a machine opens its fancy UI (see gt-shots/macerator-gui.png). The UI open path uses ATs
  (`lastSlots`, `remoteSlots`, `synchronizer`, `ServerPlayer.containerCounter/initMenu`, `Slot.x/y`, `Entity.setLevel`)
  instead of the old accessor mixins. LDLib network payloads are registered through the bundled `CommonProxy`.
- Pipes and cables use a custom block state model (`gtceu:pipe`, `client/model/pipe/PipeStateModel`): core plus one arm
  per connected side, read from the block entity connection mask at chunk-mesh time. LDLib gui textures are bundled
  (`assets/ldlib/textures`). Fluid models carry a tint from the fluid type extension color.
- Verified in a client: generator -> cable -> macerator chain (gtceu selftest) and the macerator GUI (gt-shots/).
- Known gaps: block entity renderers, item decorations, fluid tints, recipe viewer (JEI/REI/EMI) widgets,
  editor/compass LDLib packages, ShaderTexture and TextField input are still stubs.
- Dev scripts live outside the repo (gt_full.sh runs boot, scene, right-click UI and screenshot).
