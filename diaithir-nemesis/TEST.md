# Test results

## Lightmans Currency compat
LCM 26.1-1.0.0.1 never sets this_entity when it builds the coin-drop loot context, so a player kill of a mob with a coin pool threw "Missing required parameters: [this_entity]" out of its LivingDeathEvent listener (seen in the /nemesis selftest, the kill of the nemesis by the fake player). LcmLootManagerMixin adds the dying entity; it does nothing without the mod. Full pack (srv-fix2, 390 mods): /nemesis selftest PASS, 0 exceptions (before the fix: command exception, 3 log errors). Real player kills of coin-pool mobs were not run in game.

## Hidden test-account exemption
`config/diaithirnemesis_hidden.json` with `{"exempt":["Uber_Byte"]}` (names or UUIDs) stops those accounts being Shaken on respawn. Optional, no default file, not in any menu. This is the source form of the bytecode patch in PR #97 (tools/nemesis_hidden_exempt); it also matches the account name, so online-mode players work. Not compiled or run in the cloud (Java 21 here, the mod needs 25); needs one PC respawn test.
