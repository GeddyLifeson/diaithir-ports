Diaithir Early Window: Clean-Room Spec (diaithir-client)

Important up front: an *early* loading window (pre-mod-construction, before Minecraft's Window exists) cannot be built from ordinary NeoForge events. In NeoForge it is supplied through the FML early-display service (`net.neoforged.fml.earlydisplay`), loaded by `ServiceLoader`, not `@Mod` code. I'm basing this on the 21.x line's architecture; verify the service interface names against 26.1.2.109 before coding. The 3-class budget below reflects that.

1) Features

- Custom-themed early loading window (colours, background, progress bar style, logo) shown from FML bootstrap until the main menu.
- Progress display: stage label, per-mod/registry progress bar, optional log tail.
- Own fatal-error window: on a loading crash, replaces the default error screen with a themed window showing the error summary, failing mod(s), and buttons: Open Log, Open Crash Report, Copy Report, Quit.
- Graceful fallback: any init failure in the theme (GL error, missing asset) falls back to the stock window rather than crashing.

2) Config options

Early-stage config cannot use the normal `ModConfigSpec` (not loaded yet). Use a plain `diaithir-early.properties` read by the service from `config/`:

- `enabled` (bool, default true)
- `theme` (id; built-in default plus `custom`)
- `accentColor`, `backgroundColor`, `textColor` (hex)
- `backgroundImage` (path relative to config dir, optional)
- `showLogTail` (bool), `logTailLines` (int, 1-12)
- `errorWindowEnabled` (bool)
- `maxFps` (int, default 60), `fallbackOnError` (bool, default true)

3) Keybinds

None. This runs before input/keymapping exists. The error window uses mouse buttons plus keyboard shortcuts (C copy, L log, Esc/Q quit) handled directly on its own GLFW window.

4) Events/hooks

- Primary hook: FML early-display service provider (`ServiceLoader` entry in `META-INF/services/`, implementing the early-display provider interface). Receives lifecycle start/progress/handoff callbacks and owns the GLFW window until Minecraft takes over. This is *not* an event bus.
- Error handling: the same service's error-display path (invoked by FML on a `ModLoadingException`/crash) renders the fatal window.
- Optional `@Mod`-side (Dist.CLIENT only), for post-load polish:
  - `ClientTickEvent.Post` or `ScreenEvent.Opening` to detect the first `TitleScreen`, then signal the service that handoff is complete (and release resources).
- No mixins needed; the service-provider mechanism replaces the need to patch the loading screen.

5) Edge cases and compat

- Dedicated server: the service must only be discovered when `FMLEnvironment.dist == CLIENT`; no `net.minecraft.client.*` or GLFW/LWJGL class referenced from any common-loaded class. Keep the `@Mod` entry (if any) in a client-only package.
- Other early-window mods: only one provider can win. Respect FML's provider selection; if another provider is active, stay inert and log once.
- macOS: GLFW must run on the main thread (`-XstartOnFirstThread`); detect and fall back.
- Headless/CI, Wayland/old GL, or driver failures: catch `Throwable` during GL init and fall back to the stock window.
- HiDPI: scale framebuffer via content-scale queries; don't assume 1:1.
- Resource safety: free all GL objects and the GLFW window at handoff to avoid conflicts with Minecraft's own window creation.
- Error window must never throw; wrap everything and degrade to plain-text stderr plus a log path.
- Threading: render on a dedicated thread with a context made current only there; progress state shared via volatile/atomic fields.
- Accessibility: honour a reduced-motion toggle (static bar) as a config option if budget allows.

6) Package/class layout

```
net.diaithir.client
 ├─ earlywindow/
 │   ├─ DiaithirEarlyDisplayProvider   // ServiceLoader entry: lifecycle, progress, handoff, fallback
 │   └─ DiaithirTheme                  // properties loading, colours/assets, draw routines
 └─ error/
     └─ DiaithirErrorWindow            // fatal-error window: render, buttons, copy/open actions
```

Plus: `META-INF/services/<early-display-provider-interface>` pointing at `DiaithirEarlyDisplayProvider`, and `neoforge.mods.toml` marked client-side only.

Suggested next step: confirm the exact provider interface and error-callback signatures in the 26.1.2.109 FML sources, then I can draft the three classes.