# Shipyard claim area vs Tellus coordinate limits (2.5.0-diaithir.6 / .7)

Problem: Tellus' default "global_mercator_land_v2" packed BlockPos profile clamps X/Z to
[-20037509, 20037508]. VS shipyard claims (ChunkAllocator, X index -7000..7000, Z 3000..7000,
256 chunks = 4096 blocks each) put ship blocks at x ~ -28.67M. Result: ship chunks fail bounds
checks, block interaction positions are clamped (helm right-click never reaches ShipHelmBlock),
ships never become visible or seatable.

26.1.2 also added ChunkPos.MAX_COORDINATE_VALUE / isValid checks (GenerationChunkHolder etc.).

.6: MixinChunkPosBounds makes shipyard chunks count as valid ChunkPos. Not sufficient alone.
.7: claim area moved to indices -4800..4800 (blocks ~ +/-19.66M, inside Tellus limits).
    ChunkAllocator lives in the closed vs-core binary, so this is a post-build ASM step:
    tools/shipyard-claims/patch-claims.sh <built jar> <asm-9.9.jar> <jdk>.
    Constants: -7000 -> -4800, 7000 -> 4800, 7001 -> 4801 in ChunkAllocator,
    ChunkAllocator$Companion, VSCoreImpl.
Existing worlds with ships at the old claims will lose them. Alternative: run Tellus with a
different -Dtellus.experimentalHeight.coordinateProfile and keep unpatched claims.
Verified: assemble -> seat at Eureka helm -> W+Space flight on the pack server.

# Runtime-created levels (2.5.0-diaithir.8)

A level created while the server runs (a mod registering a dynamic dimension, e.g. via
Vampirism's DimensionManager or a world-per-player mod) called ShipWorld.addDimension from the
ServerLevel constructor at an arbitrary point of the tick. vs-core enforces a stage order per
tick (PRE_TICK, UPDATE_*, POST_TICK_*), so this ended in "Constraints failed" or an NPE inside
postTickGame, killing the server (seen in the integrated server as an NPE in
FQ.postTickGame <- MixinMinecraftServer.postTick). Reproduced on .7 by creating a level at the
start of tickServer.
Fix: PendingDimensions queues addDimension after startup and registers new levels right after
preTickGame of the next tick; MixinServerLevel.postTick skips a level that is still pending.

# Unregistered levels (2.5.0-diaithir.9)

Root cause of the NPE in vs-core Et.i (postTickGame) on the PC: only the overworld was registered
with the ship world (MixinMinecraftServer.postCreateLevels); the per-level addDimension in
MixinServerLevel.onInit is skipped for levels built inside createLevels (the ship world does not
exist yet) and the registration loop in preTick was commented out. Et.i looks up the
dimension's world ship id for every terrain update and NPEs on a missing entry, so the first
chunk loaded in the Nether, End or any modded dimension (Twilight Forest, Backrooms, DH, ...)
killed the tick loop. Reproduced on .8 with `execute in minecraft:the_nether run forceload add 0 0`.
Fix: PendingDimensions tracks registered ids; preTick registers every missing level right after
preTickGame; MixinServerLevel.postTick skips unregistered levels.
