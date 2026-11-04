GUI Scaler: port record (survey part 6, 2026-11-03)

Upstream: https://github.com/zendiik/gui-scaler (branch v26.1)
Licence: LGPL-3.0-only (upstream licence file applies to the ported code; headers in each Java file name the upstream)
Target: diaithir-client-view-input feature package net.diaithir.clientviewinput.guiscaler
Method: Sonnet 5.5 port from the upstream NeoForge/Common sources with direct NeoForge APIs (no Puzzles Lib/Architectury/Fabric), compile-fixed against 26.1.2.109, server boot tested.
Assets: no upstream textures or sounds copied.
Notes: Auto GUI scale per screen; use ScreenEvent.Opening/Closing or Minecraft window scale APIs, no mixin if avoidable.
