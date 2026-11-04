# Diaithir Claims

Clean-room chunk claims (stands in for FTB Chunks, all rights reserved). MIT. Written from how claim mods behave; no code taken.

- `/claim` claims the chunk you stand in (25 per player), `/claim drop`, `/claim info`, `/claim trust|untrust <player>` (per chunk), `/claim map` (opens a 21 by 21 chunk screen; `/claim map chat` prints 11 by 11 in chat: `#` yours, `X` others, `.` free, `@` you).
- Strangers cannot break, place, use blocks or blow up blocks in a claimed chunk. Game masters bypass.
- Claims are stored on the chunk, counts on the player.
- Tested headless: two fake players for claim, drop and counts, and `/claim selftest` (game master) fires break, place, use and explosion events from a stranger, the owner and a trusted player: all 8 checks pass. The map screen draws on a client only; it needs the first full-pack join.
- Look-At shows `Claimed by <name>` for the chunk under the crosshair: Look-At has no claims code; claims sends it a provider by message, the client asks the server (`ClaimQuery`, answer `ClaimInfo`, at most every 3 s per chunk). Channels are optional so plain clients still join.

## Grief log (GriefLogger replacement, stage 1)
Package `net.diaithir.claims.log`: block break and place by players, kept in memory (20000) and appended to `world/grieflog/DATE.ndjson`; ops use `/gl inspect [N]` and `/gl rollback RADIUS MINUTES`. Clean-room (GriefLogger is Apache-2.0 but needs Knot, UILib, YamlConfig, SQLite and 54 mixin points). Not covered yet: containers, entity kills, explosions, a database store, lookups by player.
## Law: handcuffs and bounties (Cuffed and Bountycraft replacement)
Package `net.diaithir.claims.law`. Handcuffs (use on a player: slowed, cannot attack, break or use things), cuff key (frees them, returns the cuffs), `/bounty place PLAYER` (item in hand goes on their head) and `/bounty list`; the killer collects the bounty. Clean-room from behaviour: Cuffed is GPL-3.0, Bountycraft is MIT but has no source we could find; no code taken, art drawn fresh.
## Parties (Open Parties and Claims replacement, stage 1)
Package `net.diaithir.claims.parties`: `/party create|invite|accept|leave|kick|disband|info|list`. Party members count as trusted on each other's claims (`Claim.allows`). Clean-room from behaviour (Open Parties and Claims is LGPL-3.0); no code taken. Later: shared claim map colours per party, party chat, claim limits per party.
## Capitals (MCA Capitals replacement, stage 1)
Package `net.diaithir.claims.capitals`: `/capital found|list|info|swear|leave|heir|abdicate`. A capital stands on your own claim; one ruler, an optional heir, subjects in the order they swore; when the ruler dies or abdicates the heir (else the first subject) rules. Clean-room from the behaviour of MCA Capitals (GPL-3.0, 1.21.1, 28.8k lines on MCA 7.7.7 internals); no code taken. Later stages: MCA Reborn villagers as subjects, nobles and houses, taxes via Lightman's Currency.
## Permission nodes (PermissionGate port)
Package `net.diaithir.claims.permissions`: nodes `pg.break`, `pg.interact`, `pg.use`, `pg.attack` on NeoForge's PermissionAPI (default allowed). From PermissionGate by ngoclinh_ (MIT, Fabric, 219 lines); events remapped to NeoForge. Test 2026-11-03: compile OK, server boots with nodes registered. Not tested with a permission handler such as LuckPerms.
## Graves (Epitaphs replacement)
Package `net.diaithir.claims.graves`. A dead player's drops and experience are set into a `diaithirclaims:grave` block at the nearest free spot (6 blocks); the owner, or an op, opens it to get everything back; others wait 20 minutes. `/grave list`, `/grave latest`. Clean-room from the behaviour of grave mods (the upstream Epitaphs source could not be located, so no code was taken); texture drawn fresh. Test 2026-11-03: compileJava OK, runServer boot `Done`, block entity saves its data (`data get block`). Not tested: a real death with a live player.
