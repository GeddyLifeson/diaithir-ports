Chat Heads: port record (survey part 6, 2026-11-03)

Upstream: https://github.com/dzwdz/chat_heads (branch architectury-26.1)
Licence: MPL-2.0 (upstream licence file applies to the ported code; headers in each Java file name the upstream)
Target: diaithir-chat-client feature package net.diaithir.chatclient.chatheads
Method: Sonnet 5.5 port from the upstream NeoForge/Common sources with direct NeoForge APIs (no Puzzles Lib/Architectury/Fabric), compile-fixed against 26.1.2.109, server boot tested.
Assets: no upstream textures or sounds copied.
Notes: Player heads beside chat lines; client only, mixins in upstream -> use mixin only where required.
