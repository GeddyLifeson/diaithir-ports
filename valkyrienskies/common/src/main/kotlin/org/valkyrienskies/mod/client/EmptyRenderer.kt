package org.valkyrienskies.mod.client

import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.world.entity.Entity

// 26.2: EntityRenderer<T, S> now takes a second EntityRenderState type param and renders via
// submit(S, ...) instead of the old render(T, ..., MultiBufferSource, ...)/getTextureLocation.
// The base EntityRenderState is enough here since this renderer draws nothing.
class EmptyRenderer(context: EntityRendererProvider.Context) :
    EntityRenderer<Entity, EntityRenderState>(context) {

    override fun createRenderState(): EntityRenderState = EntityRenderState()
}
