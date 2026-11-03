Longer Chat History: Design Spec (diaithir-chat-client)

1) Feature list

  • Raises the client chat history cap from vanilla's 100 lines to a configurable value (default 1000, max 10000).
  • Applies to both the visible chat component and the "sent messages" recall list (Up/Down arrow history in the chat box).
  • Existing history is preserved when the limit changes; lowering the cap trims oldest lines on next message.
  • History persists for the session across world/server switches only if the option below is enabled; otherwise vanilla behaviour (cleared on disconnect).
  • Optional chat-scroll improvement: Page Up/Down jumps scale with history size (no extra UI).

2) Config options (ModConfigSpec, CLIENT type, `diaithir-chat-client.toml`)

  • `chatHistoryLines` (int, 100–10000, default 1000): max stored chat lines.
  • `sentHistoryLines` (int, 10–2000, default 200): max recalled sent messages.
  • `persistAcrossWorlds` (boolean, default false): keep chat lines on disconnect.
  • `enabled` (boolean, default true): master switch, restoring vanilla caps when false.

Config is client-only; never registered on a dedicated server.

3) Keybinds

None required. Use vanilla chat scrolling (mouse wheel, Page Up/Down). An optional "Clear chat history" keybind is out of scope for v1.

4) Events/hooks (NeoForge 26.1.2, client-side)

The vanilla cap is a hardcoded constant in `ChatComponent`, so a pure event cannot change it directly. Preferred approach keeps mixins as an explicitly last resort:

  • `ClientChatReceivedEvent`: observe incoming messages to keep our own bookkeeping (e.g., persistence buffer); cannot raise the cap alone.
  • `ClientPlayerNetworkEvent.LoggingOut` / `LoggingIn`: handle the persist-or-clear logic for `persistAcrossWorlds`.
  • `ModConfigEvent.Loading` / `Reloading`: apply config changes and trim history if the cap shrank.
  • `FMLClientSetupEvent`: register the client config and perform any one-time reflective/accessor setup.

Cap override: no public hook exists for the 100-line limit. Two clean-room options, in order of preference: (a) an access transformer (`META-INF/accesstransformer.cfg`) to make the cap field accessible/non-final, then set it from config on `Loading`/`Reloading`, or (b) a single minimal mixin on the trimming call site if the constant is inlined and cannot be changed via AT. Document whichever is chosen; verify against the 26.1.2 Mojang-mapped `ChatComponent` before implementing.

5) Edge cases and compat notes

  • Dedicated server safety: all classes live under a client-only package; the mod entry point registers client listeners only when `FMLEnvironment.dist == Dist.CLIENT`. Any `net.minecraft.client.*` reference must be unreachable from common/server class loading. Use `@Mod(dist = Dist.CLIENT)` where supported.
  • Memory: 10000 wrapped lines is modest, but wrapped-line counts grow with narrow windows; the cap applies to stored (wrapped) lines, not original messages. Note this in the config comment.
  • Resize/reflow: chat is re-wrapped on window resize; ensure the raised cap is applied after `rescaleChat`/refresh so trimming doesn't drop lines prematurely.
  • Other chat mods (e.g., chat-heads, timestamps, searchable chat): they typically hook `ChatComponent` or `ClientChatReceivedEvent`; our approach avoids replacing the component, so conflicts are limited to mixins targeting the same trim method. If a mixin is used, give it a non-default priority and an `@Inject`/`ModifyConstant` rather than `@Overwrite`.
  • Lowering the cap at runtime: trim safely on the render thread via `Minecraft.execute`.
  • Signed/secure chat: no effect; history storage is local only.
  • Vanilla fallback: `enabled=false` or any failure to apply the override restores 100 and logs a single warning.

6) Package/class layout (`net.diaithir.chatclient`)

  • `ChatClientMod`: `@Mod` entry; client-dist guard; registers config and event handlers.
  • `config.ChatClientConfig`: `ModConfigSpec` definition and typed accessors.
  • `history.HistoryLimits`: applies/updates the cap (AT field set or mixin accessor), trimming logic.
  • `history.HistoryPersistence`: optional cross-world buffer and restore.
  • `event.ClientEvents`: `@EventBusSubscriber(value = Dist.CLIENT)` handlers for login/logout and config reload.
  • `mixin.ChatComponentMixin` (only if AT is insufficient): single targeted injection; listed in `diaithir-chat-client.mixins.json`.