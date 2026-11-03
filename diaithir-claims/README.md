# Diaithir Claims

Clean-room chunk claims (stands in for FTB Chunks, all rights reserved). MIT. Written from how claim mods behave; no code taken.

- `/claim` claims the chunk you stand in (25 per player), `/claim drop`, `/claim info`, `/claim trust|untrust <player>` (per chunk), `/claim map` (opens a 21 by 21 chunk screen; `/claim map chat` prints 11 by 11 in chat: `#` yours, `X` others, `.` free, `@` you).
- Strangers cannot break, place, use blocks or blow up blocks in a claimed chunk. Game masters bypass.
- Claims are stored on the chunk, counts on the player.
- Tested headless: two fake players for claim, drop and counts, and `/claim selftest` (game master) fires break, place, use and explosion events from a stranger, the owner and a trusted player: all 8 checks pass. The map screen draws on a client only; it needs the first full-pack join.
- Look-At shows `Claimed by <name>` for the chunk under the crosshair: Look-At has no claims code; claims sends it a provider by message, the client asks the server (`ClaimQuery`, answer `ClaimInfo`, at most every 3 s per chunk). Channels are optional so plain clients still join.

## Capitals (MCA Capitals replacement, stage 1)
Package `net.diaithir.claims.capitals`: `/capital found|list|info|swear|leave|heir|abdicate`. A capital stands on your own claim; one ruler, an optional heir, subjects in the order they swore; when the ruler dies or abdicates the heir (else the first subject) rules. Clean-room from the behaviour of MCA Capitals (GPL-3.0, 1.21.1, 28.8k lines on MCA 7.7.7 internals); no code taken. Later stages: MCA Reborn villagers as subjects, nobles and houses, taxes via Lightman's Currency.

## Taxes (Tale of Kingdoms idea, clean-room)
`/capital tax set ITEM AMOUNT` (ruler), `/capital pay` (subject, once per real day), `/capital treasury [take]` (ruler). Stacked on the capitals package. Tale of Kingdoms is LGPL-3.0 (Fabric 1.21.1); no code taken. Later: Lightman's Currency coins as the tax item, tax collection by MCA villagers.
