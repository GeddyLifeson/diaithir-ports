Sound Physics Perfected: port record (survey part 6, 2026-11-03)

Upstream: https://github.com/FalseMSP/SoundPhysicsPerfected (branch 26.1)
Licence: MIT (upstream licence file applies to the ported code; headers in each Java file name the upstream)
Target: diaithir-audio feature package net.diaithir.audio.soundphysics
Method: Sonnet 5.5 port from the upstream NeoForge/Common sources with direct NeoForge APIs (no Puzzles Lib/Architectury/Fabric), compile-fixed against 26.1.2.109, server boot tested.
Assets: no upstream textures or sounds copied.
Notes: Reverb and occlusion via OpenAL EFX; mixin config needed for the sound engine hooks, emit it.
