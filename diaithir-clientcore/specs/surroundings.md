Dynamic Surroundings Rebirth: port record (survey part 6, 2026-11-03)

Upstream: https://github.com/uawyiegrfv/DynamicSurroundingsRebirth (branch master)
Licence: MIT (upstream licence file applies to the ported code; headers in each Java file name the upstream)
Target: diaithir-audio feature package net.diaithir.audio.surroundings
Method: Sonnet 5.5 port from the upstream NeoForge/Common sources with direct NeoForge APIs (no Puzzles Lib/Architectury/Fabric), compile-fixed against 26.1.2.109, server boot tested.
Assets: no upstream textures or sounds copied.
Notes: Biome, weather and footstep ambience. Sound events need sounds.json entries that reference existing vanilla sounds only (no .ogg copying).
