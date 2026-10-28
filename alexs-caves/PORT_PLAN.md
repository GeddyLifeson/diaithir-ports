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
- Advanced pathfinding, villager trades, Saddleable and PowerableMob behaviours, frog variant data, custom collide overrides.
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
Round 2 (ported, in-game checked unless noted):
- Raygun beam: `client/render/misc/RaygunBeam` (two spinning crossed double-sided ribbons, `RenderTypes.eyes`). Third person is submitted from `ACPotionEffectLayer`, first person from `RenderLevelStageEvent.AfterTranslucentFeatures` (the local player model is not drawn in first person). Fix needed: 26.1 `Item.inventoryTick` is server-only and `getUseItem()` is a copy, so the client UseTime never advanced; `ClientTickingItem` (Raygun, Dreadbow, Galena Gauntlet, Resistor Shield) is now ticked from `ClientEvents.clientLivingTick`, and the use checks compare the hand stack by identity (also Sea Staff).
- Darkness trail ribbon (`ACPotionEffectLayer.submitTrail`).
- HUD: `client/gui/ACHud` through `RegisterGuiLayersEvent` (riding meters, darkness armour meter, irradiated hearts).
- Cave map: server `CaveBiomeMapWorldWorker` (time-budgeted by `CaveMapWorkers` on `ServerTickEvent.Post`), `RecipeCaveMap` (8 paper around a Cave Codex, `alexscaves:cave_map` custom recipe), item model chain on `custom_model_data` flags (empty, loading, filled), `CaveMapRenderer` plus `CaveMapSpecialRenderer` (128x128 dynamic texture from the per-biome background pngs, borders, labels, player marker). Verified end to end with a vanilla-biome target (search, fill, rendered texture and labels, no magenta); the magnetic caves target failed to find a biome within the search radius of the test spawn, which is the intended "could not find" path.
- Biome fog: `ClientEvents.fogRender` scales atmospheric near and water far distance by the biome's nearness/farness (config gated). Not visually checked.
- Emissive blocks: `BakedModelShadeLayerFullbright` replaced by `"light_emission": 15` on the unshaded elements of 18 block models (ambersol, uranium, abyssal altar, glow models, peering coprolith, forsaken idol, magnetic light, tremorzilla egg). The `emissiveBlockModels` config is ignored. Not visually checked (placement attempt ended up under terrain).

Fallbacks for what is not ported, per item:
- ACItemRenderProperties, ACItemstackRenderer: superseded by `LegacyItemSpecialRenderer` / `ACLegacyItemRenderer` (item definitions with `minecraft:special`). Nothing lost.
- ACArmorRenderProperties: superseded by `CustomArmorLayer` and equipment assets.
- ClientLayerRegistry: replaced by the AddLayers registration in `ClientModEvents`. ACInternalShaders: replaced by `ACRenderPipelines`.
- ColorBlitHelper: the Cave Book draws itself, no shader needed.
- Advancement tabs: upstream draws custom tab backgrounds with mixins into `AdvancementTab`; there is no mixin setup in this module, so vanilla tabs with the data `background` are the shipped fallback.
- Blurred glow composite targets (ClientLayerRegistry glow passes, Raygun irradiated second-pass glow): these need extra render targets and a blur chain through `FrameGraphSetupEvent`; not ported. Fallback is direct emissive and translucent render types, so glows are sharp instead of bloomed.
- Raygun and cave-map hand poses (`EventPosePlayerHand`, one and two-handed map poses): not ported, vanilla hold pose is used; the held map is a flat quad.
- Biome sky overrides (hiding sun, stars, clouds in cave biomes) and lightmap ambient overrides: not ported; the biome JSON `attributes` could carry them. The boss-bar overlay and the crosshair/xp suppression while possessed are not ported either.
- Item model of an unfilled cave map shows as an empty hand in first person (hotbar icon is fine).
- Hologram, corrodent, licowitch and ambersol batch rendering at level stages: unchanged from before.
- Quarantine now holds only what is listed above.

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

## Round 3 status (paused, code written but NOT compiled or tested)
Written and committed uncompiled, because the VM was reserved for another worker's full-pack test (/home/user/FULLPACK_TEST_RUNNING) and no gradle build was allowed:
- Advancement tabs without mixins: `client/gui/ACAdvancementsScreen`, `ACAdvancementTab`, `ACAdvancementTabs`, AT entries in accesstransformer.cfg, swap via `ScreenEvent.Opening` in `ClientEvents`.
- Boss-bar overlay and crosshair/xp/selected-item suppression while possessed or riding (`ClientEvents.onPreRenderGuiLayer`, `renderBossOverlay`).
- Hold poses: `client/render/misc/ACArmPoses` (HumanoidModel.ArmPose enum extensions in enumextensions.json, set from the render state modifier in `ACPotionEffectLayer.extract`) and first-person cave map hand poses (`CaveMapHand`, via `RenderHandEvent`).
- Biome sky, sun/moon/star/cloud hiding, ambient light and light tint as data attributes in the six biome JSONs (the biomeSkyOverrides/ambient configs no longer apply).
- Glow bloom: `client/render/GlowBloom` (own render targets through `OutputTarget`, post chains post_effect/glow_irradiated.json and glow_witch.json, shader post/glow_composite.fsh).
TODO next session: run `./gradlew compileJava` and fix errors (likely spots: AT field names, enum extension descriptor, `HumanoidModel.ArmPose.valueOf` names, PostChain external targets), then verify in game: tabs screen, boss bar, poses (raygun, gauntlet, shield, shotgum, hook, sugar rush), held cave map, biome sky/light in a cave biome, irradiated glow bloom, fog scaling, emissive blocks at night, Dreadbow/Gauntlet/Shield animations, structure interior (loot chests, submarine spawn via /place). Hologram bloom and the raygun second-pass glow are still not ported. Then rebuild jars, recopy to builds/mod-ports, update TEST.md.
