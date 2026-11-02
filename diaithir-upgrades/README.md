# Diaithir Upgrades (shared API)

One Factorio-style upgrade system for every working block. Design and plan: `/mnt/project-files/plans/UPGRADES.md`. Pure Java core (no Minecraft types) plus data; the NeoForge glue lives in the consolidated module that hosts it. Clean-room, original numbers, MIT.

- `Kind` speed, productivity, efficiency, quality; tiers 1 to 3; `Theme` tech circuit, magic ogham stone, food spice.
- `Quality` normal to legendary scales the beneficial side of an effect.
- `Target` declares slots and allowed kinds for a block id (data or tag), so any ported or rewritten mod opts in.
- `Beacon` shares speed, efficiency and quality (never productivity) with blocks in range, 1/sqrt(n) stacking.
- `Resolver` gives the final clamped effect; `Planner` swaps modules in bulk (the upgrade planner).

Test: `./gradlew test` (JUnit, no game needed).

Units: real SI per `/mnt/project-files/plans/UNITS.md`. Machines declare base draw in W and job time in s; `Resolver.powerWatts` and `jobSeconds` return the upgraded values. Magic costs are energy at 100 J per quantum, so one module maths covers every theme.

## Use from another module
Composite build (no publishing needed), in the consumer's `settings.gradle`:
```
includeBuild('../diaithir-upgrades')
```
and in its `build.gradle`: `implementation 'net.diaithir:diaithir-upgrades:1.0.0'`; add `diaithirupgrades` as a required dependency in the consumer's `neoforge.mods.toml`. Or `./gradlew publishToMavenLocal` and use `mavenLocal()`.

API: `Upgrades.insert(be, module, quality)`, `Upgrades.remove(be, slot)`, `Upgrades.host(be)`; capability `UpgradeHost.CAP` is registered on every block entity type and answers only where a Target rule matches. Rules: `data/<ns>/upgrades/targets/*.json`. Modules: `ModuleRegistry.register(spec)`. Saved with the block entity as an attachment.
