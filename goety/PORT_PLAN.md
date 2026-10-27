# Goety port plan (module `goety`, NeoForge 26.1.2.116)

Upstream: Polarice3/Goety-2, MIT. The latest 1.21.1 NeoForge build is 3.2.2 (Modrinth), but the Modrinth source_url repo only has
branches 1.19 and 1.20 (no 1.21 source is published). Code reference is therefore the 1.20 branch (Forge, version 2.5.59.2);
assets and data come from the same tree. The 3.2.2 jar was not decompiled.

## Size of upstream (1.20 branch)
- 2235 Java files, 340k lines. 396 item registrations (119 foci, 126 spell classes), 604 block ids, ~300 entity types,
  1400 recipes, 2900 textures, 1800 sounds (73 MB), 2160 Patchouli book pages, 250 structure NBTs.
- Dependencies: Curios (in pack, 26.1 build 15.0.0), Patchouli (not in pack; CC-BY-NC-SA, dropped; the pack uses Modonomicon),
  GeckoLib is NOT used by this version of Goety (all models are vanilla Java layer models), JEI (optional, dropped).
- Everything is written against the pre-1.21 API: NBT save/load, Forge capabilities, SimpleChannel, ItemStack NBT, entity
  renderers without render states, worldgen JSON of 1.20. A line-by-line port of 340k lines is not feasible in one run.

## Approach
Compact re-implementation in `net.diaithir.goety` on the 26.1 API (data components, attachments, payloads, item definitions),
reusing upstream (MIT) textures, models, sounds, lang keys and recipe patterns through `tools/gen.py`. Upstream behaviour is
followed where it can be read from the source; Patchouli, JEI and other-mod compat code are dropped.

## Stages
1. Core registries, items, blocks: materials, dark metal tools and armour, haunted wood, stones, lamps; models, textures,
   item definitions, loot tables, recipes converted to the 26.1 format.
2. Entities: servants (zombie, skeleton, ...) and projectiles, vanilla-based models and 26.1 render states. GeckoLib 5
   is on the classpath but unused unless a model needs it.
3. Magic: soul energy (attachment), totem of souls, wands and staves, foci, spells and effects, Curios rings and amulets.
4. Worldgen, structures, loot injection.
5. Server boot, /summon every entity, /give every item through rcon; results in TEST.md.

Celtic framing: display names in Diaithiris (names_map.json via archaic.py), necromancy as the Cailleach's Samhain rites,
Age gate in age_gates.json (namespace `goety`, Age V and up).
