# Alex's Caves port plan (module `alexs-caves`, NeoForge 26.1.2.116, Java 25)

Upstream: AlexModGuy / Jusey1 Alex's Caves 2.0.2, branch 1.21.1, LGPL-3.0. Code reference is the 1.21.1 source. Jar suffix `-diaithir.1`.

## Citadel assessment
Upstream depends on Citadel (LGPL-3.0, same author). Only the server-side classes Alex's Caves actually calls are inlined under
`com.github.alexthe666.citadel` (animation payload, tall-walker and advanced-pathing interfaces, entity helpers). Citadel's client code
(AdvancedModelBox model system, book GUI, tabula loaders, shaders) is not included. No Citadel jar is needed at runtime.

## Approach
Mechanical rename passes (tools/rename1..16.py, lfix.py, fiximports.py) took the 1.21.1 tree to the 26.1 API, then manual fixes:
`Identifier`, `ValueInput`/`ValueOutput`, ServerLevel-only entity APIs, data components, attachments, enum extensions (Rarity, MobCategory),
Block/Item `Properties.setId` plumbing in `ACProps`, 26.1 datapack formats (biome attributes, recipes, loot modifiers, advancements)
by tools/convert_*.py. Client item definitions and equipment assets by tools/gen_assets.py.

## Cut from the first release
- Mixins (frostmint freeze on any mob now uses vanilla powder-snow freeze ticks), tick-rate tracker.
- Nuclear Furnace (kept as a plain block; code is in quarantine/), cave-map items and recipe, advanced pathfinding, villager trades, Saddleable and PowerableMob behaviours, frog variant data, custom collide overrides.
- Citadel-model entity renderers: every mob is drawn as a hitbox-sized box with its own texture (client/FallbackRenderers). Models are the main follow-up.
- Structures: data is present but they do not generate (they need the 3D cave biome source). Only the cave chambers generate.
- Particle providers, block-entity renderers, GUIs, shaders, minigame and warning screens.

## World generation (Tellus Earth)
Upstream's cave biomes need a 3D biome source. Here a new feature `alexscaves:cave_chamber` is added to every overworld biome
(`#minecraft:is_overworld`) by a NeoForge biome modifier, in the underground-structures step. It carves an ellipsoid chamber
at least 10 blocks under the terrain, lines floor, walls and ceiling, floods it for toxic and abyssal chambers, relabels the chunk sections
to the cave biome (so that biome's mob spawn list applies) and scatters the biome's own configured decoration features.
Data is produced by tools/gen_chambers.py. Six biomes: primordial_caves, forlorn_hollows, magnetic_caves, toxic_caves, candy_cavity, abyssal_chasm.

## Diaithir layer
Names: Diaithiris display names for 626 item, block, entity and biome keys (tools/apply_names.py, built by the archaic names tool).
Age gate: age_gates.json, namespace `alexscaves`, Age IV and up. Library text for creatures, caves and sites: compute jobs
`caves_mobs`, `caves_biomes`, `caves_structures` (tools/gen_jobs.py), merge writes library-sync/alexs-caves.json.
