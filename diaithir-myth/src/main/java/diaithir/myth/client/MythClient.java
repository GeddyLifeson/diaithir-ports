package diaithir.myth.client;

import diaithir.myth.Creature;
import diaithir.myth.DiaithirMyth;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import java.util.HashMap;
import java.util.Map;

/** Client side: one renderer and one baked model layer per creature. */
public final class MythClient {
    private static final Map<String, ModelData> MODELS = new HashMap<>();

    private MythClient() {}

    public static void init(IEventBus bus) {
        bus.addListener(MythClient::renderers);
        bus.addListener(MythClient::layers);
        ClientShot.init();
    }

    private static ModelData data(String id) {
        return MODELS.computeIfAbsent(id, ModelData::new);
    }

    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (Creature c : Creature.all()) {
            event.registerLayerDefinition(MythRenderer.layer(c.id), () -> data(c.id).layer());
        }
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        for (Creature c : Creature.all()) {
            event.registerEntityRenderer(DiaithirMyth.TYPES.get(c.id).get(), ctx -> new MythRenderer(ctx, c, data(c.id)));
        }
    }
}
