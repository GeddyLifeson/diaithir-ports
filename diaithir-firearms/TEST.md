# Test results (2026-11-03)

Build: `./gradlew build` JDK 25, NeoForge 26.1.2.116, jar `diaithir-firearms-neoforge-26.1.2-1.0.0.jar`. Headless dedicated server (gradle runServer, rcon), no client.
- Boots with no ERROR lines from the mod; all recipes parse (37 items, 38 recipes).
- `/firearmsselftest` run twice (the first run only loads the test chunk, as in combat): `firearms ok=92 bad=0` (adds the 12 sound events). Covers: 17 guns are GunItems with ammo items present; each non-launcher gun fires one round at an armour stand 8 m away and hits; attachments fit, replace by kind, and detach; reload starts, fills the magazine, spends ammo, and refuses with no ammo.
- Not tested: a real client (key R, zoom, tooltip, view kick, sounds), launcher explosions, hold-to-fire auto cadence (selftest calls `Engine.fire` directly), full-pack boot with the other diaithir modules, `id_migrations.json` applied to a real world.
