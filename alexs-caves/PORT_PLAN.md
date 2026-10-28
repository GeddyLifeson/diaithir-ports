# Alex's Caves port plan (module `alexs-caves`, NeoForge 26.1.2.116, Java 25)

Upstream: AlexModGuy / Jusey1 Alex's Caves 2.0.2, branch 1.21.1, LGPL-3.0. Code reference is the 1.21.1 source. Jar suffix `-diaithir.1`.

## Citadel
Alex's Caves depends on the separate module `citadel` (branch claude/port-citadel, LGPL-3.0, NOTICE kept). No 26.1 build existed on Modrinth or
GitHub when checked. It carries the animation payload, collision and pathing interfaces, AdvancedEntityModel/AdvancedModelBox/ModelAnimator and a
"legacy bridge" (client/render/legacy): the 1.21 immediate-mode renderers and layers run unchanged against a recording buffer source, and the
already transformed vertices are replayed through the 26.1 `submitCustomGeometry` path. Items, blocks and nested entities inside a legacy render
go through LegacyRenderContext. A failing legacy renderer logs once per entity type and is skipped instead of crashing the client.
Not in Citadel yet: book GUI, tabula loaders, shaders (PostEffectRegistry is a no-op stub), the tick-rate tracker.

## Approach
Mechanical rename passes (tools/rename1..16.py, lfix.py, fiximports.py) took the 1.21.1 tree to the 26.1 API, then manual fixes:
`Identifier`, `ValueInput`/`ValueOutput`, ServerLevel-only entity APIs, data components, attachments, enum extensions (Rarity, MobCategory),
Block/Item `Properties.setId` plumbing in `ACProps`, 26.1 datapack formats (biome attributes, recipes, loot modifiers, advancements)
by tools/convert_*.py. Client item definitions and equipment assets by tools/gen_assets.py.

## Cut from the first release
- Mixins (frostmint freeze on any mob now uses vanilla powder-snow freeze ticks), tick-rate tracker.
- Cave-map items and recipe, advanced pathfinding, villager trades, Saddleable and PowerableMob behaviours, frog variant data, custom collide overrides.
- Particle providers, block-entity renderers, GUIs, shaders, minigame and warning screens.

## World generation (Tellus Earth)
Upstream's cave biomes need a 3D biome source. Here a new feature `alexscaves:cave_chamber` is added to every overworld biome
(`#minecraft:is_overworld`) by a NeoForge biome modifier, in the underground-structures step. It carves an ellipsoid chamber
at least 10 blocks under the terrain, lines floor, walls and ceiling, floods it for toxic and abyssal chambers, relabels the chunk sections
to the cave biome (so that biome's mob spawn list applies) and scatters the biome's own configured decoration features.
Data is produced by tools/gen_chambers.py. Six biomes: primordial_caves, forlorn_hollows, magnetic_caves, toxic_caves, candy_cavity, abyssal_chasm.

## Client
All 83 entity renderers use the real upstream models through the Citadel legacy bridge (client/model, client/render/entity, tools/client_port*.py).
Ported and checked in the dev client: particles, GUIs (spelunkery, Nuclear Furnace, Cave Book), block entity renderers, armour, potion layer, core shaders (ACRenderPipelines), post effects (ACPostEffects), Sack of Sating tooltip.
Still quarantined: cave map renderers, Raygun beam helper, ACItemRenderProperties/ACItemstackRenderer/ACArmorRenderProperties, advancement tabs, ColorBlitHelper, BakedModelShadeLayerFullbright, ACInternalShaders, ClientLayerRegistry (blurred glow composite targets).

## Structures
Upstream sizes its structures by sampling a 3D cave biome source. There is none here, so `structure/CaveMaskBiomeSource` stands in for it: a
cave biome exists inside a disc around the start chunk of its parent bowl structure (cake cave, dino bowl, acid pit, ferrocave, forlorn canyon,
ocean trench), recomputed from the world seed by the same random_spread maths. The parent numbers live in `ACCaveParents` and must match the
parent structure_set files (spacing 31 to 53, separation 8, salts 101 to 106). Children (volcano, donut arch, gingerbread town, licowitch tower,
soda bottle, forlorn bridge, abyssal ruins) only start inside a viable parent; viability repeats the parent's terrain gate. Bowls are squashed to
stay 14 blocks under the lowest surface of their footprint and are dropped if less than 40 tall; the trench needs an ocean floor 6 below sea level.
Ferrocave and forlorn canyon now relabel their biome themselves like the other bowls. Structure biomes are `#minecraft:is_overworld`.
The small cave chambers stay as a second source of cave biomes.

## Diaithir layer
Names: Diaithiris display names for 626 item, block, entity and biome keys (tools/apply_names.py, built by the archaic names tool).
Age gate: age_gates.json, namespace `alexscaves`, Age IV and up. Library text for creatures, caves and sites: compute jobs
`caves_mobs`, `caves_biomes`, `caves_structures` (tools/gen_jobs.py), merge writes library-sync/alexs-caves.json.
