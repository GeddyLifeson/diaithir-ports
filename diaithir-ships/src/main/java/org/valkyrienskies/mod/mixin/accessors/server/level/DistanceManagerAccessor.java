package org.valkyrienskies.mod.mixin.accessors.server.level;

import net.minecraft.server.level.DistanceManager;
import net.minecraft.world.level.TicketStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DistanceManager.class)
public interface DistanceManagerAccessor {
    // Tickets moved out of DistanceManager into TicketStorage in 26.1
    @Accessor("ticketStorage")
    TicketStorage getTicketStorage();
}
