Structure Compass: port record (survey part 6, 2026-11-03)

Upstream: https://github.com/Mrbysco/StructureCompass (branch 26.1)
Licence: MIT (upstream licence file applies to the ported code; headers in each Java file name the upstream)
Target: diaithir-structure-navigation feature package net.diaithir.structurenavigation.structurecompass
Method: Sonnet 5.5 port from the upstream NeoForge/Common sources with direct NeoForge APIs (no Puzzles Lib/Architectury/Fabric), compile-fixed against 26.1.2.109, server boot tested.
Assets: no upstream textures or sounds copied.
Notes: Compass item that locates a chosen structure. Items need registration (DeferredRegister.Items) - ship item model/lang as JSON resources under assets/diaithirstructurenavigation; draw no textures: reuse vanilla compass item model by pointing the item model at minecraft:item/compass.
