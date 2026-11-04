Mouse Tweaks: port record (survey part 6, 2026-11-03)

Upstream: https://github.com/YaLTeR/MouseTweaks
Licence: BSD-3-Clause (upstream licence file applies to the ported code; headers in each Java file name the upstream)
Target: diaithir-inventory-ui feature package net.diaithir.inventoryui.mousetweaks
Method: Sonnet 5.5 port from the upstream NeoForge/Common sources with direct NeoForge APIs (no Puzzles Lib/Architectury/Fabric), compile-fixed against 26.1.2.109, server boot tested.
Assets: no upstream textures or sounds copied.
Notes: RMB-drag split, LMB-drag distribute, scroll-wheel move, shift-drag; upstream uses mixins plus a public API for GUI authors; replace with ScreenEvent.MouseButtonPressed/Released/Scrolled/MouseDragged hooks and simple slot click packets via Minecraft.gameMode.handleInventoryMouseClick (client side). Keep the config options.
