Inventory Essentials: diaithir-inventory-ui

1) Feature list

  • Sort: keybind or small button sorts the hovered container (player inventory, chest, shulker, any vanilla-style menu). Groups by category, then item id, then count descending; stacks merged first.
  • Drag-move: hold the drag key and sweep the mouse across slots to quick-move each slot's stack to the "other" inventory (shift-click semantics).
  • Drag-distribute: with a held stack, sweep to drop one item per slot (vanilla right-drag already exists, so this is only an optional improvement for the same gesture).
  • Quick-move helpers: ctrl-click moves all matching items; hold-shift-scroll moves one item between containers; double-click-collect respects locked slots.
  • Slot locking: lock chosen player-inventory slots against sort/move.
  • Server side: sort is a validated server action; the client sends an intent packet and the server performs it with normal menu rules.

2) Config options

  • Client config: enableSortButton, buttonAnchor (TOP_RIGHT, etc.), dragMoveEnabled, sortCategoryOrder (list), shiftScrollMove, lockedSlotOverlay, showTooltipHints.
  • Server (common) config: allowSort, sortCooldownTicks (default 5), maxSortsPerSecond, sortBlacklistMenus (menu type ids), sortBlacklistBlockEntities.

3) Keybinds

All client-only, registered in a Controls category inventoryui:

  • Sort container (default R)
  • Drag-move modifier (default left Alt, held)
  • Lock slot (default L, with hover)
  • Move all matching (default left Ctrl+click, modifier only)

4) Events and hooks (NeoForge 26.1.2)

Mod bus (client):

  • RegisterKeyMappingsEvent: register keybinds.
  • RegisterClientTooltipComponentFactoriesEvent: not needed.
  • FMLClientSetupEvent: config screen registration via IConfigScreenFactory.

Game bus (client only):

  • ScreenEvent.Init.Post: add sort button to AbstractContainerScreen.
  • ScreenEvent.KeyPressed.Pre: handle sort and lock keys.
  • ScreenEvent.MouseButtonPressed.Pre / MouseDragged.Pre / MouseButtonReleased.Pre: implement drag-move, tracking the visited slot set and sending sends via MultiPlayerGameMode.handleInventoryMouseClick (vanilla packet path, so no custom protocol for moves).
  • ScreenEvent.MouseScrolled.Pre: shift-scroll move.
  • ScreenEvent.Render.Post: draw lock overlays and drag highlight.
  • ClientPlayerNetworkEvent.LoggingOut: clear lock state caches.

Mod bus (common):

  • RegisterPayloadHandlersEvent: register one playToServer payload, SortRequest(containerId), with an optional version so vanilla and unmodded clients are not rejected (.optional()).
  • ModConfigSpec registered via ModContainer.registerConfig.

Server handler: validate player.containerMenu.containerId matches, check cooldown and blacklist, permission via menu stillValid, then perform the sort as ordered ClickType.PICKUP sequences or direct Slot manipulation with a broadcastChanges() at the end.

5) Edge cases and compat

  • Dedicated server: all screen, keybind, and render classes live in .client and are only referenced from classes annotated @EventBusSubscriber(value = Dist.CLIENT); the payload record and server handler are in common packages with no net.minecraft.client imports.
  • Only sort slots whose Slot.mayPlace/mayPickup allow it; skip output slots (crafting results, furnace output), armor, offhand, and creative-menu slots.
  • Never sort inside menus with non-container slots (anvil, enchanting, villagers) via an allowlist: simple Container-backed slots only.
  • Mods adding custom slots or inventory screens: expose a tiny API tag, a menu-type blacklist, and an IInventorySortable-style check later only if requested; default to conservative behaviour.
  • Compat with other tweak mods: fire cancellation properly; do not cancel a click we do not handle.
  • Anti-abuse: rate limit; ignore the request if the menu changed between send and receive.
  • Stack data: compare with ItemStack.isSameItemSameComponents, never merging stacks with differing components.
  • Item order must be deterministic (id string, then component hash) so server and client previews agree.

6) Package and class layout

net.diaithir.inventoryui
  InventoryUiMod            (@Mod; config + payload registration)
  config/
    ClientConfig, ServerConfig
  network/
    SortRequest             (record payload + codec)
    ServerNetwork           (handler)
  sort/
    ContainerSorter         (pure logic, side-agnostic)
    SortCategories
  client/
    ClientEvents            (@EventBusSubscriber Dist.CLIENT: screen events)
    ClientSetup             (keybinds, config screen)
    DragMoveHandler
    SlotL