# Test results: deeperdarker 1.4.1-diaithir.1

Build: NeoForge 26.1.2.116, Java 25, `./gradlew jar`. Boot tests on a stand-alone NeoForge 26.1.2.116 server (installer) with only this jar, rcon-driven.

| Check | Result |
|---|---|
| Server boots, no registry or datapack errors | pass (Done in ~4 s; no "Couldn't parse" lines) |
| Dimension `deeperdarker:otherside` generates, 4 biomes found with `/locate biome`, `ancient_temple` found with `/locate structure` | pass (`gloomaze` not found within the search radius; not investigated) |
| `/summon` of all 15 entities (boats, pots, angler fish, centipede, leech, snapper, shattered, shriek worm, sludge, stalker) and 8 s of ticking | pass, no errors |
| Every registered item id accepted by `/item replace` (216 items); every block id placed with `/setblock` (149 blocks) | pass (12 ids are blocks with no item, as expected) |
| Loot tables: 5 temple chests + crystallized amber via `/loot spawn` | pass |
| Dimension travel: `/otherside light` on a reinforced-deepslate frame, pig walks into the Overworld portal and arrives in the Otherside (exit portal built) | pass |
| With the pack's Tellus jar and an Earth world (`tellus:earth`, Everest spawn, stub biomes for the Regions Unexplored / BoP ids that the Diaithir Tellus fork references) | pass: Overworld stays Tellus Earth (min Y -368), Otherside generates its own noise world beside it, mobs and structures work. Earth chunk generation itself needs the internet tile services and stalled the server watchdog in the sandbox; not a Deeper Darker issue. |
| Age gate: `othersideRequiredTag` blocks players without the entity tag | code only; not run (needs a player client) |

Not tested (no client available here): item and block rendering, armour models, the compass needle, transmitter linked model, sonorous staff charge model, portal overlay, GUI. Item definitions, equipment assets and a texture/parent audit of all models were generated and checked by script (`tools/gen_assets_26_1.py`), no missing parents or textures remain. The gloomslate pot item uses the vanilla decorated-pot model.

Known gaps: trimmed armour variants dropped; JEI plugin and datagen removed; the Warden-era `gloomaze` structure unverified.
