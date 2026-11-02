Durability Tooltip: Design Spec (diaithir-tooltips)

1) Feature list

- Appends a durability line to tooltips of damageable items: `Durability: 312 / 1561 (20%)`.
- Value coloured by remaining fraction via a green→yellow→red gradient (optional).
- Optional extra line for unbreakable items: `Unbreakable`.
- Optional "Advanced only" mode: show only when F3+H advanced tooltips are on.
- Optional compact format (`312/1561`) and optional percent-only format.
- Works for any item with `DataComponents.MAX_DAMAGE` and `DAMAGE` (modded tools/armor included).
- Line position selectable: after name, or at end of tooltip.

2) Config options (client config, `ModConfigSpec`, `ModConfig.Type.CLIENT`)

| Key | Type | Default | Purpose |
|---|---|---|---|
| `enabled` | boolean | true | Master toggle |
| `format` | enum `FULL, COMPACT, PERCENT_ONLY` | FULL | Text layout |
| `showPercent` | boolean | true | Append `(xx%)` in FULL/COMPACT |
| `colorByDurability` | boolean | true | Gradient vs. flat grey |
| `onlyWhenAdvanced` | boolean | false | Require advanced tooltips |
| `showUnbreakable` | boolean | true | Show line for unbreakable items |
| `hideWhenFull` | boolean | false | Hide on undamaged items |
| `position` | enum `AFTER_NAME, END` | END | Line placement |
| `lowThresholdPercent` | int 1–50 | 15 | Below this, line is red |

3) Keybinds

- Toggle Durability Tooltip (unbound by default): flips `enabled` at runtime and shows an actionbar confirmation. Category: `key.categories.diaithir_tooltips`.
- Registered via `RegisterKeyMappingsEvent` (mod bus); polled with `ClientTickEvent.Post` using `consumeClick()`.

4) Events/hooks (client only, no mixins)

- `ItemTooltipEvent` (game bus, `Dist.CLIENT`): the core hook. Read `event.getItemStack()`, `event.getFlags().isAdvanced()`, and mutate `event.getToolTip()`. Use `event.getContext()` where needed.
- `RegisterKeyMappingsEvent` (mod bus): register the toggle keybind.
- `ClientTickEvent.Post` (game bus): poll keybind.
- `ModConfigEvent.Loading` / `Reloading` (mod bus): refresh cached config values.
- `RegisterConfigScreenEvent`/`IConfigScreenFactory` extension point via `ModContainer.registerExtensionPoint`: config screen entry.
- Config registered with `modContainer.registerConfig(ModConfig.Type.CLIENT, spec)` in the mod constructor.

Stack inspection uses `stack.isDamageableItem()`, `stack.getDamageValue()`, `stack.getMaxDamage()`, and `stack.has(DataComponents.UNBREAKABLE)`.

5) Edge cases and compat

- Dedicated server safety: the main mod class is side-neutral and only registers config. All client classes live in `client` and are loaded solely via `@EventBusSubscriber(value = Dist.CLIENT)` or a `FMLEnvironment.dist == Dist.CLIENT` guard in the constructor. Never reference `Minecraft`, `KeyMapping`, or `Screen` from common code. Declare the mod as client-side-friendly (`displayTest` of IGNORE_SERVER_VERSION in `neoforge.mods.toml`) so servers without the mod and clients without server install both connect.
- Max damage 0 or negative: skip.
- Damage > max (corrupt/overflowed data): clamp remaining to 0 to avoid negative output.
- Division safety: compute percent as `round(100.0 * remaining / max)`; never show 0% for items with remaining > 0 (floor at 1%).
- Duplicate lines: other mods (e.g. durability viewers) may already add a line. Skip if any existing tooltip line's plain string starts with our translated prefix; no attempt to detect other mods' formats beyond that.
- Creative/search tooltips: `ItemTooltipEvent` fires for these too; `hideWhenFull` handles the clutter.
- Component-driven items: use data components only, not legacy NBT; respects custom `MAX_DAMAGE` overrides from datapacks.
- Insertion index: for `AFTER_NAME`, insert at index 1 (guard against empty list); `END` appends before mod-added attribution lines is not guaranteed, so it simply appends.
- Localisation: all strings through `Component.translatable` (`tooltip.diaithir_tooltips.durability`, `.unbreakable`); no hard-coded English.
- Performance: tooltip event is hot; cache config in plain fields, avoid allocations when item is not damageable (early return).

6) Package/class layout

```
net.diaithir.tooltips
├── DiaithirTooltips          // @Mod entry; registers config; dist-