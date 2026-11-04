# Diaithir Claims

Clean-room chunk claims (stands in for FTB Chunks, all rights reserved). MIT. Written from how claim mods behave; no code taken.

- `/claim` claims the chunk you stand in (25 per player), `/claim drop`, `/claim info`, `/claim trust|untrust <player>` (per chunk), `/claim map` (opens a 21 by 21 chunk screen; `/claim map chat` prints 11 by 11 in chat: `#` yours, `X` others, `.` free, `@` you).
- Strangers cannot break, place, use blocks or blow up blocks in a claimed chunk. Game masters bypass.
- Claims are stored on the chunk, counts on the player.
- Tested headless: two fake players for claim, drop and counts, and `/claim selftest` (game master) fires break, place, use and explosion events from a stranger, the owner and a trusted player: all 8 checks pass. The map screen draws on a client only; it needs the first full-pack join.
- Look-At shows `Claimed by <name>` for the chunk under the crosshair: Look-At has no claims code; claims sends it a provider by message, the client asks the server (`ClaimQuery`, answer `ClaimInfo`, at most every 3 s per chunk). Channels are optional so plain clients still join.

## Graves (Epitaphs replacement)
Package `net.diaithir.claims.graves`. A dead player's drops and experience are set into a `diaithirclaims:grave` block at the nearest free spot (6 blocks); the owner, or an op, opens it to get everything back; others wait 20 minutes. `/grave list`, `/grave latest`. Clean-room from the behaviour of grave mods (the upstream Epitaphs source could not be located, so no code was taken); texture drawn fresh. Test 2026-11-03: compileJava OK, runServer boot `Done`, block entity saves its data (`data get block`). Not tested: a real death with a live player.
