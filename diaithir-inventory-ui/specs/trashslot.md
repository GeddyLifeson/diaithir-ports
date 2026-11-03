TrashSlot: Design Spec (NeoForge 26.1.2)

1) Features
- Adds a trash slot overlay to container screens (player inventory, creative inventory, chests, crafting table, and other `AbstractContainerScreen`s).
- Toggle key (default `T`) shows/hides the slot; state persists across sessions.
- Draggable: hold left mouse on the slot's border and drag to reposition. Position is stored relative to the screen's `leftPos/topPos`, so it follows the GUI across resolutions and GUI scales. Holding Shift while dragging snaps to a 1px-aligned grid; right-click on the border resets to the default position.
- Drop the cursor-held stack onto the slot to trash it. The stack is removed via a server-validated packet, not client-side only.
- Shift-click an inventory item while hovering the trash slot to trash the whole stack. Ctrl+click trashes one item.
- Undo buffer: the last trashed stack is kept (optionally), and middle-click on the slot restores it to the cursor if the cursor is empty.
- Tooltip: "Trash" plus the last trashed item when undo is enabled. Hover highlight matches vanilla slot highlight.
- Optional confirmation for items matching the config's protected list.

2) Config (client TOML, `ModConfigSpec`)
- `enabledByDefault` (bool, true)
- `defaultAnchor` (enum: BOTTOM_RIGHT, BOTTOM_LEFT, TOP_RIGHT, TOP_LEFT)
- `defaultOffsetX/Y` (int)
- `rememberPosition` (bool, true), plus persisted `posX/posY` (hidden)
- `allowedScreens` / `blockedScreens` (string lists of class names)
- `undoEnabled` (bool, true)
- `confirmForTags` / `protectedItems` (lists of tags/IDs, e.g. `c:ingots/netherite`)
- `confirmThreshold` (int, trash prompt above N items; 0 = off)
- `showInCreative` (bool, true)

Server config: `allowTrashing` (bool) so servers can disable it.

3) Keybinds
- `key.diaithir_inventoryui.toggle_trash` (default `T`, `KeyConflictContext.GUI`), category `key.categories.diaithir_inventoryui`.
- Registered via `RegisterKeyMappingsEvent`.

4) Events/Hooks
All client-only unless noted.
- `RegisterKeyMappingsEvent` (mod bus): register toggle mapping.
- `ScreenEvent.Init.Post`: attach trash slot state to eligible `AbstractContainerScreen`s (check allow/block lists).
- `ScreenEvent.Render.Post`: draw the slot, highlight, and tooltip.
- `ScreenEvent.MouseButtonPressed.Pre`: begin drag on border; handle drop on slot, shift/ctrl-click trashing, middle-click undo. Cancel the event when consumed.
- `ScreenEvent.MouseDragged.Pre`: update drag position, clamp to screen bounds.
- `ScreenEvent.MouseButtonReleased.Pre`: end drag and save position.
- `ScreenEvent.KeyPressed.Pre`: handle toggle key while a screen is open (key mappings don't tick in GUIs). Skip if an `EditBox` has focus.
- `ScreenEvent.Closing`: persist state and clear undo reference.
- `ModConfigEvent` (mod bus): refresh cached config values.
- `RegisterPayloadHandlersEvent` (common mod bus, both sides): register the `TrashStackPayload` (clientbound not needed; serverbound only). The server handler validates the menu is open, the container matches `menu.containerId`, and `allowTrashing` is true, then removes the carried stack or specified slot stack.
- `ClientPlayerNetworkEvent.LoggingOut`: clear transient state.

5) Edge cases and Compat
- Dedicated server safety: all rendering, screen, and keymapping code lives under a client-only package. Register the client subscriber only with `@EventBusSubscriber(value = Dist.CLIENT)` or guarded by `FMLEnvironment.dist`. The payload type and server handler live in a common class with no client imports.
- Desync: the client never mutates stacks directly. It sends the payload and relies on vanilla container sync to correct state. If validation fails (stale `stateId`), the server resyncs the menu.
- Creative inventory: carried-stack deletion is client-authoritative in vanilla (creative slot packets), so route via `ServerboundSetCreativeModeSlotPacket` semantics or skip the payload for the creative tab to avoid double handling.
- Overlap: if the slot collides with other mods' widgets (JEI/EMI side panels, curios slots), allow drag-away and clamp to the screen. Consume clicks only inside the slot rect; never block clicks elsewhere.
- Recipe viewer drag targets: expose a hit rect so viewer overlays don't treat the slot as empty space (optional soft compat, no hard dependency).
- Shift-click path: only intercept when the pointer is over the trash slot, to avoid breaking quick