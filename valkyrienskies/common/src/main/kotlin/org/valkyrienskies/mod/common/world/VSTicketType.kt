package org.valkyrienskies.mod.common.world

import net.minecraft.server.level.TicketType

/**
 * Custom ticket type for ship chunks. Used with radius 0, giving ticket level 33 (FULL status).
 *
 * This loads ONLY the requested chunk with zero neighbor chunks:
 * - Vanilla FORCED ticket: level 31 (entity ticking) = 2-chunk radius = ~25 chunks per ship chunk
 * - Previous VS2 ticket: radius 1 (level 32, ticking) = 1-chunk radius = ~9 chunks per ship chunk
 * - Current VS2 ticket: radius 0 (level 33, FULL) = 0-chunk radius = 1 chunk per ship chunk
 *
 * For 100 ships this means loading 100 chunks instead of 900 (or 2500 vanilla) — a 9x improvement.
 * FULL status is sufficient for shipyard chunks: block reads, block entities, and terrain updates
 * all work at FULL status. Entity ticking and block ticking are not needed in the shipyard.
 *
 * 26.2: TicketType is no longer a generic `TicketType<T>` created via `.create(name, comparator)` -
 * it's now a plain (timeout, flags) record registered into a proper Minecraft registry
 * (`BuiltInRegistries.TICKET_TYPE`). There's no more per-ticket "key" object (the ChunkPos used
 * to be passed as the ticket's key at add/remove time); `ChunkMap`/`ServerChunkCache` tickets are
 * now tracked purely by (TicketType, ChunkPos) via `addTicketWithRadius`/`removeTicketWithRadius`.
 *
 * `BuiltInRegistries.TICKET_TYPE` freezes very early (as part of vanilla's own bootstrap, via
 * `BuiltInRegistries.bootStrap()`+`freeze()`), well before mod constructors run - a raw
 * `Registry.register()` call anywhere in mod code throws "Registry is already frozen" regardless of
 * how early it's placed. The only NeoForge-blessed way to add an entry is via `RegisterEvent`
 * (which NeoForge fires while briefly re-opening the registry) - see the `TICKET_TYPES`
 * `DeferredRegister` in `ValkyrienSkiesModForge`, which sets [SHIP_CHUNK] once registration
 * actually completes (in its `FMLLoadCompleteEvent` handler).
 */
object VSTicketType {
    lateinit var SHIP_CHUNK: TicketType
}
