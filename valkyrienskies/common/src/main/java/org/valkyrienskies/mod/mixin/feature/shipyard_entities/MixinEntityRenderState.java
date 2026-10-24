package org.valkyrienskies.mod.mixin.feature.shipyard_entities;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.mixinducks.client.render.EntityRenderStateDuck;

/**
 * Implements {@link EntityRenderStateDuck} so a ship reference can ride along with the render state.
 */
@Mixin(EntityRenderState.class)
public class MixinEntityRenderState implements EntityRenderStateDuck {

    @Unique
    private ClientShip vs$ship;

    @Override
    public ClientShip vs$getShip() {
        return vs$ship;
    }

    @Override
    public void vs$setShip(final ClientShip ship) {
        this.vs$ship = ship;
    }
}
