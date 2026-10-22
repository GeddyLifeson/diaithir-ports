# NOTICE

This is a modified copy of ReMorphed 8.0 by ToCraft (https://github.com/ToCraft/remorphed, tag `8.0`, commit eb97ac0),
licensed under the GNU Lesser General Public License v3.0 (LICENSE.md). This modified version is under the same licence.

Changes for Diaithir (2026-10-22), built as `remorphed-neoforge-8.0-diaithir.2.jar`:

- Permission node paths are made valid identifier paths (lower case, `:` becomes `.`) in one place,
  `PermissionManager.node`, used both when NeoForge nodes are registered and when they are checked.
  Upstream registers `remorphed:command.addShape` and `remorphed:type.minecraft:pig`, which throw inside
  `PermissionGatherEvent.Nodes` and stop every later mod's nodes from registering (spark, KubeUI in our pack).
  LuckPerms-style node names change accordingly, e.g. `remorphed.command.addshape`, `remorphed.type.minecraft.pig`.
- CraftedCore 8.x's `PermissionCheckerImpl.createNode` returns its map's previous node (null the first time), so
  NeoForge got null nodes; RemorphedNeoForge calls it twice and registers the first node (equal to the one CraftedCore keeps).
- `mod_version` is `8.0-diaithir.2`; the Fabric subproject is left out of `settings.gradle.kts` (NeoForge only).

Build: JDK 25, `./gradlew :neoforge:build` in this folder.
