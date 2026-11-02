Entity Culling: Design Spec (diaithir-client-render-culling)

1) Features

  • Entities and block entities fully occluded by opaque blocks are not rendered.
  • Visibility is computed asynchronously: a worker thread ray-casts from the camera to sample points on each target's AABB and publishes results in a concurrent map; the render thread only reads cached results (never blocks).
  • Results are cached per target and refreshed on a configurable interval; unknown or stale entries default to visible (fail-open).
  • Targets within a configurable distance are always rendered. Entities excluded by tag or by config always render.
  • Optional HUD debug line: culled/total counts for entities and block entities.
  • Toggle culling at runtime via keybind; changes take effect instantly.
  • Per-side opt-outs: disable entity culling or block-entity culling independently.

2) Config (client, ModConfigSpec, ModConfig.Type.CLIENT)

  • enabled (bool, true)
  • cullEntities (bool, true), cullBlockEntities (bool, true)
  • maxDistance (int 16–256, 128): beyond this, targets are not ray-tested (render/cull per vanilla frustum rules).
  • alwaysRenderDistance (int 0–32, 8)
  • recheckIntervalMs (int 25–1000, 100)
  • samplePoints (int 1–9, 5): AABB corners/centre rays per target.
  • workerThreads (int 1–4, 1)
  • maxTargetsPerTick (int 64–4096, 1024): bounds worker batch size.
  • entityWhitelist / blockEntityWhitelist (list of resource locations, never culled)
  • cullPlayers (bool, false)
  • cullInvisibleOrGlowing (bool, false): glowing entities are never culled by default.
  • debugOverlay (bool, false)

3) Keybinds (registered in RegisterKeyMappingsEvent, category "key.categories.diaithir")

  • Toggle culling (unbound by default)
  • Toggle debug overlay (unbound by default)

Handled in ClientTickEvent.Post via consumeClick().

4) Events/Hooks (all client-only)

  • RegisterKeyMappingsEvent (mod bus): register keys.
  • ClientTickEvent.Post: snapshot the camera position and the target lists, submit work to the executor, handle keybinds.
  • RenderLivingEvent.Pre / RenderPlayerEvent.Pre: cancel when the cached result is hidden. This covers living entities.
  • RenderLevelStageEvent: use it to collect per-frame render info only if needed. It cannot cancel renders, so it is unnecessary for culling itself.
  • For non-living entities and block entities, NeoForge exposes no cancellable pre-render event. Options without mixins:
  • Rely on a loader-provided extension, such as IClientBlockExtensions or IClientItemExtensions where relevant. Otherwise cull via the level-renderer frustum hook: ViewportEvent / a custom Frustum via RenderLevelStageEvent plus visibility check.
  • Where no hook exists, document that a minimal mixin to EntityRenderDispatcher#shouldRender and BlockEntityRenderDispatcher#render is the only reliable path. Isolate it behind a single optional mixin class so the rest of the mod stays event-based.
  • ClientPlayerNetworkEvent.LoggingOut: clear caches, stop the executor.
  • LevelEvent.Unload (client level): clear caches.
  • ModConfigEvent.Reloading: rebuild whitelist sets and resize the thread pool.
  • RenderGuiLayerEvent.Post (or RegisterGuiLayersEvent): debug overlay.

5) Edge Cases and Compat

  • Thread safety: the worker reads block states from a snapshot, never mutates anything. Use Level#getBlockState only on chunks already loaded and treat unloaded chunks as transparent (fail-open). Wrap in try/catch, and on error mark visible and disable the checker for N seconds.
  • Transparent or non-full blocks (glass, leaves, slabs): treat as occluding only if isSolidRender (full opaque cube); ignore everything else.
  • Spectator mode, camera in third person, and camera entities (shaders, freecam mods): ray origin is the actual camera position from Camera, not the player's eyes.
  • Large or multi-part entities (ender dragon), leashed entities, passengers: sample the full bounding box; always render riders of the player's vehicle and the vehicle itself.
  • Glowing, name-tagged, and currently hovered/targeted entities are never culled.
  • Block entities with oversized render bounds (beacon beams, end gateways): whitelist by default.
  • Compat: skip culling for entities with a custom renderer flagged via a NeoForge tag (#diaithir_client_render_culling:never_cull). Rendering-engine mods and shader packs may bypass the render dispatchers; the mod fails open. Minimaps and replay mods: disable when a replay/freecam camera type is detected.
  • Dedicated server safety: mod entrypoint registers nothing client-side unless FMLEnvironment.dist == Dist.CLIENT; all event classes