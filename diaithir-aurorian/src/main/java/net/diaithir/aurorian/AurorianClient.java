package net.diaithir.aurorian;

import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Vanilla models with the upstream entity textures. */
final class AurorianClient {
    static void init(IEventBus bus) {
        bus.addListener(EntityRenderersEvent.RegisterRenderers.class, AurorianClient::renderers);
    }

    private static Identifier tex(String path) {
        return Identifier.fromNamespaceAndPath(DiaithirAurorian.ID, "textures/entity/" + path + ".png");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        Identifier hollow = tex("hollow"), knight = tex("undead_knight"), slime = tex("dungeon_slime"), keeper = tex("dungeon_keeper/dungeon_keeper");
        e.registerEntityRenderer((EntityType) AurorianMobs.TYPES.get("hollow").get(), ctx -> new ZombieRenderer(ctx) {
            @Override public Identifier getTextureLocation(ZombieRenderState s) { return hollow; }
        });
        e.registerEntityRenderer((EntityType) AurorianMobs.TYPES.get("undead_knight").get(), ctx -> new ZombieRenderer(ctx) {
            @Override public Identifier getTextureLocation(ZombieRenderState s) { return knight; }
        });
        e.registerEntityRenderer((EntityType) AurorianMobs.TYPES.get("dungeon_slime").get(), ctx -> new SlimeRenderer(ctx) {
            @Override public Identifier getTextureLocation(SlimeRenderState s) { return slime; }
        });
        for (var pair : java.util.List.of(java.util.Map.entry(AurorianProjectiles.CERULEAN, "cerulean_arrow"), java.util.Map.entry(AurorianProjectiles.CRYSTAL, "crystal_arrow"))) {
            Identifier at = tex(pair.getValue());
            e.registerEntityRenderer((EntityType) pair.getKey().get(), ctx -> new net.minecraft.client.renderer.entity.ArrowRenderer<>(ctx) {
                @Override protected Identifier getTextureLocation(net.minecraft.client.renderer.entity.state.ArrowRenderState s) { return at; }
                @Override public net.minecraft.client.renderer.entity.state.ArrowRenderState createRenderState() { return new net.minecraft.client.renderer.entity.state.ArrowRenderState(); }
            });
        }
        Identifier beam = tex("crystalline_beam");
        e.registerEntityRenderer((EntityType) AurorianProjectiles.BEAM.get(), ctx -> new net.minecraft.client.renderer.entity.ArrowRenderer<>(ctx) {
            @Override protected Identifier getTextureLocation(net.minecraft.client.renderer.entity.state.ArrowRenderState s) { return beam; }
            @Override public net.minecraft.client.renderer.entity.state.ArrowRenderState createRenderState() { return new net.minecraft.client.renderer.entity.state.ArrowRenderState(); }
        });
        e.registerEntityRenderer((EntityType) AurorianMobs.TYPES.get("dungeon_keeper").get(), ctx -> new SkeletonRenderer(ctx) {
            @Override public Identifier getTextureLocation(SkeletonRenderState s) { return keeper; }
        });
    }

    private AurorianClient() {}
}
