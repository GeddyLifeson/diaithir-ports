# Realms: clean-room replacements for blocked dimension mods

Round 2 of the clean-room plan (`/mnt/project-files/plans/CLEAN_ROOM_ROUND2.md`). Each realm lives inside this module (no new jar), keeps the modid and every block and item id of the mod it replaces, and has no third-party code, art or data. Only registry ids (names) were read from the old jars. Vanilla shapes (stairs, doors, walls) are copied from Minecraft's own assets and use vanilla stand-in textures until the art pipeline supplies 64 px art.

| Realm modid | Replaces | Licence found (Modrinth, 11-03) | Dimensions | Blocks | Items | Biomes | Age (proposed) |
|---|---|---|---|---|---|---|---|
| undergarden | The Undergarden | All Rights Reserved on Modrinth; jar mods.toml says MIT; GitHub repo has no LICENSE file. Treated as ARR | 1 | 215 | 41 | 20 | II |
| the_backwoods | Backwoods | All Rights Reserved | 8 | 57 | 65 | 26 | IV |
| the_bumblezone | The Bumblezone | All Rights Reserved | 1 | 99 | 64 | 9 | II |
| aether_ii | The Aether II | Custom licence, source repo not readable | 1 | 529 | 173 | 16 | III (canon, dimension-placements) |
| stellaris | Stellaris 2.0.10 | CC-BY-NC-SA-4.0: share-alike and non-commercial cannot be folded, so rewritten (round 2, 11-03) | 2 (Moon, Earth orbit) | 105 | 82 + 15 gear | 6 | VII |

Not rewritten, because the licence is open (handed to consolidation to fold): Aerial Hell (CC-BY-NC-SA-4.0), Regions Unexplored (MIT), BCLib (MIT), Kotlin for Forge (LGPL-2.1). Libraries GlitchCore (ARR), Cristel Lib (NC-ND), CodxLib (NC-ND) and Valhelsia Core (custom) can be dropped once nothing else in the pack depends on them; none of the four realms needs them.
| aerialhell | Aerial Hell | CC-BY-NC-SA-4.0 (open, but ShareAlike would bind this module, so rewritten instead of folded) | 1 | 511 + 82 migrated | 116 | 14 | IV (proposed) |

Not rewritten, because the licence is open (handed to consolidation to fold): Stellaris (CC-BY-NC-SA-4.0), Regions Unexplored (MIT), BCLib (MIT), Kotlin for Forge (LGPL-2.1). Libraries GlitchCore (ARR), Cristel Lib (NC-ND), CodxLib (NC-ND) and Valhelsia Core (custom) can be dropped once nothing else in the pack depends on them; none of the four realms needs them.

## What is in this slice
- `tools/realms_gen.py <realm> --client-jar <minecraft client jar>` regenerates everything from `tools/realms/<realm>.{json,blocks.txt,items.txt,biomes.txt}`.
- `RealmRegistrar` registers blocks and items from `data/diaithir_overworld_biomes/realms/<modid>.json`; one tiny `@Mod` class per realm.
- Data per realm: dimension type, dimension, noise settings, one biome file per biome (multi-noise layout of our own), surface rules, loot tables, recipes (stairs, slabs, walls, fences, doors, planks, ingot/block, smelting), `c:` and vanilla tags, `id_migrations.json` (signs, beds, pots, tools, armour, boats, buckets map to vanilla), Age gate, quest, Codex entry.
- Headless test (cloud, 11-03): server boots with no new errors; all 11 dimensions load, generate chunks, and place their blocks.

## Slice 2: portals (11-03)
Right-click a frame block with the activator item and a two-high portal column appears on top; walking in teleports to the same x and z, builds a 3x3 pad and a return column at the exit. Undergarden: catalyst on forgotten_block. Backwoods (4 portals): memory_quartz on the frame block named in the manifest. Aether II: glowstone_dust on aether_portal_frame. Item-entity round trip tested headless; player activation is untested (needs a client).

## Slice 3: trees and plants (11-03)
One tree feature per wood family that has a log and leaves, one patch feature per plant block, assigned to biomes by name (tree family in the biome name, else forests/woods/groves round-robin). Placement uses the cave-safe random-offset pattern with an air-above-solid filter so it works under ceilings. Headless: wigglewood forest produced 15 logs and 173 leaves in one 32x32 sample; all 11 dimensions generate with no errors. Giant mushrooms are not done.

## Slice 4: mobs (11-03)
86 creatures (undergarden 22, backwoods 24, bumblezone 7, aether_ii 33) under the original entity ids. Each is a small subclass of a vanilla creature (zombie, illager, golem, spider, ravager, blaze, silverfish, phantom, guardian, hoglin) chosen by name, so vanilla model, renderer and melee come free; passive ones lose their target goals. Own look so far is a generated recolour of the vanilla texture per id (stand-in until slice 7 art). Spawn eggs, entity loot, add_spawns biome modifiers (spread over the dimensions), `<modid>:bosses` and `<modid>:<modid>_mobs` entity tags, bestiary Codex entry and a hunt quest per realm. Bosses are not spawned naturally: they carry the `bosses` tag and big stats so the rites lane (diaithir-bosses) can attach rituals and bars. Headless: all 86 summon without error; natural spawning and rendering untested (no client).

## Slice 5: structures (11-03)
79 structures under the original ids (undergarden 4, backwoods 24, bumblezone 33, aether_ii 18). Each is one procedurally generated template (tower, ruin or shrine, chosen by name; own design, built from the realm's own bricks and planks, with a foundation so it never floats), a jigsaw structure, template pool, structure set and a `<modid>:<modid>_structures` tag, spread over the realm's dimensions, plus a Codex entry. Original multi-piece layouts are not reproduced. Headless: every template loads with `place template`, `locate structure` finds each realm's tag.

## Slice 6: gear (11-03)
101 tools and armour pieces (swords, spears as swords, axes, battleaxes as axes, pickaxes, shovels, hoes, helmets, chestplates, leggings, boots) with real tool and armour materials per prefix. Tier picked by prefix from the vanilla ladder (wood, stone, iron, diamond; stats are my own, listed in `RealmGear`), repair tags `<modid>:repair/<prefix>`, shaped recipes, equipment assets, recolored vanilla item and armour-layer textures as stand-ins, vanilla `swords/axes/head_armor/...` tags. Shields, buckets and sign/bed style items still migrate to vanilla. Special abilities (e.g. Aether gravitite, Undergarden forgotten gear) are not implemented. Also fixed: shared `minecraft:` and `c:` tags are now merged across realms instead of overwritten.

## Not done yet (owners: mobs lane, structures lane, rites lane)
Boss rituals, per-creature models and abilities, loot rooms and chests in structures, giant mushrooms, carvers, geode teleporter, gear abilities, shields and buckets (migrated to vanilla), block entities (chests, spawners, infuser), sanity mechanic, Bumblezone bee behaviour and fluids, Aether gear and gliding. Textures are stand-ins. Age placements for Undergarden, Backwoods and Bumblezone are approved by the coordinator (11-03) but not yet written into canon.md or PLACEMENTS.md.

## Slice 7: generated art
All realm blocks and simple items now use generated 64 px textures (tools/realm_art.py, procedural, own design; stairs/slabs/etc. share their parent's look; logs and turf get _top variants; leaves/glass/portals alpha; plants are cross sprites; ores have colored spots). No vanilla stand-in textures remain. Per-creature models and hand-painted art are still open.

## Stellaris realm (round 2, 2026-11-03)
Clean-room: only registry ids were read from the 2.0.10 jar (blockstate, item, biome, structure and creature names); no code, models, textures, lang or recipes copied. Everything is regenerated by `tools/realms_gen.py stellaris` plus `tools/stellaris_extra.py` (recipes for the hand-written pieces).
- Kept under modid `stellaris`: 105 blocks (titanium, desh, novite, Moon stone, Mars blocks, lunar wood set, plating, pillars), 82 items plus 15 tools and armour pieces, 6 biomes, the Moon and Earth orbit dimensions, 9 creatures (star crawler, Heart of Luna and star crawler boss are bosses), 13 structures.
- Own code (`com.diaithir.overworld_biomes.space`): `SpaceAir` (Moon and Earth orbit are airless: survival players take damage each second unless they wear the full space suit with an oxygen module in the pack, or stand within 6 blocks of an oxygen distributor; the Moon has about a sixth of gravity and soft falls), `RocketItem` (use with a fluid cell: Terra to Moon, back to Terra from the Moon or orbit, sneak on Terra to orbit), `SpaceTravel.launch(player, target)` (target is moon, earth_orbit, terra or any Panscriptum planet id, which goes through PlanetGate, so the planets lane and the Keelwright ships lane can call the same entry; planets keep their own air), oxygen distributor block.
- Not rebuilt: the machines and fluids (generators, electrolyzer, refinery, pumpjack, cables, pipes, tanks, rocket pad, solar panel, rover, tablet and so on). Their ids migrate to vanilla stand-ins in `data/stellaris/id_migrations.json`; power and fluids belong to the tech modules (SI units, `plans/UNITS.md`). Oxygen tiers 2 and 3 are the same as tier 1 for now (no tank capacity yet).
- Art: generated 64 px textures (realm_art), recoloured vanilla creature models, as for the other realms.
- Headless test (NeoForge 26.1.2.116, whole module, all five realms loaded): boots with no new errors, 2192 recipes load, Moon and orbit generate chunks, `locate` finds structures and biomes, creatures summon, `/stellaris selftest` 12/12 PASS (air rules with a fake player, launch to terra, moon and orbit). Not tested: a real client (rendering, the damage message, gravity feel), natural spawning, structure contents.
- Fixed on the way: `RealmMobs` hooked attribute and renderer events only on the first realm's bus and read shared maps from parallel mod construction threads; with five realms this crashed boot (NPE). Each realm now wires its own creatures on its own bus and the maps are concurrent.
## Aerial Hell (round 2, 11-03 evening)
Added as the fifth realm with the same generator (`tools/realms/aerial_hell.*`, ids read from the 0.7.8 NeoForge 26.1.2 jar, no code, art or data copied). Portal: volucite_shard on stellar_portal_frame_block. 50 creatures (shurikens and projectiles left out; they migrate with the item ids), 15 structures, 14 biomes. Age IV is a proposal, not in canon. Slices 4-7 output (mobs, structures, gear, art) come from the generator and are stand-ins like the other realms.
## Slice 8: loot, gear abilities, sizes
- Every structure template now holds chests with a per-structure loot table (`<modid>:chests/<structure>`: realm gear, ingots/crystals, vanilla staples; towers and shrines are richer and enchanted).
- Gear abilities (RealmGear): weapon hits apply an effect picked from the material name (frost slows, ember ignites, rot poisons, honey heals the wielder, cloud lifts, default weakness). A full armour set of one material gives short Resistance when hit and slows the attacker.
- Per-creature size: scale comes from the creature's id hash (0.8-1.4 monsters, 0.7-1.2 passive, 1.5-2.2 bosses). Hand-modelled bodies are still open.
- Boss rituals: bosses are tagged `<modid>:bosses` for the diaithir-bosses lane; the rites thread owns the ritual itself.
