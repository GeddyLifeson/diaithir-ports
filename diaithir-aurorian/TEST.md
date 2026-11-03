# Test results

`./gradlew compileJava` and `runServer` on JDK 25, NeoForge 26.1.2.116 (cloud, flat server.properties, rcon).
- Boot: no ERROR lines from the mod; items, blocks, recipes, tags, loot tables, the dimension type, three biomes and all features load.
- `forceload` of 36 chunks in `theaurorian:the_aurorian`: no errors; block counts in x0-31, z0-31: grass 243, dirt 4223, stone 48374, silentwood log 92, leaves 670, cerulean ore 162, moonstone ore 81, coal ore 396, geode 92, cobblestone 201. `locate biome theaurorian:aurorian_forest` succeeds.
- Not tested: a client (rendering, sky, armour layers), crafting every recipe, tall grass and lavender placement, structures (dungeon, ruined house), any boss.

## Follow-up tests (headless server, rcon)
- Portal: frame lit with `/aurorianselftest portal`, a sheep teleported both ways. Furnace smelts. Mob summons, structures generate with no errors.
- Scrapper: crystal plus aurorian_steel_axe produced 6 aurorian_steel_nugget. Moonlight forge (midnight, open sky): moonstone_axe plus aurorian_steel_ingot produced aurorian_steel_axe.
- `/aurorianselftest steel`: 100 item damage events took a sharpness I aurorian steel sword to sharpness II (xp reset, multiplier 1.25).
- Items summon with no errors: amulets, strange meat, divining rod. Not tested: orb repair, spectral cleanse, umbra thorns, moonstone damage with a real player; client rendering.
- Client join (cloud, Xvfb, single mod, `./gradlew runClient` joins a local server with `--quickPlayMultiplayer`): joined, rendered the world and hotbar. Found and fixed two real bugs: Forge `separate-perspective` models (umbra greatsword, now a `display_context` item definition) and spawn eggs (26.1 has no template egg, so own textures). Items, armour bar and icons shown in the hotbar. Sky, mobs and armour layers on the model not inspected.
