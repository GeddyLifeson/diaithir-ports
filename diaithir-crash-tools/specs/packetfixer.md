Packet Fixer: port record (survey part 6, 2026-11-03)

Upstream: https://github.com/TonimatasDEV/PacketFixer (branch dev/26.1)
Licence: MIT (upstream licence file applies to the ported code; headers in each Java file name the upstream)
Target: diaithir-network-compat feature package net.diaithir.networkcompat.packetfixer
Method: Sonnet 5.5 port from the upstream NeoForge/Common sources with direct NeoForge APIs (no Puzzles Lib/Architectury/Fabric), compile-fixed against 26.1.2.109, server boot tested.
Assets: no upstream textures or sounds copied.
Notes: Raises packet size limits; mixin config needed, emit it. Both sides.
