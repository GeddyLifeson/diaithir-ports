package org.valkyrienskies.mod.mixin.feature.shipyard_entities;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.valkyrienskies.mod.mixinducks.client.render.EntityRenderStateDuck;

@Mixin(EntityRenderState.class)
public class MixinEntityRenderState implements EntityRenderStateDuck {
    @Unique
    private Entity vs$entity;

    @Override
    public @Nullable Entity vs$getEntity() {
        return vs$entity;
    }

    @Override
    public void vs$setEntity(final @Nullable Entity entity) {
        this.vs$entity = entity;
    }
}
