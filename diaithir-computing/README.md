# Diaithir Computing

Clean-room programmable block for NeoForge 26.1.2, standing in for CC: Tweaked (whose licence forbids redistributing modified copies, so it cannot be folded). Written from the public idea only: no code, texture, model, sound or name from CC: Tweaked. MIT. The Lua interpreter is LuaJ 3.0.1 (MIT), bundled with Jar-in-Jar; the API, sandbox, block and assets are ours.

**Reckoner** (native name *àiremachd*, from the archaic lexicon). Use a book and quill or written book on it: the pages become the program (16000 characters at most). Use it with an empty hand, or give it a rising redstone signal on any side, to run it once. `print` goes to the player who used it.

A program runs in slices on a LuaJ coroutine. Anything that touches the world is a yield answered on the server thread (the coroutine's own thread must never touch the world, it deadlocks), so `sleep(ticks)`, turtle actions, `inventory.list`, `monitor.write`, `net.send` and `net.wait` pause the program and it carries on next tick; use the block again to stop a running program. Programs do not survive a restart.

API (Lua), both blocks: `redstone.get(side)`, `redstone.set(side, 0-15 | boolean)`, `inventory.list(side)` (rows of `name`, `count`), `world.time()`, `world.day()`, `world.raining()`, `world.pos()`, `sleep(ticks)` (1 to 1200). Sides are `bottom top north south west east`.
- **Monitor.** `monitor.write(side, line 1-4, text)` writes to a sign on that side (returns true, or false and why). Any vanilla sign works as a monitor, no new block or renderer.
- **Networking.** Built into both blocks, wireless within 64 blocks in one dimension: `net.listen(ch)`, `net.unlisten(ch)`, `net.send(ch, text)` (returns how many computers got it; 256 characters, 32 queued per computer), `net.receive()` (channel, text, distance or nil), `net.wait()` (pauses until a message comes). Wired cables are not built.
- **Wayfarer (turtle).** A block that moves itself one block at a time: the block entity at the new position takes over the program, fuel and sixteen slots. `turtle.forward() back() up() down() turnLeft() turnRight()`, `dig(dir)`, `place(dir)`, `detect(dir)`, `inspect(dir)`, `drop(dir)`, `suck(dir)`, `select(n)`, `getCount(n)`, `getItem(n)`, `refuel(n)`, `getFuel()`. Directions: forward, back, left, right, up, down. Each call returns true or false and a reason. Fuel: one unit per second of burn time of a fuel item (coal is 80), 20 to start, one per move; moving takes 8 ticks, digging 10, placing 5. It picks up what it digs, drops onto the ground (or into the world as items), and cannot dig bedrock, a Reckoner or a Wayfarer. A broken Wayfarer drops its stacks.

Sandbox: no io, os, package, require, debug or file loading. Each slice is stopped after 200,000 instructions. Not limited: memory use of a single string operation.

Not built (CC: Tweaked parts without a replacement): large monitors and a terminal screen, disk drives, wired networking, persistent program state, putting items into chests from a turtle (it drops on the ground), crafting and attacking from a turtle, a text editor screen, a client renderer for the turtle (it is a plain block that moves).

Age gate in `data/diaithircomputing/age_gate.json` (Age VI), Codex text in `library-sync/diaithircomputing.json`, `id_migrations.json` for old worlds. `tools/gen_assets.py` (script S6845, module M0096) writes textures, models, lang, recipe, loot and tags.

Test: `/diaithircomputing selftest` (owners). Builds a Reckoner beside a chest and runs programs: print, inventory totals, redstone, io/os/debug/require refused, an infinite loop stopped, a syntax error, NBT saving; then sleep, a sign monitor, wireless messages (channel, range) and a Wayfarer that walks two blocks, turns, digs, places, refuels, is blocked, and runs out of fuel. Prints PASS or what failed. Run headless: `./gradlew runServer` with rcon enabled.

## Monitors, cables, disks, chests (added)
- **Tall monitors**: `monitor.write(side, row, text)` rows 1-4 go to the sign beside the computer; more signs hung directly below it extend the screen, up to eight signs (32 rows). `monitor.rows(side)` counts rows, `monitor.clear(side)` blanks them.
- **Cable** (`diaithircomputing:cable`): a run of up to 512 touching cables joins every computer touching it into one wired network; `net.send` reaches listeners on it at any distance, on top of the 64-block wireless range.
- **Disk Drive and Disk**: use a Disk on a drive, sneak with an empty hand to take it out. `disk.present/list/read/write/delete/free(side, ...)`; 16 files, 8000 characters each, 32000 per disk; names are letters, digits, `_ . -`. Files live in the disk item, so they travel with it.
- **Chests**: the Wayfarer's `drop` and `suck` move items into and out of a container in front of it (any item handler), else drop to or pick up from the world.
- Self-test now also covers all of these.
