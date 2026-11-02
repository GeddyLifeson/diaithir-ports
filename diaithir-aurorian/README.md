# The Aurorian (port, MIT)

Port of [shiroroku/TheAurorian](https://github.com/shiroroku/TheAurorian) (MIT, Copyright (c) 2024 Shiroroku (Elise); the 1.19.2 rewrite of the original 1.12.2 CurseForge mod 352137) to Minecraft 26.1.2 NeoForge. Licence and credit are kept in `LICENSE`. Upstream's art and data are MIT with the code, so textures and models are reused as they are.

The module id stays `theaurorian` so every upstream id is unchanged. `tools/port.py <TheAurorian checkout> .` regenerates all data from upstream (items, blocks, recipes, tags, loot, worldgen, textures, lang); the Java in `DiaithirAurorian.java` only reads `data/theaurorian/aurorian.json`.

## What works now (tested on a 26.1.2.116 dedicated server)
- 76 items: ingredients, foods and teas (with status effects), keys, tool and armour tiers (silentwood, Aurorian stone, moonstone, cerulean, Aurorian steel, aurorianite, umbra, crystalline; cerulean, steel and knight armour, shields), 48 blocks (runestone, darkstone, moon temple, natural stone and wood sets, ores, plants). Fuel values via the furnace fuel data map.
- 70 recipes converted to the 26.1 recipe format; the rest need custom crafters (see below).
- The dimension `theaurorian:the_aurorian` (eternal night), three biomes, ore, tree, rock, geode and plant features, the jigsaw dungeon and ruined house definitions. A forceload of 36 chunks generated with no errors: Aurorian grass, dirt and stone, silentwood logs and leaves, cerulean, moonstone and coal ore, geodes and cobblestone rocks were found by block count. `locate biome` finds the forest.
- Age gates (`age_gates.json`: silentwood and stone I, cerulean, moonstone, knight II, steel and spectral III, aurorianite, umbra, crystalline IV), `c:` ore, ingot and nugget tags, Codex handbook and quest chain data in `codex/`.

## Not ported yet (listed, not registered)
Behaviour that lives in upstream Java: the portal and portal frame, Aurorian furnace, moonlight forge, scrapper, silentwood chest and crafting table, boss spawner, chimney, crystal, fog walls, key-hole gates, silentwood sapling and its tree growth, special weapon and armour abilities (steel, moonstone, umbra, crystalline, spectral), curio amulets, mirror of guidance, absorption orb, divining rod, arrows, bow, sickles, strange meat, the four mobs (hollow, undead knight, dungeon keeper, dungeon slime) and spawn eggs, and the custom recipe types. Upstream itself marks the darkstone and moon temple dungeons, dungeon mobs, farming and the portal as unfinished.
Quests in `codex/aurorian_quests.json` carry `live: false` for the dungeon and boss steps until the keeper boss exists. The plan is to attach the dungeon bosses through `diaithir-bosses` profiles once the mobs are ported. Plants (tall grass, lavender) were not seen in the test area and need a look in a client. Diaithiris names, the other 129 language names and Codex prose are follow-ups. No client test yet (icons, armour layers, sky).
