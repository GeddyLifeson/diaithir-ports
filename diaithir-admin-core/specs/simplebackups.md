Simple Backups: port record (survey part 6, 2026-11-03)

Upstream: https://github.com/ChaoticTrials/SimpleBackups (branch 26.1)
Licence: Apache-2.0 (upstream licence file applies to the ported code; headers in each Java file name the upstream)
Target: diaithir-admin-core feature package net.diaithir.admincore.simplebackups
Method: Sonnet 5.5 port from the upstream NeoForge/Common sources with direct NeoForge APIs (no Puzzles Lib/Architectury/Fabric), compile-fixed against 26.1.2.109, server boot tested.
Assets: no upstream textures or sounds copied.
Notes: Scheduled zip backups of the world with /backup commands; server-side, COMMON/SERVER only.
