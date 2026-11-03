3D Skin Layers: design spec (module `diaithir-entity-animation`)

1) Features

- Hat, jacket, sleeves, and pants layers render as extruded voxel boxes (per-pixel-cell cuboids) instead of flat planes, with real depth.
- Applies to the local player, other players, and any entity using a player model (e.g. mannequins, player-model NPCs).
- Per-part toggles: head, body, arms, legs.
- Distance-based LOD: full voxels near the camera, vanilla flat layer beyond a threshold.
- Transparent pixels are skipped; only opaque cells are meshed. Hidden faces between adjacent opaque cells are culled.
- Supports both Steve (4px arm) and Alex (3px arm) models.
- First-person hand/sleeve rendering also uses voxels (toggleable).
- Meshes are cached per skin texture and rebuilt only on skin change or resource reload.
- Respects the vanilla "Skin Customization" part toggles (a hidden part stays hidden).

2) Config (client `ModConfigSpec`, `ModConfig.Type.CLIENT`)

- `enabled` (bool, true)
- `renderHead/Body/Arms/Legs` (bool, true)
- `renderInFirstPerson` (bool, true)
- `lodDistance` (double, 16.0, range 2–64 blocks)
- `voxelScale` (double, 1.0, range 1.0–1.15). Outward inflation to avoid z-fighting with the base layer.
- `renderOnOtherPlayers` (bool, true)
- `renderOnNonPlayerModels` (bool, true)
- `maxCachedMeshes` (int, 64)

3) Keybinds

- `Toggle 3D layers` (default unbound), flips `enabled` and shows an overlay message.
- `Cycle LOD preset` (default unbound), switching between Near/Medium/Far distances.

Registered via `RegisterKeyMappingsEvent` in a client-only subscriber.

4) NeoForge events and hooks

All on client dist.

- `RegisterKeyMappingsEvent` (mod bus): keybinds.
- `ClientTickEvent.Post` (game bus): consume key presses.
- `AddClientReloadListenersEvent` (mod bus): reload listener that clears the mesh cache on resource/skin reload.
- `EntityRenderersEvent.AddLayers` (mod bus): iterate `event.getSkins()` (default and slim) and add a custom `RenderLayer` to each `AvatarRenderer`/player renderer; also attach to `PlayerModel`-based renderers found via `event.getEntityTypes()` where the renderer is a `LivingEntityRenderer` with a `PlayerModel`.
- `RenderPlayerEvent.Pre/Post` (game bus): set/clear a per-render context (LOD decision, part visibility, entity-distance) read by the layer. Avoids mixins.
- `RenderHandEvent` (game bus): first-person arm/sleeve voxel pass; cancel nothing, append the voxel render after the vanilla hand.
- `ModConfigEvent.Reloading` (mod bus): invalidate cached meshes when `voxelScale` or part toggles change.
- `ClientPlayerNetworkEvent.LoggingOut` (game bus): flush cache.

The custom layer replaces the work of the vanilla second-layer flat quads by hiding the flat overlay parts (setting the model's `hat/jacket/sleeve/pants` parts invisible) during the layer pass only when voxels render, then restoring visibility. Implemented in the render-context handlers to keep it mixin-free.

5) Edge cases and compat

- Dedicated server: all classes live in a `client` package. The mod entrypoint checks `FMLEnvironment.dist.isClient()` before referencing client classes; subscribers use `@EventBusSubscriber(value = Dist.CLIENT)`. No client class is touched from common code. Mod metadata declares client-side-only display (`displayTest = IGNORE_SERVER_VERSION`).
- Skin changes mid-session (Mojang skin refresh): key the cache on texture `ResourceLocation` plus a content hash.
- Elytra/cape/armor: voxel layer renders under armor layers. When an armor piece covers a part, optionally skip that part's voxels (config `hideUnderArmor`, default true) to prevent clipping.
- Invisible/spectator/translucent entities: fall back to vanilla path; glowing outline uses the same mesh with the outline render type.
- Other animation/model mods (e.g. first-person model, Better Combat, emote mods): read part poses from the model each frame via `PartPose` transforms, so posed limbs carry voxels automatically. No assumption of vanilla poses.
- Shader packs (Iris-style): use a standard entity-translucent-cutout render type so pipelines treat voxels as ordinary entity geometry.
- Slim arms: choose mesh dimensions from the model type, not the skin pixels.
- Performance: mesh built off the render thread where possible, uploaded lazily; cap vertices per mesh; LOD skips mesh binding entirely.
- Non-64x64 legacy 64x32 skins: no second layer exists