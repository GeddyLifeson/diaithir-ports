Ksyxis: port record (survey part 6, 2026-11-03)

Upstream: https://github.com/VidTu/Ksyxis (branch main)
Licence: MIT (upstream licence file applies to the ported code; headers in each Java file name the upstream)
Target: diaithir-worldgen-chunk-opts feature package net.diaithir.worldgenchunkopts.ksyxis
Method: Sonnet 5.5 port from the upstream NeoForge/Common sources with direct NeoForge APIs (no Puzzles Lib/Architectury/Fabric), compile-fixed against 26.1.2.109, server boot tested.
Assets: no upstream textures or sounds copied.
Notes: Skips spawn-chunk loading on world load (server). Mixins required; emit mixin config.
