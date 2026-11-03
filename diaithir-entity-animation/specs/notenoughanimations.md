diaithir-entity-animation: Design Spec

1) Feature List

All behaviour is client-side, cosmetic, and applies to any rendered player (local and remote).

  • Held-item poses: arm angles adjust per held item category (tools, blocks, ranged, food, shield). Idle swing is damped; two-handed items raise both arms.
  • Use-item animations: eating/drinking raises the hand toward the mouth with a chew bob; bow/crossbow/trident/spyglass get tuned draw/aim poses; shield blocking tucks the arm.
  • Sneaking: smooth crouch lerp (no snap), torso lean, bent knees, arms drawn in.
  • Riding: seated leg splay on horses/pigs, legs together in boats, arms forward when steering.
  • Locomotion polish: arm sway scales with speed; head follows body yaw with a damped lag.
  • Idle breathing: subtle chest/arm oscillation when stationary.
  • Pose smoothing: all poses interpolate over a configurable time rather than snapping.

2) Config Options

(`ModConfigSpec`, type CLIENT)

| Key | Type | Default |
|---|---|---|
| enabled | bool | true |
| heldItemPoses | bool | true |
| useItemAnimations | bool | true |
| sneakAnimation | bool | true |
| ridingAnimation | bool | true |
| idleBreathing | bool | true |
| headLag | bool | true |
| smoothingSpeed | double 0.05–1.0 | 0.35 |
| intensity | double 0.0–2.0 | 1.0 |
| applyToOtherPlayers | bool | true |
| firstPersonArms | bool | false |
| itemOverrides | list<string> | [] (`modid:item=pose` entries) |

3) Keybinds

Registered via RegisterKeyMappingsEvent, category `key.categories.diaithir_entityanimation`:

  • Toggle animations (unbound by default; U suggested)
  • Open config screen (unbound)

4) Events/Hooks

Mod bus (client only):

  • FMLClientSetupEvent: init pose registry.
  • RegisterKeyMappingsEvent: keybinds.
  • ModConfigEvent.Reloading: rebuild override map.
  • RegisterClientReloadListenersEvent: optional datapack-style pose JSON from resource packs.
  • RegisterConfigScreen via `IConfigScreenFactory` extension point in `ModContainer`.

Game bus (client only):

  • ClientTickEvent.Post: handle keybind, advance smoothing state, sample player speed/pose.
  • RenderLivingEvent.Pre/Post<AbstractClientPlayer, …>: in 26.1 the render-state pipeline extracts state first, so capture data in the extraction phase via `ExtractRenderStateEvent` (if exposed for this version) into a custom attachment on `PlayerRenderState`.
  • Part adjustment: apply deltas to model parts after `setupAnim`. Use `RenderLivingEvent.Post`-adjacent hook only if `setupAnim` results are mutable at that stage; otherwise a single justified mixin on `PlayerModel#setupAnim` (tail) is the fallback. Verify against 26.1.2 sources before committing.
  • PlayerEvent.PlayerLoggedOutEvent (client): clear per-player smoothing cache.
  • LevelEvent.Unload (client): clear caches.

5) Edge Cases & Compat

  • Dedicated server safety: all classes live under `client/`; the main mod class registers client listeners only when `FMLEnvironment.dist.isClient()`; `@EventBusSubscriber(value = Dist.CLIENT)` on every subscriber. No client imports in common classes. Declare no server-side components.
  • Other animation mods (Player Animator-based, Epic Fight, Better Combat): detect via `ModList.get().isLoaded`; yield when a foreign animation owns the player (flag via soft API check) to avoid double-posing.
  • Custom player models/skins (slim vs. wide, third-party renderers): operate on part deltas, not absolute rotations, so arbitrary rigs remain valid.
  • Elytra, swimming, crawling, sleeping, dead: bypass; vanilla poses take priority.
  • First-person: arm changes are off by default to avoid clipping with the hand renderer.
  • Armor/held-item layers follow parent model transforms; verify no extra offsets for modded layers.
  • Cache: keep smoothing state in a `WeakHashMap` keyed by entity UUID; prune on unload.
  • Performance: pose math is allocation-free per frame; skip entities beyond a distance threshold (e.g., 48 blocks).
  • Resource pack overrides: invalid pose JSON logs a warning and falls back to defaults.

6) Package/Class Layout

```
net.diaithir.entityanimation
├─ EntityAnimationMod // @Mod, dist-guarded client init
├─ config
│ └─ AnimationConfig // ModConfigSpec CLIENT
├─ client
│ ├─ ClientBootstrap // mod-bus subscribers
│ ├─ ClientEvents // game-