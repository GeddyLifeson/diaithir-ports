# NOTICE

This is a modified copy of CraftedCore 8.1 by ToCraft (https://github.com/ToCraft/craftedcore, tag `8.1`),
licensed under the GNU Lesser General Public License v3.0 (LICENSE). This modified version is under the same licence.

Changes for Diaithir (2026-10-22), built as `craftedcore-neoforge-8.1-diaithir.1.jar`:

- `RegistryDataLoaderMixin` is no longer applied (removed from `craftedcore.mixins.json`). It replaced
  `RegistryDataLoader.SYNCHRONIZED_REGISTRIES` with a copy at class init. On NeoForge that field is a live view of
  NeoForge's networkable data-pack registries (`DataPackRegistriesHooks.grabNetworkableRegistries`), filled later by
  `DataPackRegistryEvent.NewRegistry`; the copy froze it before any mod registry was added, so no modded synced
  data-pack registry reached clients. Joining any world then failed with
  `Missing element ResourceKey[forbidden_arcanus:wand/material / forbidden_arcanus:arcane_crystal]` ("Network Protocol Error").
  The NeoForge side of CraftedCore registers its registries through NeoForge events and never used those lists.
- `mod_version` is `8.1-diaithir.1`; the Fabric and test-mod subprojects are left out of `settings.gradle.kts`.

Build: JDK 25, `./gradlew :neoforge:build` in this folder.
