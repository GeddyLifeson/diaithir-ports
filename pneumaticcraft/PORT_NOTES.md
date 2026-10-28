# Port notes

Port of upstream 1.21.1 code (8.2.19) to NeoForge 26.1.2.116 / Minecraft 26.1.2. Follows the official Mekanism 26.1 branch for API changes.

## Stubbed, removed or stashed

Source kept out of the build lives in `port-stash/`.

- Third-party integrations (JEI, EMI, Patchouli, The One Probe, Jade/Waila, Curios, CC:Tweaked, Mekanism, Immersive Engineering, Create, AE2, Botania, CraftTweaker, FTB Teams, FTB Filter System, Game Stages, Cold Sweat, Tough As Nails, CoFH, Forestry, EnderIO, BuildCraft, Thaumcraft, Fusion): removed. None of those mods are API-ready on 26.1 in the same form.
- Client side: restored, see the client restoration sections below for what remains.
- Datagen: stashed. The generated data under `src/generated/resources` is used as is (and converted to 26.1 formats where the format changed).

## Status

- `./gradlew compileJava` succeeds against NeoForge 26.1.2.116.
- A dedicated server boots with `./gradlew runServer`, prints `Done`, loads all recipes, loot tables, advancements and tags without data errors, and shuts down cleanly. The mod has not been play-tested beyond boot: no machine, drone, armor, or GUI interaction has been exercised.
- The client boots and joins a world; see the client restoration sections for remaining gaps.

## What changed to get here

- NBT/entity/block entity persistence moved to `ValueInput`/`ValueOutput`. Entities and block entities that kept `CompoundTag` code use a bridge (`saveLegacy`/`loadLegacy` stored under the `PncData` key), so existing save logic is untouched.
- Item, fluid and energy handling keeps the legacy internal handlers (`IItemHandler`, `IFluidHandler`, `IEnergyStorage`) and adapts them to the transfer API at capability registration and lookup (`compat/transfer/LegacyAdapters`, `compat/Caps`, `compat/HandlerCache`).
- Recipes: all machine recipes decode `ItemStackTemplate`/`FluidStackTemplate` and create stacks lazily, because item and fluid components are not bound when recipes are parsed. Special crafting recipes (drone upgrade/colour, gun ammo potion, compressor upgrade, pressurizable shaped) were rewritten for the new `ShapedRecipe`/`CustomRecipe` API.
- Data: recipe JSON converted to the 26.1 ingredient formats (plain string ingredients, nested `ingredient`+`count`/`amount` for sized ingredients, `neoforge:ingredient_type` for custom ingredients). `neoforge:components` ingredients were reduced to plain item ingredients. `c:slimeballs` became `c:slime_balls`. The old `global_loot_modifiers.json` list was removed (each file in `loot_modifiers/` is a modifier now).
- Config-driven durabilities for micromissiles and gun ammo are applied through `ModifyDefaultComponentsEvent` (`common/registry/ItemDefaults`).
- Block entity capabilities are registered for every PNC block entity type and filtered per instance, because block entity instances cannot be created during capability registration (item components not bound yet).
- Mixins re-verified: `MobAccess` removed (use `Mob#getDropChances`), furnace accessors renamed to the 26.1 field names, boat invoker returns `AbstractBoat`, minecart item accessor returns the entity type.
- `data/pneumaticcraft/age_gate.json` gates every item and block of the mod at Age V (`tier1`).

## Stubs and known gaps

Each of these compiles and boots but does not do its job yet.

- Client side: see "Client restoration (third pass)" for the remaining gaps.
- Villager trades and the PNC Mechanic profession trades (`VillagerTradesRegistration`): stashed. Amadron no longer offers vanilla villager trades (`VillagerTrades.TRADES` is gone).
- Camouflage dynamic occlusion and light, and elytra/gliding upgrade behaviour.
- Cat variant randomisation and painting variant cycling when hacking (the setters are private in 26.1).
- Cow sound easter egg now plays `entity.cow.milk`.
- Minigun client tick, jackhammer/tool behaviours that depended on removed `Tier`/tool APIs were reduced to the data-component equivalents.
- `ItemSearcherScreen` is a stub holding only its inventory, so the item searcher menu opens without a usable GUI.
- Third-party integrations: removed, as listed above. `thirdparty/computer_common` (CC/Lua) compiles but nothing registers it.
- Datagen is stashed. The generated data was edited by hand/script and must not be regenerated with the old datagen.


## Client restoration (second pass)

Restored and verified by joining a world under Xvfb (client run, `-Xmx6G`, no server) with screenshots in
`/mnt/project-files/tech-transport/pnc-shots/`:

- Item model definitions (`assets/pneumaticcraft/items/*.json`), custom model loaders replaced by plain models
  (fluid_container, rendered_item, pressure_tube static core), UVs clamped (26.1 rejects out-of-range UVs).
- Armor equipment assets (`assets/pneumaticcraft/equipment`, textures moved to `textures/entity/equipment`).
- Fluid models via `RegisterFluidModelsEvent` (`PncFluidClient`).
- Item fluid capabilities (tanks, liquid hopper, buckets, memory stick) on the transfer API (`ItemAccessFluidHandler`).
- GUI layer: `client/gui/compat/GuiGraphics` is a thin wrapper that keeps the old drawing API on top of
  `GuiGraphicsExtractor`; widgets, `AbstractPneumaticCraftContainerScreen`, pressure gauge, and about 45 machine
  screens are ported and registered (`PncClientSetup`). An access transformer makes the container screen size fields non-final.
- All bidirectional payloads now register a client handler (required by 26.1).

## Client restoration (third pass)

All client code compiles and the client boots and joins a world (screenshots in `pnc-shots/`).

- Block entity renderers: `client/render/LegacyBER` and `LegacyEntityRenderer` adapt the old immediate-mode
  `render(...)` code to the 26.1 extract/submit pipeline by recording vertices and replaying them with
  `submitCustomGeometry`; items and text are queued separately. Hand-written models extend `LegacyModel`.
  About 40 BERs registered (fluid tanks, gauges, assembly arms, door, elevator, sentry turret, aerial interface,
  tube module renderers), plus tube module layer definitions.
- Pressure tube connected model: `PressureTubeBlockModel` (`CustomUnbakedBlockStateModel`, blockstate type
  `pneumaticcraft:pressure_tube`) built from the tube core and connection parts.
- Entity renderers: drones (all variants), semiblock frames/agitator/gadget/crop support, vortex, ring,
  micromissile, tumbling block.
- Screens: programmer and its widget screens, Amadron, remote and editor, logistics frames, upgrade manager,
  security station hacking, tube module screens (registered in `PneumaticCraftRepressurizedClient`).
- Pneumatic armor client and HUD: upgrade handlers, trackers, options screens, HUD layers via
  `RegisterGuiLayersEvent`, 3D overlay via `RenderLevelStageEvent.AfterTranslucentFeatures`.
- Particles (air, bullet), key mappings with registered categories, micromissile tooltip, client mod entry class.

Known client gaps (not ported): area rendering (`AreaRenderManager` is a no-op), camouflage block model,
minigun special item renderer, armor colour tint and elytra layer, jackhammer drill bit item property,
block destroy particle handler, ProgWidget in-world renderers, AE2 branch of the logistics requester screen.
Only the Programmer/Security station screens and the BER/entity/tube visuals were screenshot-checked; the armor
HUD and most other screens compile but have not been exercised in play.
