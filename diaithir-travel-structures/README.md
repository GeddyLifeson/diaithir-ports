# diaithir-travel-structures

Travel structures for the Diaithir pack (NeoForge 26.1.2, Java 25). First content: laser bridges and fences, folded from Laser Bridges & Doors 6.3 by Mars (MIT, branch 26.1.2 of the upstream repo; see `NOTICE`, licence text in `LICENSE-LaserBridges-MIT`).

- `laser_source_block` / `laser_fence_source_block`: face-attached sources; powered, they grow a laser bridge (solid) or fence (thin) up to `max_length` - 1 metres (default 15 - 1 = 14), length scales with redstone strength. Dye to colour. Lasers replace air, grass and ferns only (config list).
- Mod id `diaithirtravel`; `id_migrations.json` maps the old `laserbridges:` ids. The Deimos config library is dropped for a NeoForge server config (`max_length`, `blocks_cut_through_by_lasers`).
- Recipe now uses `c:ingots/steel` and `c:glass_blocks` with the end crystal; Age VII gate in `data/diaithirtravel/age_gate.json`; Library rows in `library-sync/diaithirtravel.json`.
- `/diaithirtravel selftest` (owners): at spawn, 200 blocks up, grows a bridge at full power, shortens at power 7, stops at a solid block, clears at power 0. Last run: PASS.

Build: `./gradlew compileJava` (see CLAUDE.md for the JDK 25 recipe). Not done: Industrial Factory redraw of the five textures (upstream art, MIT, kept for now), Diaithiris names, Codex page, drawbridge (hinged bridge) block, elevators and teleporters are separate modules.
