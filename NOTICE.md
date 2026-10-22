# NOTICE

This is a modified copy of Woodwalkers 8.2 by ToCraft (https://github.com/ToCraft/woodwalkers, tag `8.2`, commit 40026e8b),
licensed under the MIT licence (LICENSE). This modified version is under the same licence.

Changes for Diaithir (2026-10-22), built as `walkers-neoforge-8.2-diaithir.1.jar`:

- `PlayerEntityRendererMixin` no longer declares its own `submit(AvatarRenderState, ...)`. On NeoForge 26.1.2,
  `AvatarRenderer` has its own `submit` (it posts `RenderPlayerEvent.Pre/Post`), so that method silently replaced
  the whole body: the player render events never fired, and ModernFix's `bugfix.entity_pose_stack` redirect on that
  method failed with a critical mixin error at client start, which took down Puzzles Lib and every mod on it.
  The shape rendering is now a cancellable `@Inject` at `HEAD`; the normal player path runs NeoForge's body.
- `mod_version` is `8.2-diaithir.1`, built against NeoForge 26.1.2.116; the Fabric subproject is left out of
  `settings.gradle.kts` (NeoForge only).

Build: JDK 25, `./gradlew :neoforge:build` in this folder.
