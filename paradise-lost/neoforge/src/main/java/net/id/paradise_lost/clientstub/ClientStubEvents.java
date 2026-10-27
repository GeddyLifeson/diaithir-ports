package net.id.paradise_lost.clientstub;

import net.id.paradise_lost.ModConstants;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@SuppressWarnings({"unchecked", "rawtypes"})
final class ClientStubEvents {
    private ClientStubEvents() {}

    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (var entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
            if (entry.getKey().identifier().getNamespace().equals(ModConstants.MOD_ID)) {
                EntityType<Entity> type = (EntityType<Entity>) entry.getValue();
                event.registerEntityRenderer(type, NoopRenderer::new);
            }
        }
    }
}
