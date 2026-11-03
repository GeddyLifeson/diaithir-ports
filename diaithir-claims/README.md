# Diaithir Claims

Clean-room chunk claims (stands in for FTB Chunks, all rights reserved). MIT. Written from how claim mods behave; no code taken.

- `/claim` claims the chunk you stand in (25 per player), `/claim drop`, `/claim info`, `/claim trust|untrust <player>` (per chunk), `/claim map` (opens a 21 by 21 chunk screen; `/claim map chat` prints 11 by 11 in chat: `#` yours, `X` others, `.` free, `@` you).
- Strangers cannot break, place, use blocks or blow up blocks in a claimed chunk. Game masters bypass.
- Claims are stored on the chunk, counts on the player.
- Tested headless: two fake players for claim, drop and counts, and `/claim selftest` (game master) fires break, place, use and explosion events from a stranger, the owner and a trusted player: all 8 checks pass. The map screen draws on a client only; it needs the first full-pack join.
- Look-At shows `Claimed by <name>` for the chunk under the crosshair: Look-At has no claims code; claims sends it a provider by message, the client asks the server (`ClaimQuery`, answer `ClaimInfo`, at most every 3 s per chunk). Channels are optional so plain clients still join.

## Permission nodes (PermissionGate port)
Package `net.diaithir.claims.permissions`: nodes `pg.break`, `pg.interact`, `pg.use`, `pg.attack` on NeoForge's PermissionAPI (default allowed). From PermissionGate by ngoclinh_ (MIT, Fabric, 219 lines); events remapped to NeoForge. Test 2026-11-03: compile OK, server boots with nodes registered. Not tested with a permission handler such as LuckPerms.
