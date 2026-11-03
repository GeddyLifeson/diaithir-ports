# Diaithir Claims

Clean-room chunk claims (stands in for FTB Chunks, all rights reserved). MIT. Written from how claim mods behave; no code taken.

- `/claim` claims the chunk you stand in (25 per player), `/claim drop`, `/claim info`, `/claim trust|untrust <player>` (per chunk), `/claim map` (opens a 21 by 21 chunk screen; `/claim map chat` prints 11 by 11 in chat: `#` yours, `X` others, `.` free, `@` you).
- Strangers cannot break, place, use blocks or blow up blocks in a claimed chunk. Game masters bypass.
- Claims are stored on the chunk, counts on the player.
- Tested headless: two fake players for claim, drop and counts, and `/claim selftest` (game master) fires break, place, use and explosion events from a stranger, the owner and a trusted player: all 8 checks pass. The map screen draws on a client only; it needs the first full-pack join.
- Look-At shows `Claimed by <name>` for the chunk under the crosshair: Look-At has no claims code; claims sends it a provider by message, the client asks the server (`ClaimQuery`, answer `ClaimInfo`, at most every 3 s per chunk). Channels are optional so plain clients still join.

## Round 2: FTB Chunks feature set (2026-11-03)
Clean-room; behaviour taken from how chunk-claim mods work, no code or assets from FTB Chunks (all rights reserved). The jar also declares modId `ftbchunks` so dependents resolve, and `/ftbchunks` is an alias of `/claim`.
- Teams: `/claim team create <name>|invite <player>|join|leave|info`. Team members may build in each other's claims. One team per player, saved in `diaithir_teams.dat` in the world folder; no other mod needed (replaces FTB Teams for claims).
- Keep loaded: `/claim load` / `/claim unload` force-loads a claimed chunk you own (5 per player), also while nobody is online. Dropping the claim unloads it.
- `/claim area <1-4>` claims the free square around you up to your limit. `/claim dropall` drops your claims near you.
- Flags per chunk: `/claim flag public_use true|false` (strangers may open doors, chests, buttons; not break or place) and `/claim flag explosions true|false`.
- Works in every dimension, so planets get claims too.
- Selftest (`/claim selftest`, also from the console with a fake owner): 14 checks incl. flags and teams, all PASS on a headless NeoForge 26.1.2.116 boot. Not tested headless: the load/unload/area/dropall commands (they need a real player) and teams.dat reload after restart.
- Not rebuilt: the minimap/large map (Atlas + Surveyor and `/claim map` cover it), FTB's per-chunk permission GUI.
