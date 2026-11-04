Better Advancements: Clean-Room Spec (diaithir-hud-overlays)

1) Feature list

- Search box on the advancements screen: filters visible advancement widgets by title/description (case-insensitive substring). Non-matching nodes dim; matching nodes highlight; tabs with no match show a muted icon.
- Match navigation: Enter jumps to next match, Shift+Enter to the previous; the view auto-scrolls and switches tab as needed.
- Tidier layout: configurable background tint, optional compact tab strip, tooltip clamped to screen bounds, and wrapped tooltip text at a configurable width.
- Tab handling: remembers last-open tab per world/server; Ctrl+Tab / Ctrl+Shift+Tab cycles tabs; scroll wheel over the tab strip switches tabs; number keys 1-9 select tabs.
- Status line: "x/y completed" per tab and overall, shown under the title.
- Filters: toggle to hide completed or show only incomplete advancements (visual dimming, not data removal).

2) Config options (ModConfigSpec, CLIENT)

- `search.enabled` (bool, true)
- `search.matchDescription` (bool, true)
- `search.dimOpacity` (double 0.1-0.9, 0.35)
- `layout.compactTabs` (bool, false)
- `layout.tooltipMaxWidth` (int 120-400, 220)
- `layout.backgroundTint` (int ARGB, 0)
- `tabs.rememberLast` (bool, true)
- `tabs.scrollSwitch` (bool, true)
- `tabs.numberKeys` (bool, true)
- `display.showProgress` (bool, true)
- `display.completionFilter` (enum ALL/HIDE_COMPLETED/ONLY_INCOMPLETE, ALL)

3) Keybinds (RegisterKeyMappingsEvent, category "key.categories.diaithir_hud")

- Focus search: Ctrl+F (screen-local, handled in screen key event; also registered mapping, default unbound, for rebinding)
- Next/previous tab: Ctrl+Tab / Ctrl+Shift+Tab
- Cycle completion filter: unbound by default

4) Events/hooks (all client, game bus unless noted)

- `RegisterKeyMappingsEvent` (mod bus): register mappings.
- `ModContainer.registerConfig(ModConfig.Type.CLIENT, ...)` plus `IConfigScreenFactory` registration for the config screen.
- `ScreenEvent.Init.Post`: if `screen instanceof AdvancementsScreen`, add the search `EditBox` and filter-toggle button via `event.addListener(...)`; restore the remembered tab.
- `ScreenEvent.Render.Post`: draw progress line, dimming overlays for non-matching widgets, and clamped tooltips.
- `ScreenEvent.KeyPressed.Pre`: handle Ctrl+F, Ctrl+Tab, number keys, Enter navigation; cancel the event when consumed (and don't steal keys while the EditBox is focused).
- `ScreenEvent.MouseScrolled.Pre`: tab-strip scroll switching.
- `ScreenEvent.Closing`: persist the last-open tab.
- `ClientPlayerNetworkEvent.LoggingIn` / `LoggingOut`: load/clear per-world tab memory.
- Widget access: read the `AdvancementsScreen` tabs/widgets via an access transformer (`accesstransformer.cfg`) rather than a mixin; the AT is only needed to expose existing private fields (tabs, selectedTab, widgets map, scroll), and no AT is required for the event hooks themselves.

5) Edge cases and compat

- Dedicated server: all classes referencing `net.minecraft.client.*` live in the `client` subpackage; the main mod class registers client wiring only when `FMLEnvironment.dist == Dist.CLIENT` (use `@EventBusSubscriber(value = Dist.CLIENT)` for the subscribers). No common-side code touches client classes.
- Other mods that replace or subclass `AdvancementsScreen`: only act on exact/instanceof matches, wrap AT field access in try/catch, and disable the features if the fields are missing.
- Mods adding custom advancement tabs: handle an arbitrary tab count; number keys apply only to the first 9.
- Search must not mutate advancement progress or send packets; it's purely visual.
- Empty search restores the normal render; resizing re-lays out the EditBox (`Init.Post` fires again).
- Large modpacks (thousands of advancements): cache lowercase titles per widget, debounce filtering by one tick.
- Keybind conflicts: screen-local keys defer to the focused EditBox and to other mods' `KeyPressed` cancellations (use low `EventPriority`).
- Per-world memory keyed by server address / world folder name; cleared on logout.

6) Package/class layout

```
net.diaithir.hudoverlays
├─ HudOverlaysMod              // @Mod; registers config; client wiring guard
├─ config
│  └─ AdvancementsConfig       // ModConfigSpec CLIENT
├─ client