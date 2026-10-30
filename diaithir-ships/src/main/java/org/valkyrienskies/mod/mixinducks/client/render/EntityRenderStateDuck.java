package org.valkyrienskies.mod.mixinducks.client.render;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Duck interface for {@link net.minecraft.client.renderer.entity.state.EntityRenderState}, which remembers the entity
 * the state was extracted from so that ship transforms can be applied when the state is submitted.
 */
public interface EntityRenderStateDuck {
    @Nullable Entity vs$getEntity();

    void vs$setEntity(@Nullable Entity entity);
}
