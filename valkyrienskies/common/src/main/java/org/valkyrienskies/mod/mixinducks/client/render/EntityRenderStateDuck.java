package org.valkyrienskies.mod.mixinducks.client.render;

import org.valkyrienskies.core.api.ships.ClientShip;

/**
 * Carries the ship an entity is riding (or standing on) from extract() through to submit().
 *
 * <p>26.2 splits entity rendering into {@code extractEntity(Entity, float)}, which still has the
 * {@code Entity}, and {@code submit(EntityRenderState, ...)}, which does not. Only extract can work
 * out which ship an entity belongs to - a mounted rider's position is already in world space, so
 * submit cannot recover the ship by looking up the render state's coordinates. Stash it on the
 * render state instead.
 */
public interface EntityRenderStateDuck {

    ClientShip vs$getShip();

    void vs$setShip(ClientShip ship);
}
