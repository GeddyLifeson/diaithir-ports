Crash Assistant: Clean-Room Design (diaithir-crash-tools)

1) Feature list

  • Crash summary screen: after a crash, a GUI shows the exception type/message, the "culprit" mod (best guess), the Minecraft/NeoForge version, and the top stack frames.
  • Buttons: Open crash report, Open logs folder, Copy report to clipboard, Copy summary only, Upload (optional, off by default), Close/Quit.
  • Suspect detection: maps stack-frame packages to loaded mods and lists the top 3 suspects with a confidence ordering.
  • Previous-crash recovery: if the JVM died hard (no screen possible), the next launch shows the screen for the newest unseen crash report.
  • Report formatting: Markdown-wrapped report (mod list, versions, JVM args, trimmed log) ready for pasting into an issue tracker.

2) Config options (client config)

  • enabled (bool, true)
  • showOnNextLaunch (bool, true) — surface crashes from the previous session
  • maxLogLines (int, 500) — trimmed log tail included in copies
  • includeModList (bool, true)
  • redactPaths (bool, true) — replace user home/username with ~
  • enableUpload (bool, false) and uploadUrl (string, empty)
  • suspectFrameDepth (int, 30)
  • autoOpenFolderOnCrash (bool, false)

3) Keybinds

  • No in-game keybind is essential, since the screen appears on crash. Optional: "Open last crash summary" (unbound by default), category key.categories.crashtools, usable from the title screen or in-world.

4) Events/hooks (NeoForge 26.1.2)

Two distinct paths are needed, because a hard crash may not reach any event.

  • Previous-crash path (event-driven, all client-side):
  • ScreenEvent.Opening — when the first TitleScreen opens, check for an unseen crash report and swap in CrashSummaryScreen.
  • ClientTickEvent.Post — drives the optional keybind (consume KeyMapping.consumeClick()).
  • RegisterKeyMappingsEvent (mod bus) — registers the keybind.
  • ModConfigSpec registered via ModContainer.registerConfig(ModConfig.Type.CLIENT, …).
  • Live-crash path: no NeoForge event fires for Minecraft.crash(...). Practical approach without mixins: install a Thread.setDefaultUncaughtExceptionHandler wrapper at client setup (FMLClientSetupEvent) that persists a marker file plus the stack summary before delegating to the previous handler. The screen is then shown on next launch via the path above. If a mixin is ever acceptable, hooking the crash-report display would allow immediate display, but treat it as a later, optional enhancement.
  • Detect crash reports by scanning gameDir/crash-reports/ for files newer than the stored "last seen" timestamp.

5) Edge cases and compat notes

  • Dedicated server safety: all screen/keybind/clipboard code lives under a client-only package. Entrypoint class contains no client class references; client init is guarded by FMLEnvironment.dist == Dist.CLIENT, with the client class loaded reflectively or via a separate @Mod-bus subscriber constrained with value = Dist.CLIENT.
  • Crash during startup (before the title screen): the screen cannot render; surface it on the next successful launch.
  • Out-of-memory / JVM abort: the handler may not run; the report-scan path still catches Minecraft-written crash reports; hs_err_pid* files are listed but not parsed.
  • Other mods replacing the title screen: if the opened screen isn't TitleScreen, defer until a TitleScreen is observed or fall back to the keybind.
  • Mixin/coremod stack frames: attribute via package-to-mod mapping from ModList; unknown packages are labelled "unknown/vanilla" and not blamed.
  • Privacy: path redaction on by default; upload strictly opt-in; never auto-send.
  • Clipboard failures: catch and show a toast, offering a save-to-file fallback.
  • Large logs: stream-tail the file; never load it whole.

6) Package/class layout (net.diaithir.crashtools)

  • CrashToolsMod — @Mod entrypoint, side-safe, registers config
  • config.CrashToolsConfig — ModConfigSpec definitions
  • client.ClientBootstrap — Dist.CLIENT init, event wiring
  • client.event.TitleScreenHook — ScreenEvent.Opening handler
  • client.event.KeybindHandler — key registration and tick polling
  • client.screen.CrashSummaryScreen — the GUI and button layout
  • crash.CrashReportLocator — finds unseen reports, tracks the last-seen marker
  • crash.CrashParser — extracts exception, frames, and sections
  • crash.SuspectAnalyzer — frame-to-mod attribution
  • crash.ReportFormatter — Markdown report, redaction, log tailing
  • crash.UncaughtHandlerInstaller — persists the marker on a live crash
  • util.ClipboardUtil, util.FolderOpener — OS-level actions with fallbacks
  •