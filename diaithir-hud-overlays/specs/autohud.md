AutoHUD: port record (survey part 6, 2026-11-03)

Upstream: https://github.com/Crendgrim/AutoHUD (branch main)
Licence: LGPL-3.0-only (upstream licence file applies to the ported code; headers in each Java file name the upstream)
Target: diaithir-hud-overlays feature package net.diaithir.hudoverlays.autohud
Method: Sonnet 5.5 port from the upstream NeoForge/Common sources with direct NeoForge APIs (no Puzzles Lib/Architectury/Fabric), compile-fixed against 26.1.2.109, server boot tested.
Assets: no upstream textures or sounds copied.
Notes: Auto-hide HUD elements by context (health, food, hotbar, xp, etc). Upstream main may target a different MC version; adapt to 26.1.2 GUI layers via RenderGuiLayerEvent.Pre/Post cancel and alpha fade. Config via ModConfigSpec only.
