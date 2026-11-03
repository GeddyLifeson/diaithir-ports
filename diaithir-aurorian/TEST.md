# Test results

`./gradlew compileJava` and `runServer` on JDK 25, NeoForge 26.1.2.116 (cloud, flat server.properties, rcon).
- Boot: no ERROR lines from the mod; items, blocks, recipes, tags, loot tables, the dimension type, three biomes and all features load.
- `forceload` of 36 chunks in `theaurorian:the_aurorian`: no errors; block counts in x0-31, z0-31: grass 243, dirt 4223, stone 48374, silentwood log 92, leaves 670, cerulean ore 162, moonstone ore 81, coal ore 396, geode 92, cobblestone 201. `locate biome theaurorian:aurorian_forest` succeeds.
- Not tested: a client (rendering, sky, armour layers), crafting every recipe, tall grass and lavender placement, structures (dungeon, ruined house), any boss.
