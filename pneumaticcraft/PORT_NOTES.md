# Port notes

Port of upstream 1.21.1 code (8.2.19) to NeoForge 26.1.2.116 / Minecraft 26.1.2. Follows the official Mekanism 26.1 branch for API changes.

## Stubbed, removed or stashed

Source kept out of the build lives in `port-stash/`.

- Third-party integrations (JEI, EMI, Patchouli, The One Probe, Jade/Waila, Curios, CC:Tweaked, Mekanism, Immersive Engineering, Create, AE2, Botania, CraftTweaker, FTB Teams, FTB Filter System, Game Stages, Cold Sweat, Tough As Nails, CoFH, Forestry, EnderIO, BuildCraft, Thaumcraft, Fusion): removed. None of those mods are API-ready on 26.1 in the same form.
- Client side (`client/`, all GUIs, renderers, models, HUD, key handling): stashed in `port-stash/` while the common side is ported; see the status list below for what is back.
- Datagen: stashed. The generated data under `src/generated/resources` is used as is (and converted to 26.1 formats where the format changed).

## Status

- `./gradlew compileJava` succeeds against NeoForge 26.1.2.116.
- A dedicated server boots with `./gradlew runServer`, prints `Done`, loads all recipes, loot tables, advancements and tags without data errors, and shuts down cleanly. The mod has not been play-tested beyond boot: no machine, drone, armor, or GUI interaction has been exercised.
- There is no client in a usable state (see below). Items and blocks will have no models/GUI until the client side is ported.

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

- Client side: all screens (`client/gui/*`), HUD, renderers, key handling, sounds, tint handlers, area rendering, fluid client models (`PNCFluidRenderProps` is plain data), pneumatic armor client handlers. Originals are in `port-stash/`. Resource packs, item model definitions and equipment assets (armor textures) for 26.1 have not been written.
- Item fluid capabilities (`IFluidCapProvider`: tanks, liquid hopper, PNC buckets) are not registered. Fluid-container items will not accept or give fluids, and `FluidContainerIngredient` matching will fail until this is done.
- Villager trades and the PNC Mechanic profession trades (`VillagerTradesRegistration`): stashed. Amadron no longer offers vanilla villager trades (`VillagerTrades.TRADES` is gone).
- Camouflage dynamic occlusion and light, and elytra/gliding upgrade behaviour.
- Cat variant randomisation and painting variant cycling when hacking (the setters are private in 26.1).
- Cow sound easter egg now plays `entity.cow.milk`.
- Minigun client tick, jackhammer/tool behaviours that depended on removed `Tier`/tool APIs were reduced to the data-component equivalents.
- `ItemSearcherScreen` is a stub holding only its inventory, so the item searcher menu opens without a usable GUI.
- Third-party integrations: removed, as listed above. `thirdparty/computer_common` (CC/Lua) compiles but nothing registers it.
- Datagen is stashed. The generated data was edited by hand/script and must not be regenerated with the old datagen.
