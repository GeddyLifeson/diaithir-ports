# Goety port test results
Build: `./gradlew build` -> goety-neoforge-26.1.2-3.2.2-diaithir.1.jar (1.5 MB), NeoForge 26.1.2.116, Java 25.
Boot: dedicated server, clean start, 0 log errors.
In-game selftest (`/goetyselftest`, tools/runtests.sh): ok=1162 bad=0
 - all items build; all 463 blocks have state and item
 - all 7 servants spawn and tick 40 ticks
 - all 62 spells run via the spell engine; wands load foci; totem round-trips souls
 - all 27 rites run at an altar with pedestals and a FakePlayer
rcon: /summon of 7 servants, /give of every item and block: 610 ok, 0 failed.
Not tested: client rendering (headless server only).
## Left
57 unported foci, most upstream entities (necromancers, wraiths, golems, illagers, bosses), custom 128x64 armour models, ritual structure checks, Cursed Cage/infuser, Patchouli guide, sounds/particles, worldgen and structures (stage 4), ~140 spell classes beyond archetypes.
## Dependencies
Curios (in pack, required). GeckoLib in pack but unused (upstream uses none). Patchouli and JEI not in pack, dropped.
