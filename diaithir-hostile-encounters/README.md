# diaithir-hostile-encounters

Home of the pack's hostile-encounter systems. Part one is the lunar events port (see `NOTICE.md`). Horde nights (Undead Nights replacement) and invasion tuning are to be merged in here.

Lunar events: blood moon (horde pressure), blue moon (luck, double XP, cheaper anvil), harvest moon (double crop drops), super variants, and a super moon of slimes. Data lives in `data/diaithirhostile/lunar_events/*.json`; datapacks can replace or add events. Each event also has a Codex quest entry in `data/diaithirhostile/quests/lunar_events.json` (tab Moons; the shared quests data contract), and `LunarEventStarted` goes out on the bus when a moon rises or sets so the quest layer can complete them. There is no on/off switch: players meet events as quests and play them however they like. Ops get `/lunar forecast|set <event>|recompute`.

Test: `./gradlew runSelftest` (JDK 25) boots a server, checks the forecast and forcing an event, then stops.

## Survey part 2 folds
- **Raided** (MIT): Inquisitor, Incinerator, Savager, Necromancer, Electromancer join the raid waves (`raided:*`).
- **Zombie Hordes / Horde Hoard** (MIT): horde stacking and multiplying, intelligent teams, leaping, grave robbers, iron golem regeneration, nightly horde spawner. Game rules `horde_stacking`, `horde_multiplying`, `iron_golem_regen_power`, `horde_spawner_chance`.
Credits and licences in `NOTICE.md`.
Selftest covers (fake player, 0 failures): forecast, forcing, horde spawns, luck effect, XP x2, harvest drops x2, sleep block, anvil and enchanting cost, beacon multiplier, meteor crater. Not covered: the client tint (needs a client run).

Blood Moon (KS, nugrevan, MIT) is folded into the lunar blood moon: boosted red-marked mobs, Moon Essence drops that give experience when used, creeper blasts that sicken, more phantoms. Written from the mod's public description; no code or art copied.
