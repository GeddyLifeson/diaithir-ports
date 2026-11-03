# Diaithir Computing

Clean-room programmable block for NeoForge 26.1.2, standing in for CC: Tweaked (whose licence forbids redistributing modified copies, so it cannot be folded). Written from the public idea only: no code, texture, model, sound or name from CC: Tweaked. MIT. The Lua interpreter is LuaJ 3.0.1 (MIT), bundled with Jar-in-Jar; the API, sandbox, block and assets are ours.

**Reckoner** (native name *àiremachd*, from the archaic lexicon). Use a book and quill or written book on it: the pages become the program (16000 characters at most). Use it with an empty hand, or give it a rising redstone signal on any side, to run it once. `print` goes to the player who used it.

API (Lua): `redstone.get(side)`, `redstone.set(side, 0-15 | boolean)`, `inventory.list(side)` (rows of `name`, `count`, totals per item from the neighbour's item capability), `world.time()`, `world.day()`, `world.raining()`, `world.pos()`. Sides are `bottom top north south west east`. Plus base, string, table, math and bit32 libraries.

Sandbox: no io, os, package, require, debug or file loading. Each run is stopped after 200,000 instructions. Lua runs on LuaJ's own thread, so everything a program can read is copied on the server thread before it starts and the world is never touched from Lua. Not limited: memory use of a single string operation.

Not built yet (CC: Tweaked parts still without a replacement): turtles, monitors and terminals, disk drives, wired and wireless networking, persistent program state between runs, sleeping and events, a text editor screen.

Age gate in `data/diaithircomputing/age_gate.json` (Age VI), Codex text in `library-sync/diaithircomputing.json`, `id_migrations.json` for old worlds. `tools/gen_assets.py` (script S6845, module M0096) writes textures, models, lang, recipe, loot and tags.

Test: `/diaithircomputing selftest` (owners). Builds a Reckoner beside a chest and runs programs: print, inventory totals, redstone output seen by a neighbour, redstone input, io/os/debug/require refused, an infinite loop stopped, a syntax error reported, NBT saving. Prints PASS or what failed. Run headless: `./gradlew runServer` with rcon enabled.
