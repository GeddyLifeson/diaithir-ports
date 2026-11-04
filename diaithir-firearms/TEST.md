# Test results (2026-11-03)

Build: `./gradlew build` JDK 25, NeoForge 26.1.2.116, jar `diaithir-firearms-neoforge-26.1.2-1.0.0.jar`. Headless dedicated server (gradle runServer, rcon), no client.
- Boots with no ERROR lines from the mod; all recipes parse (37 items, 38 recipes).
- `/firearmsselftest` run twice (the first run only loads the test chunk, as in combat): `firearms ok=92 bad=0` (adds the 12 sound events). Covers: 17 guns are GunItems with ammo items present; each non-launcher gun fires one round at an armour stand 8 m away and hits; attachments fit, replace by kind, and detach; reload starts, fills the magazine, spends ammo, and refuses with no ammo.
- Not tested: a real client (key R, zoom, tooltip, view kick, sounds), launcher explosions, hold-to-fire auto cadence (selftest calls `Engine.fire` directly), full-pack boot with the other diaithir modules, `id_migrations.json` applied to a real world.

## Roster test (2026-11-03)
265 guns (17 archetypes + 248 real designs from `roster/`), 78 ammo items. Headless server, `/firearmsselftest` twice: `firearms ok=1042 bad=0`, no recipe parse errors. Each gun fires at a stand 8 m away and hits (up to 12 tries, since spread is random). Not tested: real client, sounds by ear, auto cadence, launcher explosions, full-pack boot.

## Round 2: ammo containers and animation hook (2026-11-03)
Headless dedicated server (gradle runServer, rcon), `/firearmsselftest` twice (the first run only loads the test chunk and reports bad=15 hits; the second is the result): `firearms ok=1315 bad=0`. New checks cover container kinds per class, container fit rules (class, round, other namespace), and drain bounds on plain json. Not tested: the real `diaithircrafting:ammo_container` component (that module is not built yet), a reload from a real container stack, a client, the AnimHook with diaithir-animation loaded (no-op here).
