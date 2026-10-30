package org.valkyrienskies.mod.mixin.accessors.server.level;

import net.minecraft.server.level.DistanceManager;
import net.minecraft.world.level.TicketStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DistanceManager.class)
public interface DistanceManagerAccessor {
    // 26.2: DistanceManager no longer holds a `tickets` map directly; ticket bookkeeping moved to
    // the TicketStorage SavedData, reachable via DistanceManager.ticketStorage. Callers should use
    // TicketStorage#getTickets(long) (returns an empty list for chunks with no ticket) in place of
    // the old tickets.containsKey(long) check.
    @Accessor("ticketStorage")
    TicketStorage getTicketStorage();
}
