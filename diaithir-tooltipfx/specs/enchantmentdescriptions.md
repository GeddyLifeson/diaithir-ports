Enchantment Descriptions: port record (survey part 6, 2026-11-03)

Upstream: https://github.com/Darkhax-Minecraft/Enchantment-Descriptions (branch 26.1.2)
Licence: LGPL-2.1-only (upstream licence file applies to the ported code; headers in each Java file name the upstream)
Target: diaithir-tooltipfx feature package net.diaithir.tooltips.enchantmentdescriptions
Method: Sonnet 5.5 port from the upstream NeoForge/Common sources with direct NeoForge APIs (no Puzzles Lib/Architectury/Fabric), compile-fixed against 26.1.2.109, server boot tested.
Assets: no upstream textures or sounds copied.
Notes: Description lines under enchantments in tooltips; descriptions come from lang keys enchantment.<ns>.<path>.desc; keep that key scheme and add a lang resource for vanilla enchantments if upstream ships one.
