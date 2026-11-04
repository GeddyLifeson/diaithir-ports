No Chat Reports: port record (survey part 6, 2026-11-03)

Upstream: https://github.com/Aizistral-Studios/No-Chat-Reports (branch 26.1-Unified)
Licence: WTFPL (upstream licence file applies to the ported code; headers in each Java file name the upstream)
Target: diaithir-chat-client feature package net.diaithir.chatclient.nochatreports
Method: Sonnet 5.5 port from the upstream NeoForge/Common sources with direct NeoForge APIs (no Puzzles Lib/Architectury/Fabric), compile-fixed against 26.1.2.109, server boot tested.
Assets: no upstream textures or sounds copied.
Notes: Unsigned chat, server and client sides; strip the config screens and server-list icons (client UI extras). Keep core signing suppression and the server 'enforceSecureProfile' handling.
