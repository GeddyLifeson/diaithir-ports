Clean-room design: Dynamic Lights (client-side)

1) Feature list

- Held items emit light: main hand and offhand of the local player and other players. Luminance comes from the item's block light level (torch, glowstone, lantern, etc.), with the brighter hand winning.
- Dropped item entities emit light based on the stack's item.
- Mobs/entities: burning entities emit light (level 15); glowing/charged creepers, blazes, magma cubes, glow squids, allays, and fireballs use per-type luminance.
- Armor/equipment slots optionally count (e.g. a lantern-type item on the head).
- Light follows movement smoothly, updating per tick and fading out when the source despawns or the item is swapped.
- Underwater behaviour: optional reduction of light underwater.
- Light is purely visual: the client modifies the lighting it renders, with no server-side light changes, no block changes, and no mob spawning effects.

2) Config options (ModConfigSpec, CLIENT type)

- `enabled` (bool, true)
- `heldItems`, `droppedItems`, `entities`, `selfOnly` (bool): per-source toggles
- `maxLightLevel` (int 0–15, default 15): clamp
- `updateRateTicks` (int 1–10, default 2): recompute cadence
- `maxDistance` (int 8–64, default 32): ignore sources farther than this
- `waterSensitive` (bool, true): halve light in water
- `itemLuminanceOverrides` (list of `modid:item=level`)
- `entityLuminanceOverrides` (list of `modid:entity=level`)
- `blacklistEntities` (list of entity type IDs)
- `debugOverlay` (bool, false)

3) Keybinds

- Toggle dynamic lights (unbound by default; registered in a "Diaithir Visuals" category)
- Cycle quality mode (Off / Fast / Full), unbound by default

4) Events / hooks (client only)

- `RegisterKeyMappingsEvent` (mod bus, client): register keybinds.
- `ClientTickEvent.Post` (game bus): poll key presses; every `updateRateTicks`, iterate loaded entities in the client level (`level.entitiesForRendering()`), compute luminance, update the source map, and mark chunk sections dirty when a source's block position changes.
- `ClientPlayerNetworkEvent.LoggingOut` / `LoggingIn`: clear all tracked sources.
- `EntityLeaveLevelEvent` (filter `level.isClientSide()`): remove the source immediately.
- `RegisterClientCommandsEvent` (optional): `/dynlights reload|debug`.
- `ConfigReloadEvent` / `ModConfigEvent.Reloading`: refresh cached overrides.
- Core limitation: NeoForge has no event that alters per-position light values in the lightmap/chunk light pipeline. The only non-mixin route is reading the light via the vanilla light engine; to inject values you need either (a) a small, well-isolated mixin into the client light-getter used by chunk meshing (e.g. `LevelRenderer`/`LevelLightEngine` brightness lookup), or (b) a client-side `LightTexture` approach. Plan: ship one narrowly-scoped mixin (`getLightColor` hook) as the single exception, and keep all logic outside it in `DynamicLightEngine`. Verify the exact method target against the 26.1.2 Mojang mappings before writing it.

5) Edge cases and compat

- Dedicated server: all classes live under a `client` package; the mod entrypoint registers client handlers only when `FMLEnvironment.dist == Dist.CLIENT` (or via `@EventBusSubscriber(value = Dist.CLIENT)`); the mixin config lists the mixin under `client` only.
- Shaders (Iris-style packs) and alternate renderers may bypass the vanilla light getter: detect and disable with a one-time chat/log notice.
- Sodium-like chunk renderers: the mixin may not apply; fail soft (catch, log, set `enabled=false` internally).
- Overlapping sources: use the max, not the sum, to match vanilla light semantics.
- Chunk-rebuild cost: throttle dirty marking, coalesce per section, and cap rebuilds per tick.
- Unloaded chunks and dimension changes: purge sources on `ClientPlayerNetworkEvent` and on level change.
- Invisible/spectator entities and entities in unrendered chunks are skipped.
- Modded items: respect an item's block-light level via its default block state when it is a `BlockItem`, otherwise use overrides only.
- Thread safety: the source map is mutated on the client thread only; the mixin reads from the render thread, so use a concurrent map or copy-on-write snapshot.

6) Package/class layout (`net.diaithir.worldvisuals`)

- `WorldVisualsMod`: mod entrypoint; client init guard
- `lights/client/DynamicLightEngine`: source tracking, luminance query, dirty marking
- `lights/client/LightSource`: record (entity id, pos, level)
- `lights/client/LuminanceResolver`: item/entity/override logic
- `lights/client/DynamicLightsConfig`: ModConfigSpec