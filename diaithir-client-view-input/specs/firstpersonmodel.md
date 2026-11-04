First Person Model: Behaviour Spec & Design

1) Features (user-visible)

  • Renders the local player's body (torso, legs, feet, worn armor) in first person, anchored to the camera so it appears below/behind the view.
  • Body yaw follows the vanilla body-rotation lag, so turning the head shows the body trailing naturally.
  • Head and helmet hidden by default so they never obstruct vision; optional helmet/hat visibility toggle.
  • Pose-aware: sneaking, swimming, crawling, gliding, sleeping, riding, and crouch-walking render with correct body pose, with a pitch-aware body offset so the torso doesn't clip the camera when looking down.
  • Vanilla first-person arm/hand rendering stays untouched (held-item and swing animations unchanged).
  • Works with player skin model (classic/slim), cape off by default, and held-item/armor layers.

2) Config options (ModConfigSpec, CLIENT type)

| Option | Default | Notes |
|---|---|---|
| enabled | true | Master switch |
| showHelmet | false | Render head-slot item |
| showHead | false | Render head model part (off = hidden) |
| bodyOffsetX/Z | 0.0 | Forward/back, side nudge (-1.0..1.0) |
| lookDownOffset | 0.25 | Extra back-shift scaled by pitch |
| renderInVehicles | true | While riding |
| renderWhileSleeping | false | Bed pose usually clips |
| renderWhileGliding | true | |
| renderWhileSwimming | true | |
| hideInSpectator | true | |
| modBlacklistedScreens | [] | Screen class names that suppress it |

3) Keybinds (KeyMapping, category key.categories.clientviewinput)

  • Toggle Body Visibility: unbound by default (suggest `B`); flips enabled for the session and saves config.
  • Toggle Helmet Visibility: unbound; flips showHelmet.

4) Events/hooks (client-only, NeoForge 26.1.2)

  • RegisterKeyMappingsEvent (mod bus) — register keybinds.
  • ClientTickEvent.Post (game bus) — poll keybind consumeClick(), apply toggles, show action-bar feedback message.
  • RenderPlayerEvent.Pre (game bus) — fires for the local player in first person on current NeoForge. Detect `entity == Minecraft.getInstance().player && options.getCameraType().isFirstPerson()`. If conditions pass, mark the render as "first-person body pass" and let it proceed; apply head/helmet hiding via the renderer's model-part visibility (set `PlayerModel.head/hat.visible = false` for the pass), restored in `RenderPlayerEvent.Post`.
  • RenderLevelStageEvent (e.g. AFTER_ENTITIES) — fallback if vanilla skips local player rendering at first-person: manually call `EntityRenderDispatcher.render` for the local player with a camera-anchored offset, using the event's PoseStack and buffer source.
  • ModConfigEvent.Loading/Reloading (mod bus) — cache config values into a client-side settings snapshot.
  • ClientPlayerNetworkEvent.LoggingOut — clear cached state.
  • ScreenEvent.Opening — optionally suppress body when blacklisted screens are open.
  • Camera position correction: if the body clips the near plane, adjust via `ViewportEvent.ComputeCameraAngles` only for read-only pose queries; avoid altering camera. Prefer offsetting the body, not the camera.

5) Edge cases & compat

  • Dedicated server safety: all classes live under `client` package; mod entrypoint registers client listeners only when `FMLEnvironment.dist == Dist.CLIENT`; `@EventBusSubscriber(value = Dist.CLIENT)` on every subscriber. No client class references in common code.
  • Shaders/OptiFine-style renderers (Iris): entity render path is standard, but the manual `RenderLevelStageEvent` fallback may render in the wrong shader pass; default to the `RenderPlayerEvent` path and only enable fallback if the player isn't otherwise rendered.
  • Other mods adding player layers/models (Epic Fight, Better Combat, Player Animator): honor their pose changes by rendering via the standard renderer; add a conflict list to disable pitch-offset when detected via `ModList.get().isLoaded`.
  • Armor/elytra layers can clip near plane; apply a small forward-clip offset instead of hiding.
  • Third-person/front toggle must fully revert hidden parts (restore visibility in Post even on exceptions via try/finally).
  • Freecam / camera-detach mods: only render when the camera entity is the local player.
  • Spectator, dead, and cutscene (non-player camera entity) states skip rendering.
  • Crawling/swim pose near-plane clipping: raise offset dynamically by pose.

6) Package/class layout (`net.diaithir.clientviewinput`)

```
net.diaithir.clientviewinput
├─ ClientViewInputMod            // @Mod entrypoint; guards client init by dist
└─ client
   ├─ ClientInit                 