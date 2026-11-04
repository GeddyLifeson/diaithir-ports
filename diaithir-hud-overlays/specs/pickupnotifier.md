Pick Up Notifier: port record (survey part 6, 2026-11-03)

Upstream: https://github.com/Fuzss/pickupnotifier (branch 26.1.x)
Licence: MPL-2.0 (upstream licence file applies to the ported code; headers in each Java file name the upstream)
Target: diaithir-hud-overlays feature package net.diaithir.hudoverlays.pickupnotifier
Method: Sonnet 5.5 port from the upstream NeoForge/Common sources with direct NeoForge APIs (no Puzzles Lib/Architectury/Fabric), compile-fixed against 26.1.2.109, server boot tested.
Assets: no upstream textures or sounds copied.
Notes: Server sends take-item packets to the picking player; client shows an item pickup log HUD. Server part must work on dedicated server (COMMON side), client part client-only.
