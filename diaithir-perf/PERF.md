# Measurements

Full Diaithir server side (316 mods, pack branch claude/project-thread-ysjkxr), NeoForge 26.1.2.116, cloud 4 cores, -Xmx6G, level-seed=diaithir, view distance 6. `bench/bench.sh`: retained heap = used after a forced full GC; worldgen = 1,024 chunks via four `/forceload add` of 256.

| run | server load | level prep | heap idle | data fixer held | 1,024 chunks |
|---|---|---|---|---|---|
| baseline | 316 s | 53 s | 1,656 MB | 271 MB | 97 s |
| fix 1, first boot (cache empty) | 300 s | 52 s | 1,655 MB | 271 MB | 103 s |
| fix 1, later boots | 291 s | 38 s | 1,321 MB | 8 MB | 75 s |

## Fix 1: structure upgrade cache
Mods ship structure .nbt files saved by older game versions. Every load runs them through Mojang's data fixer during worldgen, and the first one forces the whole data fixer to be built (271 MB). `StructureUpgradeCache` stores each upgraded template under `diaithirperf/structures/`, keyed by content hash and data version, so later boots never build the data fixer: -335 MB idle heap, -28% level prep, -23% worldgen time.

## Client (title screen)
Cloud client, full pack (331 mods), software GL under Xvfb, -Xmx8G, `bench/client.sh`: retained heap after a forced full GC at the title screen. FPS is not meaningful on software GL.

| run | heap at title |
|---|---|
| baseline | 3,677 MB |
| fix 2 | 2,541 MB |
| fix 2 + 3 | 2,360 MB |

### In world (singleplayer, client + integrated server)
Same copy of the bench world (no forced chunks), `JOIN=1 bench/client.sh`, live heap after a full GC 3 minutes after joining.

| run | heap in world |
|---|---|
| baseline | 7,712 MB (of an 8 GB cap) |
| fixes 1-3 | 5,794 MB |
| fixes 1-4 (0.2.0) | 5,405 MB |
| 0.2.0 + `-XX:+UseCompactObjectHeaders` | 4,857 MB |

## Fix 2: CTM overlay tables on demand
CTM (connected textures, used by Chisel) built all 6 x 625 overlay patterns of every connected-texture model at load, each as an ArrayList in an unmodifiable wrapper: 14.6 million lists. They are now built the first time a block needs one, as compact immutable lists: -1,136 MB (-31%). The mixin applies only when CTM is installed (`MixinPlugin`).

## Fix 3: shared baked quads
Many block models bake to identical quads (shared parents, variants). Quads from `FaceBakery` are interned for the duration of a bake and the table is dropped when baking finishes: 5.24 M -> 2.84 M quads, -181 MB.

## Fix 4: JEI anvil recipes at top level only
JEI simulated the anvil for every enchantable item with every enchantment at every level and kept each result: 1.65 M item stacks and 947 K enchantment maps in world. Each item and enchantment pair now shows one recipe at the top level: 0.93 M stacks, -389 MB in world. The redirect is optional (`require = 0`), so a JEI update that renames the lambda only switches it off.

## Launcher flag: compact object headers
Java 25 can shrink every object header from 12 to 8 bytes (`-XX:+UseCompactObjectHeaders`, a product flag since JDK 25). With this pack's tens of millions of small objects it saves another 548 MB in world. It is a JVM argument, so it goes in the Prism instance, not in the mod.

## Fix 5: indexed mod-jar resource listing (0.3.0)
Each resource listing (tags and data maps for every registry, registry entries) walked every entry of every mod jar: 62% of server startup CPU in a JFR profile (`bench/startup.sh`). ModernFix's own index for this is not taking effect on 26.1. Each pack now keeps a sorted list of its file names per pack type, built on first use and dropped when the pack closes. Server load 268 s -> 127 s; same 47,035 recipes, 2,054 advancements and tag warnings as before.

## Real world type: Tellus Earth (0.3.0)
Same bench on `level-type=tellus:diaithir_age_1` (Y -2032 to 2031, 254 sections per chunk, Tellus 0.8.3-diaithir.3), structure cache warm. Server load 125 s, heap idle 1,283 MB, heap with 1,024 chunks loaded 2,496 MB, 1,024 chunks generated in 70 s, then tick median 6.5 ms / p95 12.4 ms / max 26.9 ms over 1 min, 20 TPS. Tellus logs HTTP 404 from the ETH canopy-height tile service in the cloud and falls back to biome trees; that is Tellus/network, not this mod.

## Scout candidate: ScalableLux (server A/B, vanilla-type world, 0.3.0 on both sides)
| | without | with |
|---|---|---|
| 1,024 chunks | 79 s | 73 s |
| tick median | 23.3 ms | 21.3 ms |
| tick p95 | 39 ms | 35 ms |
| worst tick | 102 ms | 47 ms |
It wins on lighting, so it is recommended for the pack (side both). Caveats: 0.3.0-alpha.0.18, and on a dedicated server it logs 2,325 harmless "Failed to analyze class BlockState for dynamic lighting" traces at boot. The client-only scout mods (More Culling, Particle Core, Cut Through) can only be checked for a clean load in the cloud (software GL, no real FPS); Let Me Despawn changes gameplay, so it is left to the design threads.

## Fix 6: C2ME storage threads on first use (0.3.2)
C2ME replaces every vanilla IOWorker with a `C2MEStorageThread` that starts its own platform thread in its constructor. Each ServerLevel opens three storages (chunks, POI, entities) and this pack registers 89 dimensions (72 Backrooms levels plus Twilight Forest, Aether II, Stellaris, AE2, Neovitae and the Diaithir worlds), so a dedicated server idled with 267 "C2ME Storage #n" threads parked in `Object.wait()`, nearly all in dimensions nobody has entered. The thread now starts on its first task (`wakeUp()`), and `flush()` returns at once for a storage that never started (the autosave right after "Done" would otherwise start two per level). `bench/` method: `jcmd <pid> Thread.print` 45 s after "Done", same world, same 344-mod server pack, -Xmx4500M.

| | threads in the dump | "C2ME Storage" threads |
|---|---|---|
| 0.3.1 (before) | 359 | 267 |
| 0.3.2 with the wakeUp hook only | 285 | 178 (the "Saving chunks" autosave right after Done started two per level; inferred from the log order and the thread numbers) |
| 0.3.2 | 101 | 0 |

The other per-dimension pool, Distant Horizons' (about 15 threads per level), was already handled in 0.3.1 (`DhIgnore`). Both injectors are required (no `require = 0`): if a C2ME update renames `wakeUp` or the constructor, boot fails loudly rather than leaving a storage whose thread never starts.

## Fix 7: Occultism's JEI recipe sync without JEI (0.3.3, client)
Occultism 1.256.1 subscribes `JeiPlugin.ClientRecipeSync` to `RecipesReceivedEvent` without checking that JEI is installed. With JEI dropped, every join logged `NoClassDefFoundError: mezz/jei/api/IModPlugin` out of the `neoforge:recipe_content` payload ("Failed to process a synchronized task"), and the recipe listeners that run after Occultism's (Spoiled's recipe list, Reliquary, Powah) were skipped. A `@Pseudo` mixin cancels the handler while `jei` is not loaded. No effect when JEI is present.

## Existing optimizers: keep them
Nothing in this mod duplicates ModernFix, FerriteCore, Lithium-style ports, Sodium or EntityCulling; the five fixes above are for costs those mods don't touch (structure upgrades, CTM tables, quad duplication, JEI anvil pages, mod-jar resource scans). Removing any of them would lose more than this mod adds, so none are removed. Sodium and EntityCulling licences don't allow folding their code in.

## Recommended client setting
diaithir-perf 0.3.0 + `-XX:+UseCompactObjectHeaders`: in-world client heap 7,712 MB -> 4,629 MB. Prism max memory 6144 MB leaves ~1.5 GB headroom.
