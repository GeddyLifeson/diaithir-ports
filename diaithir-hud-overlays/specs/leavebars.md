Leave My Bars Alone: port record (survey part 6, 2026-11-03)

Upstream: https://github.com/Fuzss/leavemybarsalone (branch 26.1.x)
Licence: MPL-2.0 (upstream licence file applies to the ported code; headers in each Java file name the upstream)
Target: diaithir-hud-overlays feature package net.diaithir.hudoverlays.leavebars
Method: Sonnet 5.5 port from the upstream NeoForge/Common sources with direct NeoForge APIs (no Puzzles Lib/Architectury/Fabric), compile-fixed against 26.1.2.109, server boot tested.
Assets: no upstream textures or sounds copied.
Notes: Keeps the food and experience bars visible while riding a mount; mixin in upstream -> prefer a NeoForge GUI layer / event solution or a minimal mixin.
