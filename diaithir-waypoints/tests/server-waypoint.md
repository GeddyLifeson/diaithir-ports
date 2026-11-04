# Test: server waypoint fold (headless, NeoForge 26.1.2.109)
compileJava clean; server boots to Done. Via rcon: `wp addlist`, `wp add base Home 10 64 10`, `wp list`, `wp list base` (showed dimension and coordinates), `wp add` into a missing list (refused), `wp remove`, `save-all flush`. Not exercised: `/wp nav` and `/wp tp` (need a player), persistence across restart.
