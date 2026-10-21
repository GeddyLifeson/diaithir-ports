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

## Fix 2: CTM overlay tables on demand
CTM (connected textures, used by Chisel) built all 6 x 625 overlay patterns of every connected-texture model at load, each as an ArrayList in an unmodifiable wrapper: 14.6 million lists. They are now built the first time a block needs one, as compact immutable lists: -1,136 MB (-31%). The mixin applies only when CTM is installed (`MixinPlugin`).

## Fix 3: shared baked quads
Many block models bake to identical quads (shared parents, variants). Quads from `FaceBakery` are interned for the duration of a bake and the table is dropped when baking finishes: 5.24 M -> 2.84 M quads, -181 MB.
