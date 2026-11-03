# Diaithir Claims

Clean-room chunk claims (stands in for FTB Chunks, all rights reserved). MIT. Written from how claim mods behave; no code taken.

- `/claim` claims the chunk you stand in (25 per player), `/claim drop`, `/claim info`, `/claim trust|untrust <player>` (per chunk), `/claim map` (11 by 11 chunks in chat: `#` yours, `X` others, `.` free, `@` you).
- Strangers cannot break, place, use blocks or blow up blocks in a claimed chunk. Game masters bypass.
- Claims are stored on the chunk, counts on the player.
- Tested headless with two fake players: claim, repeat claim refused, per-player counts, drop by owner, drop refused for others and for trusted players. Break/place/use/explosion guards compile but could not be triggered from the console; they need the first full-pack join. No graphical map screen yet (chat map only).
