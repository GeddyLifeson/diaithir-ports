Extreme Sound Muffler: port record (survey part 6, 2026-11-03)

Upstream: https://github.com/LeoBeliik/ExtremeSoundMuffler (branch 26.1.2)
Licence: LGPL-3.0-only (upstream licence file applies to the ported code; headers in each Java file name the upstream)
Target: diaithir-audio feature package net.diaithir.audio.soundmuffler
Method: Sonnet 5.5 port from the upstream NeoForge/Common sources with direct NeoForge APIs (no Puzzles Lib/Architectury/Fabric), compile-fixed against 26.1.2.109, server boot tested.
Assets: no upstream textures or sounds copied.
Notes: Per-sound volume control GUI plus optional muffler block. Skip the block and its textures if it needs art; keep the client GUI and per-sound volume.
