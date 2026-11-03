# Realms: clean-room replacements for blocked dimension mods

Round 2 of the clean-room plan (`/mnt/project-files/plans/CLEAN_ROOM_ROUND2.md`). Each realm lives inside this module (no new jar), keeps the modid and every block and item id of the mod it replaces, and has no third-party code, art or data. Only registry ids (names) were read from the old jars. Vanilla shapes (stairs, doors, walls) are copied from Minecraft's own assets and use vanilla stand-in textures until the art pipeline supplies 64 px art.

| Realm modid | Replaces | Licence found (Modrinth, 11-03) | Dimensions | Blocks | Items | Biomes | Age (proposed) |
|---|---|---|---|---|---|---|---|
| undergarden | The Undergarden | All Rights Reserved on Modrinth; jar mods.toml says MIT; GitHub repo has no LICENSE file. Treated as ARR | 1 | 215 | 41 | 20 | II |
| the_backwoods | Backwoods | All Rights Reserved | 8 | 57 | 65 | 26 | IV |
| the_bumblezone | The Bumblezone | All Rights Reserved | 1 | 99 | 64 | 9 | II |
| aether_ii | The Aether II | Custom licence, source repo not readable | 1 | 529 | 173 | 16 | III (canon, dimension-placements) |

Not rewritten, because the licence is open (handed to consolidation to fold): Aerial Hell (CC-BY-NC-SA-4.0), Stellaris (CC-BY-NC-SA-4.0), Regions Unexplored (MIT), BCLib (MIT), Kotlin for Forge (LGPL-2.1). Libraries GlitchCore (ARR), Cristel Lib (NC-ND), CodxLib (NC-ND) and Valhelsia Core (custom) can be dropped once nothing else in the pack depends on them; none of the four realms needs them.

## What is in this slice
- `tools/realms_gen.py <realm> --client-jar <minecraft client jar>` regenerates everything from `tools/realms/<realm>.{json,blocks.txt,items.txt,biomes.txt}`.
- `RealmRegistrar` registers blocks and items from `data/diaithir_overworld_biomes/realms/<modid>.json`; one tiny `@Mod` class per realm.
- Data per realm: dimension type, dimension, noise settings, one biome file per biome (multi-noise layout of our own), surface rules, loot tables, recipes (stairs, slabs, walls, fences, doors, planks, ingot/block, smelting), `c:` and vanilla tags, `id_migrations.json` (signs, beds, pots, tools, armour, boats, buckets map to vanilla), Age gate, quest, Codex entry.
- Headless test (cloud, 11-03): server boots with no new errors; all 11 dimensions load, generate chunks, and place their blocks.

## Slice 2: portals (11-03)
Right-click a frame block with the activator item and a two-high portal column appears on top; walking in teleports to the same x and z, builds a 3x3 pad and a return column at the exit. Undergarden: catalyst on forgotten_block. Backwoods (4 portals): memory_quartz on the frame block named in the manifest. Aether II: glowstone_dust on aether_portal_frame. Item-entity round trip tested headless; player activation is untested (needs a client).

## Not done yet (owners: mobs lane, structures lane, rites lane)
Mobs and bosses, structures and template pools, placed features (trees, mushrooms, plants), carvers, geode teleporter, tools and armour (migrated to vanilla iron), block entities (chests, spawners, infuser), sanity mechanic, Bumblezone bee behaviour and fluids, Aether gear and gliding. Textures are stand-ins. Age placements for Undergarden, Backwoods and Bumblezone are approved by the coordinator (11-03) but not yet written into canon.md or PLACEMENTS.md.
