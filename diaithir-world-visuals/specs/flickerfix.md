FlickerFix: Design Spec (diaithir-world-visuals)

1) Feature list

- Steady night vision near expiry: vanilla's night-vision brightness flashes during the last 10 seconds (200 ticks). FlickerFix replaces this with a stable output.
- Selectable end-of-effect behaviour: hold full brightness until the effect ends, or fade smoothly to zero over the final N ticks (no strobing).
- Applies to the local player only. Works for potion, beacon-style, and command-applied night vision, since only the client-side effect instance and its remaining duration matter.
- Optional: also smooths the Conduit Power / Darkness-style gamma pulses? No. Scope is night vision only.

2) Config options

Client config (`ModConfigSpec`, `ModConfig.Type.CLIENT`), file `diaithir-world-visuals-flickerfix-client.toml`:

| Key | Type | Default | Meaning |
|---|---|---|---|
| `enabled` | boolean | `true` | Master switch |
| `mode` | enum `HOLD` / `FADE` | `FADE` | Behaviour in the final window |
| `fadeTicks` | int 1–200 | `40` | Fade length (FADE mode only) |
| `fadeCurve` | enum `LINEAR` / `SMOOTH` | `SMOOTH` | Interpolation (smoothstep vs linear) |
| `affectInfinite` | boolean | `true` | Treat infinite-duration effects as constant 1.0 |

3) Keybinds

- `key.flickerfix.toggle`: toggles `enabled` at runtime (unbound by default), registered via `RegisterKeyMappingsEvent`, category `key.categories.diaithir.worldvisuals`. Shows an overlay-bar message on toggle.

No other keybinds.

4) Events / hooks (NeoForge 26.1.2)

All client-side. Registered from a `@Mod(value = "diaithir_world_visuals", dist = Dist.CLIENT)` entrypoint or `@EventBusSubscriber(value = Dist.CLIENT)` classes.

- `FMLClientSetupEvent` (mod bus): nothing heavy; config registration is done in the mod constructor via `ModContainer.registerConfig(ModConfig.Type.CLIENT, spec)`.
- `RegisterKeyMappingsEvent` (mod bus): register toggle key.
- `ClientTickEvent.Post` (game bus): consume toggle key clicks; send overlay message.
- Brightness override: vanilla computes `GameRenderer.getNightVisionScale(LivingEntity, float partialTick)`, which flickers when `getDuration() <= 200`. No event exposes this in vanilla.

Event-first approach, in order of preference:
1. Check whether NeoForge 26.1.2 exposes a night-vision-scale hook (a render event or `ClientHooks` callback). If present, use it and return the computed value from `NightVisionMath`.
2. Fallback without mixins: on `ClientTickEvent.Pre`, if night vision has ≤200 ticks remaining, swap the local player's `MobEffectInstance` for one with a normalised remaining duration, so vanilla's own check never reaches the flicker branch. This only changes the client-side copy, which the server overwrites with its own update packet anyway.
3. Mixin only if neither works: a single `@Redirect`/`@ModifyReturnValue` on `getNightVisionScale`. Flag it as the last resort in the module docs.

- `ClientPlayerNetworkEvent.LoggingOut` (game bus): clear cached state.

5) Edge cases and compat

- Server-sent effect updates overwrite client durations each sync. The fallback approach must be idempotent and re-applied each tick, and must never write back to the server.
- Infinite effects (`MobEffectInstance.INFINITE_DURATION = -1`): return 1.0 when `affectInfinite` is true. Do not divide or compare on duration.
- `HOLD` mode: clamp the scale to 1.0 while remaining > 0; the effect still ends on time (no extended gameplay effect, visual only).
- Other night-vision mods and shaders: if a shader pack or another mod overrides gamma/night vision, FlickerFix must not fight it. Skip the override if `affectInfinite`-style hooks detect a foreign modifier, and make all logic purely read/compute so it composes.
- Spectator/third-person: apply only when the camera entity is the local player.
- Dedicated server safety: no client classes (`Minecraft`, `KeyMapping`, `GameRenderer`) referenced outside `Dist.CLIENT`-guarded classes. The mod entrypoint carries `dist = Dist.CLIENT`, so a dedicated server never loads the client package. `NightVisionMath` stays pure Java with no MC imports, making it safe anywhere.
- Config reload: read values via `Supplier`/`ModConfigSpec` getters per tick, not cached copies.

6) Package / class layout

```
net.diaithir.worldvisuals
├── WorldVisualsMod            // @Mod entrypoint (common-safe, registers nothing client-side)
├── flickerfix
│   ├── FlickerFixCl