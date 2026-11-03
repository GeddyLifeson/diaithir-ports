# Diaithir Claims

Clean-room chunk claims (stands in for FTB Chunks, all rights reserved). MIT. Written from how claim mods behave; no code taken.

- `/claim` claims the chunk you stand in (25 per player), `/claim drop`, `/claim info`, `/claim trust|untrust <player>` (per chunk), `/claim map` (opens a 21 by 21 chunk screen; `/claim map chat` prints 11 by 11 in chat: `#` yours, `X` others, `.` free, `@` you).
- Strangers cannot break, place, use blocks or blow up blocks in a claimed chunk. Game masters bypass.
- Claims are stored on the chunk, counts on the player.
- Tested headless: two fake players for claim, drop and counts, and `/claim selftest` (game master) fires break, place, use and explosion events from a stranger, the owner and a trusted player: all 8 checks pass. The map screen draws on a client only; it needs the first full-pack join.
- Look-At shows `Claimed by <name>` for the chunk under the crosshair: Look-At has no claims code; claims sends it a provider by message, the client asks the server (`ClaimQuery`, answer `ClaimInfo`, at most every 3 s per chunk). Channels are optional so plain clients still join.

## Law: handcuffs and bounties (Cuffed and Bountycraft replacement)
Package `net.diaithir.claims.law`. Handcuffs (use on a player: slowed, cannot attack, break or use things), cuff key (frees them, returns the cuffs), `/bounty place PLAYER` (item in hand goes on their head) and `/bounty list`; the killer collects the bounty. Clean-room from behaviour: Cuffed is GPL-3.0, Bountycraft is MIT but has no source we could find; no code taken, art drawn fresh.
