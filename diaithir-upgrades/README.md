# Diaithir Upgrades (shared API)

One Factorio-style upgrade system for every working block. Design and plan: `/mnt/project-files/plans/UPGRADES.md`. Pure Java core (no Minecraft types) plus data; the NeoForge glue lives in the consolidated module that hosts it. Clean-room, original numbers, MIT.

- `Kind` speed, productivity, efficiency, quality; tiers 1 to 3; `Theme` tech circuit, magic ogham stone, food spice.
- `Quality` normal to legendary scales the beneficial side of an effect.
- `Target` declares slots and allowed kinds for a block id (data or tag), so any ported or rewritten mod opts in.
- `Beacon` shares speed, efficiency and quality (never productivity) with blocks in range, 1/sqrt(n) stacking.
- `Resolver` gives the final clamped effect; `Planner` swaps modules in bulk (the upgrade planner).

Test: `./gradlew test` (JUnit, no game needed).

Units: real SI per `/mnt/project-files/plans/UNITS.md`. Machines declare base draw in W and job time in s; `Resolver.powerWatts` and `jobSeconds` return the upgraded values. Magic costs are energy at 100 J per quantum, so one module maths covers every theme.
