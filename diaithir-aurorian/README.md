# The Aurorian (port, MIT)

Port of [shiroroku/TheAurorian](https://github.com/shiroroku/TheAurorian) (MIT, Copyright (c) 2024 Shiroroku (Elise); the 1.19.2 rewrite of the original 1.12.2 CurseForge mod 352137) to Minecraft 26.1.2 NeoForge. Licence and credit are kept in `LICENSE`. Upstream's art and data are MIT with the code, so textures and models are reused as they are.

The module id stays `theaurorian` so every upstream id is unchanged. `tools/port.py <TheAurorian checkout> .` regenerates all data from upstream (items, blocks, recipes, tags, loot, worldgen, textures, lang); the Java in `DiaithirAurorian.java` only reads `data/theaurorian/aurorian.json`.

## What works now (tested on a 26.1.2.116 dedicated server)
- 76 items: ingredients, foods and teas (with status effects), keys, tool and armour tiers (silentwood, Aurorian stone, moonstone, cerulean, Aurorian steel, aurorianite, umbra, crystalline; cerulean, steel and knight armour, shields), 48 blocks (runestone, darkstone, moon temple, natural stone and wood sets, ores, plants). Fuel values via the furnace fuel data map.
- 70 recipes converted to the 26.1 recipe format; the rest need custom crafters (see below).
- The dimension `theaurorian:the_aurorian` (eternal night), three biomes, ore, tree, rock, geode and plant features, the jigsaw dungeon and ruined house definitions. A forceload of 36 chunks generated with no errors: Aurorian grass, dirt and stone, silentwood logs and leaves, cerulean, moonstone and coal ore, geodes and cobblestone rocks were found by block count. `locate biome` finds the forest.
- Age gates (`age_gates.json`: silentwood and stone I, cerulean, moonstone, knight II, steel and spectral III, aurorianite, umbra, crystalline IV), `c:` ore, ingot and nugget tags, Codex handbook and quest chain data in `codex/`.

## Key gates
The five key-hole blocks work from code (`GateKeyholeBlock`): the matching key, or lockpicks on the runestone gate, opens every `theaurorian:dungeon_gates` block within 3 blocks. Registered and booted; the click itself is not tested without a player.

## Follow-up (draft PR, all headless-tested unless noted)
- Portal frame (flint and steel) and two-way portal, Aurorian furnace with chimney multiplier, silentwood chest and crafting table, crystal block.
- Four mobs (hollow, undead knight, dungeon slime, dungeon keeper) with spawn eggs, boss spawner, fog wall, and a `diaithir-bosses` profile for the Dungeon Keeper (`data/diaithirbosses/boss/`; the bosses mod was not on the test classpath, so the attach is untested).
- Scrapper and moonlight forge as data-driven recipes (`data/theaurorian/machine_recipes/{scrapper,moonlight_forge}/`, datapack-reloadable). They use the five-slot hopper screen instead of upstream's custom screens.
- Abilities: moonstone durability, Aurorian steel XP levelling of enchantments (tested), spectral cleanse, umbra thorns while crouching and greatsword slow/resist, crystalline shield and absorption orb repair, living divining rod, strange meat.
- Amulets: the pack has no Curios API, so the five amulets apply their attribute modifiers while held in the offhand (clean-room replacement, switch to a curio slot if Curios 26.1 joins the pack).
- Upgrade rules for the furnace, scrapper and forge in `data/theaurorian/upgrades/targets/` (#117 data rules). No unit-bearing quantities exist in this module, so UNITS.md needs nothing beyond showing times in seconds.
- Scripts: `tools/port.py` = S6880, `tools/rcon.py` = S6881 (claimed with Global-Repo `claim_id.py`; catalogue rows still to be added there).

- Silentwood sapling grows the silentwood tree (tested with random ticks, 13-log trunk).

## Not ported yet (listed, not registered)
Bow and the cerulean and crystal arrows, crystalline sword beam, mirror of guidance, sickles, the umbra pickaxe selection mode, the crystalline pickaxe ore treasure, and upstream's custom machine screens. Upstream itself marks the darkstone and moon temple dungeons, dungeon mobs, farming and the portal as unfinished.
Quests in `codex/aurorian_quests.json` carry `live: false` for the dungeon and boss steps until the keeper boss exists. The plan is to attach the dungeon bosses through `diaithir-bosses` profiles once the mobs are ported. Plants (tall grass, lavender) were not seen in the test area and need a look in a client. Diaithiris names, the other 129 language names and Codex prose are follow-ups. No client test yet (icons, armour layers, sky).
