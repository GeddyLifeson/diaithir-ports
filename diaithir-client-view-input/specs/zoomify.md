Zoomify: port record (survey part 6, 2026-11-03)

Upstream: https://github.com/isXander/Zoomify
Licence: LGPL-3.0-only (upstream licence file applies to the ported code; headers in each Java file name the upstream)
Target: diaithir-client-view-input feature package net.diaithir.clientviewinput.zoomify
Method: Sonnet 5.5 port from the upstream NeoForge/Common sources with direct NeoForge APIs (no Puzzles Lib/Architectury/Fabric), compile-fixed against 26.1.2.109, server boot tested.
Assets: no upstream textures or sounds copied.
Notes: Zoom key with smooth transitions, scroll-zoom, optional mouse sensitivity scaling; use ComputeFovModifierEvent and InputEvent/ScrollEvent. Drop the YACL screen (config via ModConfigSpec only) and the Stonecutter preprocessor comments.
