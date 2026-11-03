# Diaithir Waypoints

Clean-room replacement for Waystones (all rights reserved, so it cannot be folded). MIT. Written from how waystone mods behave; no code or art taken.

- Waypoint Stone: use it to light it for you. Names come from position only.
- `/route list`, `/route go <n>`, `/route forget <n>`; list entries are clickable. Going needs you within 4 m of a stone. Cost is XP: 1 per 100 m, max 200, dimension change counts as 2000 m. Broken stones drop off your list when you try them.
- Waypoint Scroll (c:scrolls): walks you to the stone you lit last, from anywhere.
- `data/diaithirwaypoints/id_migrations.json` maps every waystones:* waystone, portstone and sharestone to the stone and every scroll to the scroll. Warp stones, plates, shards and portals are not rebuilt yet (Age gate and upgrade rules to follow with the consolidation fold).
- Tested headless: compiles on JDK 25, server boots to Done with the pack data loading. Click and teleport not tested (no client).
